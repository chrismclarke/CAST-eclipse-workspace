package loess;

import java.awt.*;

import dataView.*;
import axis.*;
import regn.*;


public class LinearPowerEquationView extends LinearEquationView {
	
//	private Font superscriptFont;
	private NumCatAxis xAxis, yAxis;
	
	public LinearPowerEquationView(DataSet theData, XApplet applet, String modelKey, String yName,
								String xName, NumValue minIntercept, NumValue maxIntercept, NumValue minSlope,
								NumValue maxSlope, NumCatAxis xAxis, NumCatAxis yAxis) {
		super(theData, applet, modelKey, yName, xName, minIntercept, maxIntercept, minSlope, maxSlope, null, null);
//		superscriptFont = applet.getSmallFont();
		this.xAxis = xAxis;
		this.yAxis = yAxis;
	}

//--------------------------------------------------------------------------------
	
	private int getNameWidth(Graphics g, String name, NumCatAxis axis) {
		if (axis instanceof TransAxisInterface)
			return axis.maxNamePowerWidth(g);
		else
			return g.getFontMetrics().stringWidth(name);
	}
	
//	private int drawName(Graphics g, int horizPos, int baseline, String name,
//																		NumCatAxis axis, boolean yNotX) {
//		FontMetrics fm = g.getFontMetrics();
//		int rightPos = horizPos + getNameWidth(g, name, axis);
//		int nameWidth = fm.stringWidth(name);
//		NumValue power = axis.getPower();
//		if (power.value == 0.0) {
//			g.drawString(name, rightPos - nameWidth, baseline);
//			
//			g.drawString("log", horizPos, baseline);
//			int logWidth = fm.stringWidth("log");
//			
//			Font oldFont = g.getFont();
//			g.setFont(superscriptFont);
//			g.drawString("10", horizPos + logWidth, baseline + 3);
//			g.setFont(oldFont);
//		}
//		else if (power.value == 1.0) {
//			if (yNotX)
//				g.drawString(name, rightPos - nameWidth, baseline);
//			else
//				g.drawString(name, horizPos, baseline);
//		}
//		else if (yNotX) {
//			Font oldFont = g.getFont();
//			g.setFont(superscriptFont);
//			int powerWidth = power.stringWidth(g);
//			power.drawRight(g, rightPos - powerWidth, baseline - 3);
//			g.setFont(oldFont);
//			g.drawString(name, rightPos - powerWidth - nameWidth, baseline);
//		}
//		else {
//			String leftString = (power.value < 0.0) ? ("-(" + name) : name;
//			String rightString = (power.value < 0.0) ? ")" : "";
//			int leftWidth = fm.stringWidth(leftString);
//			int rightWidth = fm.stringWidth(rightString);
//			Font oldFont = g.getFont();
//			g.setFont(superscriptFont);
//			int powerWidth = power.stringWidth(g);
//			int powerStart = yNotX ? horizPos + leftWidth : rightPos - rightWidth - powerWidth;
//			power.drawRight(g, powerStart, baseline - 3);
//			g.setFont(oldFont);
//			
//			int leftStart = powerStart - leftWidth;
//			g.drawString(leftString, leftStart, baseline);
//			int rightStart = powerStart + powerWidth;
//			g.drawString(rightString, rightStart, baseline);
//		}
//		
//		return rightPos;
//	}
	
	protected int getExplanWidth(Graphics g, int paramIndex) {
		return getNameWidth(g, xName, xAxis) + g.getFontMetrics().stringWidth("log10");
	}
	
	protected int getResponseWidth(Graphics g) {
		return getNameWidth(g, yName, yAxis) + g.getFontMetrics().stringWidth(" = ");
	}
	
	protected int drawExplan(Graphics g, int paramIndex, int horizPos, int baseline) {
		horizPos += g.getFontMetrics().stringWidth(" ");
		int maxWidth = getNameWidth(g, xName, xAxis);
		return xAxis.drawNamePower(g, horizPos, baseline, NumCatAxis.LEFT_ALIGN, maxWidth);
	}
	
	protected int drawResponse(Graphics g, int horizPos, int baseline) {
		int maxWidth = getNameWidth(g, yName, yAxis);
		horizPos = yAxis.drawNamePower(g, horizPos, baseline, NumCatAxis.RIGHT_ALIGN, maxWidth);
		g.drawString(" = ", horizPos, baseline);
		
		return horizPos + g.getFontMetrics().stringWidth(" = ");
	}
}
