package regn;

import java.awt.*;

import dataView.*;
import models.*;

import linMod.*;


public class LinearEquationView extends CoreLinearEquationView {
	
	protected String modelKey;
	protected String xName;
	protected NumValue minIntercept, maxIntercept, minSlope, maxSlope;
	
	private Color interceptColor, slopeColor;
	
	private int selectedParamIndex = -1;
	
	public LinearEquationView(DataSet theData, XApplet applet, String modelKey, String yName, String xName,
								NumValue minIntercept, NumValue maxIntercept, NumValue minSlope,
								NumValue maxSlope, Color interceptColor, Color slopeColor) {
		super(theData, applet, yName);
		this.modelKey = modelKey;
		if (yName == null || xName == null)
			RegnImages.loadRegn(applet);
		this.xName = xName;
		this.minIntercept = minIntercept;
		this.maxIntercept = maxIntercept;
		this.minSlope = minSlope;
		this.maxSlope = maxSlope;
		this.interceptColor = interceptColor;
		this.slopeColor = slopeColor;
	}
	
	public LinearEquationView(DataSet theData, XApplet applet, String modelKey,
							String yName, String xName, NumValue minIntercept, NumValue maxIntercept,
							NumValue minSlope, NumValue maxSlope) {
		this(theData, applet, modelKey, yName, xName, minIntercept,
												maxIntercept, minSlope, maxSlope, null, null);
	}
	
	public void setExplanName(String xName) {
		this.xName = xName;
	}
	
	public void setSelectedParamIndex(int selectedParamIndex) {
		this.selectedParamIndex = selectedParamIndex;
	}
	
	public void setMinMaxParams(NumValue minIntercept, NumValue maxIntercept,
																NumValue minSlope, NumValue maxSlope) {
		this.minIntercept = minIntercept;
		this.maxIntercept = maxIntercept;
		this.minSlope = minSlope;
		this.maxSlope = maxSlope;
		reinitialise();
		invalidate();
		
//		System.out.println("minIntercept = " + minIntercept + ", maxIntercept = "
//										+ maxIntercept + ", minSlope = " + minSlope + ", maxSlope = " + maxSlope);
	}
	
	public void setModelKey(String modelKey) {
		this.modelKey = modelKey;
	}

//--------------------------------------------------------------------------------
	
	protected String getExplanName(int paramIndex) {
		return xName;
	}
	
	protected Image getExplanImage(int paramIndex) {
		return RegnImages.x;
	}
	
	protected Color getParamColor(int paramIndex) {
		return (paramIndex == 0) ? interceptColor : slopeColor;
	}
	
	protected NumValue getParamValue(int paramIndex) {
		LinearModel lm = (LinearModel)getVariable(modelKey);
		return (paramIndex == 0) ? lm.getIntercept() : lm.getSlope();
	}
	
	protected int getNoOfParams() {
		return 2;
	}
	
	protected int getMaxParamValueWidth(Graphics g, int paramIndex) {
		if (paramIndex == 0) {
			int paramDecimals = Math.max(minIntercept.decimals, maxIntercept.decimals);
			return Math.max(minIntercept.stringWidth(g, paramDecimals),
														maxIntercept.stringWidth(g, paramDecimals));
		}
		else {
			int paramDecimals = Math.max(minSlope.decimals, maxSlope.decimals);
			return Math.max(minSlope.stringWidth(g, paramDecimals),
														maxSlope.stringWidth(g, paramDecimals));
		}
	}
	
	protected boolean parameterSelected(int paramIndex) {
		return (paramIndex == selectedParamIndex);
	}
	
	protected boolean doDrawParameter(int paramIndex) {
		return true;
	}
}
