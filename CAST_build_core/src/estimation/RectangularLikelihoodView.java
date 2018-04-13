package estimation;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;


public class RectangularLikelihoodView extends DataView {
	
	static final private int kParamSteps = 50;
	
	private String distnKey;
	private double dataMax;
	private int n;
	
	private HorizAxis paramAxis;
	private VertAxis likelihoodAxis;

	
	public RectangularLikelihoodView(DataSet theData, XApplet applet, String distnKey, int n,
															double dataMax,	HorizAxis paramAxis, VertAxis likelihoodAxis) {
		super(theData, applet, new Insets(0, 5, 0, 5));
		this.distnKey = distnKey;
		this.n = n;
		this.dataMax = dataMax;
		this.paramAxis = paramAxis;
		this.likelihoodAxis = likelihoodAxis;
	}
	
	public void paintView(Graphics g) {
		try {
			int paramPos = paramAxis.numValToPosition(dataMax);
			double maxL = Math.pow(dataMax, -n);
			int likelihoodPos = likelihoodAxis.numValToPosition(maxL);
			Point pOnAxis = translateToScreen(paramPos, 0, null);
			Point pOnCurve = translateToScreen(paramPos, likelihoodPos, null);
			
			g.drawLine(0, pOnAxis.y, pOnAxis.x, pOnAxis.y);
			g.drawLine(pOnAxis.x, pOnAxis.y, pOnAxis.x, pOnCurve.y);
			
		} catch (AxisException e) {
		}
		
		double paramVal[] = new double[kParamSteps + 1];
		double yVal[] = new double[kParamSteps + 1];
		double minX = dataMax;
		double maxX = paramAxis.maxOnAxis;
		
		for (int i=0 ; i<=kParamSteps ; i++) {
			paramVal[i] = minX + i * (maxX - minX) / kParamSteps;
			yVal[i] = Math.pow(paramVal[i], -n);
		}
		
		drawCurve(g, paramVal, yVal, paramAxis, likelihoodAxis);
		
		g.setColor(Color.red);
		RectangularDistnVariable yVar = (RectangularDistnVariable)getVariable(distnKey);
		double selectedParam = yVar.getMax().toDouble();
		double likelihood = (selectedParam < dataMax) ? 0.0 : Math.pow(selectedParam, -n);
		
		try {
			int paramPos = paramAxis.numValToPosition(selectedParam);
			int likelihoodPos = likelihoodAxis.numValToPosition(likelihood);
			Point pOnCurve = translateToScreen(paramPos, likelihoodPos, null);
			
			g.drawLine(pOnCurve.x, 0, pOnCurve.x, getSize().height);
			g.drawLine(pOnCurve.x, pOnCurve.y, 0, pOnCurve.y);
			
		} catch (AxisException e) {
		}
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (distnKey.equals(key))
			repaint();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}