package ebook;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import dataView.*;


public class BigButton extends JLabel {
	static final private Color kStdButtonBackground = new Color(0x65a9d7);
	static final private Color kMouseoverButtonBackground = new Color(0x3d779c);
	static final private Color kButtonBorderColor = DataView.mixColors(kStdButtonBackground, Color.black);
	
	private Font kButtonFont = new Font("Arial", Font.BOLD, 24);
	private int topBottomMargin;
	
	public BigButton(String buttonNameString, int fontSize, int topBottomMargin) {
																												//	Assumes that it is in a transparent JPanel
		super(buttonNameString, JLabel.LEFT);
		
		this.topBottomMargin = topBottomMargin;
		kButtonFont = new Font("Arial", Font.BOLD, fontSize);
		
		setBorder(BorderFactory.createEmptyBorder(10, topBottomMargin, 10, topBottomMargin));
		setFont(kButtonFont);
		setForeground(Color.white);
		setBackground(kStdButtonBackground);
		
		addMouseListener(new MouseAdapter() {
							public void mouseExited(MouseEvent e) {
								if (isEnabled())
									setBackground(kStdButtonBackground);
							}
							public void mouseEntered(MouseEvent e) {
								if (isEnabled())
									setBackground(kMouseoverButtonBackground);
							}
							//	A second MouseListener should be added to implement mouseClicked()
						});
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}
	
	public BigButton(String buttonNameString) {
		this(buttonNameString, 24, 15);
	}
	
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		setBackground(kStdButtonBackground);
	}
	
	public void paintComponent(Graphics g) {
		Dimension arcs = new Dimension(topBottomMargin, topBottomMargin); //	Border corners arcs {width,height}
		int width = getWidth();
		int height = getHeight();
		Graphics2D graphics = (Graphics2D) g;
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		Color c = g.getColor();
		g.setColor(getBackground());
		g.fillRoundRect(0, 0, width-1, height-1, arcs.width, arcs.height);
		g.setColor(c);
		
		super.paintComponent(g);
	
		g.setColor(kButtonBorderColor);
		g.drawRoundRect(0, 0, width-1, height-1, arcs.width, arcs.height);
	}
}
