package app.truck.TruckRC.orientation.provider;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import app.truck.TruckRC.orientation.Orientation;
import app.truck.TruckRC.orientation.OrientationProvider;

/**
 * 
 * A Bubble level for Android phones
 * 
 * Under GPL v3 : http://www.gnu.org/licenses/gpl-3.0.html
 * 
 * @author antoine vianey
 *
 */
public class ProviderOrientation extends OrientationProvider {
	
	private static OrientationProvider provider;
        
	private Orientation orientation;
    private float pitch;
    private float roll;
	
	private ProviderOrientation() {}
	
	public static OrientationProvider getInstance() {
		if (provider == null) {
			provider = new ProviderOrientation();
		}
		return provider;
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	public void onSensorChanged(SensorEvent event) {
			
        pitch = event.values[1] - getCalibratedPitch();
        roll = event.values[2] - getCalibratedRoll();
 
        if (pitch < -45 && pitch > -135) {
            // top side up
            orientation = Orientation.TOP;
        } else if (pitch > 45 && pitch < 135) {
            // bottom side up
            orientation = Orientation.BOTTOM;
        } else if (roll > 45) {
            // right side up
            orientation = Orientation.RIGHT;
        } else if (roll < -45) {
            // left side up
            orientation = Orientation.LEFT;
        } else {
        	// landing
        	orientation = Orientation.LANDING;
        }
            
        getListener().onOrientationChanged(orientation, pitch, roll);
	}

	@Override
	protected int getSensorType() {
		return Sensor.TYPE_ORIENTATION;
	}

}