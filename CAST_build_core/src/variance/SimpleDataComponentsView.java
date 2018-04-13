package variance;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;
import images.*;

import ssq.*;


public class SimpleDataComponentsView extends DataWithComponentView {
	
	static final private int kNearParamBorder = 20;
	static final private int kAwayParamBorder = 45;
	
	static final private String muFile = "anova/muBlack.gif";
	static final private String xBarFile = "anova/xBarOrange.gif";
	static final private int kMaxWait = 30000;		//		30 seconds
	static final private int kMuWidth = 10;
	static final private int kMuAboveCentre = 7;
	
	static final private Color kTargetColor = new Color(0x333333);
	static final private Color kSampleMean = new Color(0xFF9900);
	
	private double target;
	private Image muImage, xBarImage;
	
	public SimpleDataComponentsView(DataSet theData, XApplet applet, VertAxis yAxis, String xKey,
															String yKey, String modelKey, double target, int initialComponentType) {
		super(theData, applet, null, yAxis, xKey, yKey, null, modelKey, initialComponentType);
		this.target = target;
		
		MediaTracker tracker = new MediaTracker(this);
			muImage = CoreImageReader.getImage(muFile);
			xBarImage = CoreImageReader.getImage(xBarFile);
		tracker.addImage(muImage, 0);
		tracker.addImage(xBarImage, 0);
		try {
			tracker.waitForAll(kMaxWait);
		} catch (InterruptedException e) {
		}
	}
	
	protected int drawOverallMean(Graphics g, String yKey, Color meanColor) {
		int meanOnScreen = super.drawOverallMean(g, yKey, kSampleMean);
		
		g.setColor(Color.white);
		g.fillRect(0, 0, kAwayParamBorder, getSize().height);
		g.fillRect(getSize().width - kNearParamBorder, 0, kNearParamBorder, getSize().height);
		
		g.drawImage(xBarImage, getSize().width - 5 - kMuWidth, meanOnScreen - kMuAboveCentre, this);
		
		int targetPos = yAxis.numValToRawPosition(target);
		int targetOnScreen = translateToScreen(0, targetPos, null).y;
		g.setColor(kTargetColor);
		g.drawLine(kNearParamBorder, targetOnScreen, getSize().width - kAwayParamBorder, targetOnScreen);
		
		g.drawImage(muImage, 5, targetOnScreen - kMuAboveCentre, this);
		
		return meanOnScreen;
	}
	
	protected void drawOneComponent(Graphics g, int meanOnScreen, Point dataPoint, Variable xVar,
															Value x, int xPos, CoreModelVariable lsFit, Point targetPoint) {
		if (componentDisplay == SimpleComponentVariable.FROM_TARGET
																	|| componentDisplay == SimpleComponentVariable.MEAN) {
			int targetPos = yAxis.numValToRawPosition(target);
			targetPoint  = translateToScreen(xPos, targetPos, targetPoint);
		}
		g.setColor(SimpleComponentVariable.kComponentColor[componentDisplay]);
		switch (componentDisplay) {
			case SimpleComponentVariable.FROM_TARGET:
				g.drawLine(dataPoint.x, dataPoint.y, dataPoint.x, targetPoint.y);
				break;
			case SimpleComponentVariable.MEAN:
				g.drawLine(dataPoint.x, targetPoint.y, dataPoint.x, meanOnScreen);
				break;
			case SimpleComponentVariable.FROM_MEAN:
				g.drawLine(dataPoint.x, dataPoint.y, dataPoint.x, meanOnScreen);
				break;
			default:
				break;
		}
	}
	
	protected void drawAllComponents(Graphics g, int meanOnScreen, CoreModelVariable lsFit,
																int selectedIndex) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		Variable xVar = (Variable)getVariable(xKey);
		
		Value x = xVar.valueAt(selectedIndex);
		int xPos = getXPos(x, xVar, selectedIndex);
		
		double y = yVar.doubleValueAt(selectedIndex);
		int yPos = yAxis.numValToRawPosition(y);
		Point dataPoint  = translateToScreen(xPos, yPos, null);
		
		int targetPos = yAxis.numValToRawPosition(target);
		Point targetPoint  = translateToScreen(xPos, targetPos, null);
		
		g.setColor(SimpleComponentVariable.kFromTargetColor);
		drawArrow(g, dataPoint.x - kLineOffset, dataPoint.y, targetPoint.y);
		
		g.setColor(SimpleComponentVariable.kMeanColor);
		drawArrow(g, dataPoint.x + kLineOffset, meanOnScreen, targetPoint.y);
		
		g.setColor(SimpleComponentVariable.kFromMeanColor);
		drawArrow(g, dataPoint.x, dataPoint.y, meanOnScreen);
	}
}
	
