package scatter;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class ColouredScatterView extends ScatterView {
//	static public final String COLOURED_SCATTER_PLOT = "colouredScatterPlot";
	
	private CatVariable groupingVariable;
	private boolean showingGroups = false;
	
	public ColouredScatterView(DataSet theData, XApplet applet, HorizAxis xAxis,
												VertAxis yAxis, String xKey, String yKey, String groupKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
		groupingVariable = (CatVariable)theData.getVariable(groupKey);
	}
	
	protected int groupIndex(int itemIndex) {
		if (showingGroups) {
			int rawIndex = groupingVariable.getItemCategory(itemIndex);
			if (rawIndex == 1)
				rawIndex = 3;		//		because CatKey2 doesn't draw in red
			return rawIndex;
		}
		else
			return 0;
	}
	
	public void showGroups(boolean showNotHide) {
		showingGroups = showNotHide;
		repaint();
	}
}