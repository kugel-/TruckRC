package app.truck.TruckRC;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableLayout;

public class TruckView extends TableLayout implements OnClickListener 
{
	int buttonHeight = -1;
	public TruckView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/**
	 * 
	 * @return Die Gesamthöhe der Buttons
	 */
	public int getButtonHeight()
	{
		return buttonHeight;
	}

	/*
	 * Wird vom Android-System aufgerufen, wenn das Layout festgelegt wird
	 * Hier können wir die Höhe der Buttons fest stellen und sie später
	 * für die TruckListView zur verfügung stellen
	 */
	@Override
	public void onLayout(boolean changed, int l, int t, int r, int b)
	{
		super.onLayout(changed, l, t, buttonHeight, b);
		if (buttonHeight != -1)
			return;
		buttonHeight = 0;
		for(int i = 1; i < getChildCount(); i++)
		{
			View row = getChildAt(i);
			buttonHeight += row.getHeight();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	}
}
