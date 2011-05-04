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

public interface IMotionChanged 
{
	/**
	 * Callback, welches aufgerufen wird wenn der Sensor neue Daten hat
	 * @param values Array, welches mit Bewegungsdaten in X, Y und Z Richtung 
	 * 				  gefüllt wird
	 */
	public void onMotionChanged(float[] values);
}
