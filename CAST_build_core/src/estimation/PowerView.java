package estimation;

import java.awt.*;

import dataView.*;
import axis.*;
import formula.*;


public class PowerView extends DataView {
	static final private int kParamSteps = 30;
	
	static final private int kArrowSize = 4;
	static final private int kPowerDecimals = 3;
	static final private int kLeftPowerMargin = 10;
	static final private int kBottomPowerMargin = 5;
	
	static final private Color kNullHypothColor = new Color(0x009900);
	
	private CorePowerFinder powerFinder;
	
	private HorizAxis paramAxis;
	private VertAxis powerAxis;
	
	private NumValue arrowParam = null;
	private NumValue nullProb = null;
	
	public PowerView(DataSet theData, XApplet applet, CorePowerFinder powerFinder,
																								HorizAxis paramAxis, VertAxis powerAxis) {
		super(theData, applet, new Insets(0, 5, 0, 5));
		this.powerFinder = powerFinder;
		this.paramAxis = paramAxis;
		this.powerAxis = powerAxis;
	}
	
	public void setArrowValue(NumValue arrowParam) {
		this.arrowParam = arrowParam;
		repaint();
	}
	
	public void setNullProb(NumValue nullProb) {
		this.nullProb = nullProb;
	}
	
	public void paintView(Graphics g) {
		g.setFont(getApplet().getBigBoldFont());
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int lineHeight = ascent + fm.getDescent();
		if (nullProb != null) {
			g.setColor(kNullHypothColor);
			
			String sigLabel = getApplet().translate("Significance level");
			int labelWidth = fm.stringWidth(sigLabel);
			double sigLevel = powerFinder.getPower(nullProb.toDouble());
			String sigLevelString = new NumValue(sigLevel, kPowerDecimals).toString();
			int sigLevelWidth = fm.stringWidth(sigLevelString);
			int sigWidth = Math.max(labelWidth, sigLevelWidth);
			
			int baseline = kBottomPowerMargin + ascent;
			g.drawString(sigLabel, getSize().width - kLeftPowerMargin - (sigWidth + labelWidth) / 2, baseline);
			
			baseline += lineHeight + 5;
			g.drawString(sigLevelString, getSize().width - kLeftPowerMargin - (sigWidth + sigLevelWidth) / 2, baseline);
			
			g.setColor(Color.black);
		}
		
		double paramVal[] = new double[kParamSteps + 1];
		double yVal[] = new double[kParamSteps + 1];
		double minX = paramAxis.minOnAxis;
		double maxX = paramAxis.maxOnAxis;
		
		for (int i=0 ; i<=kParamSteps ; i++) {
			paramVal[i] = minX + i * (maxX - minX) / kParamSteps;
			yVal[i] = powerFinder.getPower(paramVal[i]);
		}
		
		drawCurve(g, paramVal, yVal, paramAxis, powerAxis);
		
		if (arrowParam != null) {
			g.setColor(Color.red);
			double paramValue = arrowParam.toDouble();
			double power = powerFinder.getPower(paramValue);
			
			try {
				int paramPos = paramAxis.numValToPosition(paramValue);
				int powerPos = powerAxis.numValToPosition(power);
				Point pOnCurve = translateToScreen(paramPos, powerPos, null);
				
				g.drawLine(pOnCurve.x, 0, pOnCurve.x, getSize().height);
				g.drawLine(pOnCurve.x, pOnCurve.y, 0, pOnCurve.y);
				g.drawLine(0, pOnCurve.y, kArrowSize, pOnCurve.y + kArrowSize);
				g.drawLine(0, pOnCurve.y, kArrowSize, pOnCurve.y - kArrowSize);
				
				NumValue powerValue = new NumValue(power, kPowerDecimals);
				String powerString = "P(" + getApplet().translate("reject") + MText.expandText("H#sub0#) = ") + powerValue.toString();
				
				int baseline = getSize().height - kBottomPowerMargin;
				if (pOnCurve.y > baseline - lineHeight)
					baseline -= lineHeight * 3 / 2;
				g.drawString(powerString, kLeftPowerMargin, baseline);
			} catch (AxisException e) {
			}
		}
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}