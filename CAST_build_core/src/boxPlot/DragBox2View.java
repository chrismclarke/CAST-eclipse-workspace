package boxPlot;

import java.awt.*;

import dataView.*;
import axis.*;


public class DragBox2View extends DragBoxView {
//	static public final String DRAG_BOX_PLOT2 = "dragBoxPlot2";
	
//	static private final int kQuartileHitSlop = 4;
	
//	private int selectedQuartile = NO_SELECTED_QUART;
//	private int hitOffset;
	
	protected int counts[] = new int[4];
//	private int minPos[] = new int[5];
//	private int maxPos[] = new int[5];
//	private boolean hints = false;
	
	public DragBox2View(DataSet theData, XApplet applet, NumCatAxis theAxis) {
		super(theData, applet, theAxis);
	}
	
	protected void drawOneCount(Graphics g, Point p, int count, int noOfValues) {
		NumValue proportion = new NumValue((double)count, 0);
		proportion.drawCentred(g, p.x, p.y);
	}
}
	
