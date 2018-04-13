package exerciseEstim;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import utils.*;
import valueList.*;


public class TProbLookupView extends CoreLookupView {
//	static public final String T_PROB_LOOKUP_VIEW = "tProbLookupView";
	
	static final private int kTopMargin = 2;
	
	static final private NumValue kMaxProb = new NumValue(9.9999, 4);
	
	static final private Color kLowColor = new Color(0xFF6666);
	static final private Color kHighColor = new Color(0x9999FF);
	
	static final private Color kLowValueColor = new Color(0x990000);
	static final private Color kHighValueColor = new Color(0x000099);
	
	private FixedValueView lowProb, highProb;
	
	private double tValue;
	
	public TProbLookupView(DataSet data, XApplet applet, HorizAxis axis, String distnKey) {
		super(data, applet, axis, distnKey);
		
		distnDrawer.setFillColor(kLowColor);
		distnDrawer.setHighlightColor(kHighColor);
		
			theEdit = new XNumberEditPanel("t =", "0.0", 5, applet);
			tValue = 0.0;
			theEdit.setOpaque(false);
		
		add("tValue", theEdit);
		
			lowProb = new FixedValueView(null, kMaxProb, 0.5, applet);
//			lowProb.setValueBackground(Color.yellow);
			lowProb.setFont(applet.getBigBoldFont());
			lowProb.unboxValue();
			lowProb.setForeground(kLowValueColor);
		add("lowProb", lowProb);
		
			highProb = new FixedValueView(null, kMaxProb, 0.5, applet);
//			highProb.setValueBackground(Color.yellow);
			highProb.setFont(applet.getBigBoldFont());
			highProb.unboxValue();
			highProb.setForeground(kHighValueColor);
		add("highProb", highProb);
	}
	
	public void paintView(Graphics g) {
		distnDrawer.paintDistn(g, this, axis, Double.NEGATIVE_INFINITY, tValue);
		
		Rectangle tRect = theEdit.getBounds();
		int boxY = tRect.y + tRect.height / 2;
		int boxX = tRect.x + tRect.width / 2;
		
		g.setColor(getForeground());
		
		try {
			int tPos = axis.numValToPosition(tValue);
			int densityAtT = getSize().height - (int)Math.round(distnDrawer.getHeightAt(tPos, this, axis));
			int distanceFromEnd = Math.min(getSize().width - tPos, tPos);
			int lineY = densityAtT - (densityAtT - boxY) * distanceFromEnd / getSize().width;
																												//		a bit above density
			
			g.drawLine(boxX, boxY, tPos, lineY);
			g.drawLine(tPos, lineY, tPos, getSize().height);
		} catch (AxisException e) {
			int tPos = axis.numValToRawPosition(tValue);
			g.drawLine(boxX, boxY, tPos, getSize().height);
		}
		
		ContinDistnVariable distn = (ContinDistnVariable)getVariable(distnKey);
		double pLess = distn.getCumulativeProb(tValue);
		double lowPCenterX = distn.getQuantile(pLess / 2);
		
		int centerX = axis.numValToRawPosition(lowPCenterX);
		int centerY = getSize().height - (int)Math.round(distnDrawer.getHeightAt(centerX, this, axis)) / 2;
		
		Rectangle lowPRect = lowProb.getBounds();
		g.setColor(kLowValueColor);
		g.drawLine(lowPRect.x + lowPRect.width / 2, lowPRect.y + lowPRect.height / 2,
																										centerX, centerY);
																										
		double highPCenterX = distn.getQuantile((1 + pLess) / 2);
		
		centerX = axis.numValToRawPosition(highPCenterX);
		centerY = getSize().height - (int)Math.round(distnDrawer.getHeightAt(centerX, this, axis)) / 2;
		
		Rectangle highPRect = highProb.getBounds();
		g.setColor(kHighValueColor);
		g.drawLine(highPRect.x + highPRect.width / 2, highPRect.y + highPRect.height / 2,
																										centerX, centerY);
	}

//-----------------------------------------------------------------------------------
	
	protected void setPendingEdit() {
		lowProb.setValue(Double.NaN);
		highProb.setValue(Double.NaN);
		
		repaint();
	}
	
	protected void setFixedEdit() {
		ContinDistnVariable distn = (ContinDistnVariable)getVariable(distnKey);
		
		tValue = theEdit.getDoubleValue();
		
		double pLess = distn.getCumulativeProb(tValue);
		lowProb.setValue(pLess);
		highProb.setValue(1.0 - pLess);
		
//		repaint();
	}

//-----------------------------------------------------------------------------------
	
	public void layoutContainer(Container parent) {
		int height = parent.getSize().height;
		int width = parent.getSize().width;
		
		Dimension d = theEdit.getPreferredSize();
		theEdit.setBounds((width - d.width) / 2, kTopMargin, d.width, d.height);
		
		int probTop = (height - d.height) / 2;
		d = lowProb.getPreferredSize();
		lowProb.setBounds(5, probTop, d.width, d.height);
		
		d = highProb.getPreferredSize();
		highProb.setBounds(width - d.width - 5, probTop, d.width, d.height);
	}
}