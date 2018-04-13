package formula;

import java.awt.*;

import dataView.*;


abstract public class MFormula extends XPanel implements LayoutManager2 {
	protected FormulaContext context;
	
	private boolean initialised = false;
	protected int ascent, descent;
	
	protected int layoutWidth, layoutAscent, layoutDescent;
	
	public MFormula(FormulaContext context) {
		setLayout(this);
		
		this.context = context;
		
		setForeground(context.getColor());
		lockBackground(context.getBackground());
		setFont(context.getFont());
	}

//----------------------------------------------------------------
	
	abstract protected void paintAroundItems(Graphics g);
	abstract public void layoutContainer(Container parent);

//----------------------------------------------------------------
	
	protected void initialise(Graphics g) {
		if (!initialised) {
			doInitialisation(g);
			initialised = true;
		}
	}
	
	protected void doInitialisation(Graphics g) {
		g.setFont(context.getFont());
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		descent = fm.getDescent();
	}
	
	public void reinitialise() {
		initialised = false;
		invalidate();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		BufferedCanvas.checkAliasing(g);
		paintAroundItems(g);
	}

//----------------------------------------------------------------------

	public void addLayoutComponent(String name, Component comp) {
	}

	public void removeLayoutComponent(Component comp) {
	}
	
	public Dimension preferredLayoutSize(Container parent) {
		initialise(context.getGraphics());
		Insets insets = getInsets();
		
		return new Dimension(layoutWidth + insets.left + insets.right,
												layoutAscent + layoutDescent + insets.top + insets.bottom);
	}
	
	public Dimension minimumLayoutSize(Container parent) {
		return preferredLayoutSize(parent);
	}
	
	public Dimension maximumLayoutSize(Container parent) {
		return preferredLayoutSize(parent);
	}

	public void addLayoutComponent(Component comp, Object constraints) {
	}

	public float getLayoutAlignmentX(Container target) {
		return 0.0f;
	}

	public float getLayoutAlignmentY(Container target) {
		initialise(context.getGraphics());
		return layoutAscent / (float)(layoutAscent + layoutDescent);
	}

	public void invalidateLayout(Container target) {
	}
}