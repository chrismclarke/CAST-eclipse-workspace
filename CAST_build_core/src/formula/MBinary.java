package formula;

import java.awt.*;


public class MBinary extends MFormula implements BinaryConstants {
	
	static final private int kOperatorGap = 4;
	static final private int kOperatorWidth = 7 + 2 * kOperatorGap;
	
	private int operator, operatorWidth;
	private MFormula val1, val2;
	
	public MBinary(int operator, MFormula val1, MFormula val2, FormulaContext context) {
		super(context);
		
		this.operator = operator;
		operatorWidth = (operator == BLANK) ? kOperatorGap : kOperatorWidth;
		setFormula(val1, val2);
	}
	
	public MBinary(int operator, FormulaContext context) {
		super(context);
		
		this.operator = operator;
		operatorWidth = (operator == BLANK) ? kOperatorGap : kOperatorWidth; // assumes that setFormula() will be called immediately
	}
	
	public void setFormula(MFormula val1, MFormula val2) {
		this.val1 = val1;
		this.val2 = val2;
		
		add(val1);
		add(val2);
	}
	
	public void reinitialise() {
		val1.reinitialise();
		val2.reinitialise();
		super.reinitialise();
	}
	
	protected void doInitialisation(Graphics g) {
		super.doInitialisation(g);
		
		val1.initialise(g);
		val2.initialise(g);
		
		layoutWidth = val1.layoutWidth + operatorWidth + val2.layoutWidth;
		
		layoutAscent = Math.max(val1.layoutAscent, val2.layoutAscent);
		layoutDescent = Math.max(val1.layoutDescent, val2.layoutDescent);
	}
	
	public void layoutContainer(Container parent) {
		initialise(context.getGraphics());
		int horizStart = (getSize().width - layoutWidth) / 2;
		int baseline = (getSize().height + layoutAscent - layoutDescent) / 2;
		
		val1.setBounds(horizStart, baseline - val1.layoutAscent, val1.layoutWidth,
															val1.layoutAscent + val1.layoutDescent);
		horizStart += val1.layoutWidth + operatorWidth;
		
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
			case LESS_EQUAL:
				g.drawLine(horizStart + 3, baseline - 7, horizStart, baseline - 4);
				g.drawLine(horizStart + 3, baseline - 1, horizStart, baseline - 4);
				if (operator == LESS_EQUAL)
					g.drawLine(horizStart + 2, baseline, horizStart - 1, baseline - 3);
				break;
			case GREATER_THAN:
			case GREATER_EQUAL:
				g.drawLine(horizStart, baseline - 7, horizStart + 3, baseline - 4);
				g.drawLine(horizStart, baseline - 1, horizStart + 3, baseline - 4);
				if (operator == GREATER_EQUAL)
				g.drawLine(horizStart + 1, baseline, horizStart + 4, baseline - 3);
				break;
			case PLUS_MINUS:
				g.drawLine(horizStart, baseline - 5, horizStart + 6, baseline - 5);
				g.drawLine(horizStart + 3, baseline - 7, horizStart + 3, baseline - 3);
				g.drawLine(horizStart, baseline - 1, horizStart + 6, baseline - 1);
				break;
			case BLANK:
				break;
		}
	}
}