package formula;

import java.awt.*;


public class Binary extends FormulaPanel implements BinaryConstants {
	
	static final private int kOperatorGap = 4;
	static final private int kOperatorWidth = 7 + 2 * kOperatorGap;
	
	private int operator;
	private FormulaPanel val1, val2;
	
	public Binary(int operator, FormulaPanel val1, FormulaPanel val2, FormulaContext context) {
		super(context);
		
		this.operator = operator;
		this.val1 = val1;
		this.val2 = val2;
		
		val1.setParent(this);
		val2.setParent(this);
		add(val1);
		add(val2);
	}
	
	public Binary(int operator, FormulaContext context) {
		super(context);		//		Must be followed immediately by addSubFormulae().
													//		Used when sub-formulae cannot be created by
													//		static methods (to get references to parts).
		
		this.operator = operator;
	}
	
	public void changeOperator(int operator) {
		this.operator = operator;
		repaint();
	}
	
	protected void addSubFormulae(FormulaPanel val1, FormulaPanel val2) {
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
			
			layoutWidth = val1.layoutWidth + kOperatorWidth + val2.layoutWidth;
			
			layoutAscent = Math.max(val1.layoutAscent, val2.layoutAscent);
			layoutDescent = Math.max(val1.layoutDescent, val2.layoutDescent);
			return true;
		}
		else
			return false;
	}
	
	public void layoutContainer(Container parent) {
		initialise(getGraphics());
		int horizStart = (getSize().width - layoutWidth) / 2;
		int baseline = (getSize().height + layoutAscent - layoutDescent) / 2;
		
		val1.setBounds(horizStart, baseline - val1.layoutAscent, val1.layoutWidth,
															val1.layoutAscent + val1.layoutDescent);
		horizStart += val1.layoutWidth + kOperatorWidth;
		
		val2.setBounds(horizStart, baseline - val2.layoutAscent, val2.layoutWidth,
															val2.layoutAscent + val2.layoutDescent);
	}
	
	protected void paintAroundItems(Graphics g) {
		int horizStart = (getSize().width - layoutWidth) / 2;
		int baseline = (getSize().height + layoutAscent - layoutDescent) / 2;
		
		horizStart += val1.layoutWidth + kOperatorGap;
		
		switch (operator) {
			case PLUS:
				g.drawLine(horizStart, baseline - 4, horizStart + 6, baseline - 4);
				g.drawLine(horizStart + 3, baseline - 7, horizStart + 3, baseline - 1);
				break;
			case MINUS:
				g.drawLine(horizStart, baseline - 4, horizStart + 6, baseline - 4);
				break;
			case TIMES:
				g.drawLine(horizStart, baseline - 7, horizStart + 6, baseline - 1);
				g.drawLine(horizStart, baseline - 1, horizStart + 6, baseline - 7);
				break;
			case EQUALS:
				g.drawLine(horizStart, baseline - 3, horizStart + 6, baseline - 3);
				g.drawLine(horizStart, baseline - 6, horizStart + 6, baseline - 6);
				break;
			case LESS_THAN:
				g.drawLine(horizStart + 3, baseline - 7, horizStart, baseline - 4);
				g.drawLine(horizStart + 3, baseline - 1, horizStart, baseline - 4);
				break;
			case LESS_EQUAL:
				g.drawLine(horizStart + 3, baseline - 8, horizStart, baseline - 5);
				g.drawLine(horizStart + 3, baseline - 2, horizStart, baseline - 5);
				g.drawLine(horizStart + 2, baseline, horizStart - 1, baseline - 3);
				break;
			case GREATER_THAN:
				g.drawLine(horizStart, baseline - 7, horizStart + 3, baseline - 4);
				g.drawLine(horizStart, baseline - 1, horizStart + 3, baseline - 4);
				break;
			case GREATER_EQUAL:
				g.drawLine(horizStart, baseline - 8, horizStart + 3, baseline - 5);
				g.drawLine(horizStart, baseline - 2, horizStart + 3, baseline - 5);
				g.drawLine(horizStart + 1, baseline, horizStart + 4, baseline - 3);
				break;
		}
	}
	
	protected double evaluateFormula() {
		double v1 = val1.evaluateFormula();
		double v2 = val2.evaluateFormula();
		return (operator == PLUS) ? v1 + v2
				: (operator == MINUS) ? v1 - v2
				: (operator == TIMES) ? v1 * v2
				: (operator == EQUALS) ? v2
				: Double.NaN;
	}
}