package formula;

import java.awt.*;


public class MSubscript extends MFormula {
	static final private int kSubscriptGap = 2;
	static final private int kSubscriptDrop = 3;
	
//	static final private double kSubSizeFactor = 0.75;
	
	private MFormula val, subscript;
	
	public MSubscript(MFormula val, String subString, FormulaContext context) {
		super(context);
		
		this.val = val;
		add(val);
		
		subscript = new MText(subString, context.getSmallerContext());
		add(subscript);
	}
	
	public void reinitialise() {
		val.reinitialise();
		subscript.reinitialise();
		super.reinitialise();
	}
	
	protected void doInitialisation(Graphics g) {
		super.doInitialisation(g);
		
		val.initialise(g);
		subscript.initialise(g);
		
		layoutWidth = val.layoutWidth + kSubscriptGap + subscript.layoutWidth;
		
		layoutAscent = val.layoutAscent;
		layoutDescent = Math.max(val.layoutDescent, kSubscriptDrop + subscript.layoutDescent);
	}
	
	public void layoutContainer(Container parent) {
		initialise(context.getGraphics());
		int horizStart = (getSize().width - layoutWidth) / 2;
		int baseline = (getSize().height + layoutAscent - layoutDescent) / 2;
		
		val.setBounds(horizStart, baseline - val.layoutAscent, val.layoutWidth,
															val.layoutAscent + val.layoutDescent);
															
		baseline += kSubscriptDrop;
		horizStart += val.layoutWidth + kSubscriptGap;
		subscript.setBounds(horizStart, baseline - subscript.layoutAscent, subscript.layoutWidth,
															subscript.layoutAscent + subscript.layoutDescent);
	}
	
	protected void paintAroundItems(Graphics g) {
	}
}