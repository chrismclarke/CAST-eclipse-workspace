package regn;

import java.awt.*;

import dataView.*;
import models.*;


public class QuadraticEquationView extends LinearEquationView {
//	static public final String QUADRATIC_MODEL = "quadraticEquation";
	private NumValue minCurvature, maxCurvature;
	
	private int maxCurvatureWidth;
	
	public QuadraticEquationView(DataSet theData, XApplet applet, String modelKey, String yName, String xName,
													NumValue minIntercept, NumValue maxIntercept, NumValue minSlope,
													NumValue maxSlope, NumValue minCurvature, NumValue maxCurvature) {
		super(theData, applet, modelKey, yName, xName, minIntercept, maxIntercept, minSlope, maxSlope, null, null);
		this.minCurvature = minCurvature;
		this.maxCurvature = maxCurvature;
	}

//--------------------------------------------------------------------------------
	
	protected boolean initialise(Graphics g) {
		if (super.initialise(g)) {
			int curvatureDecimals = Math.max(minCurvature.decimals, maxCurvature.decimals);
			maxCurvatureWidth = Math.max(minCurvature.stringWidth(g, curvatureDecimals),
															maxCurvature.stringWidth(g, curvatureDecimals));
			
			FontMetrics fm = g.getFontMetrics();
			int charsWidth = fm.stringWidth(" +  " + xName + "2");
			modelWidth += charsWidth + getValueSize(fm, maxCurvatureWidth).width;
			return true;
		}
		else
			return false;
	}
	
	public int paintModel(Graphics g) {
		int horizPos = super.paintModel(g);
		
		FontMetrics fm = g.getFontMetrics();
		g.drawString(" + ", horizPos, baseline);
		horizPos += fm.stringWidth(" + ");
		
		QuadraticModel qm = (QuadraticModel)getVariable(modelKey);
		NumValue curvature = qm.getCurvature();
		drawParameter(g, curvature.toString(), maxCurvatureWidth, horizPos, baseline);
		horizPos += getValueSize(fm, maxCurvatureWidth).width;
		
		g.drawString(" " + xName, horizPos, baseline);
		horizPos += fm.stringWidth(" " + xName);
		
		Font oldFont = g.getFont();
		g.setFont(new Font(oldFont.getName(), oldFont.getStyle(), oldFont.getSize() - 2));
		FontMetrics fms = g.getFontMetrics();
		g.drawString("2", horizPos, baseline - 3);
		horizPos += fms.stringWidth("2");
		g.setFont(oldFont);
		
		return horizPos;
	}
}
