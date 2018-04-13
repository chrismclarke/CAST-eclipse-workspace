package propnVenn;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import axis.*;

import contin.*;


public class AreaContin3View extends AreaContinCoreView {
//	static public final String AREA_CONTIN3 = "areaContin3";
	
//	static final private Color kDarkBackground = new Color(0xD9AE82);
	static final private Color kDarkGray = new Color(0x222222);
	static final public Color[] kDarkFillColor = {new Color(0xA3A3D9), new Color(0xD9D982), new Color(0xFFCCCC), new Color(0xDDDDDD)};
	
	static final private int kLabelGap = 7;
	
	protected String zKey;
	
	private boolean adjustedBorder = false;
	private double pz0;
	private int maxLabelLength;
	
	protected int selectedZ = -1;
	
	protected PropnVennDrawer zDrawer[];
	protected TopVennDrawer topDrawer;
	protected RightVennDrawer rightDrawer[];
	
	private Color darkBackground;
	
	public AreaContin3View(DataSet theData, XApplet applet, VertAxis yAxis, HorizAxis xAxis, String yKey,
						String xKey, String zKey, boolean yMargin) {
		super(theData, applet, yAxis, xAxis, yKey, xKey, CAN_SELECT, yMargin);
		
		darkBackground = darkenColor(applet.getBackground(), 0.1);
		
		this.zKey = zKey;
		
		CatVariable zVar = (CatVariable)getVariable(zKey);
		int zMarginCount[] = zVar.getCounts();
		int nValues = zVar.noOfValues();
		pz0 = zMarginCount[0] / (double)nValues;
		
		topDrawer = new TopVennDrawer(xAxis, this, false);
		
		zDrawer = new PropnVennDrawer[2];
		rightDrawer = new RightVennDrawer[2];
		
		zDrawer[0] = new PropnVennDrawer(0.0, pz0, xAxis, yAxis, this);
		zDrawer[0].setFillColors(kDarkFillColor);
		rightDrawer[0] = new RightVennDrawer(0.0, pz0, xAxis, yAxis, false, this);
		
		zDrawer[1] = new PropnVennDrawer(pz0, 1.0, xAxis, yAxis, this);
		rightDrawer[1] = new RightVennDrawer(pz0, 1.0, xAxis, yAxis, false, this);
	}
	
	public int getSelectedZ() {
		return selectedZ;
	}
	
//	private void printCounts(int value[], String title) {
//		System.out.println("\n" + title);
//		for (int i=0 ; i<value.length ; i++)
//			System.out.print(value[i] + "  ");
//		System.out.println("\n");
//	}
//	
//	private void printCounts(int value[][], String title) {
//		System.out.println("\n" + title);
//		for (int i=0 ; i<value.length ; i++) {
//			for (int j=0 ; j<value[i].length ; j++)
//				System.out.print(value[i][j] + "  ");
//			System.out.println("");
//		}
//	}
//	
//	private void printCounts(int value[][][], String title) {
//		System.out.println("\n" + title);
//		for (int i=0 ; i<value.length ; i++) {
//			for (int j=0 ; j<value[i].length ; j++) {
//				for (int k=0 ; k<value[i][j].length ; k++)
//					System.out.print(value[i][j][k] + "  ");
//				System.out.println("");
//			}
//			System.out.println("");
//		}
//	}
//	
//	private void printValues(double value[], String title) {
//		System.out.println("\n" + title);
//		for (int i=0 ; i<value.length ; i++)
//			System.out.print(value[i] + "  ");
//		System.out.println("\n");
//	}
//	
//	private void printValues(double value[][], String title) {
//		System.out.println("\n" + title);
//		for (int i=0 ; i<value.length ; i++) {
//			for (int j=0 ; j<value[i].length ; j++)
//				System.out.print(value[i][j] + "  ");
//			System.out.println("");
//		}
//	}
	
	protected boolean initialise() {
		if (!super.initialise())
			return false;
		
		CatVariable yVar = (CatVariable)getVariable(yKey);
		CatVariable xVar = (CatVariable)getVariable(xKey);
		CatVariable zVar = (CatVariable)getVariable(zKey);
		
		int countZXY[][][] = ((CatVariable)zVar).getCounts(xVar, yVar);
		int countZX[][] = ((CatVariable)zVar).getCounts(xVar);
		int zMarginCount[] = zVar.getCounts();
		
//		printCounts(zMarginCount, "z MarginCount");
//		printCounts(countZX, "z-x MarginCount");
//		printCounts(countZXY, "z-x-y MarginCount");
		
		int nXCats = xVar.noOfCategories();
		int nYCats = yVar.noOfCategories();
		
		for (int k=0 ; k<2 ; k++) {
			double xMarginProb[] = new double[nXCats];
			double yConditProb[][] = new double[nXCats][nYCats];
			double nTotal = zMarginCount[k];
			for (int i=0 ; i<nXCats ; i++)
				xMarginProb[i] = countZX[k][i] / nTotal;
			
			for (int i=0 ; i<nXCats ; i++)
				for (int j=0 ; j<nYCats ; j++)
					yConditProb[i][j] = countZXY[k][i][j] / (double)countZX[k][i];
			
//			printValues(xMarginProb, "Z=" + k + " marginal probs for X");
//			printValues(yConditProb, "Z=" + k + " conditional probs for X");
			
			zDrawer[k].initialise(xMarginProb, yConditProb);
		}
		return true;
	}
	
	private int getMaxLabelLength() {
		CatVariable zVar = (CatVariable)getVariable(zKey);
		Graphics g = getGraphics();
		g.setFont(getApplet().getBigFont());
		maxLabelLength = 0;
		for (int i=0 ; i<zVar.noOfCategories() ; i++)
		 	maxLabelLength = Math.max(maxLabelLength, zVar.getLabel(i).stringWidth(g));
		 return maxLabelLength;
	}
	
	
	public Insets getViewBorder() {
		if (adjustedBorder)
			return super.getViewBorder();
		else {
			Insets border = super.getViewBorder();
			CatVariableInterface yVar = (CatVariableInterface)getVariable(yKey);
			CatVariableInterface xVar = (CatVariableInterface)getVariable(xKey);
			
			border.top += topDrawer.topLabelBorder(xVar);
			border.right += rightDrawer[0].rightLabelBorder(xVar, yVar, border);
			
			border.right += getMaxLabelLength() + kLabelGap;
			
			adjustedBorder = true;
			return border;
		}
	}
	
	private void drawTitles(Graphics g) {
		CatVariable zVar = (CatVariable)getVariable(zKey);
		Font oldFont = g.getFont();
		g.setFont(getApplet().getBigFont());
		FontMetrics fm = g.getFontMetrics();
		int baselineOffset = (fm.getAscent() - fm.getDescent()) / 2;
		int labelCenterHoriz = getSize().width - 2 - maxLabelLength / 2;
		
		g.setColor(Color.black);
		int vertPos = yAxis.numValToRawPosition(pz0 / 2) + baselineOffset;
		Point pt = translateToScreen(0, vertPos, null);
		zVar.getLabel(0).drawCentred(g, labelCenterHoriz, pt.y);
		
		g.setColor(kDarkGray);
		vertPos = yAxis.numValToRawPosition((1.0 + pz0) / 2) + baselineOffset;
		pt = translateToScreen(0, vertPos, pt);
		zVar.getLabel(1).drawCentred(g, labelCenterHoriz, pt.y);
		
		g.setFont(oldFont);
	}
	
	private void shadeZ0(Graphics g) {
		int vertPos = yAxis.numValToRawPosition(pz0);
		Point pt = translateToScreen(0, vertPos, null);
		g.setColor(darkBackground);
		
		g.fillRect(0, pt.y, getSize().width, getSize().height - pt.y);
		
		g.setColor(getForeground());
	}
	
	public void paintView(Graphics g) {
		initialise();
		
		double framePropn = getFramePropn();
		
		CatVariableInterface xVar = (CatVariableInterface)getVariable(xKey);
		CatVariableInterface yVar = (CatVariableInterface)getVariable(yKey);
		
		shadeZ0(g);
		
		for (int k=0 ; k<2 ; k++) {
				zDrawer[k].drawDiagram(selectedX, selectedY, framePropn, g);
				rightDrawer[k].drawRightLabels(-1, -1, null, xVar, yVar, zDrawer[k], framePropn, g);
			}
		
		topDrawer.drawTopLabels(-1, -1, null, xVar, yVar, zDrawer[0], framePropn, g);	
		
		drawTitles(g);
		
		g.setFont(getApplet().getBigBoldFont());
		g.setColor(Color.red);
		for (int k=0 ; k<2 ; k++)
			zDrawer[k].drawConditionalArrow(selectedX, selectedY, framePropn, g);
		
		if (getCurrentFrame() == kFinalFrame && theChoice != null)
			theChoice.endAnimation();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}

	public void mousePressed(MouseEvent e) {
		if (getCurrentFrame() == kFinalFrame)
			super.mousePressed(e);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (getCurrentFrame() != kFinalFrame)
			return null;
		
		CatVariable yVar = (CatVariable)getVariable(yKey);
		CatVariable xVar = (CatVariable)getVariable(xKey);
		CatVariable zVar = (CatVariable)getVariable(zKey);
		
		try {
			Point hitPos = translateFromScreen(x, y, null);
			double yTarget = yAxis.positionToNumVal(hitPos.y);
			double xTarget = xAxis.positionToNumVal(hitPos.x);
			
			int zMarginCount[] = zVar.getCounts();
			double nValues = zVar.noOfValues();
			double pz0 = zMarginCount[0] / (double)nValues;
			int zHit;
			if (yTarget < pz0) {
				zHit = 0;
				yTarget /= pz0;
			}
			else {
				zHit = 1;
				yTarget = (yTarget - pz0) / (1.0 - pz0);
			}
			
			ContinCatInfo catInfo = (ContinCatInfo)zDrawer[zHit].findHit(yTarget, xTarget, xVar, yVar, marginForY);
			if (catInfo != null)
				catInfo.zIndex = zHit;
			return catInfo;
		} catch (AxisException e) {
		}
		
		return null;
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		ContinCatInfo catInfo = (ContinCatInfo)startInfo;
		int newZ = (catInfo == null) ? -1 : catInfo.zIndex;
		int newX = (catInfo == null) ? -1 : catInfo.xIndex;
		int newY = (catInfo == null) ? -1 : catInfo.yIndex;
		if (newX != selectedX || newY != selectedY || newZ != selectedZ) {
			selectedZ = newZ;
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
