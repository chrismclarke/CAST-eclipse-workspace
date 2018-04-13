package dotPlot;

import dataView.*;
import axis.*;


public class ColouredDotPlotView extends StackingDotPlotView {
//	static public final String COLOURED_DOTPLOT = "colouredDotPlot";
	
	private CatVariable groupingVariable;
	private boolean showingGroups = false;
	
	public ColouredDotPlotView(DataSet theData, XApplet applet, NumCatAxis numAxis) {
		super(theData, applet, numAxis, 0.5);
	}
	
	protected int groupIndex(int itemIndex) {
		if (showingGroups)
			return groupingVariable.getItemCategory(itemIndex);
		else
			return 0;
	}
	
	protected void findInitPositions() {
		groupingVariable = getCatVariable();
		super.findInitPositions();
	}
	
	public void showGroups(boolean showNotHide) {
		showingGroups = showNotHide;
		repaint();
	}
	
	public void stackCrosses(boolean stackNotJitter) {
		if (stackNotJitter)
			setFinalFrame();
		else
			reset();
	}
}