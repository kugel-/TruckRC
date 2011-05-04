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

import android.view.View;
import android.view.View.OnClickListener;
import app.truck.TruckRC.R;

public class ButtonMotionListener extends MotionListener implements OnClickListener 
{
	private float[] mLastButtons;
	private float[] mFilter;
	
	public ButtonMotionListener(IMotionChanged cb)
	{
		super("ButtonMotion", cb);
		mLastButtons = new float[3];
		mFilter = new float[3];
	}

	/**
	 * Ließt die aktuellen Werte aus. Weitere Aufrufe
	 * haben eine Filterung zur Folge, sodass die Werte
	 * über die Zeit kleiner werden (gegen 0), bis
	 * sie durch einen erneuten Buttondruck wieder auf
	 * -10 bzw. 10 aufgefüllt werden  
	 */
	@Override
	public void getMotionData(float[] values) 
	{
        final float alpha = 0.9f;

        mFilter[0] = alpha * mFilter[0] + (1 - alpha) * mLastButtons[0];
        mFilter[1] = alpha * mFilter[1] + (1 - alpha) * mLastButtons[1];
        mFilter[2] = alpha * mFilter[2] + (1 - alpha) * mLastButtons[2];

        for(int i = 0; i < mFilter.length; i++)
        	if (Math.abs(mFilter[i]) < 0.05f) mFilter[i] = 0.0f;

        values[0] = mLastButtons[0] - mFilter[0];
        values[1] = mLastButtons[1] - mFilter[1];
        values[2] = mLastButtons[2] - mFilter[2];
	}

	/**
	 * Button-Dispatch auf den Button Sensor
	 * 
	 * Wenn die Buttons gedrückt werden, werden sie
	 * die Sensorwerte mit den max. Werten belegt
	 * 
	 * weitere Aufrufe zu getMotionData lassen die
	 * Werte dann abklingen und gegen 0 gehen
	 */
	@Override
	public void onClick(View v) 
	{
		switch(v.getId())
		{
		case R.id.buttonUp:
			mFilter[0] = -10.0f;
			break;
		case R.id.buttonDown:
			mFilter[0] = 10.0f;
			break;
		case R.id.buttonLeft:
			mFilter[1] = -10.0f;
			break;
		case R.id.buttonRight:
			mFilter[1] = 10.0f;
			break;
		}
	}

}
