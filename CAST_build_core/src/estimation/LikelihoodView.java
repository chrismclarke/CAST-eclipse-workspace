package estimation;

import java.awt.*;

import dataView.*;
import axis.*;


public class LikelihoodView extends DataView {
	
	static final private int kParamSteps = 50;
	
	private String distnKey;
	
	protected CoreLikelihoodFinder likelihoodFinder;
	protected HorizAxis paramAxis;
	protected VertAxis likelihoodAxis;
	
	private boolean drawSlope = false;
	private boolean drawQuadratic = false;

	
	public LikelihoodView(DataSet theData, XApplet applet, String distnKey,
																								CoreLikelihoodFinder likelihoodFinder,
																								HorizAxis paramAxis, VertAxis likelihoodAxis) {
		super(theData, applet, new Insets(0, 5, 0, 5));
		this.distnKey = distnKey;
		this.likelihoodFinder = likelihoodFinder;
		this.paramAxis = paramAxis;
		this.likelihoodAxis = likelihoodAxis;
	}
	
	public void setDrawSlope(boolean drawSlope) {
		this.drawSlope = drawSlope;
	}
	
	public void setDrawQuadratic(boolean drawQuadratic) {
		this.drawQuadratic = drawQuadratic;
	}
	
	protected void drawExtras(Graphics g, double minX, double maxX) {
		g.setColor(Color.red);
		double selectedParam = getParam();
		double likelihood = getLikelihood(selectedParam);		//	or log-likelihood for sub-class
		
		try {
			int paramPos = paramAxis.numValToPosition(selectedParam);
			int likelihoodPos = likelihoodAxis.numValToPosition(likelihood);
			Point pOnCurve = translateToScreen(paramPos, likelihoodPos, null);
			
			g.drawLine(pOnCurve.x, 0, pOnCurve.x, getSize().height);
			g.drawLine(pOnCurve.x, pOnCurve.y, 0, pOnCurve.y);
			
			if (drawSlope) {
				g.setColor(Color.green);
				
				minX -= (maxX - minX) * 0.05;					//	to make sure line reaches both edges of screen
				maxX += (maxX - minX) * 0.05;
				
				double slope = getDerivative(selectedParam);
//				double minY = likelihoodAxis.minOnAxis;
//				double maxY = likelihoodAxis.maxOnAxis;
				
				double yAtMinX = likelihood - slope * (selectedParam - minX);
				
				int leftXPos = paramAxis.numValToRawPosition(minX);
				int leftYPos = likelihoodAxis.numValToRawPosition(yAtMinX);
				Point leftPt = translateToScreen(leftXPos, leftYPos, null);
				
				double yAtMaxX = likelihood + slope * (maxX - selectedParam);
				int rightXPos = paramAxis.numValToRawPosition(maxX);
				int rightYPos = likelihoodAxis.numValToRawPosition(yAtMaxX);
				Point rightPt = translateToScreen(rightXPos, rightYPos, null);
				
				g.drawLine(leftPt.x, leftPt.y, rightPt.x, rightPt.y);
			}
			else if (drawQuadratic) {
				g.setColor(Color.green);
				
				double paramVal[] = new double[kParamSteps + 1];
				double yVal[] = new double[kParamSteps + 1];
				minX = paramAxis.minOnAxis;
				maxX = paramAxis.maxOnAxis;
				
				double x = selectedParam;
				double y = likelihood;
				double y1 = getDerivative(selectedParam);
				double y2 = get2ndDerivative(selectedParam);
				
				double b2 = y2 / 2;
				double b1 = y1 - 2 * b2 * x;
				double b0 = y - b1 * x - b2 * Math.pow(x, 2);
				
				for (int i=0 ; i<=kParamSteps ; i++) {
					paramVal[i] = minX + i * (maxX - minX) / kParamSteps;
					yVal[i] = b0 + (b1 + b2 * paramVal[i]) * paramVal[i];
				}
				
				drawCurve(g, paramVal, yVal, paramAxis, likelihoodAxis);
			}
		} catch (AxisException e) {
		}
	}
	
	protected void drawBackground(Graphics g) {
	}
	
	public void paintView(Graphics g) {
//		DiscreteDistnVariable y = (DiscreteDistnVariable)getVariable(distnKey);
		
		double paramVal[] = new double[kParamSteps + 1];
		double yVal[] = new double[kParamSteps + 1];
		double minX = paramAxis.minOnAxis;
		double maxX = paramAxis.maxOnAxis;
		
		for (int i=0 ; i<=kParamSteps ; i++) {
			paramVal[i] = minX + i * (maxX - minX) / kParamSteps;
			yVal[i] = getLikelihood(paramVal[i]);
		}
		
		drawBackground(g);
		g.setColor(getForeground());
		
		drawCurve(g, paramVal, yVal, paramAxis, likelihoodAxis);
		
		drawExtras(g, minX, maxX);
	}
	
	protected double getLikelihood(double param) {
		return likelihoodFinder.getLikelihood(param);
	}
	
	protected double getDerivative(double param) {
		return likelihoodFinder.getDerivative(param);
	}
	
	protected double get2ndDerivative(double param) {
		return likelihoodFinder.get2ndDerivative(param);
	}
	
	protected double getParam() {
		return likelihoodFinder.getParam();
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