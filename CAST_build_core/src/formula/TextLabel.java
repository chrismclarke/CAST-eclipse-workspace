package formula;

import java.awt.*;

import dataView.*;


public class TextLabel extends FormulaPanel {
	private Value theValue;
	
	public TextLabel(Value theValue, FormulaContext context) {
		super(context);
		
		this.theValue = theValue;
	}
	
	public TextLabel(String theString, FormulaContext context) {
		this(new LabelValue(theString), context);
	}
	
	public void changeText(String newText) {
		theValue = new LabelValue(newText);
		reinitialise();
		invalidate();
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
		return Double.NaN;
	}
}