package randomStat;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;


public class StackedDiscBinomialView extends DotPlotView implements DataPlusDistnInterface {
	
	static protected final int kOffAxisPos = -100;		//	a value small enough to be off the axes after grouping
//	private static final int kMinWidth = 50;
	
	private boolean initialised = false;
	private String distnKey;
	private int densityType = CONTIN_DISTN;
	private Color distnColor = new Color(0xFF9999);
	
	protected int vertPos[] = null;
	protected int horizPos[] = null;
	private int minIndex[];
	private int maxStackHeight;
	
	private BackgroundNormalArtist backgroundDrawer;
	
	public StackedDiscBinomialView(DataSet theData, XApplet applet, NumCatAxis theAxis,
																															String distnKey, String normalKey) {
		super(theData, applet, theAxis, 0.0);
		this.distnKey = distnKey;
		if (normalKey != null)
			backgroundDrawer = new BackgroundNormalArtist(normalKey, theData);
	}
	
	public void setDimData(boolean dimData) {
		setForeground(dimData ? mixColors(distnColor, Color.black) : Color.black);
	}
	
	protected boolean initialise() {
		if (initialised)
			return false;
		
		NumVariable y = getNumVariable();
		int noOfValues = y.noOfValues();
		
		if (horizPos == null || horizPos.length != noOfValues)
			horizPos = new int[noOfValues];
		if (vertPos == null || vertPos.length != noOfValues)
			vertPos = new int[noOfValues];
		
		BinomialDistnVariable distn = (BinomialDistnVariable)getVariable(distnKey);
		int n = distn.getCount();
		minIndex = new int[n + 1];
		
		addPositions(y, 0);
		
		initialised = true;
		return true;
	}
	
	private int[] resizeArray(int[] array, int newSize) {
		int[] temp = array;
		array = new int[newSize];
		System.arraycopy(temp, 0, array, 0, temp.length);
		return array;
	}
	
	private void addPositions(NumVariable y, int startIndex) {
		BinomialDistnVariable distn = (BinomialDistnVariable)getVariable(distnKey);
		int n = distn.getCount();
		
		int groupSize = getCrossSize() * 2 + 3;
	
		for (int i=startIndex ; i<horizPos.length ; i++)
			try {
				double prob = y.doubleValueAt(i);
				int groupIndex = (int)Math.round(prob * n);
				int vertIndex = minIndex[groupIndex];
				vertPos[i] = vertIndex * groupSize;
				horizPos[i] = axis.numValToPosition(prob);
				minIndex[groupIndex] ++;
				
			} catch (AxisException ex) {
				horizPos[i] = kOffAxisPos;
			}
		
		int maxIndex = 0;
		for (int i=0 ; i<=n ; i++)
			if (minIndex[i] > maxIndex)
				maxIndex = minIndex[i];
		maxStackHeight = (maxIndex + 1) * groupSize;
	}
	
	private void addCrossInfo() {
		NumVariable y = getNumVariable();
		int noOfValues = y.noOfValues();
		
		if (horizPos == null || horizPos.length >= noOfValues) {
			initialised = false;
			initialise();
			return;
		}
		
		int oldNoOfValues = horizPos.length;
		
		horizPos = resizeArray(horizPos, noOfValues);
		vertPos = resizeArray(vertPos, noOfValues);
		
		addPositions(y, oldNoOfValues);
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
		if (horizPos == null || horizPos[index] < 0)
			return null;
		
		int theVertPos = vertPos[index];
		int displayHeight = getDisplayWidth();
		if (maxStackHeight > displayHeight)
			theVertPos = theVertPos * displayHeight / maxStackHeight;
		
		return translateToScreen(horizPos[index], theVertPos, thePoint);
	}
	
	protected void paintBackground(Graphics g) {
		g.setColor(distnColor);
		BinomialDistnVariable distn = (BinomialDistnVariable)getVariable(distnKey);
		double probFactor = distn.getProbFactor();
		double maxProb = distn.getMaxScaledProb() * probFactor;
		
		int n = distn.getCount();
		int displayHeight = getSize().height;
		int displayXOffset = getViewBorder().left;
		
		int posCount = 0;
		int pos0 = 0;
		int halfBarWidth = 0;
		for (int i=0 ; i<=n ; i++)
			try {
				double pi = i / (double)n;
				int xPos = axis.numValToPosition(pi);
				if (posCount == 0)
					pos0 = xPos;
				else if (posCount == 1) {
					int distance = xPos - pos0;
					if (distance >= 20)
						halfBarWidth = 3;
					else if (distance >= 8)
						halfBarWidth = 2;
					else if (distance >= 5)
						halfBarWidth = 1;
					else
						halfBarWidth = 0;
					break;
				}
				posCount ++;
			} catch (AxisException e) {
			}
		
		for (int i=0 ; i<=n ; i++)
			try {
				double pi = i / (double)n;
				double prob = distn.getScaledProb(i) * probFactor;
				
				int xPos = displayXOffset + axis.numValToPosition(pi);
				int barHt = (int)Math.round(prob / maxProb * displayHeight);
				
				int yPos = displayHeight - barHt;
				g.fillRect(xPos - halfBarWidth, yPos, 2 * halfBarWidth + 1, barHt);
			} catch (AxisException e) {
			}
	}
	
	public void paintView(Graphics g) {
		initialise();
		if (densityType == DISCRETE_DISTN)
			paintBackground(g);
		else if (densityType == CONTIN_DISTN)
			backgroundDrawer.paintDistn(g, this, axis);
		
		super.paintView(g);
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(distnKey) || key.equals(getActiveNumKey()))
			initialised = false;
		super.doChangeVariable(g, key);
	}
	
	protected void doAddValues(Graphics g, int noOfValues) {
		addCrossInfo();
		repaint();
	}
	
	public void setShowDensity (int densityType) {
		this.densityType = densityType;
		repaint();
	}
	
	public void setDensityColor(Color c) {
		distnColor = c;
		if (backgroundDrawer != null)
			backgroundDrawer.setFillColor(c);
	}
	
	public void setDistnLabel(LabelValue label, Color labelColor) {
	}
}