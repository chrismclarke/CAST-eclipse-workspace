package propnVenn;

import java.awt.*;

import dataView.*;
import axis.*;


public class AreaContin00View extends AreaContin2View {
//	static public final String AREA_CONTIN00 = "areaContin00";
	
	public AreaContin00View(DataSet theData, XApplet applet, VertAxis yAxis, HorizAxis xAxis, String yKey,
						String xKey, boolean canSelect, boolean yMargin) {
		super(theData, applet, yAxis, xAxis, yKey, xKey, canSelect, yMargin);
	}
	
	protected void drawVennExtras(CatVariableInterface yVar, CatVariableInterface xVar,
																Graphics g, double framePropn) {
		g.setColor(Color.black);
		theDrawer.drawHorizArrow(0, 0, framePropn, g);
	}
}
