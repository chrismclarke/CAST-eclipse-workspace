package exerciseEstim;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import utils.*;
import valueList.*;


public class TQuantileLookupView extends CoreLookupView {
//	static public final String T_QUANTILE_LOOKUP_VIEW = "tQuantileLookupView";
	
	static final private int kTopMargin = 5;
	static final private int kBottomMargin = 5;
	
	static final private NumValue kMaxQuantile = new NumValue(-99.999, 3);
	
	static final private Color kMiddleColor = new Color(0xFF6666);
	static final private Color kTailColor = new Color(0x9999FF);
//	static final private Color kTailColor = new Color(0xCCCCCC);
	
	private FixedValueView lowLimit, highLimit;
	
	private double minSelection, maxSelection;
	
	public TQuantileLookupView(DataSet data, XApplet applet, HorizAxis axis, String distnKey) {
		super(data, applet, axis, distnKey);
		
		distnDrawer.setFillColor(kMiddleColor);
		distnDrawer.setHighlightColor(kTailColor);
		
			theEdit = new XNumberEditPanel(null, "0.5", 4, applet);
			theEdit.setDoubleType(0.0, 1.0);
			theEdit.setOpaque(false);
		
		add("coverage", theEdit);
		
			lowLimit = new FixedValueView(null, kMaxQuantile, Double.NaN, applet);
			lowLimit.setCenterValue(true);
			lowLimit.unboxValue();
//			lowLimit.setValueBackground(Color.yellow);
		add("lowLimit", lowLimit);
		
			highLimit = new FixedValueView(null, kMaxQuantile, Double.NaN, applet);
			highLimit.setCenterValue(true);
			highLimit.unboxValue();
//			highLimit.setValueBackground(Color.yellow);
		add("highLimit", highLimit);
	}
	
	public void paintView(Graphics g) {
		if (minSelection == maxSelection)
			distnDrawer.paintDistn(g, this, axis, minSelection, maxSelection - 1);
													//	prevents 1-pixel line from being drawn
		else
			distnDrawer.paintDistn(g, this, axis, minSelection, maxSelection);
		
		g.setColor(getForeground());
		Rectangle lowQuantileRect = lowLimit.getBounds();
		int lowX = lowQuantileRect.x + lowQuantileRect.width / 2;
		int lowY = lowQuantileRect.y + lowQuantileRect.height / 2;
		
		int densityX = axis.numValToRawPosition(minSelection);
		int densityY = getSize().height - (int)Math.round(distnDrawer.getHeightAt(densityX, this, axis));
		
		g.drawLine(lowX, lowY, densityX, densityY);
		
		Rectangle highQuantileRect = highLimit.getBounds();
		int highX = highQuantileRect.x + highQuantileRect.width / 2;
		int highY = highQuantileRect.y + highQuantileRect.height / 2;
		
		densityX = axis.numValToRawPosition(maxSelection);
		densityY = getSize().height - (int)Math.round(distnDrawer.getHeightAt(densityX, this, axis));
		
		g.drawLine(highX, highY, densityX, densityY);
	}

//-----------------------------------------------------------------------------------
	
	protected void setPendingEdit() {
		minSelection = maxSelection = Double.NaN;
		
		lowLimit.setValue(minSelection);
		highLimit.setValue(maxSelection);
		
		repaint();
	}
	
	protected void setFixedEdit() {
		ContinDistnVariable distn = (ContinDistnVariable)getVariable(distnKey);
		
		double ciLevel = theEdit.getDoubleValue();
		
		minSelection = distn.getQuantile((1 - ciLevel) / 2);
		maxSelection = distn.getQuantile((1 + ciLevel) / 2);
		
		int lowWidth = lowLimit.getPreferredSize().width;
		int lowPos;
		try {
			lowPos = axis.numValToPosition(minSelection);
		} catch (AxisException e) {
			lowPos = 0;
		}
		int left = Math.max(3, Math.min(getSize().width / 2 - lowWidth - 3, lowPos - lowWidth / 2));
		lowLimit.setLocation(left, kTopMargin);
		lowLimit.setValue(minSelection);
		
		int highPos;
		int highWidth = highLimit.getPreferredSize().width;
		try {
			highPos = axis.numValToPosition(maxSelection);
		} catch (AxisException e) {
			highPos = getSize().width;
		}
		left = Math.max(getSize().width / 2 + 3, Math.min(getSize().width - highWidth - 3, highPos - highWidth / 2));
		highLimit.setLocation(left, kTopMargin);
		highLimit.setValue(maxSelection);
		
//		repaint();
	}

//-----------------------------------------------------------------------------------
	
	public void layoutContainer(Container parent) {
		int height = parent.getSize().height;
		int width = parent.getSize().width;
		
		Dimension d = theEdit.getPreferredSize();
		theEdit.setBounds((width - d.width) / 2, height - d.height - kBottomMargin, d.width, d.height);
		
		d = lowLimit.getPreferredSize();
		lowLimit.setBounds((width - 2 * d.width) / 4, kTopMargin, d.width, d.height);
		
		d = highLimit.getPreferredSize();
		highLimit.setBounds((3 * width - 2 * d.width) / 4, kTopMargin, d.width, d.height);
	}
}