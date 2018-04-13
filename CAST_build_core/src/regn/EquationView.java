package regn;

import java.awt.*;

import dataView.*;


abstract public class EquationView extends DataView {
	private static final int kValueBorder = 3;	//	at all sides of value inside box
	
	private boolean initialised = false;
	protected int modelWidth, modelHeight;
	private int extraPixels = 0;		//		MultiXApplet needs to allow extra pixels so that
												//		x-variable's name can be changed.
	
	public EquationView(DataSet theData, XApplet applet) {
		super(theData, applet, null);
	}
	
	public void setExtraPixels(int extraPixels) {
		this.extraPixels = extraPixels;
	}

//--------------------------------------------------------------------------------
	
	abstract public int paintModel(Graphics g);

//--------------------------------------------------------------------------------
	
	protected boolean initialise(Graphics g) {
		if (!initialised) {
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	protected void reinitialise() {
		initialised = false;
	}
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		return new Dimension(modelWidth + extraPixels, modelHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	protected Dimension getValueSize(FontMetrics fm, int maxValueWidth) {
		return new Dimension(maxValueWidth + 2 * kValueBorder + 2,
													fm.getAscent() + fm.getDescent() + 2 * kValueBorder + 2);
	}
	
	protected int getValueBaseline(FontMetrics fm) {
		return kValueBorder + 1 + fm.getAscent();
	}
	
	protected void drawParameter(Graphics g, String theValue, int maxValueWidth, int horiz,
																										int baseline) {
		drawParameter(g, theValue, maxValueWidth, horiz, baseline, false);
	}
	
	protected void drawParameter(Graphics g, String theValue, int maxValueWidth, int horiz,
																			int baseline, boolean selected) {
		if (theValue == null)
			theValue = "???";
		
		FontMetrics fm = g.getFontMetrics();
		int baselineOffset = getValueBaseline(fm);
		
		Dimension valueDim = getValueSize(fm, maxValueWidth);
		g.drawRect(horiz, baseline - baselineOffset, valueDim.width - 1, valueDim.height - 1);
		
		g.setColor(selected ? Color.yellow : Color.white);
		g.fillRect(horiz + 1, baseline - baselineOffset + 1, valueDim.width - 2,
																							 valueDim.height - 2);
		g.setColor(getForeground());
		g.drawString(theValue, horiz + kValueBorder + 1, baseline);
	}
	
	public void paintView(Graphics g) {
		initialise(getGraphics());
		paintModel(g);
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
}
