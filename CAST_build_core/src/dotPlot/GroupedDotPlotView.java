package dotPlot;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;



public class GroupedDotPlotView extends DotPlotView {
	private static final int kMaxVertJitter = 30;
	public static final int kEndFrame = 40;
	
//	static public final String GROUPED_DOTPLOT = "groupedDotPlot";
	
	private NumCatAxis groupAxis;
	private CatVariable groupingVariable;
	
	private boolean isHorizontal;
	
	public GroupedDotPlotView(DataSet theData, XApplet applet, NumCatAxis numAxis, NumCatAxis groupAxis) {
		super(theData, applet, numAxis, 1.0);
		this.groupAxis = groupAxis;
		isHorizontal = groupAxis instanceof VertAxis;
	}
	
	protected int groupIndex(int itemIndex) {
		if (getCurrentFrame() > 1)
			return groupingVariable.getItemCategory(itemIndex);
		else
			return 0;
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		Point newPoint = super.getScreenPoint(index, theVal, thePoint);
		if (newPoint != null) {
			int groupIndex = groupingVariable.getItemCategory(index);
			int offset = groupAxis.catValToPosition(groupIndex) - currentJitter / 2;
			if (isHorizontal)
				newPoint.y -= (offset * getCurrentFrame() / kEndFrame);
			else
				newPoint.x = (offset * getCurrentFrame() / kEndFrame) + newPoint.x;
		}
		return newPoint;
	}
	
	protected int getMaxJitter() {
		int noOfGroups = groupingVariable.noOfCategories();
		return Math.min(kMaxVertJitter,
							(getSize().height - getViewBorder().top - getViewBorder().bottom) / noOfGroups / 2);
	}
	
	public void paintView(Graphics g) {
		if (groupingVariable == null) {
			groupingVariable = getCatVariable();
			setJitter(0.5);
		}
		groupAxis.show(getCurrentFrame() == kEndFrame);
		super.paintView(g);
	}
	
	public void doGroupingAnimation(XSlider controller) {
		animateFrames(1, kEndFrame - 1, 10, controller);
	}
}