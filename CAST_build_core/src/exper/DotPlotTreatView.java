package exper;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class DotPlotTreatView extends DotPlotView {
	private static final int kMaxHorizJitter = 30;
	
//	static public final String DOTPLOT_TREAT = "dotPlotTreat";
	
	private NumCatAxis responseAxis;
	private CatVariable groupingVariable;
	
	public DotPlotTreatView(DataSet theData, XApplet applet, NumCatAxis numAxis, NumCatAxis groupAxis,
								double initialJittering) {
		super(theData, applet, groupAxis, initialJittering);
		this.responseAxis = numAxis;
		groupingVariable = getCatVariable();
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		try {
			int vertPos = responseAxis.numValToPosition(theVal.toDouble());
			int groupIndex = groupingVariable.getItemCategory(index);
			int offset = (currentJitter > 0 && jittering != null) ? ((currentJitter * jittering[index]) >> 14) - currentJitter / 2 : 0;
			int horizPos = axis.catValToPosition(groupIndex) + offset;
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	protected int getMaxJitter() {
		int noOfGroups = groupingVariable.noOfCategories();
		return Math.min(kMaxHorizJitter,
							(getSize().width - getViewBorder().left - getViewBorder().right) / noOfGroups / 4);
	}
}