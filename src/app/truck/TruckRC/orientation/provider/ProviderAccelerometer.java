package app.truck.TruckRC.orientation.provider;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
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
public class ProviderAccelerometer extends OrientationProvider implements SensorEventListener {
	
	private static OrientationProvider provider;
    
	private Orientation orientation;
 
    private float x;
    private float y;
    private float z;
    private float pitch;
    private float roll;
    private double norm;
	
	private ProviderAccelerometer() {}
	
	public static OrientationProvider getInstance() {
		if (provider == null) {
			provider = new ProviderAccelerometer();
		}
		return provider;
	}
 
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
 
    public void onSensorChanged(SensorEvent event) {
 
        x = event.values[0];
        y = event.values[1];
        z = event.values[2];
            
            // calcul du pitch
        norm = Math.sqrt(x*x + z*z);
        if (norm != 0) {
        	pitch = (float) (- Math.atan2(y, norm) * 180 / Math.PI);
        } else {
        	pitch = 0;
        }
        
        // calcul du roll
        norm = Math.sqrt(y*y + z*z);
        if (norm != 0) {
        	roll = (float) (Math.atan2(x, norm) * 180 / Math.PI);
        } else {
        	roll = 0;
        }
            
        pitch -= getCalibratedPitch();
        roll -= getCalibratedRoll();
 
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
		return Sensor.TYPE_ACCELEROMETER;
	}
	
}