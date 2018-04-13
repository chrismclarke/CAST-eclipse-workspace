package formula;

import java.awt.*;


public class Ratio extends FormulaPanel {
	
	static final private int kEditLineGap = 2;
	static final private int kFractionExtra = 2;
	
	private FormulaPanel val1, val2;
	private int numerHeight, denomHeight;
	
	public Ratio(FormulaPanel val1, FormulaPanel val2, FormulaContext context) {
		super(context);
		
		this.val1 = val1;
		this.val2 = val2;
		
		val1.setParent(this);
		val2.setParent(this);
		add(val1);
		add(val2);
	}
	
	public void reinitialise() {
		val1.reinitialise();
		val2.reinitialise();
		super.reinitialise();
	}
	
	protected boolean initialise(Graphics g) {
		if (super.initialise(g)) {
			val1.initialise(val1.getGraphics());
			val2.initialise(val2.getGraphics());
			
			layoutWidth = Math.max(val1.layoutWidth, val2.layoutWidth) + 2 * kFractionExtra;
			
			numerHeight = val1.layoutAscent + val1.layoutDescent;
			denomHeight = val2.layoutAscent + val2.layoutDescent;
			
			int height = numerHeight + denomHeight + 2 * kEditLineGap + 1;
			
			int meanAscent = (val1.layoutAscent + height - val2.layoutDescent) / 2;
			
			layoutAscent = meanAscent + (val1.layoutAscent - val2.layoutAscent) / 3;
			layoutDescent = height - layoutAscent;
			return true;
		}
		else
			return false;
	}
	
	public void layoutContainer(Container parent) {
		initialise(getGraphics());
		int numerHStart = (getSize().width - val1.layoutWidth) / 2;
		int numerTop = (getSize().height - layoutAscent - layoutDescent) / 2;
		val1.setBounds(numerHStart, numerTop, val1.layoutWidth, numerHeight);
		
		int denomHStart = (getSize().width - val2.layoutWidth) / 2;
		int denomTop = numerTop + numerHeight + 2 * kEditLineGap + 1;
		val2.setBounds(denomHStart, denomTop, val2.layoutWidth, denomHeight);
	}
	
	protected void paintAroundItems(Graphics g) {
		int horizStart = (getSize().width - layoutWidth) / 2;
		int numerTop = (getSize().height - layoutAscent - layoutDescent) / 2;
		
		int lineVert = numerTop + numerHeight + kEditLineGap;
		g.drawLine(horizStart, lineVert, horizStart + layoutWidth -1, lineVert);
	}
	
	protected double evaluateFormula() {
		double v1 = val1.evaluateFormula();
		double v2 = val2.evaluateFormula();
		return v1 / v2;
	}
}