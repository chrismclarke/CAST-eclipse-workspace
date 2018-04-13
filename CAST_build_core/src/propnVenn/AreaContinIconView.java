package propnVenn;

import java.awt.*;

import dataView.*;
import axis.*;


public class AreaContinIconView extends AreaContin2View {
//	static public final String AREA_CONTIN_ICON = "areaContinIcon";
	
	private int gridWidth, gridHeight;
	private Rectangle staticPoints[];
	private GridIterator dynStartPoints[];
	private GridIterator dynEndPoints[];
	private Image itemImage[];
	
//	private int iconXGap, iconYGap, icon0Left, icon0Top;
	
	private boolean shadeAreas = false;
//	private boolean drawCounts = true;
	
	public AreaContinIconView(DataSet theData, XApplet applet, VertAxis yAxis, HorizAxis xAxis, String yKey,
						String xKey, int gridWidth, int gridHeight, Rectangle staticPoints[],
						GridIterator dynStartPoints[], GridIterator dynEndPoints[], Image itemImage[]) {
		super(theData, applet, yAxis, xAxis, yKey, xKey, CAN_SELECT, Y_MARGIN);
		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;
		this.staticPoints = staticPoints;
		this.dynStartPoints = dynStartPoints;
		this.dynEndPoints = dynEndPoints;
		this.itemImage = itemImage;
	}
	
	public void setShadeAreas(boolean shadeAreas) {
		this.shadeAreas = shadeAreas;
		repaint();
	}
	
	private Point getIconTopLeft(double xp, double yp, Point p) {
		int xPos = xAxis.numValToRawPosition(xp);
		int yPos = yAxis.numValToRawPosition(yp);
		
		p = translateToScreen(xPos, yPos, p);
		p.x -= ItemImages.kItemWidth / 2;
		p.y -= ItemImages.kItemHeight / 2;
		return p;
	}
	
	private void drawIcons(Graphics g, double framePropn) {
		Point p = null;
		for (int i=0 ; i<4 ; i++) {
			for (int x=staticPoints[i].x ; x<staticPoints[i].x+staticPoints[i].width ; x++)
				for (int y=staticPoints[i].y ; y<staticPoints[i].y+staticPoints[i].height ; y++) {
					p = getIconTopLeft((x + 0.5) / gridWidth, (y + 0.5) / gridHeight, p);
					g.drawImage(itemImage[i], p.x, p.y, this);
				}
			
//			boolean morePts = true;
			GridIterator startIter = dynStartPoints[i];
			GridIterator endIter = dynEndPoints[i];
			for (startIter.reset(),endIter.reset()
													; !startIter.finished() && !endIter.finished()
													; startIter.nextItem(),endIter.nextItem()) {
				double xp = (startIter.x * framePropn + endIter.x * (1 - framePropn) + 0.5) / gridWidth;
				double yp = (startIter.y * framePropn + endIter.y * (1 - framePropn) + 0.5) / gridHeight;
				p = getIconTopLeft(xp, yp, p);
				g.drawImage(itemImage[i], p.x, p.y, this);
			}
		}
	}
	
	private Point probToPos(double x, double y, Point p) {
			int xPos = xAxis.numValToRawPosition(x);
			int yPos = yAxis.numValToRawPosition(y);
			return translateToScreen(xPos, yPos, p);
	}
	
	public void paintView(Graphics g) {
		initialise();
		
		double framePropn = getFramePropn();
		
		CatVariableInterface xVar = (CatVariableInterface)getVariable(xKey);
		CatVariableInterface yVar = (CatVariableInterface)getVariable(yKey);
		
		if (shadeAreas) {
			theDrawer.drawDiagram(selectedX, selectedY, framePropn, g);
			g.setColor(getForeground());
			theDrawer.drawCounts((CatVariable)xVar, (CatVariable)yVar, framePropn, g, getApplet());
		}
		else {
			Point p0 = probToPos(0.0, 1.0, null);
			Point p1 = probToPos(1.0, 0.0, null);
			
			g.setColor(Color.white);
			g.fillRect(p0.x, p0.y, p1.x - p0.x, p1.y - p0.y);
			
			if (framePropn == 0.0) {
				if (selectedX >= 0 && selectedY >= 0) {
					g.setColor(Color.yellow);
					double leftP = (selectedX == 0) ? 0.0 : getXConditProb(0, selectedY);
					double rightP = (selectedX == 0) ? getXConditProb(0, selectedY) : 1.0;
					double bottomP = (selectedY == 0) ? 0.0 : getYMarginProb(0);
					double topP = (selectedY == 0) ? getYMarginProb(0) : 1.0;
					p0 = probToPos(leftP, topP, p0);
					p1 = probToPos(rightP, bottomP, p1);
					g.fillRect(p0.x, p0.y, p1.x - p0.x, p1.y - p0.y);
				}
				
				g.setColor(Color.lightGray);
				
				p0 = probToPos(0.0, getYMarginProb(0), p0);
				p1 = probToPos(1.0, getYMarginProb(0), p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
				
				p0 = probToPos(getXConditProb(0, 0), 0.0, p0);
				p1 = probToPos(getXConditProb(0, 0), getYMarginProb(0), p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
				
				p0 = probToPos(getXConditProb(0, 1), 1.0, p0);
				p1 = probToPos(getXConditProb(0, 1), getYMarginProb(0), p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			}
			else if (framePropn == 1.0) {
				if (selectedX >= 0 && selectedY >= 0) {
					g.setColor(Color.yellow);
					double leftP = (selectedX == 0) ? 0.0 : getXMarginProb(0);
					double rightP = (selectedX == 0) ? getXMarginProb(0) : 1.0;
					double bottomP = (selectedY == 0) ? 0.0 : getYConditProb(0, selectedX);
					double topP = (selectedY == 0) ? getYConditProb(0, selectedX) : 1.0;
					p0 = probToPos(leftP, topP, p0);
					p1 = probToPos(rightP, bottomP, p1);
					g.fillRect(p0.x, p0.y, p1.x - p0.x, p1.y - p0.y);
				}
				
				g.setColor(Color.lightGray);
				
				p0 = probToPos(getXMarginProb(0), 1.0, p0);
				p1 = probToPos(getXMarginProb(0), 0.0, p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
				
				p0 = probToPos(0.0, getYConditProb(0, 0), p0);
				p1 = probToPos(getXMarginProb(0), getYConditProb(0, 0), p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
				
				p0 = probToPos(1.0, getYConditProb(0, 1), p0);
				p1 = probToPos(getXMarginProb(0), getYConditProb(0, 1), p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			}
			drawIcons(g, framePropn);
		}
		
		Rectangle selectedRect = theDrawer.getBoundingRect(selectedX, selectedY, framePropn);
		
		topDrawer.drawTopLabels(selectedX, selectedY, selectedRect, xVar, yVar, theDrawer, framePropn, g);
		rightDrawer.drawRightLabels(selectedX, selectedY, selectedRect, xVar, yVar, theDrawer, framePropn, g);
		
		if (getCurrentFrame() == kFinalFrame && theChoice != null)
			theChoice.endAnimation();
	}
}
