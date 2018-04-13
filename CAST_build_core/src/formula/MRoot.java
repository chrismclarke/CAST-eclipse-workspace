package formula;

import java.awt.*;


public class MRoot extends MFormula {
	
	static final private int kRootLeftWidth = 11;
	static final private int kRootRightWidth = 3;
	static final private int kRootExtraHeight = 3;
	
	private MFormula val;
	
	public MRoot(MFormula val, FormulaContext context) {
		super(context);
		
		this.val = val;
		
		add(val);
	}
	
	public void reinitialise() {
		val.reinitialise();
		super.reinitialise();
	}
	
	protected void doInitialisation(Graphics g) {
		super.doInitialisation(g);
		
		val.initialise(g);
		
		layoutWidth = val.layoutWidth + kRootLeftWidth + kRootRightWidth;
		
		layoutAscent = val.layoutAscent + kRootExtraHeight;
		layoutDescent = val.layoutDescent + kRootExtraHeight;
	}
	
	public void layoutContainer(Container parent) {
		initialise(context.getGraphics());
		int horizStart = (getSize().width - layoutWidth) / 2;
		int baseline = (getSize().height + layoutAscent - layoutDescent) / 2;
		
		horizStart += kRootLeftWidth;
		
		val.setBounds(horizStart, baseline - val.layoutAscent, val.layoutWidth,
															val.layoutAscent + val.layoutDescent);
	}
	
	protected void paintAroundItems(Graphics g) {
		int horizStart = (getSize().width - layoutWidth) / 2;
		int baseline = (getSize().height + layoutAscent - layoutDescent) / 2;
		
		int rootTop = baseline - layoutAscent;
		int rootBottom = baseline + layoutDescent;
		g.drawLine(horizStart, rootBottom - 7, horizStart + 4, rootBottom - 1);
		g.drawLine(horizStart + 4, rootBottom - 1, horizStart + 8, rootTop);
		g.drawLine(horizStart + 8, rootTop, horizStart + layoutWidth - 1, rootTop);
		g.drawLine(horizStart + layoutWidth - 1, rootTop, horizStart + layoutWidth - 1, rootTop + 3);
	}
}