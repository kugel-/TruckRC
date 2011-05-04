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

import android.content.Context;
import app.truck.TruckRC.orientation.Orientation;
import app.truck.TruckRC.orientation.OrientationListener;
import app.truck.TruckRC.orientation.OrientationProvider;
import app.truck.TruckRC.orientation.provider.ProviderAccelerometer;

public class SensorMotionListener extends MotionListener implements OrientationListener
{
	float mPitch, mRoll;
	private OrientationProvider mProvider;
	private Context mContext;
	public SensorMotionListener(IMotionChanged cb, Context c)
	{
		super("Sensor Motion", cb);
		mContext = c;
		mProvider = ProviderAccelerometer.getInstance();
	}

	/**
	 * Liefert die letzten Sensorwerte, 
	 * inkl. konvertierung auf den Bereich -10...10
	 * (passend zur Steuerung des Trucks)
	 */
	@Override
	public void getMotionData(float[] values) 
	{
		float roll, pitch;
		roll = Math.abs(mRoll);
		if (roll > 55)	/* 90°...55° => -10...0 */
			values[0] = (roll - 55) / -3.5f;
		else			/* 55°...0°  => 0...10 */
			values[0] = (55 - roll) / 5.5f;

		/* -45°...45° => -10...10 
		 * <-45 und >45 wird auf -10 bzw. 10 begrenzt */
		pitch = mPitch / 4.5f;
		if (pitch > 10.0f) pitch = 10.0f;
		if (pitch < -10.0f) pitch = -10.0f;
		
		values[1] = pitch;
		values[2] = 0;
	}
	
	public void startListening()
	{
		super.startListening();
		mProvider.startListening(mContext, this);
	}
	
	public void stopListening()
	{
		super.stopListening();
		mProvider.stopListening();
	}

	@Override
	public void onOrientationChanged(Orientation orientation, float pitch,
			float roll) 
	{
		
		/*	   z	
		 * 	   |/
		 * ---------y
		 *	  /|
		 *   x
		 */
		mRoll = roll; /* Drehung auf der Y-Achse, -90°...90° */
		mPitch = pitch; /* Drehung auf der X-Achse -90°...90° */
	}
}
