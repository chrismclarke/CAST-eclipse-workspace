package statistic2;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;


abstract public class BasicDataView extends MarginalDataView {
	static final public int kMinDisplayWidth = 40;
	
//	static final private String kPlusMinusSDString = "mean � 2s  =  ";
	
	static final private Color k2SDColor = new Color(0xDDDDFF);
	static final private Color k1SDColor = new Color(0xBBBBFF);
	
	static final private double kEps = 1.0e-6;
	
	protected String yKey;
	private XTextArea message;
	
	protected double classStart[];
	protected double classCount[];
	protected int tooLowCount, tooHighCount;
	
	private boolean initialised = false;
	private boolean show4s = false;
	
	private NumValue meanValue, sdValue;
	
	protected boolean allowDrag = false;
	protected int dragIndex = -1;
	protected int hitOffset;
	
	public BasicDataView(DataSet theData, XApplet applet,
							NumCatAxis valAxis, String yKey, int meanDecimals, int sdDecimals) {
		super(theData, applet, new Insets(0, 0, 0, 0), valAxis);
																//		no border under histo
		setFont(applet.getStandardBoldFont());
		this.yKey = yKey;
		meanValue = new NumValue(0.0, meanDecimals);
		sdValue = new NumValue(0.0, sdDecimals);
		allowDrag = (yKey == null);
	}
	
	public void setShow4s(boolean show4s) {
		this.show4s = show4s;
	}
	
	public void setLinkedMessage(XTextArea message) {
		this.message = message;
	}
	
	abstract public void changeClasses(double class0Start, double classWidth);
	
	abstract public void setMeanSD(double mean, double sd);

//-------------------------------------------------------------------
	
	protected void resetMessage() {
		if (message != null)
			message.setText(0);
	}
	
	protected boolean initialise() {
		if (initialised)
			return false;
		
		show4s = false;
		
		if (allowDrag) {
			classStart = defaultClasses();
			classCount = defaultCounts(classStart);
		}
		else {
			classStart = initialiseClasses();
			classCount = countClasses(classStart);
		}
		
		initialised = true;
		return true;
	}
	
	public void resetClasses() {
		initialised = false;
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
	
	abstract public void setMessages(String[] messageArray, NumValue exactAnswer);

//-------------------------------------------------------------------
	
	protected int topBorder(Graphics g) {
		int ascent = g.getFontMetrics().getAscent();
		if (allowDrag)
			return 2 * ascent + 20;
		else
			return ascent + 16;
	}
	
	protected void drawTopBorder(Graphics g) {
		double mean = findMeanFromHisto();
		int ascent = g.getFontMetrics().getAscent();
		if (show4s) {
			double sd = findSDFromHisto();
			
			if (Double.isNaN(mean) || Double.isNaN(sd))
				return;
			
			if (allowDrag) {
				Point p = translateToScreen(axis.numValToRawPosition(mean - 2 * sd), 0, null);
				int lowPos = p.x;
				p = translateToScreen(axis.numValToRawPosition(mean + 2 * sd), 0, null);
				int highPos = p.x;
				g.setColor(k2SDColor);
				g.fillRect(lowPos, 0, highPos - lowPos, getSize().height);
				g.setColor(k1SDColor);
				g.drawLine(lowPos, 0, lowPos, getSize().height);
				g.drawLine(highPos, 0, highPos, getSize().height);
				
				p = translateToScreen(axis.numValToRawPosition(mean - sd), 0, null);
				lowPos = p.x;
				p = translateToScreen(axis.numValToRawPosition(mean + sd), 0, null);
				highPos = p.x;
				g.setColor(k1SDColor);
				g.fillRect(lowPos, 0, highPos - lowPos, getSize().height);
				g.setColor(Color.blue);
				g.drawLine(lowPos, 0, lowPos, getSize().height);
				g.drawLine(highPos, 0, highPos, getSize().height);
				
				int meanBaseline = ascent + 2;
				p = translateToScreen(axis.numValToRawPosition(mean), 0, null);
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
//					g.drawLine(meanPos + i, arrowCentre - i, meanPos + i, arrowCentre + i);
					g.drawLine(sdPos - i, arrowCentre - i, sdPos - i, arrowCentre + i);
				}
				
				sdValue.setValue(sd);
				l = new LabelValue("sd = " + sdValue.toString());
				l.drawRight(g, meanPos + 3, sdBaseline);
			}
			else {
				g.setColor(Color.blue);
				double low = mean - 2 * sd;
				double high = mean + 2 * sd;
				
				int baseline = ascent + 2;
				
				Point p = translateToScreen(axis.numValToRawPosition(mean), 0, null);
				int meanPos = p.x;
				p = translateToScreen(axis.numValToRawPosition(low), 0, null);
				int lowPos = p.x;
				p = translateToScreen(axis.numValToRawPosition(high), 0, null);
				int highPos = p.x;
				
//				sdValue.setValue(2 * sd);
//				meanValue.setValue(mean);
//				LabelValue l = new LabelValue(kPlusMinusSDString + meanValue.toString()
//																			+ " � " + sdValue.toString());
				
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
			LabelValue l = new LabelValue(getApplet().translate("mean"));
			int meanBaseline = ascent + 2;
			l.drawRight(g, meanPos + 3, meanBaseline);
		}
	}

//-----------------------------------------------------------------------------------
	
	public double findMeanFromHisto() {
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
	
	public double findSDFromHisto() {
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
	
