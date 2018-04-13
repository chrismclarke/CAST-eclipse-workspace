package twoFactor;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;


public class EffectDiagramView extends CoreEffectDiagramView {
//	static final public String EFFECT_DIAGRAM = "effectDiagram";
	
	public EffectDiagramView(DataSet theData, XApplet applet, String yKey, String[] xKey, String modelKey,
									HorizAxis horizAxis, VertAxis yAxis, int horizIndex) {
		super(theData, applet, yKey, xKey, modelKey, horizAxis, yAxis, horizIndex);
	}
	
	public void paintView(Graphics g) {
		CoreModelVariable model = (CoreModelVariable)getVariable(modelKey);
		
		boolean hasInteraction = false;
		if (model instanceof TwoFactorModel && ((TwoFactorModel)model).getHasInteraction())
			hasInteraction = true;
		CatVariable horizVar = (CatVariable)getVariable(xKey[horizIndex]);
		int nHoriz = horizVar.noOfCategories();
		
		double mean[] = new double[nHoriz];
		
		if (hasInteraction) {
			TwoFactorModel twoFactorModel = (TwoFactorModel)model;
			CatVariable otherVar = (CatVariable)getVariable(xKey[1 - horizIndex]);
			int nOther = otherVar.noOfCategories();
			double rightMean[] = new double[nOther];
			Value otherLabel[] = new Value[nOther];
			for (int j=0 ; j<nOther ; j++) {
				for (int i=0 ; i<nHoriz ; i++)
					mean[i] = (horizIndex == 0) ? twoFactorModel.evaluateMean(i,j) : twoFactorModel.evaluateMean(j,i);
				Color lineColor = RotateDragFactorsView.kCatColor[j];
				drawMeans(g, mean, lineColor);
				rightMean[j] = mean[nHoriz - 1];
				otherLabel[j] = otherVar.getLabel(j);
			}
			
			drawOtherLabels(g, rightMean, RotateDragFactorsView.kCatColor, otherLabel);
		}
		else if (model instanceof TwoFactorModel) {
			CatVariable otherVar = (CatVariable)getVariable(xKey[1 - horizIndex]);
			int nOther = otherVar.noOfCategories();
			TwoFactorModel twoFactorModel = (TwoFactorModel)model;
			for (int i=0 ; i<nHoriz ; i++) {
				double sumMean = 0.0;
				for (int j=0 ; j<nOther ;j++)
					sumMean += (horizIndex == 0) ? twoFactorModel.evaluateMean(i,j) : twoFactorModel.evaluateMean(j,i);
				mean[i] = sumMean / nOther;
			}
			drawMeans(g, mean, Color.gray);
		}
		else {
			GroupsModelVariable oneFactorModel = (GroupsModelVariable)model;
			for (int i=0 ; i<nHoriz ; i++)
				mean[i] = oneFactorModel.getMean(i).toDouble();
			drawMeans(g, mean, Color.gray);
		}
	}
}