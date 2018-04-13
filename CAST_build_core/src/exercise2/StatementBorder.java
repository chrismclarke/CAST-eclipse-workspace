package exercise2;

import java.awt.*;
import javax.swing.border.*;

import dataView.*;


public class StatementBorder extends EmptyBorder {
	static final private Font kDefaultNumberFont = new Font("serif", Font.BOLD, 48);
	
	private int leftBorder;
	private String label;
	private Font numberFont = kDefaultNumberFont;
	private Color numberColor = Color.red;
	
	public StatementBorder(int top, int left, int bottom, int right, int index) {
		super(top, left, bottom, right);
		leftBorder = left;
		label = index + ".";
	}
	
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		BufferedCanvas.checkAliasing(g);
		g.setColor(c.getBackground());
		g.fillRect(x, y, leftBorder, height);
		
		g.setColor(numberColor);
		Font oldFont = g.getFont();
		g.setFont(numberFont);
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int qnWidth = fm.stringWidth(label);
		g.drawString(label, x + (leftBorder - qnWidth) / 2, y + (height + ascent) / 2);
		
		g.setFont(oldFont);
	}
	
	public boolean isBorderOpaque() {
		return true;
	}
	
	public void setNumberFont(Font numberFont, Color numberColor) {
		this.numberFont = numberFont;
		this.numberColor = numberColor;
	}
}