package app.truck.TruckRC;

public interface IOperationCompleted 
{
	/**
	 * Callback bei Fertigstellung asynchroner Operationen
	 * @param op Befehlscode
	 * @param result true, wenn Operation erfolgreich war
	 */
	void onOperationCompleted(int op, boolean result);
}
