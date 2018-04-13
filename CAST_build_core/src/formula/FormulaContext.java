package formula;

import java.awt.*;

import dataView.*;


public class FormulaContext {
	static final private double kSmallerSizeFactor = 0.75;
	
	private Color c, background;
	private Font f;
	
	private XApplet applet;

	public FormulaContext(Color c, Color background, Font f, XApplet applet) {
		this.c = (c == null) ? Color.black : c;
		this.f = (f == null) ? new Font("SansSerif", Font.PLAIN, 12) : f;
		this.applet = applet;
		this.background = background;
	}

	public FormulaContext(Color c, Font f, XApplet applet) {
		this(c, Color.white, f, applet);
	}
	
	public Color getColor() {
		return c;
	}
	
	public Color getBackground() {
		return background;
	}
	
	public Font getFont() {
		return f;
	}
	
	public XApplet getApplet() {
		return applet;
	}
	
	public FormulaContext getSmallerContext() {
		int smallerSize = (int)Math.round(kSmallerSizeFactor * f.getSize());
		Font smallerFont = new Font(f.getName(), f.getStyle(), smallerSize);
		return new FormulaContext(c, background, smallerFont, applet);
	}
	
	public FormulaContext getRecoloredContext(Color newColor) {
		return new FormulaContext(newColor, background, f, applet);
	}
	
	public FormulaContext getBoldContext() {
		Font boldFont = new Font(f.getName(), Font.BOLD, f.getSize());
		return new FormulaContext(c, background, boldFont, applet);
	}
	
	public Graphics getGraphics() {
		return applet.getGraphics();
	}
}