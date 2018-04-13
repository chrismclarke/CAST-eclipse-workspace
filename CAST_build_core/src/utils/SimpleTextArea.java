package utils;

import java.awt.*;

import dataView.*;


public class SimpleTextArea extends XPanel {
							//		must be laid out with a LayoutManager that will fix the width
	static final private int kLeftRightBorder = 4;
	static final private int kTopBottomBorder = 2;
	
	private String theText = "";
	private int noOfLines = 0;
	
	private boolean initialised = false;
	private int ascent, descent, leading;
	
	private int leftRightBorder = kLeftRightBorder;
	private int topBottomBorder = kTopBottomBorder;
	
	public SimpleTextArea() {
	}
	
	public SimpleTextArea(int noOfLines) {
		this.noOfLines = noOfLines;
	}
	
	public void setBorders(int leftRightBorder, int topBottomBorder) {
		this.leftRightBorder = leftRightBorder;
		this.topBottomBorder = topBottomBorder;
	}
	
	public void setText(String newText) {
		this.theText = newText;
		repaint();
	}

//-----------------------------------------------------------------------------------
	
	private void initialise(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		leading = fm.getLeading();
		descent = fm.getDescent();
		initialised = true;
	}
	
	public Dimension getMinimumSize() {
		Graphics g = getGraphics();
		g.setFont(getFont());
		initialise(g);
		return new Dimension(20, (ascent + descent + leading) * noOfLines + 2 * (topBottomBorder + 3));
	}
							//		must be laid out with a LayoutManager that will fix the width
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

//-----------------------------------------------------------------------------------
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (getSize().width <= 0 || getSize().height <= 0)		//	not laid out yet
			return;
			
		corePaint(g);
	}
	
	public void corePaint(Graphics g) {
		if (!initialised)
			initialise(g);
			
		g.setColor(getBackground());
		g.fillRect(3, 3, getSize().width - 6, getSize().height - 6);
		g.setColor(Color.black);
		g.drawRect(2, 2, getSize().width - 5, getSize().height - 5);
		
		g.setColor(Color.lightGray);
		g.drawLine(0, 0, getSize().width - 1, 0);
		g.drawLine(0, 0, 0, getSize().height - 1);
		g.drawLine(1, 1, getSize().width - 2, 1);
		g.drawLine(1, 1, 1, getSize().height - 2);
		
		g.setColor(Color.white);
		g.drawLine(1, getSize().height - 1, getSize().width - 1, getSize().height - 1);
		g.drawLine(getSize().width - 1, 1, getSize().width - 1, getSize().height - 1);
		g.drawLine(2, getSize().height - 2, getSize().width - 1, getSize().height - 2);
		g.drawLine(getSize().width - 2, 2, getSize().width - 2, getSize().height - 1);
		
		g.setColor(getForeground());
		int vertPos = ascent + topBottomBorder + 3;
		
		StringLineTokenizer slt = new StringLineTokenizer(theText, g, getSize().width - 2 * (leftRightBorder + 3));
		while (slt.hasMoreLines()) {
			g.drawString(slt.nextLine(), leftRightBorder + 3, vertPos);
			vertPos += (ascent + descent + leading);
		}
	}
}