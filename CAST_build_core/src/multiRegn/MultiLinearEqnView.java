package multiRegn;

import java.awt.*;

import dataView.*;
import models.*;

import linMod.*;
import regn.*;


public class MultiLinearEqnView extends CoreLinearEquationView {
	
	private String modelKey;
	private String xName[];
	private String xSubscript[];
	private NumValue minParam[];
	private NumValue maxParam[];
	
	private boolean showParameter[];
	private boolean squaredExplan[];
	private int highlightIndex = -1;
	
	public MultiLinearEqnView(DataSet theData, XApplet applet, String modelKey, String yName, String[] xName,
								NumValue[] minParam, NumValue[] maxParam) {
		super(theData, applet, yName);
		this.modelKey = modelKey;
		this.xName = xName;
		xSubscript = new String[xName.length];
		for (int i=0 ; i<xName.length ; i++) {
			int separator = xName[i].indexOf('/');
			if (separator >= 0) {
				xSubscript[i] = xName[i].substring(separator + 1);
				this.xName[i] = xName[i].substring(0, separator);
			}
		}
		this.minParam = minParam;
		this.maxParam = maxParam;
		
		MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
		int noOfParams = model.noOfParameters();
		showParameter = new boolean[noOfParams];
		for (int i=0 ; i<noOfParams ; i++)
			showParameter[i] = true;
		
		squaredExplan = new boolean[xName.length];
		for (int i=0 ; i<xName.length ; i++)
			squaredExplan[i] = false;
	}
	
	public void setLastDrawParameter(int paramIndex) {
		for (int i=0 ; i<showParameter.length ; i++)
			showParameter[i] = (i <= paramIndex);
	}
	
	public void setDrawParameters(boolean[] showParameter) {
		this.showParameter = showParameter;
	}
	
	public void setHighlightIndex(int highlightIndex) {
		this.highlightIndex = highlightIndex;
	}
	
	public void setXNames(String[] xName) {
		this.xName = xName;
		reinitialise();
	}
	
	public void setSquaredExplan(int index, boolean squaredExplan) {
		this.squaredExplan[index] = squaredExplan;
	}

//--------------------------------------------------------------------------------
	
	protected String getExplanName(int paramIndex) {
		return xName[paramIndex - 1];
	}
	
	protected Image getExplanImage(int paramIndex) {
		return (paramIndex == 1) ? RegnImages.x
				: (paramIndex == 2) ? RegnImages.z
				: null;
	}
	
	protected Color getParamColor(int paramIndex) {
		return null;
	}
	
	protected boolean parameterSelected(int paramIndex) {
		return paramIndex == highlightIndex;
	}
	
	protected NumValue getParamValue(int paramIndex) {
		MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
		return model.getParameter(paramIndex);
	}
	
	protected int getNoOfParams() {
		MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
		return model.noOfParameters();
	}
	
	protected int getMaxParamValueWidth(Graphics g, int paramIndex) {
		int paramDecimals = Math.max(minParam[paramIndex].decimals, maxParam[paramIndex].decimals);
		return Math.max(minParam[paramIndex].stringWidth(g, paramDecimals),
													maxParam[paramIndex].stringWidth(g, paramDecimals));
	}
	
	protected boolean doDrawParameter(int paramIndex) {
		return showParameter[paramIndex];
	}
	
	protected int getExplanWidth(Graphics g, int paramIndex) {
		int width = super.getExplanWidth(g, paramIndex);
														//	to cope with possible squared variable
		if (squaredExplan[paramIndex - 1]) {
			Font oldFont = g.getFont();
			g.setFont(new Font(oldFont.getName(), oldFont.getStyle(), oldFont.getSize() - 2));
			FontMetrics fms = g.getFontMetrics();
			width += 2 + fms.stringWidth("2");
			g.setFont(oldFont);
		}
		if (xSubscript[paramIndex - 1] != null) {		//	for subscript in categorical terms
			Font oldFont = g.getFont();
			g.setFont(new Font(oldFont.getName(), Font.ITALIC, oldFont.getSize() - 2));
			FontMetrics fms = g.getFontMetrics();
			width += 2 + fms.stringWidth(xSubscript[paramIndex - 1]);
			g.setFont(oldFont);
		}
		return width;
	}
	
	protected int drawExplan(Graphics g, int paramIndex, int horizPos, int baseline) {
		horizPos = super.drawExplan(g, paramIndex, horizPos, baseline);
														//	to cope with possible squared variable
		if (squaredExplan[paramIndex - 1]) {
			Font oldFont = g.getFont();
			g.setFont(new Font(oldFont.getName(), oldFont.getStyle(), oldFont.getSize() - 2));
			FontMetrics fms = g.getFontMetrics();
			horizPos += 2;
			g.drawString("2", horizPos, baseline - 5);
			horizPos += fms.stringWidth("2");
			g.setFont(oldFont);
		}
		if (xSubscript[paramIndex - 1] != null) {		//	for subscript in categorical terms
			Font oldFont = g.getFont();
			g.setFont(new Font(oldFont.getName(), Font.ITALIC, oldFont.getSize() - 2));
			FontMetrics fms = g.getFontMetrics();
			horizPos += 2;
			g.drawString(xSubscript[paramIndex - 1], horizPos, baseline + 4);
			horizPos += fms.stringWidth(xSubscript[paramIndex - 1]);
			g.setFont(oldFont);
		}
		return horizPos;
	}
}
