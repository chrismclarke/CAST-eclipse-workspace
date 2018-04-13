package glmAnova;

import java.awt.*;

import dataView.*;
import models.*;


public class SsqChartView extends DataView implements SetLastExplanInterface {
//	static final public String SSQ_CHART = "ssqChart";
	
	static final protected int kBarWidth = 20;
	static final protected int kMinBarHeight = 100;
	static final protected int kArrowWidth = 14;
	static final protected int kArrowHead = 4;
	
	private String componentKey[];		//	first is total, last is residual
	private LabelValue explainedLabel, unexplainedLabel, totalLabel;
	private Color explainedColor, unexplainedColor, totalColor;
	
	private int lastSeparateX;
	
	private boolean initialised = false;
	private int ascent, descent, leftLabelWidth, rightLabelWidth;
	
	public SsqChartView(DataSet theData, XApplet applet, String[] componentKey, LabelValue explainedLabel,
									LabelValue unexplainedLabel, LabelValue totalLabel, Color explainedColor,
									Color unexplainedColor, Color totalColor) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.componentKey = componentKey;
		this.explainedLabel = explainedLabel;
		this.unexplainedLabel = unexplainedLabel;
		this.totalLabel = totalLabel;
		this.explainedColor = explainedColor;
		this.unexplainedColor = unexplainedColor;
		this.totalColor = totalColor;
		
		lastSeparateX = componentKey.length - 3;		//	-1 for no explan
	}
	
	public void setLastExplanatory(int lastSeparateX) {
		this.lastSeparateX = lastSeparateX;
		repaint();
	}

//--------------------------------------------------------------------
	
	protected void doInitialisation(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		descent = fm.getDescent();
		
		leftLabelWidth = Math.max(explainedLabel.stringWidth(g), unexplainedLabel.stringWidth(g));
		rightLabelWidth = totalLabel.stringWidth(g);
	}
	
	final protected boolean initialise(Graphics g) {
		if (initialised)
			return false;
		else {
			doInitialisation(g);
			initialised = true;
			return true;
		}
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		CoreComponentVariable totalComp = (CoreComponentVariable)getVariable(componentKey[0]);
		double totalSsq = totalComp.getSsq();
		
		double explainedSsq = 0.0;
		for (int i=1 ; i<=lastSeparateX+1 ; i++) {
			CoreComponentVariable explainedComp = (CoreComponentVariable)getVariable(componentKey[i]);
			explainedSsq += explainedComp.getSsq();
		}
		
		g.setColor(explainedColor);
		int explainedHt = (int)Math.round(getSize().height * explainedSsq / totalSsq);
		int arrowHoriz = leftLabelWidth + kArrowWidth / 2;
		if (explainedHt > 0) {
			g.fillRect(leftLabelWidth + kArrowWidth, 0, kBarWidth, explainedHt);
			int explainedBaseline = Math.max(ascent, explainedHt / 2 + (ascent - descent) / 2);
			explainedLabel.drawLeft(g, leftLabelWidth, explainedBaseline);
			drawArrow(arrowHoriz, 0, explainedHt, g);
		}
		
		g.setColor(unexplainedColor);
		g.fillRect(leftLabelWidth + kArrowWidth, explainedHt, kBarWidth, getSize().height - explainedHt);
		int unexplainedBaseline = Math.min(getSize().height - descent, (explainedHt + getSize().height) / 2 + (ascent - descent) / 2);
		unexplainedLabel.drawLeft(g, leftLabelWidth, unexplainedBaseline);
		drawArrow(arrowHoriz, explainedHt, getSize().height, g);
		
		g.setColor(totalColor);
		arrowHoriz = leftLabelWidth + kArrowWidth + kBarWidth + kArrowWidth / 2;
		int totalBaseline = getSize().height / 2 + (ascent - descent) / 2;
		totalLabel.drawRight(g, leftLabelWidth + 2 * kArrowWidth + kBarWidth, totalBaseline);
		drawArrow(arrowHoriz, 0, getSize().height, g);
		
		g.setColor(getForeground());
		double cumSsq = 0.0;
		for (int i=1 ; i<componentKey.length-1 ; i++) {
			CoreComponentVariable explainedComp = (CoreComponentVariable)getVariable(componentKey[i]);
			cumSsq += explainedComp.getSsq();
			int cumHt = (int)Math.round(getSize().height * cumSsq / totalSsq);
			g.drawLine(leftLabelWidth + kArrowWidth, cumHt, leftLabelWidth + kArrowWidth + kBarWidth - 1, cumHt);
		}
	}
	
	private void drawArrow(int arrowHoriz, int top, int bottom, Graphics g) {
		g.drawLine(arrowHoriz, top + 1, arrowHoriz, bottom - 2);
		g.drawLine(arrowHoriz + 1, top + 1, arrowHoriz + 1, bottom - 2);
		
		g.drawLine(arrowHoriz + 1, top + 1, arrowHoriz + 1 + kArrowHead, top + 1 + kArrowHead);
		g.drawLine(arrowHoriz, top + 1, arrowHoriz - kArrowHead, top + 1 + kArrowHead);
		g.drawLine(arrowHoriz + 1, bottom - 2, arrowHoriz + 1 + kArrowHead, bottom - 2 - kArrowHead);
		g.drawLine(arrowHoriz, bottom - 2, arrowHoriz - kArrowHead, bottom - 2 - kArrowHead);
	}

//-----------------------------------------------------------------------------------
		
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		return new Dimension(leftLabelWidth + rightLabelWidth + 2 * kArrowWidth + kBarWidth,
																																				kMinBarHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}