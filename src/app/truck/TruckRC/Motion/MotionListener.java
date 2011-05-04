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

package app.truck.TruckRC.Motion;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public abstract class MotionListener extends Timer 
{
	/** 
	 * MotionListener ist eine abstrakte, die Sensordaten sammelt
	 * und einen Thread managed, der alle 100ms das IMotionChanged Interface
	 * mit neuen Daten versort. 
	 * Kindklassen müssen getMotionData implementieren, die, abhängig vom Sensor,
	 * die neuesten Sensordaten zurückgibt
	 */
	final private TimerTask mTask;
	final private IMotionChanged mCb;
	final private float[] mValues;

	/**
	 * Instanziert den MotionListener und started den Thread.
	 * 
	 * Kindklassen sollten diesen Konstruktor mit einem passenden Namen aufrufen
	 * @param name Name des Sensors
	 * @param cb Interface, welches onMotionChanged implementiert und mit Daten versorgt wird
	 */
	protected MotionListener(final String name, IMotionChanged cb) 
	{
		super(name, true);
		mValues = new float[3];
		Arrays.fill(mValues, 0.0f);
		mCb = cb;
		mTask = new TimerTask() {
			@Override
			public void run() 
			{ 
				getMotionData(mValues);
				mCb.onMotionChanged(mValues);
			}
		};
	}

	/**
	 * Installiert den Listener. Er wird onMotionChanged() aufrufen
	 * 
	 * Falls die Kindklasse es implementiert muss super.startListening()
	 * aufgerufen werden
	 */
	public void startListening()
	{
		schedule(mTask, 100, 100);
	}
	
	/**
	 * Deinstalliert den Listener
	 * 
	 * Falls die Kindklasse es implementiert muss super.startListening()
	 * aufgerufen werden
	 */
	public void stopListening()
	{
		mTask.cancel();
	}

	/**
	 * Ruft die aktuellen Daten vom Sensor ab
	 * @param values Array, das die Daten in X, Y und Z Ebene enthalten wird
	 */
	public abstract void getMotionData(float[] values);
}
