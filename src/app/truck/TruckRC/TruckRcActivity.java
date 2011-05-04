package app.truck.TruckRC;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import app.truck.TruckRC.Motion.ButtonMotionListener;
import app.truck.TruckRC.Motion.IMotionChanged;
import app.truck.TruckRC.Motion.SensorMotionListener;

public class TruckRcActivity extends Activity implements IOperationCompleted, IMotionChanged
{
	private TruckListView mListView;
	/* TruckConnection instanziert von der Application-Klasse */
	private TruckConnection mTruck = null;
	/* Zwei Provider-Klassen um Bewegungsdaten für den Truck zu bekommen */ 
	private ButtonMotionListener mButtons;	/* Bewegung per Button in der GUI */
	private SensorMotionListener mSensor;	/* Bewegung per Bewegungssensor des Telefonts */
	private TruckRcApplication mApp;
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gui);
        mApp = TruckRcApplication.getInstance();
        /* mListView ist in gui.xml definiert und wird von Android instanziert */
		mListView = (TruckListView)findViewById(R.id.text);
		/* ListView mit dem Ausgabebuffer verbinden */
        mListView.setAdapter(mApp.getAdapter());
		mTruck = mApp.getTruckConnection(this);
		mButtons = new ButtonMotionListener(this);
		/* Empfange Bewegungsdaten über die Buttons */
		mButtons.startListening();
		/* Empfange Bewegungsdaten über den Bewegungssensor, 
		 * aber nur wenn das Telefon quer gehalten wird */
		mSensor = new SensorMotionListener(this, this);
		if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT)
			mSensor.startListening();
		
		/* Display muss an bleiben, da sonst die Activity pausiert wird und der
		 * Truck anhält */
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

   
	public void onStart()
	{
		super.onStart();
        mListView.append("Hallo!");
	}
	
	/**
	 * Fügt text dem Ausgabebuffer zur Anzeige hinzu
	 * 
	 * Ist Thread-safe, kann also außerhalb des UI-Threads aufgerufen werden
	 * @param text anzuzuzeigender Text
	 */
	/* Android erlaubt es nicht die GUI außerhalb des UI-Threads zu manipulieren */
	void addText(final String text)
	{
		runOnUiThread(new Runnable() 
		{   @Override
			public void run() {
				mListView.append(text);
			}
		});
	}

	/**
	 * Callback zur Benachrichtigung über asynchrone Operationen,
	 * 
	 * Wird von der Truckverbindung aufgerufen, sobald eine Operation fertig ist.
	 * Netzwerkoperationen sollten nicht im UI-Thread aufgerufen werden, sondern in
	 * einem seperaten Thread, damit die UI nicht hängt wenn das Netzwerk langsam ist 
	 */
	@Override
	public void onOperationCompleted(int op, boolean result)
	{
		switch (op)
		{
		case TruckConnection.TRUCK_DISCONNECT: 
			addText("Verbindung getrennt!");
			break;
		case TruckConnection.TRUCK_CONNECT:
			if (result)
				addText("Verbunden!");
			else
				addText("Verbindungsaufbau gescheitert :(");
			break;
		case TruckConnection.TRUCK_MOVE:
			if (result)
				addText("Daten übermittelt");
			else
			{
				addText("Fehler während der Datenübertragung");
				mTruck.disconnect();
			}
			break;
		case TruckConnection.TRUCK_CLOSE:
			addText("Verbindung geschlossen");
			break;
		}
	}

	/**
	 * Wird augerufen, wenn ein Button in der GUI gedrückt wird
	 * 
	 * Wird automatisch vom Android-System aufgerufen	
	 * @param v View-Object, in welchem die Buttons angeordnet sind
	 */
	public void onButtonClick(View v)
	{
		switch(v.getId()) 
		{
		case R.id.buttonConnect:
			if (mTruck.isConnected() == false)
			{
				mListView.append("Verbundung aufbauen...");
				mTruck.connect();
			}
			else
			{
				mListView.append("Verbindung trennen...");
				mTruck.disconnect();
			}
			break;
		case R.id.buttonUp:
		case R.id.buttonDown:
		case R.id.buttonLeft:
		case R.id.buttonRight:
			/* Richtungsbuttons an den "ButtonSensor" weitergeben
			 * der uns daraufhin mit Bewegungsdaten für den Truck versorgt */
			mButtons.onClick(v);
			if (!mTruck.isConnected())
				mListView.append("Nicht verbunden :(");
			break;
		case R.id.buttonQuit:
			if (mTruck.isConnected())
				mTruck.disconnect();
			finish();
		}
	}
	
	/**
	 * Konvertiert float-Werte (von -10 bis +10 in jeder Richtung) in Werte
	 * die der Truck verabeiten kann
	 * 
	 * Für die Geschwindigkeit wollen wir Werte zwischen 0 und 200, wobei
	 * 70 Stillstand bedeutet
	 * Für die Richtung wollen wir Werte zwischen 10 und 190, 0 ist geradeaus
	 * @param values Array aus Eingabedaten, für X, Y und Z Dimension
	 * @param out Ausgabearray, auch für jeweils X, Y und Z Dimension
	 */
	private void motionToTruck(float[] values, byte[] out)
	{
		float speed = values[0];
		int direction;
		if (speed < 0) /* -10...0 (rückwärts) => 0-70 */
			speed = (10.0f - (-speed)) * 7;
		else
			speed = speed * 13f + 70; /* 0...10 (vorwärts) => 200-70 */
		if (speed < 0) speed = 0;
		if (speed > 200) speed = 200;
		out[0] = (byte)speed;

		direction = (int)(values[1]*9+100); /* -10...10 => 10...190 */
		if (direction < 0) direction = 0;
		if (direction > 190) direction = 190;
		out[1] = (byte) (direction);
		out[2] = 0;
	}
	
	/**
	 * Callback für die Sensoren, wenn diese Bewegungsänderung registrieren
	 */
	public void onMotionChanged(float[] values)
	{
		if (mTruck != null && mTruck.isConnected() && (Math.abs(values[0]) > 0.1f || Math.abs(values[1]) > 0.1f))
		{
			byte[] data = new byte[3];
			motionToTruck(values, data);
			byte[] data_for_truck = { (byte)0, data[0], (byte)1, data[1] };
			/* Gib die konvertieren Bewegungsdaten auf dem Display aus */
			String debug = "Sending Bytes... { ";
			for(byte b : data_for_truck)
				debug += "" + (int)(b&0xff) + ", ";
			debug += "}";
			addText(debug);
			/* Bewegungsdaten dem Truck übergeben */
			mTruck.write(data_for_truck);
		}
	}
	
	/**
	 * Wird vom Android-System aufgerufen, wenn die Activity zerstört wird.
	 * Das passiert u.a. wenn das Telefon gedreht wird oder die Zurücktaste gedrückt wird
	 */
	public void onDestroy() 
	{
		super.onDestroy();
		mApp.onActivityDestroyed();
		/* Ohne Activity brauchen keine neuen Bewegungsdaten mehr */
		mSensor.stopListening();
		mButtons.stopListening();
		/* zur Sicherheit... */
		mTruck = null;
	}
}