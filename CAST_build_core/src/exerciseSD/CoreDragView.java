package exerciseSD;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;


abstract public class CoreDragView extends MarginalDataView implements StatusInterface {
	static final public int kMinDisplayWidth = 40;
	
//	static final private Color k3SDColor = new Color(0xEEEEFF);
//	static final private Color k2SDColor = new Color(0xDDDDFF);
//	static final private Color k1SDColor = new Color(0xBBBBFF);
	
	static final private Color kMeanBackgroundColor = new Color(0xFFBBBB);
	static final private Color k2SdBackgroundColor = new Color(0xFFCCCC);
	static final private Color k4SdBackgroundColor = new Color(0xFFDDDD);
	static final private Color k6SdBackgroundColor = new Color(0xFFEEEE);
	
	static final private double kEps = 1.0e-6;
	
	protected String yKey;
	
	protected double classStart[];
	protected double classCount[];
	protected int tooLowCount, tooHighCount;
	
	protected boolean initialised = false;
	private boolean show4s = false;
	
	private NumValue meanValue, sdValue;
	
	protected boolean allowDrag = false;
	protected int dragIndex = -1;
	protected int hitOffset;
	
	public CoreDragView(DataSet theData, XApplet applet, NumCatAxis valAxis, String yKey) {
		super(theData, applet, new Insets(0, 0, 0, 0), valAxis);
																//		no border under histo
		setFont(applet.getStandardBoldFont());
		this.yKey = yKey;
		meanValue = new NumValue(0.0, 0);
		sdValue = new NumValue(0.0, 0);
		allowDrag = (yKey == null);
	}
	
	public void setShow4s(boolean show4s) {
		this.show4s = show4s;
	}
	
	protected boolean getShow4s() {
		return show4s;
	}
	
	public void setMeanSdDecimals(int meanDecimals, int sdDecimals) {
		meanValue.decimals = meanDecimals;
		sdValue.decimals = sdDecimals;
	}
	
	abstract public void setMeanSD(double mean, double sd);
	
	abstract public String getStatus();
	abstract public void setStatus(String status);
	
//-------------------------------------------------------------------
	
	final public void initialise() {
		if (!initialised) {
			doInitialisation();
			initialised = true;
		}
	}
	
	protected void doInitialisation() {
		show4s = false;
		
		if (allowDrag) {
			classStart = defaultClasses();
			if (classCount == null)
				classCount = defaultCounts(classStart);
		}
		else {
			classStart = initialiseClasses();
			classCount = countClasses(classStart);
		}
	}
	
	public void resetClasses() {
		initialised = false;
		classCount = null;
		classStart = null;
	}
	
	abstract protected double[] defaultClasses();
	
	abstract protected double[] initialiseClasses();
	
	abstract protected double[] defaultCounts(double localClassStart[]);
	
	protected double[] countClasses(double localClassStart[]) {
		int noOfClasses = localClassStart.length - 1;
		double localClassCount[] = new double[noOfClasses];
		
		NumVariable theVariable = (NumVariable)getVariable(yKey);
		NumValue sortedVals[] = theVariable.getSortedData();
		int noOfVals = sortedVals.length;
		int index = 0;
		while (index < noOfVals && sortedVals[index].toDouble() < localClassStart[0])
			index++;
		tooLowCount = index;	//	to be sure to pick up extremes that might be rounded out of
		tooHighCount = 0;			//	outer classes for box plot
		
		int classIndex = 0;
		while (index < noOfVals) {
			if (sortedVals[index].toDouble() <= localClassStart[classIndex + 1]) {
				localClassCount[classIndex]++;
				index++;
			}
			else {
				classIndex++;
				if (classIndex >= noOfClasses) {
					tooHighCount = noOfVals - index;
					break;
				}
			}
		}
		return localClassCount;
	}

//-------------------------------------------------------------------
	
	protected int topBorder(Graphics g) {
		int ascent = g.getFontMetrics().getAscent();
		if (allowDrag)
			return 2 * ascent + 20;
		else
			return ascent + 16;
	}
	
	protected void drawTopBorder(Graphics g) {
		double mean = getMeanFromGraph();
		int ascent = g.getFontMetrics().getAscent();
		if (show4s) {
			double sd = getSDFromGraph();
			
			if (Double.isNaN(mean) || Double.isNaN(sd))
				return;
			
			if (allowDrag) {
				shade123SD(g, mean, sd);
				
				int meanBaseline = ascent + 2;
				Point p = translateToScreen(axis.numValToRawPosition(mean), 0, null);
				int meanPos = p.x;
				g.setColor(Color.white);
				g.drawLine(meanPos, 0, meanPos, getSize().height);
				g.setColor(Color.black);
				g.drawLine(meanPos, meanBaseline + 2, meanPos, getSize().height);
				
				meanValue.setValue(mean);
				LabelValue l = new LabelValue("mean = " + meanValue.toString());
				l.drawCentred(g, meanPos, meanBaseline);
				
				int sdBaseline = meanBaseline + ascent + 10;
				p = translateToScreen(axis.numValToRawPosition(mean + sd), 0, null);
				int sdPos = p.x;
				
				int arrowCentre = meanBaseline + 5;
				g.drawLine(meanPos, arrowCentre, sdPos, arrowCentre);
				g.drawLine(meanPos + 1, arrowCentre - 1, sdPos - 1, arrowCentre - 1);
				g.drawLine(meanPos + 1, arrowCentre + 1, sdPos - 1, arrowCentre + 1);
				
				for (int i=2 ; i<5 ; i++) {
					g.drawLine(sdPos - i, arrowCentre - i, sdPos - i, arrowCentre + i);
				}
				
				sdValue.setValue(sd);
				l = new LabelValue("sd = " + sdValue.toString());
				l.drawRight(g, meanPos + 3, sdBaseline);
			}
			else {
				shade123SD(g, mean, sd);
				g.setColor(Color.blue);
				double low = mean - 2 * sd;
				double high = mean + 2 * sd;
				
				int baseline = ascent + 2;
				
				Point p = translateToScreen(axis.numValToRawPosition(mean), 0, null);
				int meanPos = p.x;
				p = translateToScreen(axis.numValToRawPosition(low), 0, p);
				int lowPos = p.x;
				p = translateToScreen(axis.numValToRawPosition(high), 0, p);
				int highPos = p.x;
				
				sdValue.setValue(4 * sd);
				LabelValue l = new LabelValue("4 sd = " + sdValue.toString());
				l.drawCentred(g, meanPos, baseline);
				
				int arrowCentre = baseline + 4;
				g.drawLine(lowPos, arrowCentre, highPos, arrowCentre);
				g.drawLine(lowPos + 1, arrowCentre - 1, highPos - 1, arrowCentre - 1);
				g.drawLine(lowPos + 1, arrowCentre + 1, highPos - 1, arrowCentre + 1);
				
				for (int i=2 ; i<5 ; i++) {
					g.drawLine(lowPos + i, arrowCentre - i, lowPos + i, arrowCentre + i);
					g.drawLine(highPos - i, arrowCentre - i, highPos - i, arrowCentre + i);
				}
			}
		}
		else if (!allowDrag) {
			g.setColor(Color.gray);
			Point p = translateToScreen(axis.numValToRawPosition(mean), 0, null);
			int meanPos = p.x;
			g.drawLine(meanPos, 0, meanPos, getSize().height);
			
			meanValue.setValue(mean);
			LabelValue l = new LabelValue("mean");
			int meanBaseline = ascent + 2;
			l.drawRight(g, meanPos + 3, meanBaseline);
		}
	}
	
	private void shade123SD(Graphics g, double mean, double sd) {
		int lowPos = translateToScreen(axis.numValToRawPosition(mean - 3 * sd), 0, null).x;
		int highPos = translateToScreen(axis.numValToRawPosition(mean + 3 * sd), 0, null).x;
		g.setColor(k6SdBackgroundColor);
		g.fillRect(lowPos, 0, highPos - lowPos, getSize().height);
		g.setColor(k4SdBackgroundColor);
		g.drawLine(lowPos, 0, lowPos, getSize().height);
		g.drawLine(highPos, 0, highPos, getSize().height);
		
		lowPos = translateToScreen(axis.numValToRawPosition(mean - 2 * sd), 0, null).x;
		highPos = translateToScreen(axis.numValToRawPosition(mean + 2 * sd), 0, null).x;
		g.fillRect(lowPos, 0, highPos - lowPos, getSize().height);
		g.setColor(k2SdBackgroundColor);
		g.drawLine(lowPos, 0, lowPos, getSize().height);
		g.drawLine(highPos, 0, highPos, getSize().height);
		
		lowPos = translateToScreen(axis.numValToRawPosition(mean - sd), 0, null).x;
		highPos = translateToScreen(axis.numValToRawPosition(mean + sd), 0, null).x;
		g.fillRect(lowPos, 0, highPos - lowPos, getSize().height);
		g.setColor(kMeanBackgroundColor);
		g.drawLine(lowPos, 0, lowPos, getSize().height);
		g.drawLine(highPos, 0, highPos, getSize().height);
		
		int meanPos = translateToScreen(axis.numValToRawPosition(mean), 0, null).x;
		g.drawLine(meanPos, 0, meanPos, getSize().height);
	}

//-----------------------------------------------------------------------------------
	
	public double getMeanFromGraph() {
		initialise();
		double totalCount = 0.0;
		double sum = 0.0;
		
		for (int i=0 ; i<classCount.length ; i++) {
				totalCount += classCount[i];
				double a = classStart[i];
				double b = classStart[i + 1];
				double meani = (a + b) * 0.5;
				
				sum += classCount[i] * meani;
			}
		
		if (totalCount < kEps)
			return Double.NaN;
		
		return sum / totalCount;
	}
	
	public double getSDFromGraph() {
		initialise();
		double totalCount = 0.0;
		for (int i=0 ; i<classCount.length ; i++)
			totalCount += classCount[i];
		
		if (totalCount < kEps)
			return Double.NaN;
		
		double mean = 0.0;
		double exp2 = 0.0;
		
		for (int i=0 ; i<classCount.length ; i++)
			if (classCount[i] > 0.0) {
				double pi = classCount[i] / totalCount;
				double a = classStart[i];
				double b = classStart[i + 1];
				double meani = (a + b) * 0.5;
				double vari = (b - a) * (b - a) / 12.0;
				
				mean += pi * meani;
				exp2 += pi * (vari + meani * meani);
			}
		return Math.sqrt(exp2 - mean * mean);
	}
	
	public double getExtremeSD(boolean maxNotMin) {
		initialise();
		double totalCount = 0.0;
		for (int i=0 ; i<classCount.length ; i++)
			totalCount += classCount[i];
		double medianCount = totalCount * 0.5;
		
		if (totalCount < kEps)
			return Double.NaN;
		
		double sx = 0.0;
		double sxx = 0.0;
		double cumCount = 0.0;
		
		for (int i=0 ; i<classCount.length ; i++) {
			if ((cumCount < medianCount) == (cumCount + classCount[i] < medianCount)) {
																														//	all weight at one end of interval
				boolean lowEnd = (maxNotMin == (cumCount < medianCount));
				double x = lowEnd ? classStart[i] : classStart[i + 1];
				sx += x * classCount[i];
				sxx += x * x * classCount[i];
			}
			else {		//	middle class
				double lowCount = medianCount - cumCount;
				double highCount = classCount[i] - lowCount;
				if (maxNotMin) {
					double x1 = classStart[i];
					double x2 = classStart[i + 1];
					sx += x1 * lowCount + x2 * highCount;
					sxx += x1 * x1 * lowCount + x2 * x2 * highCount;
				}
				else {		//	all in middle of class
					double x = (classStart[i] * highCount + classStart[i + 1] * lowCount) / (highCount + lowCount);
					sx += x * classCount[i];
					sxx += x * x * classCount[i];
				}
			}
			cumCount += classCount[i];
		}
//		return Math.sqrt((sxx - sx * sx / totalCount) / (totalCount - 1));
		return Math.sqrt((sxx - sx * sx / totalCount) / totalCount);		//	popnSD not sampSD since totalCount==1 for dragging version
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return allowDrag;
	}

	public int minDisplayWidth() {
		return kMinDisplayWidth;
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		initialised = false;
		repaint();
	}
	
}
	
