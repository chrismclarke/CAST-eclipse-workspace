package formula;

import java.awt.*;

import dataView.*;


public class Const extends FormulaPanel {
	private NumValue theValue;
	
	public Const(NumValue theValue, FormulaContext context) {
		super(context);
		
		this.theValue = theValue;
	}
	
	protected boolean initialise(Graphics g) {
		if (super.initialise(g)) {
			layoutWidth = theValue.stringWidth(g);
			
			layoutAscent = ascent;
			layoutDescent = descent;
			return true;
		}
		else
			return false;
	}
	
	public void layoutContainer(Container parent) {
	}
	
	protected void paintAroundItems(Graphics g) {
		int horizStart = (getSize().width - layoutWidth) / 2;
		int baseline = (getSize().height + layoutAscent - layoutDescent) / 2;
		
		theValue.drawRight(g, horizStart, baseline);
	}
	
	protected double evaluateFormula() {
		return theValue.toDouble();
	}
}