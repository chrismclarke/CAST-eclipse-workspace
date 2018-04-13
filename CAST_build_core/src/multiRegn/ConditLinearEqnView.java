package multiRegn;

import java.awt.*;

import dataView.*;
import models.*;

import linMod.*;
import regn.*;


public class ConditLinearEqnView extends CoreLinearEquationView {
//	static public final String CONDIT_LINEAR_MODEL = "conditLinearEqn";
	
	private String modelKey;
	private String xName[];
	private NumValue minParam[];
	private NumValue maxParam[];
	
	private int conditType;
	private double conditValue;
	
	public ConditLinearEqnView(DataSet theData, XApplet applet, String modelKey, String yName,
								String[] xName, NumValue[] minParam, NumValue[] maxParam) {
		super(theData, applet, yName);
		this.modelKey = modelKey;
		this.xName = xName;
		this.minParam = minParam;
		this.maxParam = maxParam;
	}
	
	public void setCondit(int conditType, double conditValue) {
		this.conditType = conditType;
		this.conditValue = conditValue;
		repaint();
	}

//--------------------------------------------------------------------------------
	
	protected String getExplanName(int paramIndex) {
		return xName[1 - conditType];
	}
	
	protected Image getExplanImage(int paramIndex) {
		return (paramIndex == 1) ? RegnImages.x
				: (paramIndex == 2) ? RegnImages.z
				: null;
	}
	
	protected Color getParamColor(int paramIndex) {
		return getForeground();
	}
	
	protected NumValue getParamValue(int paramIndex) {
		MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
		if (paramIndex == 0) {
			NumValue b0 = model.getParameter(0);
			return new NumValue(b0.toDouble() + model.getParameter(1 + conditType).toDouble() * conditValue, b0.decimals);
		}
		else
			return model.getParameter(2 - conditType);
	}
	
	protected int getNoOfParams() {
		return 2;
	}
	
	protected int getMaxParamValueWidth(Graphics g, int paramIndex) {
		if (paramIndex == 0)
			return maxOriginalParamWidth(g, 0);
		else
			return Math.max(maxOriginalParamWidth(g, 1), maxOriginalParamWidth(g, 2));
	}
	
	private int maxOriginalParamWidth(Graphics g, int paramIndex) {
		int paramDecimals = Math.max(minParam[paramIndex].decimals, maxParam[paramIndex].decimals);
		return Math.max(minParam[paramIndex].stringWidth(g, paramDecimals),
													maxParam[paramIndex].stringWidth(g, paramDecimals));
	}
	
	protected boolean doDrawParameter(int paramIndex) {
		return true;
	}
	
	protected boolean parameterSelected(int paramIndex) {
		return (paramIndex == 1);
	}
}
