package app.truck.TruckRC;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TableRow;

public class TruckListView extends ListView 
{
	private TruckView parent = null;
	private int parentHeight;
	
	/* Constructors werden nicht implizit vererbt :( */
	public TruckListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public TruckListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public TruckListView(Context context) {
		super(context);
	}

	/**
	 * Fügt der Liste eine Zeile hinzu, und scrollt automatisch zur dieser Zeile
	 * @param text Hinzuzufügende Zeile
	 */
	@SuppressWarnings("unchecked")
	public void append(String text)
	{
		ArrayAdapter<String> foo = (ArrayAdapter<String>)getAdapter();
		foo.add(text);
		setSelection(foo.getCount()); /* scrolle ans Ende */
	}

	/* Die Methode passt das Layout einmalig for dem ersten Zeichen an */
	@Override
	protected void onDraw(Canvas canvas)
	{
		boolean newParent = parent == null;
		if (parent == null)
		{
			parent = (TruckView)getParent().getParent();
			parentHeight = parent.getButtonHeight();
		}
        if (newParent || (parent.getButtonHeight() != parentHeight)) 
        {	/* Maximiere die Liste, aber nur soweit, das die 6 Buttons unten noch hinpassen */
            TableRow.LayoutParams params = (android.widget.TableRow.LayoutParams) getLayoutParams();
            params.height = ((View)parent.getParent()).getHeight() - parent.getButtonHeight();
            this.setLayoutParams(params);
        }
        /* jetzt können wir die UI zeichnen */
        super.onDraw(canvas);
	}
}
