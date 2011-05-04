/*
 *  ___           ____
 * |   |         |###|
 * |___|___      |###|___     ___     ___     ___
 *     |    \    |#######|   |###|   |###|   |###|
 *  ___|___   \  |###____|   |###|   |###|   |###|
 * |   |   |   | |###|       |###|   |###|   |###|
 * |   |   |   | |### \      |####\  |###|  /####|___
 * |   |   |   |  \######|    \##############|   |   |
 * |___|   |___|    \####|      \############|   |___|
 *
 * Hochschule für Technik und Wirtschaft Berlin
 * University of Appplied Sciences
 *
 * Copyright 2011 Thomas Martitz
 *
 * 
 * TruckRC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * TruckRC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TruckRC. If not, see <http://www.gnu.org/licenses/>.
 */

package app.truck.TruckRC;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class TruckConnection
{
	/* Thread der für Netzwerkkommunikation zuständig ist */
	final private Thread mThread;
	/* Thread der alle halbe Sekunde Stillstandsdaten zum Truck sendet,
	 * da dieser denkt die Verbindung sei unterbrochen
	 * 
	 * Arbeitet Watchdog-artig, d.h. nur im Falle eines Timeouts sendet
	 * er Stillstandsdaten, das Timeout kann zurückgesetzt werden
	 */
	final private Thread mTimeoutThread;
	/* Sockets für die Verbindung zum Truck */
	private Socket[] sock;
	/* Handler-Objekt für Inter-Thread-Kommunikation */
	private Handler mHandler = null;
	
	/* Mögliche Befehle die der Handler abarbeitet, sie werden per Message übergeben */
	public static final int TRUCK_DISCONNECT = 0;
	public static final int TRUCK_CONNECT = 1;
	public static final int TRUCK_MOVE = 2;
	public static final int TRUCK_CLOSE = 3;
	public static final int TRUCK_TIMEOUT = 4;

	private static final String TruckIP = "192.168.2.105";
	
	private static final int timeout_interval_in_ms = 500;
	private boolean connected = false;

	public TruckConnection(final IOperationCompleted iface) 
	{
		sock = new Socket[4];
		mThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				mHandler = new Handler(new Handler.Callback() 
				{
					public boolean handleMessage(Message msg) 
					{
						boolean result = true;
						/* Befehl eingegangen, Timeout zurücksetzen */
						mTimeoutThread.interrupt();
						switch (msg.what)
						{
						case TRUCK_DISCONNECT:
							if (isConnected())
							{
								doDisconnect();
								connected = false;
							}
							break;
						case TRUCK_CONNECT:
							try {
								if (!isConnected())
									doConnect();
							} catch (UnknownHostException e) {
								Log.d("TruckRC", "Unknown host");
								result = false;
							} catch (Exception e) {
								Log.d("TruckRC", e.getClass().getName());
								e.printStackTrace();
								result = false;
							}
							connected = result;
							break;
						case TRUCK_MOVE:
						{
							/* wenn die Message ein Objekt hat, nimm dieses,
							 * es ist dann ein Array aus bytes (anstatt zwei Bytes
							 * arg1 und arg2)
							 */
							if (msg.obj != null)
							{
								if (doWrite((byte[])msg.obj) != 0)
									result = false;
								break;
							}
							byte[] data = { (byte)msg.arg1, (byte)msg.arg2 }; 
							if (doWrite(data) != 0)
								result = false;
							break;
						}
						case TRUCK_CLOSE:
							mHandler.getLooper().quit();
							break;
						case TRUCK_TIMEOUT:
						{
							byte[] no_motion = { 0, 70, 1, 100 };
							return doWrite(no_motion) != 0;
						}
						}
						/* Interface-Callback aufrufen, da asynchrone 
						 * Operation fertig ist */
						iface.onOperationCompleted(msg.what, result);
						return result;
					}
				});
				Looper.loop();
			}
		}, "truck connection thread");
		mThread.start();

		/* Warte bis der truck connection thread gestartet ist und den 
		 * Handler instanziert hat
		 */
		while(mHandler == null)
			Thread.yield();
		
		mTimeoutThread = new Thread(new Runnable()
		{	
			public boolean quit = false;
			@Override
			public void run() {
				while(true) {
					try {
						if (quit)
							return;
						Thread.sleep(timeout_interval_in_ms);
					} catch (InterruptedException e) {
						/* Kein Fehler: Befehl ist vom Benutzer eingegangen,
						 * Timeout einfach neu starten
						 */
						continue;
					}
					if (isConnected())
						mHandler.sendMessage(getMessage(TRUCK_TIMEOUT));
				}
			}
		}, "timeout thread");
		mTimeoutThread.start();
	}

	
	/* getMessage sind nur wrapper für Message.obtain() */
	private Message getMessage(int what)
	{
		Message ret = Message.obtain(mHandler, what, 0, 0);
		ret.obj = null;
		return ret;
	}
	
	private Message getMessage(int what, byte[] data)
	{
		return Message.obtain(mHandler, what, data);
	}

	/* Interne, synchrone Methode zum Verbindungsaufbau */
	private void doConnect() throws ConnectException, IOException, UnknownHostException
	{
		final int delay = 100;
		/* Der Truck besteht darauf, dass wir uns auf alle Ports verbinden
		 * obwohl wir für das Fahren nur den 8003er brauchen
		 */
		final int[] ports = { 8000, 8001,/*  8003 */ 2101, 8800};

		for(int i = 0; i < sock.length; i++)
		{
			try {
				/* Der Konstructor verbindet auch gleichzeitig */
			sock[i] = new Socket(TruckIP, ports[i]);
			/* ohne mini-sleep() klappts nicht */
			Thread.sleep(delay);
			} catch (ConnectException e) {
				if (ports[i] == 2101)
					throw e; /* Motor ist kritisch */
				else
					Log.w("TruckRC", e.getMessage());
			} catch (InterruptedException e) { /* kein Fehler */ }
		}
	}

	/* Interne, synchrone Methode zur Verbindungstrennung */
	private void doDisconnect()
	{
		try {
			for(Socket s:sock)
				if (s != null)
					s.close(); /* Schließe alle Sockets */
		} catch (IOException e) {
			Log.w("TruckRC", "Error while closing socket");
		};
		Arrays.fill(sock, null);
	}

	/* Interne, synchrone Methode zur Datenübertragung */
	private int doWrite(byte[] data)
	{
		int result = 0;
		try {
			OutputStream out = sock[2].getOutputStream();
			out.write(data); /* Schiebe Daten über den Socket */ 
		} catch (IOException e) {
			result = -1;
		}
		return result;
	}

	/* Public Wrapper-Methoden, die von anderen Threads asynchron aufgerufen werden */
	/**
	 * Verbindung mit dem Truck aufbauen
	 */
	public void connect()
	{
		mHandler.sendMessage(getMessage(TRUCK_CONNECT));
	}

	/**
	 * Verbindung zum Truck trennen
	 */
	public void disconnect()
	{
		mHandler.sendMessage(getMessage(TRUCK_DISCONNECT));
	}

	/**
	 * Bewegungsdaten senden und den Truck in Bewegung setzen
	 * 
	 * @param data Payload: Die Daten müssen in Byte-Paaren vorliegen,
	 * das erste mit ID 0 für Vorwärts/Rückwärts oder ID 1 für Links/Rechts,
	 * das 2. Byte mit den jeweiligen (passenden) Daten; z.B. { 0, 70, 1, 100 }
	 */
	public void write(byte[] data)
	{
		mHandler.sendMessage(getMessage(TRUCK_MOVE, data));
	}

	/**
	 *  Verbindung schließen und freigeben 
	 */
	public void close()
	{
		mHandler.sendMessage(getMessage(TRUCK_CLOSE));
		while (mThread.isAlive() == true)
		{
			try {
				mThread.join();
			} catch (InterruptedException e) {}
		}
	}

	/**
	 * @return true, wenn die Verbindung zum Truck steht
	 */
	public boolean isConnected()
	{
		return connected;
	}
}
