package utils;

import java.awt.*;

import dataView.*;

public class ArrowCanvas extends XPanel {
	static final public int RIGHT = 0;
	static final public int DOWN = 1;
	
	private int baseLength, halfBaseWidth, headLength, halfHeadWidth;
//	private Color arrowColor;
	private int direction;
	
	private int xPos[] = new int[8];
	private int yPos[] = new int[8];
	
	public ArrowCanvas(int baseLength, int halfBaseWidth, int headLength, int halfHeadWidth,
																																						int direction) {
		this.baseLength = baseLength;
		this.halfBaseWidth = halfBaseWidth;
		this.headLength = headLength;
		this.halfHeadWidth = halfHeadWidth;
		this.direction = direction;
		
		setOpaque(false);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	public Dimension getMinimumSize() {
		int x = baseLength + headLength;
		int y = 2 * halfHeadWidth;
		return (direction == RIGHT) ? new Dimension(x, y) : new Dimension(y, x);
	}
	
	public void paintComponent(Graphics g) {
		Graphics2DActions.setAliasing(g, Graphics2DActions.ANTI_ALIASING_ON);
		super.paintComponent(g);
		
		xPos[0] = xPos[6] = xPos[7] = 0;
		xPos[1] = xPos[2] = xPos[4] = xPos[5] = baseLength;
		xPos[3] = baseLength + headLength;
		
		yPos[0] = yPos[1] = yPos[7] = halfHeadWidth - halfBaseWidth;
		yPos[2] = 0;
		yPos[3] = halfHeadWidth;
		yPos[4] = 2 * halfHeadWidth;
		yPos[5] = yPos[6] = halfHeadWidth + halfBaseWidth;
		
		if (direction == DOWN) {
			int temp[] = xPos;
			xPos = yPos;
			yPos = temp;
		}
		
		Dimension arrowSize = getMinimumSize();
		int xOffset = (getSize().width - arrowSize.width) / 2;
		int yOffset = (getSize().height - arrowSize.height) / 2;
		for (int i=0 ; i<8 ; i++) {
			xPos[i] += xOffset;
			yPos[i] += yOffset;
		}
		
		g.fillPolygon(xPos, yPos, 8);
	}
}