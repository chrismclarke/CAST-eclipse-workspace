package bivarCat;

import java.awt.*;

import dataView.*;


abstract public class TwoWayView extends DataView {
	static public final int XMAIN = 0;
	static public final int YMAIN = 1;
	
	static public final int COUNT = 0;
	static public final int PROPN_IN_X = 1;
	static public final int PROPN_IN_Y = 2;
	static public final int PERCENT_IN_X = 3;
	static public final int PERCENT_IN_Y = 4;
	
	protected String xKey, yKey;
	
	protected int mainGrouping = XMAIN;
	protected int vertScale = COUNT;
	protected boolean stacked = false;
	
	private boolean initialised = false;
	protected int [][] jointCounts;
	protected int [] xCounts, yCounts;
	
	public TwoWayView(DataSet theData, XApplet applet, String xKey, String yKey, Insets theInsets) {
		super(theData, applet, theInsets);
		this.xKey = xKey;
		this.yKey = yKey;
	}
	
	public TwoWayView(DataSet theData, XApplet applet, String xKey, String yKey) {
		this(theData, applet, xKey, yKey, new Insets(0, 0, 0, 0));
	}
	
	protected boolean initialise(CatVariable x, Variable y) {
		if (!initialised) {
			jointCounts = x.getCounts(y);
			
			int noOfXCats = jointCounts.length;
			int noOfYCats = jointCounts[0].length;
			yCounts = new int[noOfYCats];
			xCounts = new int[noOfXCats];
			for (int i=0 ; i<noOfXCats ; i++)
				for (int j=0 ; j<noOfYCats ; j++) {
					int c = jointCounts[i][j];
					yCounts[j] += c;
					xCounts[i] += c;
				}
			
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	public int getMainGrouping() {
		return mainGrouping;
	}
	
	public int getVertScale() {
		return vertScale;
	}
	
	public boolean getStacked() {
		return stacked;
	}
	
	public void reinitialise() {
		initialised = false;
	}
	
	abstract public void setDisplayType(int newMainGrouping, int newHeightType,
																						boolean newStacked);
	
//-------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
