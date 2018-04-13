package sampling;

import java.awt.*;
import java.util.*;

import dataView.*;
import valueList.*;


public class PlusMinusProportionView extends ProportionView {
//	static public final String PLUS_MINUS_PROPORTION_VIEW = "plusMinusProportion";
	
	private String kStartString, kEndString;
	
	private int startWidth, zWidth, endWidth;
	
	public PlusMinusProportionView(DataSet theData, String variableKey, XApplet applet) {
		super(theData, variableKey, applet);
		StringTokenizer st = new StringTokenizer(applet.translate("P( X is within * st devns of its mean )"), "*");
		kStartString = st.nextToken();
		kEndString = st.nextToken();
	}
	
	protected int getLabelWidth(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		startWidth = fm.stringWidth(kStartString);
		zWidth = fm.stringWidth("0.00");
		endWidth = fm.stringWidth(kEndString);
		
		return startWidth + zWidth + endWidth;
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		g.drawString(kStartString, startHoriz, baseLine);
		drawZValue(g, baseLine);
		g.setColor(getForeground());
		g.drawString(kEndString, startHoriz + startWidth + zWidth, baseLine);
	}
	
	protected void drawZValue(Graphics g, int baseLine) {
		DistnVariable v = (DistnVariable)getVariable(variableKey);
		double z = v.getMaxSelection();
		NumValue zValue = new NumValue(z, 2);
		
		g.setColor(getBackground());
		g.fillRect(kLabelLeftBorder + startWidth, 0, zWidth, getSize().height);
		
		g.setColor(Color.blue);
		zValue.drawRight(g, kLabelLeftBorder + startWidth, baseLine);
	}
	
	protected boolean highlightValue() {
		return true;
	}

//--------------------------------------------------------------------------------
	
	public void redrawValue() {
		redrawAll();								//	to ensure that label gets redrawn when selection changes
	}
}
