package graphics;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;


public class VertBarView extends DataView {
//	static final public String VERT_BAR_VIEW = "vertBarView";
	
	static final private Color kGridColor = new Color(0xDDDDDD);
	static final private Color kBarColor = new Color(0x000099);
	static final private Color kDimBarColor = new Color(0x9999FF);
	
	static final private int kBarValueGap = 5;
	
	private String yKey;
	private HorizAxis catAxis;
	private VertAxis yAxis;
	
	private boolean showValues = false;
	private boolean valuesInBars = false;
	
	public VertBarView(DataSet theData, XApplet applet, String yKey, HorizAxis catAxis, VertAxis yAxis) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.yKey = yKey;
		this.catAxis = catAxis;
		this.yAxis = yAxis;
	}
	
	public void setShowValues(boolean showValues) {
		this.showValues = showValues;
	}
	
	public void setValuesInBars(boolean valuesInBars) {
		this.valuesInBars = valuesInBars;
	}
	
	private void drawGrid(Graphics g) {
		g.setColor(kGridColor);
		Point p = null;
		Enumeration e = yAxis.getLabels().elements();
		while (e.hasMoreElements()) {
			AxisLabel nextLabel = (AxisLabel)e.nextElement();
			double labelValue = ((NumValue)nextLabel.label).toDouble();
			int y = yAxis.numValToRawPosition(labelValue);
			p = translateToScreen(0, y, p);
			g.drawLine(0, p.y, getSize().width, p.y);
		}
	}
	
	public void paintView(Graphics g) {
		drawGrid(g);
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		int nVals = yVar.noOfValues();
		
		Flags selection = getSelection();
		int selectedIndex = selection.findSingleSetFlag();
		
		int halfBarWidth = getSize().width / (nVals * 6);
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		
		Point topCenter = null;
		for (int i=0 ; i<nVals ; i++) {
			boolean drawDim = selectedIndex >= 0 && !selection.valueAt(i);
			Color barColor = drawDim ? kDimBarColor : kBarColor;
			g.setColor(barColor);
			
			int x = catAxis.catValToPosition(i);
			int y = yAxis.numValToRawPosition(yVar.doubleValueAt(i));
			topCenter = translateToScreen(x, y, topCenter);
			g.fillRect(topCenter.x - halfBarWidth, topCenter.y, 2 * halfBarWidth, getSize().height - topCenter.y);
			
			if (showValues && !drawDim) {
				Value yVal = yVar.valueAt(i);
				boolean canSuprimpose = valuesInBars && (yVal.stringWidth(g) < 2 * halfBarWidth - 2);
				int baseline = topCenter.y - kBarValueGap;
				if (canSuprimpose) {
					g.setColor(Color.white);
					baseline += 2 * kBarValueGap + ascent;
				}
				yVar.valueAt(i).drawCentred(g, topCenter.x, baseline);
			}
		}
		
		if (selectedIndex >= 0) {
			int x = catAxis.catValToPosition(selectedIndex);
			int y = yAxis.numValToRawPosition(yVar.doubleValueAt(selectedIndex));
			topCenter = translateToScreen(x, y, topCenter);
			g.setColor(Color.red);
			g.drawLine(1, topCenter.y - 1, getSize().width - 2, topCenter.y - 1);
			
			g.drawLine(0, topCenter.y, topCenter.x - halfBarWidth - 1, topCenter.y);
			g.drawLine(1, topCenter.y + 1, topCenter.x - halfBarWidth - 1, topCenter.y + 1);
			g.drawLine(0, topCenter.y, 4, topCenter.y - 4);
			g.drawLine(0, topCenter.y, 4, topCenter.y + 4);
			
			
			g.drawLine(topCenter.x + halfBarWidth, topCenter.y, getSize().width - 1, topCenter.y);
			g.drawLine(topCenter.x + halfBarWidth, topCenter.y + 1, getSize().width - 2, topCenter.y + 1);
			g.drawLine(getSize().width - 5, topCenter.y - 4, getSize().width - 1, topCenter.y);
			g.drawLine(getSize().width - 5, topCenter.y + 4, getSize().width - 1, topCenter.y);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		int nearestCat = catAxis.positionToCatVal(hitPos.x);
		
		return new IndexPosInfo(nearestCat);
	}
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
}