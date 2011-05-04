package app.truck.TruckRC;

import java.util.ArrayList;

import android.app.Application;
import android.util.Log;
import android.widget.ArrayAdapter;

public class TruckRcApplication extends Application implements IOperationCompleted 
{
	/* Aktuelle Instanz, TruckRcApplication ist eine Singletone-Klasse */
	static private TruckRcApplication instance;
	/* Ausgabebuffer, hält Strings zur Anzeige auf dem Display */
	private ArrayAdapter<String> adapter;
	/* Netzwerkverbindung zum Truck, Activities interagieren mit dem Truck,
	 * aber TruckRcApplication erzeugt die Verbindung damit sie nicht beim
	 * Activity-Neustart abbricht */
	private TruckConnection truck = null;
	/* Interface zur Signalisierung von asychnroner Operationen,
	 * Die Application-Klasse implementiert es nicht selbst, sondern
	 * ruft ein übergebenes Interface auf
	 */
	private IOperationCompleted mIface;
	
	/**
	 * Wird, im Gegensatz zur Activity, einmalig zum Start der Applikation aufgerufen
	 * 
	 * Hier erzeugen wir die Verbindug zum Truck und den Ausgabebuffer, damit beide
	 * einen Neustart der Activity überleben (die Activity startet neu z.B. wenn man
	 * das Telefon dreht)
	 */
    public void onCreate() 
    {
        super.onCreate();
        Log.d("TruckRC", "app.onCreate()");
        instance = this;
        ArrayList<String> buf = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, R.layout.trucklistitem, buf);
		truck = new TruckConnection(this);
    }

    public static TruckRcApplication getInstance()
    {
    	/* Android instanziert die Application im Hintergrund, wir sehen nur onCreate();
    	 * deswegen können wir hier keine Instanz erzeugen wenn es keine gibt
    	 */
    	return instance;
    }
    
    public ArrayAdapter<String> getAdapter()
    {
    	return adapter;
    }
    
    /**
     * Gibt die Verbindung zum Truck zurück
     * @param iface Interface-Instanz, die die alte überschreibt
     * @return
     */
    public TruckConnection getTruckConnection(IOperationCompleted iface)
    {
    	/* Die aktuelle Activity implementiert eigentlich onOperationCompleted */
    	mIface = iface; 
    	return truck;
    }
    
    /**
     * Gib der Application, das eine Activity zerstört wurde
     */
    public void onActivityDestroyed()
    {
    	mIface = null;
    	adapter.clear(); /* Ausgabebuffer löschen */
    }

    /**
     * Ruft das Callback des in getTruckConnection() übergebenen Interface auf,
     * ist also nicht direkt implementiert
     */
	@Override
	public void onOperationCompleted(int op, boolean result) 
	{
		/* Die aktuelle Activity implementiert eigentlich onOperationCompleted */
		if (mIface != null)
			mIface.onOperationCompleted(op, result);
	}
}
