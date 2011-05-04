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
