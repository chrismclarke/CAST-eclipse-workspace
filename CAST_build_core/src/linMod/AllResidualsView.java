package linMod;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import random.*;

//import boxPlot.*;


public class AllResidualsView extends MarginalDataView {
//	static public final String ALL_RESID_PLOT = "allResiduals";
	
	static final public int DOT_PLOTS = 0;
	static final public int BOX_PLOTS = 1;
	static final public int TWO_SD = 2;
	
	static final private int kInitJitterLength = 20;
	
	static final private Color kBoxColor = new Color(0x000099);
	static final private Color kBoxFillColor = new Color(0xDDDDDD);
	
	private String[] residKey;
	private HorizAxis xAxis;
	
	private double x[];
	
	private int displayType = DOT_PLOTS;
	
	private int currentJitter = 0;
	private int jittering[] = null;
	private double initialJittering = 1.0;
	private boolean jitteringInitialised = false;
	
	public AllResidualsView(DataSet summaryData, XApplet applet, HorizAxis xAxis, VertAxis yAxis, String[] residKey,
						DataSet sourceData, String xKey) {
		super(summaryData, applet, new Insets(5, 5, 5, 5), yAxis);
		this.residKey = residKey;
		
		NumVariable xVar = (NumVariable)sourceData.getVariable(xKey);
		x = new double[xVar.noOfValues()];
		for (int i=0 ; i<x.length ; i++)
			x[i] = xVar.doubleValueAt(i);
		
		this.xAxis = xAxis;
	}
	
	public void setDisplayType(int displayType) {
		this.displayType = displayType;
		repaint();
	}
	
	protected Point getScreenPoint(int index, int xIndex, NumVariable[] resids, Point thePoint) {
		try {
			int xPos = xAxis.numValToPosition(x[xIndex]);
			if (currentJitter > 0 && jittering != null && index < jittering.length)
				xPos += ((currentJitter * jittering[index]) >> 14) - currentJitter / 2;
			
			double resid = resids[xIndex].doubleValueAt(index);
			int yPos = axis.numValToPosition(resid);
			
			return translateToScreen(yPos, xPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	public void paintView(Graphics g) {
		g.setColor(Color.lightGray);
		int zeroPos = axis.numValToRawPosition(0.0);
		Point p = translateToScreen(zeroPos, 0, null);
		g.drawLine(0, p.y, getSize().width, p.y);
		
		switch (displayType) {
			case DOT_PLOTS:
				drawCrosses(g);
				break;
			case BOX_PLOTS:
				drawBoxes(g);
				break;
			case TWO_SD:
			default:
				drawSDs(g);
				break;
		}
	}
	
	private void drawBoxes(Graphics g) {
		g.setColor(kBoxColor);
		for (int i=0 ; i<x.length ; i++) {
			BoxInfo boxInfo = new BoxInfo();
			boxInfo.setFillColor(kBoxFillColor);
			NumVariable resid = (NumVariable)getVariable(residKey[i]);
			boxInfo.initialiseBox(resid.getSortedData(), true, axis);
			boxInfo.vertMidLine = xAxis.numValToRawPosition(x[i]);
			boxInfo.boxBottom = boxInfo.vertMidLine - boxInfo.getBoxHeight() / 2;
			
			boxInfo.drawBoxPlot(g, this, resid.getSortedData(), axis);
		}
	}
	
	private void drawSDs(Graphics g) {
		g.setColor(Color.blue);
		for (int i=0 ; i<x.length ; i++) {
			int xPos = xAxis.numValToRawPosition(x[i]);
			NumVariable resid = (NumVariable)getVariable(residKey[i]);
			
			int n = 0;
			double sr = 0.0;
			double srr = 0.0;
			ValueEnumeration re = resid.values();
			while (re.hasMoreValues()) {
				double r = re.nextDouble();
				n ++;
				sr += r;
				srr += r * r;
			}
			double mean = sr / n;
			double sd = Math.sqrt((srr - sr * mean) / (n - 1));
			
			int meanPos = axis.numValToRawPosition(mean);
			int plusPos = axis.numValToRawPosition(mean + 2.0 * sd);
			int minusPos = axis.numValToRawPosition(mean - 2.0 * sd);
			
			Point pMean = translateToScreen(meanPos, xPos, null);
			Point pPlus = translateToScreen(plusPos, xPos, null);
			Point pMinus = translateToScreen(minusPos, xPos, null);
			
			g.drawLine(pMean.x - kInitJitterLength / 2, pMean.y, pMean.x + kInitJitterLength / 2,
																																									pMean.y);
			g.drawLine(pMinus.x - 1, pMinus.y - 1, pPlus.x - 1, pPlus.y + 1);
			g.drawLine(pMinus.x, pMinus.y, pPlus.x, pPlus.y);
			g.drawLine(pMinus.x + 1, pMinus.y - 1, pPlus.x + 1, pPlus.y + 1);
			
			for (int j=2 ; j<6 ; j++) {
				g.drawLine(pPlus.x - j, pPlus.y + j, pPlus.x + j, pPlus.y + j);
				g.drawLine(pMinus.x - j, pMinus.y - j, pMinus.x + j, pMinus.y - j);
			}
		}
	}
	
	private void drawCrosses(Graphics g) {
		checkJittering();
		
		NumVariable resids[] = new NumVariable[residKey.length];
		for (int i=0 ; i<residKey.length ; i++)
			resids[i] = (NumVariable)getVariable(residKey[i]);
		
		int n = resids[0].noOfValues();
		setCrossSize(n > 10 ? SMALL_CROSS : MEDIUM_CROSS);
		
		Point p = null;
		
		g.setColor(Color.red);
		FlagEnumeration fe = getSelection().getEnumeration();
		for (int i=0 ; i<n ; i++) {
			boolean nextSel = fe.nextFlag();
			if (nextSel)
				for (int j=0 ; j<residKey.length ; j++) {
					p = getScreenPoint(i, j, resids, p);
					drawCrossBackground(g, p);
				}
		}
		
		g.setColor(getForeground());
		for (int i=0 ; i<n ; i++) {
			for (int j=0 ; j<residKey.length ; j++) {
				p = getScreenPoint(i, j, resids, p);
				drawCross(g, p);
			}
		}
	}

//-----------------------------------------------------------------------------------
	
	protected void initialiseJittering() {
		if (!jitteringInitialised) {
			setJitter(initialJittering);
			jitteringInitialised = true;
		}
	}
	
	protected void checkJittering() {
		initialiseJittering();
		int dataLength = ((NumVariable)getVariable(residKey[0])).noOfValues();
		
		if (jittering == null) {
			int initialLength = Math.max(dataLength, kInitJitterLength);
			RandomBits generator = new RandomBits(14, initialLength);			//	between 0 and 2^14 = 16384
			jittering = generator.generate();
		}
		else if (jittering.length < dataLength) {
			int tempJittering[] = jittering;
			jittering = new int[2 * dataLength];
			System.arraycopy(tempJittering, 0, jittering, 0, tempJittering.length);
			
			RandomBits generator = new RandomBits(14, jittering.length - tempJittering.length);
			tempJittering = generator.generate();
			System.arraycopy(tempJittering, 0, jittering, jittering.length - tempJittering.length,
																																			tempJittering.length);
		}
	}
	
	public void newRandomJittering() {
		jittering = null;
		repaint();
	}
	
	protected int getMaxJitter() {
//		int available = getDisplayWidth();
		
		int minSpacing = Integer.MAX_VALUE;
		int lastPos = xAxis.numValToRawPosition(x[0]);
		
		for (int i=1 ; i<x.length ; i++) {
			int nextPos = xAxis.numValToRawPosition(x[i]);
			minSpacing = Math.min(minSpacing, nextPos - lastPos);
			lastPos = nextPos;
		}
		
		return minSpacing / 3;
	}
	
	public void setJitter(double fraction) {
		int maxJitter = getMaxJitter();
		currentJitter = (int)(fraction * maxJitter);
		repaint();
	}

//-----------------------------------------------------------------------------------
	
	public int minDisplayWidth() {
		return 200;		//	Arbitary value -- needed for MarginalDataView
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
