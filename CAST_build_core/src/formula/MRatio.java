package formula;

import java.awt.*;


public class MRatio extends MFormula {
	
	static final private int kEditLineGap = 2;
	static final private int kFractionExtra = 2;
	static final private int kDivWidth = 12;
	static final private int kSuperSubOffset = 2;
	static final private int kInlineBorder = 2;
	
	private boolean inline = false;
	
	private MFormula val1, val2;
	
	private int numerHeight, denomHeight;
	
	public MRatio(MFormula val1, MFormula val2, FormulaContext context) {
		super(context);
		
		this.val1 = val1;
		this.val2 = val2;
		add(val1);
		add(val2);
	}
	
	public MRatio(MFormula val1, MFormula val2, boolean inline, FormulaContext context) {
		this(val1, val2, context);
		
		this.inline = inline;
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
		
		if (inline) {
			layoutAscent = Math.max(val1.layoutAscent, val2.layoutAscent) + kSuperSubOffset;
			layoutDescent = Math.max(val1.layoutDescent, val2.layoutDescent) + kSuperSubOffset;
			layoutWidth = val1.layoutWidth + val2.layoutWidth + kDivWidth + 2 * kInlineBorder;
		}
		else {
			layoutWidth = Math.max(val1.layoutWidth, val2.layoutWidth) + 2 * kFractionExtra;
			
			numerHeight = val1.layoutAscent + val1.layoutDescent;
			denomHeight = val2.layoutAscent + val2.layoutDescent;
			
			int height = numerHeight + denomHeight + 2 * kEditLineGap + 1;
			
			int meanAscent = (val1.layoutAscent + height - val2.layoutDescent) / 2;
			
			layoutAscent = meanAscent + (val1.layoutAscent - val2.layoutAscent) / 3;
			layoutDescent = height - layoutAscent;
		}
	}
	
	public void layoutContainer(Container parent) {
		initialise(context.getGraphics());
		
		if (inline) {
			int numerHStart = (getSize().width - layoutWidth) / 2 + kInlineBorder;
			int numerTop = (getSize().height - layoutAscent - layoutDescent) / 2;
			val1.setBounds(numerHStart, numerTop, val1.layoutWidth, val1.layoutAscent + val1.layoutDescent);
			
			int denomHStart = numerHStart + val1.layoutWidth + kDivWidth;
			int denomTop = numerTop + 2 * kSuperSubOffset;
			val2.setBounds(denomHStart, denomTop, val2.layoutWidth, val2.layoutAscent + val2.layoutDescent);
		}
		else {
			int numerHStart = (getSize().width - val1.layoutWidth) / 2;
			int numerTop = (getSize().height - layoutAscent - layoutDescent) / 2;
			val1.setBounds(numerHStart, numerTop, val1.layoutWidth, numerHeight);
			
			int denomHStart = (getSize().width - val2.layoutWidth) / 2;
			int denomTop = numerTop + numerHeight + 2 * kEditLineGap + 1;
			val2.setBounds(denomHStart, denomTop, val2.layoutWidth, denomHeight);
		}
	}
	
	protected void paintAroundItems(Graphics g) {
		if (inline) {
			int horizStart = (getSize().width - layoutWidth) / 2 + val1.layoutWidth + kInlineBorder;
			int lineTop = (getSize().height - layoutAscent - layoutDescent) / 2;
			int lineBottom = lineTop + layoutAscent + layoutDescent;
			
			g.drawLine(horizStart, lineBottom, horizStart + kDivWidth, lineTop);
		}
		else {
			int horizStart = (getSize().width - layoutWidth) / 2;
			int numerTop = (getSize().height - layoutAscent - layoutDescent) / 2;
			
			int lineVert = numerTop + numerHeight + kEditLineGap;
			g.drawLine(horizStart, lineVert, horizStart + layoutWidth -1, lineVert);
		}
	}
}