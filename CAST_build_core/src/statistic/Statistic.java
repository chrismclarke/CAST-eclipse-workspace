package statistic;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.Sigma;


class EquationInfo {
	EquationInfo(int width, int above, int below) {
		this.width = width;
		this.above = above;
		this.below = below;
	}
	int width, above, below;
}


class Extremes {
	Extremes(double lowExtreme, double highExtreme) {
		this.lowExtreme = lowExtreme;
		this.highExtreme = highExtreme;
	}
	double lowExtreme, highExtreme;
}


public abstract class Statistic {
	static private final String equalsString = " = ";
	
	static public final int ALL = 0;
	static public final int LOW = 1;
	static public final int HIGH = 2;
	
	static public final boolean NO_SHOW_GRAPH = false;
	static public final boolean SHOW_GRAPH = true;
	
	static public final boolean NO_SHOW_EQN = false;
	static public final boolean SHOW_EQN = true;

	Font textFont;
	Font superscriptFont;
	Font bracketFont;
	
	static final int kSuperscript = 6;
	
	protected int lowOrHigh;
	private boolean showNotHideGraph;
	private boolean showNotHideEqn;
	private NumValue theVal;
	private EquationInfo equationSize;
	private int equalsStringWidth, valueStringWidth, valAscent;
	
	private Extremes ext = null;
	protected int gx[], gy[];				//	coordinates for points on graph of statistic
	
	public Statistic(int lowOrHigh, boolean showOnGraph, boolean showEqn, XApplet applet) {
		this.lowOrHigh = lowOrHigh;
		this.showNotHideGraph = showOnGraph;
		this.showNotHideEqn = showEqn;
		textFont = applet.getBigFont();
		superscriptFont = applet.getStandardFont();
		bracketFont = new Font(XApplet.FONT, Font.PLAIN, 18);
	}
	
	public void setupValue(NumVariable variable, NumValue theConst) {
		theVal = evaluate(variable, theConst);
	}
	
	public EquationInfo getSize(Graphics g) {
		if (equationSize == null)
			equationSize = equationDimensions(g);
		g.setFont(textFont);
		FontMetrics fm = g.getFontMetrics();
		equalsStringWidth = fm.stringWidth(equalsString);
		valueStringWidth = theVal.stringWidth(g);
		valAscent = fm.getAscent();
		int width = equationSize.width + equalsStringWidth + valueStringWidth + 4;
		return new EquationInfo(width, Math.max(equationSize.above, valAscent + 2),
																			Math.max(equationSize.below, 2));
	}
	
	public void draw(Graphics g, int left, int baseLine) {
									//	assumes getSize() has just been called to initialise
									//	equalsStringWidth and valueStringWidth
		int leftPos = drawEquation(g, left, baseLine);
		
		g.setFont(textFont);
		g.drawString(equalsString, leftPos, baseLine);
		leftPos += equalsStringWidth;
		Color oldColor = g.getColor();
		g.setColor(Color.white);
		g.fillRect(leftPos + 1, baseLine - valAscent - 1, valueStringWidth + 2, valAscent + 4);
		g.setColor(oldColor);
		g.drawRect(leftPos, baseLine - valAscent - 2, valueStringWidth + 3, valAscent + 5
		);
		
		theVal.drawRight(g, leftPos + 2, baseLine);
	}
	
	public Extremes getExtremes(double axisMin, double axisMax, NumVariable variable) {
		if (!showNotHideGraph)
			return null;
			
		if (ext == null)
			ext = coreGetExtremes(axisMin, axisMax, variable);
		return ext;
	}

	protected EquationInfo sigmaSize(Graphics g) {
		return new EquationInfo(Sigma.getWidth(g), Sigma.getAbove(g), Sigma.getBelow(g));
	}

	protected int drawSigma(Graphics g, int left, int baseline) {
		return Sigma.drawSigma(g, left, baseline);
	}
	
//----------------------------------------------------------------------------------
	
	public boolean drawZeroOnGraph() {			//		only called for lowOrHigh = ALL
		return false;
	}
	
	public boolean getGraphVisibility() {
		return showNotHideGraph;
	}
	
	public boolean getEqnVisibility() {
		return showNotHideEqn;
	}
	
	public void setGraphVisibility(boolean showNotHide) {
		this.showNotHideGraph = showNotHide;
	}
	
	public void setEqnVisibility(boolean showNotHide) {
		this.showNotHideEqn = showNotHide;
	}
	
//----------------------------------------------------------------------------------
	
	public void drawGraph(Graphics g, Extremes ext, int graphTop, int graphBottom,
													int graphLeft, NumCatAxis axis, NumVariable variable) {
		if (!showNotHideGraph)
			return;
		
		if (gx == null || gy == null)
			setupGraph(ext, graphTop, graphBottom, graphLeft, axis, variable);
		for (int i=1 ; i<gx.length ; i++)
			g.drawLine(gx[i-1], gy[i-1], gx[i], gy[i]);
	}
	
	protected int getHeight(double value, Extremes ext, int graphTop, int graphBottom) {
		return graphBottom - (int)((value - ext.lowExtreme) / (ext.highExtreme - ext.lowExtreme) * (graphBottom - graphTop));
	}
	
	protected abstract NumValue evaluate(NumVariable variable, NumValue theConst);
	
	protected abstract EquationInfo equationDimensions(Graphics g);
	
	protected abstract int drawEquation(Graphics g, int left, int baseline);
	
	protected abstract Extremes coreGetExtremes(double axisMin, double axisMax,
																								NumVariable variable);
	
	protected abstract void setupGraph(Extremes ext, int graphTop, int graphBottom,
													int graphLeft, NumCatAxis axis, NumVariable variable);
}