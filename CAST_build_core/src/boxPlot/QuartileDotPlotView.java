package boxPlot;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class QuartileDotPlotView extends DotPlotView {
	
//	static public final String QUARTILE_DOTPLOT = "quartileDotPlot";
	
	static final private int kArrowSize = 4;
	static final private int kTopBorder = 3;
	
	static final private Color kOrange = new Color(0x660033);
	static final private LabelValue kValuesLabel = new LabelValue("values");
	
	private String kQuartileLabel1[];
	private String kQuartileLabel2[];
	
	private boolean initialised = false;
	
	private int arrowEnd;
	private int labelBase1, labelBase2, countBase1, countBase2;
	
	private String yKey;
	private int noOfTiles;
	private double tile[];
	private String tileLabel1[], tileLabel2[];
	private int nBetweenTiles[];
	
	int highlight = -1;
	
	public QuartileDotPlotView(DataSet theData, XApplet applet, String yKey, NumCatAxis numAxis,
																																				int noOfTiles) {
		super(theData, applet, numAxis, 1.0);
		setActiveNumVariable(yKey);
		this.yKey = yKey;
		this.noOfTiles = noOfTiles;
		
		kQuartileLabel1 = new String[3];
		kQuartileLabel2 = new String[3];
		StringTokenizer st = new StringTokenizer(applet.translate("Lower*quartile"), "*");
		kQuartileLabel1[0] = st.nextToken();
		if (st.hasMoreTokens())
			kQuartileLabel2[0] = st.nextToken();
		st = new StringTokenizer(applet.translate("Median*"), "*");
		kQuartileLabel1[1] = st.nextToken();
		if (st.hasMoreTokens())
			kQuartileLabel2[1] = st.nextToken();
		st = new StringTokenizer(applet.translate("Upper*quartile"), "*");
		kQuartileLabel1[2] = st.nextToken();
		if (st.hasMoreTokens())
			kQuartileLabel2[2] = st.nextToken();
	}
	
	public void setNoOfTiles(int noOfTiles) {
		this.noOfTiles = noOfTiles;
		initialised = false;
		repaint();
	}
	
	protected boolean initialise(Graphics g) {
		if (initialised)
			return false;
		
		tile = new double[noOfTiles-1];
		initialiseTileLabels(noOfTiles);
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		NumValue[] ySorted = yVar.getSortedData();
		int nVals = yVar.noOfValues();
		
		if (tile.length == 3) {		//	quartiles
			if (nVals % 2 == 0)
				tile[1] = 0.5 * (ySorted[nVals / 2].toDouble() + ySorted[nVals / 2 - 1].toDouble());
			else
				tile[1] = ySorted[nVals / 2].toDouble();
			int halfN = nVals / 2;
			if (halfN % 2 == 0) {
				tile[0] = 0.5 * (ySorted[halfN / 2].toDouble() + ySorted[halfN / 2 - 1].toDouble());
				tile[2] = 0.5 * (ySorted[nVals - halfN / 2].toDouble() + ySorted[nVals - halfN / 2 - 1].toDouble());
			}
			else {
				tile[0] = ySorted[halfN / 2].toDouble();
				tile[2] = ySorted[nVals - halfN / 2 - 1].toDouble();
			}
		}
		else										//	assumes deciles
			for (int i=0 ; i<tile.length ; i++) {
				int tenPos = (i+1) * nVals - 5;
				if (tenPos % 10 == 5)
					tile[i] = 0.5 * (ySorted[tenPos / 10].toDouble() + ySorted[tenPos / 10 + 1].toDouble());
				else
					tile[i] = ySorted[(nVals * (i+1) + 5) / 10].toDouble();
			}
		
		nBetweenTiles = new int[noOfTiles];
		int sortedIndex = 0;
		for (int i=0 ; i<noOfTiles-1 ; i++) {
			int count = 0;
			while (sortedIndex < nVals && ySorted[sortedIndex].toDouble() < tile[i]) {
				count ++;
				sortedIndex ++;
			}
			nBetweenTiles[i] = count;
		}
		nBetweenTiles[noOfTiles - 1] = nVals - sortedIndex;
		
		arrowEnd = getSize().height - getMaxJitter() - 10;
		
		FontMetrics fm = g.getFontMetrics();
		labelBase1 = fm.getAscent() + kTopBorder;
		labelBase2 = labelBase1 + fm.getHeight();
		
		countBase1 = (labelBase2 + arrowEnd) / 2;
		countBase2 = countBase1 + fm.getHeight();
		
		initialised = true;
		return true;
	}
	
	private void initialiseTileLabels(int noOfTiles) {
		if (noOfTiles == 4) {
			tileLabel1 = kQuartileLabel1;
			tileLabel2 = kQuartileLabel2;
		}
		else {											//		assumes deciles
			tileLabel1 = new String[noOfTiles-1];
			tileLabel2 = new String[noOfTiles-1];
			for (int i=0 ; i<noOfTiles-1 ; i++)
				tileLabel1[i] = "D" + (i+1);
		}
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		shadeBackground(g);
		super.paintView(g);
		drawTiles(g);
	}
	
	private void shadeBackground(Graphics g) {
		if (highlight < 0)
			return;
		
		g.setColor(Color.yellow);
		
		int lowPos, highPos;
		if (highlight == 0)
			lowPos = 0;
		else {
			int low = axis.numValToRawPosition(tile[highlight - 1]);
			Point p = translateToScreen(low, 0, null);
			lowPos = p.x;															//	assumes horizontal plot
		}
		
		if (highlight == (tile.length))
			highPos = getSize().width;
		else {
			int high = axis.numValToRawPosition(tile[highlight]);
			Point p = translateToScreen(high, 0, null);
			highPos = p.x;															//	assumes horizontal plot
		}
		
		g.fillRect(lowPos, 0, (highPos - lowPos), getSize().height);
		
		g.setColor(kOrange);
		(new NumValue(nBetweenTiles[highlight], 0)).drawCentred(g, (highPos + lowPos) / 2,
																														countBase1);
		kValuesLabel.drawCentred(g, (highPos + lowPos) / 2, countBase2);
		
		g.setColor(getForeground());
	}
	
	private void drawTiles(Graphics g) {
		g.setColor(Color.red);
		FontMetrics fm = g.getFontMetrics();
		
		Point p = null;
		for (int i=0 ; i<tile.length ; i++)
			try {
				int xAxisPos = axis.numValToPosition(tile[i]);
				p = translateToScreen(xAxisPos, 0, p);
				int xPos = p.x;
				int label1Width = fm.stringWidth(tileLabel1[i]);
				int xLabel1Pos = Math.max(0, Math.min(getSize().width - label1Width, xPos - label1Width / 2));
				
				int labelBottom;
				if (tileLabel2[i] == null) {
					labelBottom = (labelBase1 + labelBase2) / 2;
					g.drawString(tileLabel1[i], xLabel1Pos, labelBottom);
				}
				else {
					g.drawString(tileLabel1[i], xLabel1Pos, labelBase1);
					int label2Width = fm.stringWidth(tileLabel2[i]);
					int xLabel2Pos = Math.max(0, Math.min(getSize().width - label2Width, xPos - label2Width / 2));
					g.drawString(tileLabel2[i], xLabel2Pos, labelBase2);
					labelBottom = labelBase2;
				}
				g.drawLine(xPos, labelBottom + 4, xPos, arrowEnd);
				g.drawLine(xPos, arrowEnd, xPos - kArrowSize, arrowEnd - kArrowSize);
				g.drawLine(xPos, arrowEnd, xPos + kArrowSize, arrowEnd - kArrowSize);
			} catch (AxisException e) {
			}
		g.setColor(getForeground());
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		initialise(getGraphics());
		
		Point hitPos = translateFromScreen(x, y, null);
		double hitVal = 0.0;
		try {
			hitVal = axis.positionToNumVal(hitPos.x);
		} catch (AxisException e) {
			if (e.axisProblem == AxisException.TOO_LOW_ERROR)
				hitVal = axis.minOnAxis;
			else if (e.axisProblem == AxisException.TOO_HIGH_ERROR)
				hitVal = axis.maxOnAxis;
		}
		for (int i=0 ; i<tile.length ; i++)
			if (hitVal < tile[i])
				return new IndexPosInfo(i);
		
		return new IndexPosInfo(tile.length);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo == null) {
			highlight = -1;
			repaint();
		}
		else {
			highlight = ((IndexPosInfo)startInfo).itemIndex;
			repaint();
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		startDrag(null);
	}
}