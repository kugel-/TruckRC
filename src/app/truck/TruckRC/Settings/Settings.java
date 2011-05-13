package app.truck.TruckRC.Settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import app.truck.TruckRC.R;

public class Settings extends PreferenceActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}
