package formula;

import java.awt.*;

import dataView.*;


abstract public class FormulaPanel extends XPanel implements LayoutManager {
	protected FormulaContext context;
	
	protected FormulaPanel parent;
	
	private boolean initialised = false;
	protected int ascent, descent;
	
	protected int layoutWidth, layoutAscent, layoutDescent;
	
	public FormulaPanel(FormulaContext context) {
		setLayout(this);
		this.context = context;
		
		setForeground(context.getColor());
		setFont(context.getFont());
	}

//----------------------------------------------------------------
	
	abstract protected void paintAroundItems(Graphics g);
	abstract public void layoutContainer(Container parent);
	abstract protected double evaluateFormula();

//----------------------------------------------------------------
	
	protected boolean initialise(Graphics g) {
		if (!initialised) {
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			descent = fm.getDescent();
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	public void reinitialise() {
		initialised = false;
		invalidate();
	}
	
	protected void setParent(FormulaPanel parent) {
		this.parent = parent;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		BufferedCanvas.checkAliasing(g);
		initialise(g);
		paintAroundItems(g);
	}

//----------------------------------------------------------------------

	public void addLayoutComponent(String name, Component comp) {
	}

	public void removeLayoutComponent(Component comp) {
	}
	
	public Dimension minimumLayoutSize(Container parent) {
		initialise(getGraphics());
		Insets insets = parent.getInsets();
		
		return new Dimension(layoutWidth + insets.left + insets.right,
												layoutAscent + layoutDescent + insets.top + insets.bottom);
	}
	
	public Dimension preferredLayoutSize(Container parent) {
		return minimumLayoutSize(parent);
	}
}