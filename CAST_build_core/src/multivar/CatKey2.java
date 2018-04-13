package multivar;

import java.awt.*;

import dataView.*;
import utils.CatKey;

public class CatKey2 extends CatKey {
//	static public final String CAT_KEY2 = "catKey2";
	
	static public final Color groupBColor = Color.green;
	static public final Color groupCColor = Color.blue;
	
	public CatKey2(DataSet theData, String variableKey, XApplet applet, int orientation) {
		super(theData, variableKey, applet, orientation);
	}
	
	protected void drawSymbol(Graphics g, int x, int y, int index) {
		for (int i=0 ; i<2 ; i++)
			for (int j=0 ; j<2 ; j++) {
				Point p = new Point(x + i, y + j);
				switch (index) {
					case 0:
						g.setColor(Color.black);
						drawCross(g, p);
						break;
					case 1:
						g.setColor(groupBColor);
						drawSquare(g, p);
						break;
					default:
						g.setColor(groupCColor);
						drawPlus(g, p);
						break;
				}
			}
	}
}
