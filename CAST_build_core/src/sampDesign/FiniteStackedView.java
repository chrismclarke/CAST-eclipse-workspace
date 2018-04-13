package sampDesign;

import java.awt.*;

import dataView.*;
import axis.*;


public class FiniteStackedView extends MarginalDataView {
//	static public final String FINITE_STACKED = "finiteStacked";
	
	static final private int kMinDisplayWidth = 50;
	
	private boolean initialised = false;
	
	protected String sampleKey;
	
	protected int vertPos[] = null;
	protected int horizPos[] = null;
	
	public FiniteStackedView(DataSet theData, XApplet applet, NumCatAxis theAxis, String sampleKey) {
		super(theData, applet, new Insets(5, 5, 5, 5), theAxis);
		this.sampleKey = sampleKey;
	}
	
	public int minDisplayWidth() {
		return kMinDisplayWidth;
	}
	
	protected boolean initialise() {
		if (initialised)
			return false;
		
		FinitePopnSampleVariable y = (FinitePopnSampleVariable)getVariable(sampleKey);
		NumValue[] popnValue = y.getPopnValues();
		int noOfValues = popnValue.length;
		
		int groupSize = getCrossSize() * 2 + 3;
		int noOfGroups = (axis.getAxisLength() - 1) / groupSize + 1;
		
		if (horizPos == null || horizPos.length != noOfValues)
			horizPos = new int[noOfValues];
		if (vertPos == null || vertPos.length != noOfValues)
			vertPos = new int[noOfValues];
		
		int minIndex[] = new int[noOfGroups];
		
		for (int i=0 ; i<noOfValues ; i++)
			try {
				int axisPos = axis.numValToPosition(popnValue[i].toDouble());
				
				int groupIndex = axisPos / groupSize;
				int vertIndex = minIndex[groupIndex];
				vertPos[i] = vertIndex * groupSize;
				horizPos[i] = groupIndex * groupSize + getCrossSize() + 1;
				minIndex[groupIndex] ++;
				
			} catch (AxisException ex) {
			}
		
		initialised = true;
		return true;
	}
	
	public void setCrossSize(int crossSize) {
		super.setCrossSize(crossSize);
		initialised = false;
		repaint();
	}
	
	protected Point getScreenPoint(int index, Point thePoint) {
		if (horizPos == null || horizPos[index] < 0)
			return null;
		return translateToScreen(horizPos[index], vertPos[index], thePoint);
	}
	
	public void paintView(Graphics g) {
		initialise();
		
		FinitePopnSampleVariable y = (FinitePopnSampleVariable)getVariable(sampleKey);
		NumValue[] popnValue = y.getPopnValues();
		Point thePoint = null;
		
		ValueEnumeration e = y.values();
		NumValue nextSampleVal = e.hasMoreValues() ? (NumValue)e.nextValue() : null;
		for (int i=0 ; i<popnValue.length ; i++) {
			thePoint = getScreenPoint(i, thePoint);
				if (popnValue[i] == nextSampleVal) {
					g.setColor(getForeground());
					drawBlob(g, thePoint);
					nextSampleVal = e.hasMoreValues() ? (NumValue)e.nextValue() : null;
				}
				else {
					g.setColor(Color.lightGray);
					drawCross(g, thePoint);
				}
		}
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(sampleKey)) {
			initialised = false;
			super.doChangeVariable(g, key);
		}
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}