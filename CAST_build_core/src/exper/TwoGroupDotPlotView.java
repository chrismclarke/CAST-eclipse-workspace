package exper;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class TwoGroupDotPlotView extends DotPlotView {
//	static public final String TWOGROUP_DOTPLOT = "twoGroupDotPlot";
	
	private CatVariable groupingVariable;
	
	public TwoGroupDotPlotView(DataSet theData, XApplet applet, NumCatAxis numAxis) {
		super(theData, applet, numAxis, 1.0);
		groupingVariable = getCatVariable();
	}
	
	protected int groupIndex(int itemIndex) {
		return groupingVariable.getItemCategory(itemIndex);
	}
}