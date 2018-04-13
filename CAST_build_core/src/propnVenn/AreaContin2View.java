package propnVenn;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import axis.*;

import contin.*;


public class AreaContin2View extends AreaContinCoreView {
//	static public final String AREA_CONTIN2 = "areaContin2";
	
	private boolean adjustedBorder = false;
	
	protected PropnVennDrawer theDrawer;
	protected TopVennDrawer topDrawer;
	protected RightVennDrawer rightDrawer;
	
	public AreaContin2View(DataSet theData, XApplet applet, VertAxis yAxis, HorizAxis xAxis, String yKey,
					String xKey, boolean canSelect, boolean yMargin) {
		super(theData, applet, yAxis, xAxis, yKey, xKey, canSelect, yMargin);
		
		theDrawer = new PropnVennDrawer(0.0, 1.0, xAxis, yAxis, this);
		topDrawer = new TopVennDrawer(xAxis, this, true);
		rightDrawer = new RightVennDrawer(0.0, 1.0, xAxis, yAxis, canSelect, this);
	}
	
	protected boolean initialise() {
		if (!super.initialise())
			return false;
		
		CoreVariable yVar = getVariable(yKey);
		CoreVariable xVar = getVariable(xKey);
		
		if (yVar instanceof ContinResponseVariable && xVar instanceof CatDistnVariable) {
			ContinResponseVariable yVar2 = (ContinResponseVariable)yVar;
			CatDistnVariable xVar2 = (CatDistnVariable)xVar;
			
			theDrawer.initialise(xVar2.getProbs(), yVar2.getConditionalProbs());
		}
		else {
			CatVariable xVar2 = (CatVariable)xVar;
			int count[][] = ((CatVariable)yVar).getCounts(xVar2);
			int xMarginCount[] = xVar2.getCounts();
			int nXCats = xMarginCount.length;
			int nYCats = count.length;
			double nTotal = xVar2.noOfValues();
			double xMarginProb[] = new double[nXCats];
			for (int i=0 ; i<nXCats ; i++)
				xMarginProb[i] = xMarginCount[i] / nTotal;
			
			double yConditProb[][] = new double[nXCats][nYCats];
			for (int i=0 ; i<nXCats ; i++)
				for (int j=0 ; j<nYCats ; j++)
					yConditProb[i][j] = count[j][i] / (double)xMarginCount[i];
			theDrawer.initialise(xMarginProb, yConditProb);
		}
		return true;
	}
	
	public double getXMarginProb(int x) {
		initialise();
		return theDrawer.getXMarginProb(x);
	}
	
	public double getYMarginProb(int y) {
		initialise();
		return theDrawer.getYMarginProb(y);
	}
	
	public double getXConditProb(int x, int y) {
		initialise();
		return theDrawer.getXConditProb(x, y);
	}
	
	public double getYConditProb(int y, int x) {
		initialise();
		return theDrawer.getYConditProb(y, x);
	}
	
	
	public Insets getViewBorder() {
		if (adjustedBorder)
			return super.getViewBorder();
		else {
			Insets border = super.getViewBorder();
			CatVariableInterface yVar = (CatVariableInterface)getVariable(yKey);
			CatVariableInterface xVar = (CatVariableInterface)getVariable(xKey);
			
			border.top += topDrawer.topLabelBorder(xVar);
			border.right += rightDrawer.rightLabelBorder(xVar, yVar, border);
			adjustedBorder = true;
			return border;
		}
	}
	
	protected void drawVennExtras(CatVariableInterface yVar, CatVariableInterface xVar,
																Graphics g, double framePropn) {
		if (yVar instanceof CatVariable) {
			g.setColor(getForeground());
			theDrawer.drawCounts((CatVariable)xVar, (CatVariable)yVar, framePropn, g, getApplet());
		}
	}
	
	public void paintView(Graphics g) {
		initialise();
		
		double framePropn = getFramePropn();
		
		CatVariableInterface xVar = (CatVariableInterface)getVariable(xKey);
		CatVariableInterface yVar = (CatVariableInterface)getVariable(yKey);
		
		theDrawer.drawDiagram(selectedX, selectedY, framePropn, g);
		drawVennExtras(yVar, xVar, g, framePropn);
		
		Rectangle selectedRect = theDrawer.getBoundingRect(selectedX, selectedY, framePropn);
		
		topDrawer.drawTopLabels(selectedX, selectedY, selectedRect, xVar, yVar, theDrawer, framePropn, g);
		rightDrawer.drawRightLabels(selectedX, selectedY, selectedRect, xVar, yVar, theDrawer, framePropn, g);
		
		if (getCurrentFrame() == kFinalFrame && theChoice != null)
			theChoice.endAnimation();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return canSelect;
	}
	
	public void mousePressed(MouseEvent e) {
		if (getCurrentFrame() != kFinalFrame)			//		we don't want super.mousePressed()
			return;										//		to pause the animation
		super.mousePressed(e);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (getCurrentFrame() != kFinalFrame)
			return null;
		
		CatVariableInterface yVar = (CatVariableInterface)getVariable(yKey);
		CatVariableInterface xVar = (CatVariableInterface)getVariable(xKey);
		
		try {
			Point hitPos = translateFromScreen(x, y, null);
			double yTarget = yAxis.positionToNumVal(hitPos.y);
			double xTarget = xAxis.positionToNumVal(hitPos.x);
			
			return theDrawer.findHit(yTarget, xTarget, xVar, yVar, marginForY);
		} catch (AxisException e) {
		}
		
		return null;
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		ContinCatInfo catInfo = (ContinCatInfo)startInfo;
		int newX = (catInfo == null) ? -1 : catInfo.xIndex;
		int newY = (catInfo == null) ? -1 : catInfo.yIndex;
		if (newX != selectedX || newY != selectedY) {
			selectedX = newX;
			selectedY = newY;
			getData().variableChanged(xKey);
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
	}
}
