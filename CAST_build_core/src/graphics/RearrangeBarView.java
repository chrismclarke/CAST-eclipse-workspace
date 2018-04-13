package graphics;

import java.awt.*;

import dataView.*;


public class RearrangeBarView extends DataView {
//	static final public String REARRANGE_BAR_VIEW = "rearrangeBarView";
	
	static final private int kEndFrame = 80;
	static final private int kFramesPerSec = 20;
	
	static final private int kMinHalfBarWidth = 4;
	static final private int kLabelVertGap = 4;
	
	private String yKey, xKey, groupKey;
	private GroupedHorizAxis xAxis;
	private GroupedVertAxis yAxis;
	private Color groupColor[];
	
	private int halfBarWidth;
	
	public RearrangeBarView(DataSet theData, XApplet applet,
															String yKey, String xKey, String groupKey, GroupedHorizAxis xAxis,
															GroupedVertAxis yAxis, Color[] groupColor) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.yKey = yKey;
		this.xKey = xKey;
		this.groupKey = groupKey;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.groupColor = groupColor;
	}
	
	public void setArrangeInRow(boolean arrangeInRow) {
		if (arrangeInRow)
			animateFrames(0, kEndFrame, kFramesPerSec, null);
		else
			animateFrames(kEndFrame, -kEndFrame, kFramesPerSec, null);
	}
	
	private void setHalfBarWidth() {
		CatVariable groupVar = (CatVariable)getVariable(groupKey);
		CatVariable xVar = (CatVariable)getVariable(xKey);
		int nGroups = groupVar.noOfCategories();
		int nX = xVar.noOfCategories();
		
		int multiRowWidth = xAxis.axisLength / (nX * 4);
		int inRowWidth = Math.max(xAxis.axisLength / (nX * nGroups * 4), kMinHalfBarWidth);
		
		int currentFrame = getCurrentFrame();
		halfBarWidth = (currentFrame * inRowWidth + (kEndFrame - currentFrame) * multiRowWidth) / kEndFrame;
	}
	
	private Point barTopLeft(int catIndex, int group, double y, Point p) {
		int currentFrame = getCurrentFrame();
		
		int inRowX = xAxis.catValToGroupPosition(catIndex, group);
		int multiRowX = xAxis.catValToPosition(catIndex);
		int x = (currentFrame * inRowX + (kEndFrame - currentFrame) * multiRowX) / kEndFrame;
		
		int inRowTop = yAxis.numValToRawPosition(y);
		int multiRowTop = yAxis.numValToGroupPosition(y, group);
		int top = (currentFrame * inRowTop + (kEndFrame - currentFrame) * multiRowTop) / kEndFrame;
		
		return translateToScreen(x - halfBarWidth, top, p);
	}
	
	private Point barBottomRight(int catIndex, int group, Point p) {
		int currentFrame = getCurrentFrame();
		
		int inRowX = xAxis.catValToGroupPosition(catIndex, group);
		int multiRowX = xAxis.catValToPosition(catIndex);
		int x = (currentFrame * inRowX + (kEndFrame - currentFrame) * multiRowX) / kEndFrame;
		
		int inRowBottom = 0;
		int multiRowBottom = yAxis.groupAxisPosition(group);
		int bottom = (currentFrame * inRowBottom + (kEndFrame - currentFrame) * multiRowBottom) / kEndFrame;
		
		return translateToScreen(x + halfBarWidth, bottom, p);
	}
	
	private Point groupLabelTop(int group, int noOfCats, Point p) {
		int currentFrame = getCurrentFrame();
		
		int inRowX = xAxis.catValToGroupPosition(noOfCats / 2, group);
		int multiRowX = xAxis.catValToPosition(noOfCats / 2);
		int x = (currentFrame * inRowX + (kEndFrame - currentFrame) * multiRowX) / kEndFrame;
		
		int inRowTop = yAxis.numValToRawPosition(yAxis.maxOnAxis);
		int multiRowTop = yAxis.numValToGroupPosition(yAxis.maxOnAxis, group);
		int top = (currentFrame * inRowTop + (kEndFrame - currentFrame) * multiRowTop) / kEndFrame;
		
		return translateToScreen(x, top, p);
	}
	
	private Color axisColor() {
		int currentFrame = getCurrentFrame();
		int colorIndex = currentFrame * 255 / kEndFrame;
		return new Color(colorIndex, colorIndex, colorIndex);
	}
	
	public void paintView(Graphics g) {
		Point p1 = null;
		CatVariable groupVar = (CatVariable)getVariable(groupKey);
		CatVariable xVar = (CatVariable)getVariable(xKey);
		int nGroups = groupVar.noOfCategories();
		int nX = xVar.noOfCategories();
		
		if (getCurrentFrame() < kEndFrame) {
			g.setColor(axisColor());
			for (int i=0 ; i<nGroups ; i++) {
				int vertPos = yAxis.numValToGroupPosition(yAxis.minOnAxis, i);
				p1 = translateToScreen(0, vertPos, p1);
				g.drawLine(0, p1.y + 1, getSize().width, p1.y + 1);
			}
		}
		
		int ascent = g.getFontMetrics().getAscent();
		for (int i=0 ; i<nGroups ; i++) {
			Value groupName = groupVar.getLabel(i);
			p1 = groupLabelTop(i, nX, p1);
			g.setColor(groupColor[i]);
			groupName.drawCentred(g, p1.x, p1.y + ascent + kLabelVertGap);
		}
		
		Point p2 = null;
		NumVariable yVar = (NumVariable)getVariable(yKey);
		setHalfBarWidth();
		
		for (int i=0 ; i<yVar.noOfValues() ; i++) {
			int group = groupVar.getItemCategory(i);
			int xIndex = xVar.getItemCategory(i);
			double y = yVar.doubleValueAt(i);
			
			p1 = barTopLeft(xIndex, group, y, p1);
			p2 = barBottomRight(xIndex, group, p2);
			g.setColor(groupColor[group]);
			g.fillRect(p1.x, p1.y, (p2.x - p1.x), (p2.y - p1.y));
			g.drawRect(p1.x, p1.y, (p2.x - p1.x), (p2.y - p1.y));
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}