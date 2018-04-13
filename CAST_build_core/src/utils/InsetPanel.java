package utils;

import java.awt.*;

import dataView.*;

public class InsetPanel extends XPanel {
	private int left, right, top, bottom;
	private Color borderColor = null;
	
	public InsetPanel(int left, int top, int right, int bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}
	
	public InsetPanel(int leftRight, int topBottom) {
		this.left = leftRight;
		this.top = topBottom;
		this.right = leftRight;
		this.bottom = topBottom;
	}
	
	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	public Insets insets() {
		return new Insets(top, left, bottom, right);
	}
	
  public void paintComponent(Graphics g) {
  	super.paintComponent(g);
  	if (borderColor != null) {
  		g.setColor(borderColor);
  		g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
  	}
  }
}