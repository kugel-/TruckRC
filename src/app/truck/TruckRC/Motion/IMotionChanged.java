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
