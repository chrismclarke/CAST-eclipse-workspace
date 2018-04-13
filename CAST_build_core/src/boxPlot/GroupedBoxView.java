package boxPlot;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class GroupedBoxView extends BoxAndDotView {
	static public final int DOT_PLOT = 0;
	static public final int BOX_PLOT = 1;
	
//	static final private String GROUPED_BOX_PLOT = "groupedBoxPlot";
	static final private int kMaxGroupingJitter = 20;
	
	static final private Color kGridColor = new Color(0xD5D5D5);
	
	protected BoxInfo groupedBoxInfo[];
	protected NumVariable numSubset[];
	protected NumCatAxis groupAxis;
	private boolean isHorizontal;
	protected CatVariable groupingVariable;
	
	protected int plotType = DOT_PLOT;
	
	private boolean drawGrid = false;
	
	public GroupedBoxView(DataSet theData, XApplet applet, NumCatAxis theAxis, NumCatAxis groupAxis) {
		super(theData, applet, theAxis);
		this.groupAxis = groupAxis;
		isHorizontal = groupAxis instanceof VertAxis;
	}
	
	public void setPlotType(int newType) {
		plotType = newType;
		repaint();
	}
	
	public int getPlotType() {
		return plotType;
	}
	
	public void setDrawGrid(boolean drawGrid) {
		this.drawGrid = drawGrid;
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		Point newPoint = super.getScreenPoint(index, theVal, thePoint);
		if (newPoint != null) {
			int groupIndex = groupingVariable.getItemCategory(index);
			int offset = groupAxis.catValToPosition(groupIndex) - currentJitter / 2;
			if (isHorizontal)
				newPoint.y -= offset;
			else
				newPoint.x = offset + newPoint.x;
		}
		return newPoint;
	}
	
	protected int getMaxJitter() {
		int noOfGroups = groupingVariable.noOfCategories();
		return Math.min(kMaxGroupingJitter, getDisplayWidth() / noOfGroups / 2);
	}
	
	protected void initialiseAllBoxes() {
		int noOfCats = numSubset.length;
		groupedBoxInfo = new BoxInfo[noOfCats];
		for (int i=0 ; i<noOfCats ; i++)
			groupedBoxInfo[i] = new BoxInfo();
		
		for (int i=0 ; i<noOfCats ; i++) {
			NumValue sortedData[] = numSubset[i].getSortedData();
//			if (i == 5)
//				for (int j=0 ; j<sortedData.length ; j++)
//					System.out.println(j + ": " + sortedData[j].toString());
			initialiseBox(sortedData, groupedBoxInfo[i]);
			groupedBoxInfo[i].vertMidLine = groupAxis.catValToPosition(i);
			groupedBoxInfo[i].boxBottom = groupedBoxInfo[i].vertMidLine - groupedBoxInfo[i].getBoxHeight() / 2;
		}
	}
	
	protected void initialise(NumVariable variable) {
		int noOfCats = groupingVariable.noOfCategories();
		int noOfValues = Math.min(variable.noOfValues(), groupingVariable.noOfValues());
		
		jittering = null;				//		we cannot initialise jittering if initialise() is called
											//		from GroupedStatsView before this component is sized
		numSubset = new NumVariable[noOfCats];
		
		int groupCount[] = new int[noOfCats];
		for (int i=0 ; i<noOfValues ; i++)
			groupCount[groupingVariable.getItemCategory(i)]++;
		
		for (int i=0 ; i<noOfCats ; i++) {
			numSubset[i] = new NumVariable("");
			numSubset[i].setNoOfGroups(groupCount[i]);
		}
		
		int cumCount[] = new int[noOfCats];
		for (int i=0 ; i<noOfValues ; i++) {
			int group = groupingVariable.getItemCategory(i);
			numSubset[group].setValueAt(variable.valueAt(i), cumCount[group]++);
		}
		
		initialiseAllBoxes();
	}
	
	public void reinitialiseAfterTransform() {
		int noOfCats = groupingVariable.noOfCategories();
		for (int i=0 ; i<noOfCats ; i++)
			initialiseBox(numSubset[i].getSortedData(), groupedBoxInfo[i]);
	}
	
	protected void checkInitialisation(NumVariable variable) {
		if (groupingVariable == null)
			groupingVariable = getCatVariable();
		
		if (!initialised) {
			initialise(variable);
			initialised = true;
		}
	}
	
	public NumVariable getGroupVariable(int catNo) {
		NumVariable variable = getNumVariable();
		checkInitialisation(variable);
		return numSubset[catNo];
	}
	
	public BoxInfo getBoxInfo(int catNo) {
		NumVariable variable = getNumVariable();
		checkInitialisation(variable);
		return groupedBoxInfo[catNo];
	}
	
	public void resetForNewVariables() {
		groupingVariable = null;
		initialised = false;
	}
	
	protected void paintGroupBackground(Graphics g, NumVariable variable, BoxInfo theBoxInfo,
																										int catNo) {
	}
	
	private void drawBackgroundGrid(Graphics g) {
		g.setColor(kGridColor);
		
		Vector labels = axis.getLabels();
		Enumeration le = labels.elements();
		while (le.hasMoreElements()) {
			AxisLabel label = (AxisLabel)le.nextElement();
			double x = axis.minOnAxis + label.position * (axis.maxOnAxis - axis.minOnAxis);
			int xPos = getViewBorder().left + axis.numValToRawPosition(x);
			g.drawLine(xPos, 0, xPos, getSize().height);
		}
	}
	
	public void paintView(Graphics g) {
		NumVariable variable = getNumVariable();
		checkInitialisation(variable);
		if (jittering == null)
			initialiseJittering();
		
		if (drawGrid)
			drawBackgroundGrid(g);
		
		if (plotType == DOT_PLOT) {
			for (int i=0 ; i<groupingVariable.noOfCategories() ; i++)
				paintGroupBackground(g, numSubset[i], groupedBoxInfo[i], i);
			drawDotPlot(g, variable);
		}
		else {
			for (int i=0 ; i<groupingVariable.noOfCategories() ; i++)
				drawBoxPlot(g, numSubset[i].getSortedData(), groupedBoxInfo[i]);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(getActiveNumKey()) || key.equals(getActiveCatKey())) {
			resetForNewVariables();
			repaint();
		}
	}
}