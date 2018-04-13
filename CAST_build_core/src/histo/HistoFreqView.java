package histo;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;


public class HistoFreqView extends DataView {
	
	static final private Color kCrossColor = Color.black;
	static final private Color kHistoFillColor = new Color(0xC8CDEB);
	
	static final private double kDimProportion = 0.8;
	
	private HorizAxis xAxis;
	private VertAxis freqAxis;
	
	private double class0Start, classWidth;
	private int classCount[];
	
	private int classGrouping;
	private boolean showCrosses = true;
	
	private boolean initialised = false;
	
	private int selectedGroup = -1;
	
	public HistoFreqView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis freqAxis,
																											String groupingInfo, int startGrouping) {
		super(theData, applet, new Insets(0,0,-1,0));
		this.xAxis = xAxis;
		this.freqAxis = freqAxis;
		
		StringTokenizer st = new StringTokenizer(groupingInfo);
		class0Start = Double.parseDouble(st.nextToken());
		classWidth = Double.parseDouble(st.nextToken());
		
		classGrouping = startGrouping;
		
		setFont(applet.getBigBoldFont());
	}
	
	public void changeClassGrouping(int classGrouping) {
		this.classGrouping = classGrouping;
		repaint();
	}
	
	public void setShowCrosses(boolean showCrosses) {
		this.showCrosses = showCrosses;
		repaint();
	}
	
	protected void doInitialisation(Graphics g) {
		NumVariable xVar = getNumVariable();
		NumValue sortedVal[] = xVar.getSortedData();
		
//		double classBottom = class0Start;
		double classTop = class0Start + classWidth;
		int nClasses = (int)Math.round(Math.ceil((Math.max(xAxis.maxOnAxis,
																		sortedVal[sortedVal.length - 1].toDouble()) - class0Start) / classWidth));
		classCount = new int[nClasses];
		
		int i = 0;
		int classIndex = 0;
		while (true) {
			if (i >= sortedVal.length)
				break;
			if (sortedVal[i].toDouble() >= classTop) {
				while (sortedVal[i].toDouble() >= classTop) {
//					classBottom = classTop;
					classTop += classWidth;
					classIndex ++;
				}
			}
			
			classCount[classIndex] = 0;
			while (i < sortedVal.length && sortedVal[i].toDouble() < classTop) {
				classCount[classIndex] ++;
				i ++;
			}
		}
	}
	
	private void initialise(Graphics g) {
		if (!initialised) {
			doInitialisation(g);
			initialised = true;
		}
	}
	
	private void drawOneClass(Graphics g, int i, Color fillColor, Color outlineColor,
														Color crossColor, double classBottom, double classTop, int xBottom,
														int xTop, int classPix, Point p0, Point p1, boolean drawCount) {
		int freq = 0;
		for (int j=0 ; j<classGrouping ; j++)
			if (i + j < classCount.length)
				freq += classCount[i + j];
		
		int classY = freqAxis.numValToRawPosition(freq);
		
		p0 = translateToScreen(xBottom, classY, p0);
		p1 = translateToScreen(xTop, 0, p1);
		
		g.setColor(fillColor);
		g.fillRect(p0.x, p0.y, (p1.x - p0.x), (p1.y - p0.y));
		
		g.setColor(outlineColor);
		g.drawRect(p0.x, p0.y, (p1.x - p0.x), (p1.y - p0.y));
		
		if (drawCount) {
			g.setColor(Color.red);
			g.drawLine(0, p0.y, p0.x - 1, p0.y);
			g.drawLine(1, p0.y - 1, p1.x, p0.y - 1);
			g.drawLine(1, p0.y + 1, p0.x - 1, p0.y + 1);
			g.drawLine(0, p0.y, 5, p0.y - 5);
			g.drawLine(0, p0.y, 5, p0.y + 5);
			
			String freqString = freq + (freq == 1 ? " value in class" : " values in class");
			FontMetrics fm = g.getFontMetrics();
			int freqWidth = fm.stringWidth(freqString);
			int baseline = p0.y - 4;
			int left = Math.max(5, (p1.x - freqWidth) / 2);
			g.drawString(freqString, left, baseline);
		}
		
		if (showCrosses) {
			g.setColor(crossColor);
			int crossSize = (classPix * 2) / 3;
			int halfCrossSize = crossSize / 2;
			
			for (int j=0 ; j<classGrouping ; j++)
				if (i + j < classCount.length) {
					int columnCount = classCount[i + j];
					
					int xCenter = xAxis.numValToRawPosition(classBottom + j * classWidth + classWidth / 2);
					
					for (int k=0 ; k<columnCount ; k++) {
						p1 = translateToScreen(xCenter, classPix / 2 + k * classPix, p1);
						g.drawLine(p1.x - halfCrossSize, p1.y - halfCrossSize, p1.x + halfCrossSize,
																																						p1.y + halfCrossSize);
						g.drawLine(p1.x - halfCrossSize, p1.y + halfCrossSize, p1.x + halfCrossSize,
																																						p1.y - halfCrossSize);
					}
				}
		}
	}
	
	private void drawHisto(Graphics g) {
		Color dimFillColor = dimColor(kHistoFillColor, kDimProportion);
		Color dimOutlineColor = dimColor(getForeground(), kDimProportion);
		Color dimCrossColor = dimColor(kCrossColor, kDimProportion);
		
		Point p0 = null;
		Point p1 = null;
		
		int classPix = xAxis.numValToRawPosition(class0Start + classWidth)
																									- xAxis.numValToRawPosition(class0Start);
		
		double classBottom = class0Start;
		double classTop = class0Start + classGrouping * classWidth;
		int xBottom = xAxis.numValToRawPosition(classBottom);
		int xTop = xAxis.numValToRawPosition(classTop);
		
		if (selectedGroup >= 0) {
			for (int i=0 ; i<classCount.length ; i+=classGrouping) {
				int histoClass = i / classGrouping;
				if (histoClass != selectedGroup)
					drawOneClass(g, i, dimFillColor, dimOutlineColor, dimCrossColor, classBottom,
																			classTop, xBottom, xTop, classPix, p0, p1, false);
				
				classBottom = classTop;
				classTop += classGrouping * classWidth;
				xBottom = xTop;
				xTop = xAxis.numValToRawPosition(classTop);
			}
			
			classBottom = class0Start;
			classTop = class0Start + classGrouping * classWidth;
			xBottom = xAxis.numValToRawPosition(classBottom);
			xTop = xAxis.numValToRawPosition(classTop);
		}
		
		for (int i=0 ; i<classCount.length ; i+=classGrouping) {
			int histoClass = i / classGrouping;
			if (selectedGroup < 0 || selectedGroup == histoClass)
				drawOneClass(g, i, kHistoFillColor, getForeground(), kCrossColor, classBottom, classTop,
																		xBottom, xTop, classPix, p0, p1, selectedGroup == histoClass);
			
			classBottom = classTop;
			classTop += classGrouping * classWidth;
			xBottom = xTop;
			xTop = xAxis.numValToRawPosition(classTop);
		}
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		drawHisto(g);
	}
	
//-----------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		int horiz = translateFromScreen(x, y, null).x;
		try {
			double hitVal = xAxis.positionToNumVal(horiz);
			int stackIndex = (int)Math.round(Math.floor((hitVal - class0Start) / classWidth));
			return new ClassPosInfo(stackIndex / classGrouping);
		} catch (AxisException e) {
			return null;
		}
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo == null)
			return false;
		ClassPosInfo startPos = (ClassPosInfo)startInfo;
		selectedGroup = startPos.classIndex;
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		selectedGroup = -1;
		repaint();
	}
	
}