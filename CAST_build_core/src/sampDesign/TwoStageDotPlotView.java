package sampDesign;

import java.awt.*;

import dataView.*;
import axis.*;


public class TwoStageDotPlotView extends StratifiedSampleView {
//	static public final String TWO_STAGE_DOT_PLOT = "twoStageDot";
	
	static final private Color kBackgroundColor = new Color(0xEEEEFF);
	
	public TwoStageDotPlotView(DataSet theData, XApplet applet, HorizAxis stratumAxis,
																																	VertAxis numAxis, String yKey) {
		super(theData, applet, stratumAxis, numAxis, yKey, null);
	}
	
	protected int getNoOfStrata(String yKey) {
		Sample2StageVariable yVar = (Sample2StageVariable)getVariable(yKey);
		return yVar.getNPrimaryUnits();
	}
	
	protected int getPopnSize(String yKey) {
		Sample2StageVariable yVar = (Sample2StageVariable)getVariable(yKey);
		return yVar.noOfValues();
	}
	
	protected int stratumToPos(int stratum) throws AxisException {
		return stratumAxis.numValToPosition(stratum);
	}
	
	public void paintView(Graphics g) {
		Sample2StageVariable yVar = (Sample2StageVariable)getData().getVariable(yKey);
		Point thePoint = null;
		
		checkJittering();
		
		int nSecondary = yVar.getNSecondaryUnits();
		int nPrimary = yVar.getNPrimaryUnits();
		
		g.setColor(kBackgroundColor);
		for (int i=0 ; i<nPrimary ; i+=2) {
			int horizPos1 = stratumAxis.numValToRawPosition(i - 0.5);
			int horizPos2 = stratumAxis.numValToRawPosition(i + 0.5);
			thePoint = translateToScreen(horizPos1, 0, thePoint);
			int left = thePoint.x;
			thePoint = translateToScreen(horizPos2, 0, thePoint);
			int right = thePoint.x;
			g.fillRect(left, 0, right - left, getSize().height);
		}
		
		g.setColor(Color.lightGray);
		for (int index=0 ; index<nPrimary*nSecondary ; index++) {
			NumValue popValue = (NumValue)yVar.popnValueAt(index);
			thePoint = getScreenPoint(index, popValue, index / nSecondary, thePoint);
			if (thePoint != null)
				drawCross(g, thePoint);
		}
		
		g.setColor(kSampleColor);
		for (int index=0 ; index<nPrimary*nSecondary ; index++)
			if (yVar.secondaryUnitSampled(index)) {
				NumValue popValue = (NumValue)yVar.popnValueAt(index);
				thePoint = getScreenPoint(index, popValue, index / nSecondary, thePoint);
				if (thePoint != null)
					drawBlob(g, thePoint);
			}
	}
}
	
