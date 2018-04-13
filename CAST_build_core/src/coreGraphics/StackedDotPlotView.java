package coreGraphics;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import axis.*;


public class StackedDotPlotView extends DotPlotView {
//	static public final String STACK_DOTPLOT = "stackedDotPlot";
	
	static protected final int kOffAxisPos = -100;		//	a value small enough to be off the axes after grouping
	
	private boolean initialised = false;
	
	protected String freqKey;
	protected boolean popNotSamp;
	
	protected int vertPos[] = null;
	protected int horizPos[] = null;
	private int maxStackHeight;
	
	public StackedDotPlotView(DataSet theData, XApplet applet, NumCatAxis theAxis,
																										String freqKey, boolean popNotSamp) {
		super(theData, applet, theAxis, 0.0);
		this.freqKey = freqKey;
		this.popNotSamp = popNotSamp;
		addComponentListener(this);
	}
	
	public StackedDotPlotView(DataSet theData, XApplet applet, NumCatAxis theAxis) {
		this(theData, applet, theAxis, null, false);
	}
	
	public void setFreqKey(String freqKey) {
		this.freqKey = freqKey;
		initialised = false;
		repaint();
	}
	
	protected int getClassSize() {
		return getCrossPix() * 2 + 3;
	}
	
	protected int getNoOfClasses() {
		return (axis.getAxisLength() - 1) / getClassSize() + 1;
	}
	
	protected boolean initialise() {
		if (initialised)
			return false;
		
		NumVariable yVar = getNumVariable();
		int noOfValues = yVar.noOfValues();
		
		boolean useFreq = !popNotSamp && (freqKey != null);
		FreqVariable f = useFreq ? (FreqVariable)getVariable(freqKey) : null;
		
		int groupSize = getClassSize();
		int noOfGroups = getNoOfClasses();
		
		if (horizPos == null || horizPos.length != noOfValues)
			horizPos = new int[noOfValues];
		if (vertPos == null || vertPos.length != noOfValues)
			vertPos = new int[noOfValues];
		
		int minIndex[] = new int[noOfGroups];
		
		for (int i=0 ; i<noOfValues ; i++)
			try {
				int freq = useFreq ? ((FreqValue)f.valueAt(i)).intValue : 1;
				if (freq == 0) {
					horizPos[i] = kOffAxisPos;
					continue;
				}
				double y = ((NumValue)yVar.valueAt(i)).toDouble();
				if (Double.isNaN(y))
					vertPos[i] = -100;		//		so it does not display
				else {
					int axisPos = axis.numValToPosition(y);
					
					int groupIndex = axisPos / groupSize;
					int vertIndex = minIndex[groupIndex];
					vertPos[i] = vertIndex * groupSize;
					horizPos[i] = groupIndex * groupSize + getCrossPix() + 1;
					minIndex[groupIndex] ++;
				}
			} catch (AxisException ex) {
				horizPos[i] = kOffAxisPos;
			}
		
		int maxIndex = 0;
		for (int i=0 ; i<noOfGroups ; i++)
			if (minIndex[i] > maxIndex)
				maxIndex = minIndex[i];
		maxStackHeight = (maxIndex + 1) * groupSize;
		
		int availableHeight = getDisplayWidth();
		if (maxStackHeight - groupSize > availableHeight)
			for (int i=0 ; i<noOfValues ; i++)
				vertPos[i] = (vertPos[i] * availableHeight) / (maxStackHeight - groupSize);
		
		initialised = true;
		return true;
	}
	
	protected void checkJittering() {
	}
	
	protected int getMaxStackHeight() {
		return maxStackHeight;
	}
	
	public void setCrossSize(int crossSize) {
		super.setCrossSize(crossSize);
		initialised = false;
		repaint();
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		if (horizPos == null || index >= horizPos.length || horizPos[index] < 0)
			return null;
		return translateToScreen(horizPos[index], vertPos[index], thePoint);
	}
	
	protected void paintBackground(Graphics g) {
	}
	
	public void paintView(Graphics g) {
		initialise();
		paintBackground(g);
		super.paintView(g);
	}
	
	protected void doAddValues(Graphics g, int noOfValues) {
		initialised = false;
		repaint();
	}
	
	public void setActiveNumVariable(String key) {
		super.setActiveNumVariable(key);
		initialised = false;
	}

//-----------------------------------------------------------------------------------

	public void componentResized(ComponentEvent e) {
		initialised = false;
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(freqKey) || key.equals(getActiveNumKey()))
			initialised = false;
		super.doChangeVariable(g, key);
	}
	
	protected boolean canDrag() {
		return super.canDrag() && (!popNotSamp || (freqKey != null));
	}
}