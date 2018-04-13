package formula;

import java.awt.*;


public class MSuperscript extends MFormula {
	static final private int kSuperscriptGap = 2;
	static final private int kSuperscriptRise = 5;
	
	private MFormula val, superscript;
	
	public MSuperscript(MFormula val, String superString, FormulaContext context) {
		super(context);
		
		this.val = val;
		add(val);
		
		superscript = new MText(superString, context.getSmallerContext());
		add(superscript);
	}
	
	public void reinitialise() {
		val.reinitialise();
		superscript.reinitialise();
		super.reinitialise();
	}
	
	protected void doInitialisation(Graphics g) {
		super.doInitialisation(g);
		
		val.initialise(g);
		superscript.initialise(g);
		
		layoutWidth = val.layoutWidth + kSuperscriptGap + superscript.layoutWidth;
		
		layoutAscent = Math.max(val.layoutAscent, kSuperscriptRise + superscript.layoutAscent);
		layoutDescent = val.layoutDescent;
	}
	
	public void layoutContainer(Container parent) {
		initialise(context.getGraphics());
		int horizStart = (getSize().width - layoutWidth) / 2;
		int baseline = (getSize().height + layoutAscent - layoutDescent) / 2;
		
		val.setBounds(horizStart, baseline - val.layoutAscent, val.layoutWidth,
															val.layoutAscent + val.layoutDescent);
															
		baseline -= kSuperscriptRise;
		horizStart += val.layoutWidth + kSuperscriptGap;
		superscript.setBounds(horizStart, baseline - superscript.layoutAscent, superscript.layoutWidth,
															superscript.layoutAscent + superscript.layoutDescent);
	}
	
	protected void paintAroundItems(Graphics g) {
	}
}