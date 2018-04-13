package exerciseBivarProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreVariables.*;
import random.*;
import models.*;
import exercise2.*;
import formula.*;

import exerciseBivar.*;


public class SlopeInterceptHarderApplet extends SlopeInterceptGuessApplet {
	static final private Color kTemplateBackground = new Color(0xFFE594);
	
	private RandomNormal xGenerator, yGenerator;
	
	private SlopeTemplatePanel slopeTemplate;
	private InterceptTemplatePanel interceptTemplate;
	
//================================================
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("sampleSize", "int");
	}
	
	private int getSampleSize() {
		return getIntParam("sampleSize");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = super.getWorkingPanels(data);
		
		theView.setDataKeys("xData", "yData");
		
		return thePanel;
	}
	
	protected XPanel getTemplatePanel(FormulaContext stdContext) {
		XPanel templatePanel = new XPanel();
		templatePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel innerPanel = new InsetPanel(16, 5);
			innerPanel.setLayout(new BorderLayout(0, 0));
				slopeTemplate = new SlopeTemplatePanel(stdContext);
				registerStatusItem("slopeTemplate", slopeTemplate);
			innerPanel.add("North", slopeTemplate);
			
				interceptTemplate = new InterceptTemplatePanel(stdContext);
				registerStatusItem("interceptTemplate", interceptTemplate);
			innerPanel.add("Center", interceptTemplate);
			
			innerPanel.lockBackground(kTemplateBackground);
		templatePanel.add(innerPanel);
		
		return templatePanel;
	}
	
	protected void setDisplayForQuestion() {
		super.setDisplayForQuestion();
		
		NumVariable xVar = (NumVariable)data.getVariable("x01");
		Vector axisLabels = xAxis.getLabels();
		AxisLabel label0 = (AxisLabel)axisLabels.firstElement();
		int nLabels = axisLabels.size();
		AxisLabel label1 = (AxisLabel)axisLabels.elementAt(nLabels - 1);
		NumValue x0 = (NumValue)label0.label;
		NumValue x1 = (NumValue)label1.label;
		xVar.setValueAt(x0, 0);
		xVar.setValueAt(x1, 1);
		
		LinearModel lineVar = (LinearModel)data.getVariable("line");
		
		NumValue maxSlope = new NumValue(lineVar.getSlope());
		maxSlope.setValue(maxSlope.toDouble() * 100);
		maxSlope.decimals ++;
		slopeTemplate.changeMaxValue(maxSlope);
		slopeTemplate.setValues(kZeroValue, kZeroValue, kZeroValue, kZeroValue);
		
		NumValue maxIntercept = new NumValue(lineVar.getIntercept());
		maxIntercept.setValue(maxIntercept.toDouble() * 100);
		maxIntercept.decimals ++;
		interceptTemplate.changeMaxValue(maxIntercept);
		interceptTemplate.setValues(kZeroValue, kZeroValue, kZeroValue);
	}
	
/*
	private NumValue round(double y, int decimals) {
		for (int i=0 ; i<decimals ; i++)
			y *= 10.0;
		y = Math.rint(y);
		for (int i=0 ; i<decimals ; i++)
			y /= 10.0;
		return new NumValue(y, decimals);
	}
*/
	
	protected void setSampleData(double xMin, double xMax, double yMin, double yMax, LinearModel lineVar,
																																							NumValue slope) {
		int n = getSampleSize();
		
		NumSampleVariable xVar = (NumSampleVariable)data.getVariable("xData");
		RandomNormal xGenerator = (RandomNormal)xVar.getGenerator();
		double xDistnMean = (xMin + xMax) / 2;
		double xDistnSd = (xMax - xMin) / 6;
		xVar.setSampleSize(n);
		xGenerator.setParameters(xDistnMean, xDistnSd);
		xVar.generateNextSample();
		
		double sx = 0;
		double sxx = 0;
		for (int i=0 ; i<n ; i++) {
			double x = xVar.doubleValueAt(i);
			sx += x;
			sxx += x * x;
		}
		double xMean = sx / n;
		double xSd = Math.sqrt((sxx - sx * xMean) / (n - 1));
		
		NumSampleVariable zyVar = (NumSampleVariable)data.getVariable("zy");
		zyVar.setSampleSize(n);
		zyVar.generateNextSample();
			
		CorrelatedVariable yVar = (CorrelatedVariable)data.getVariable("yData");
		double yMean = lineVar.evaluateMean(xMean);
		double ySd = (yMax - yMin) / 6;
		double r = slope.toDouble() * xSd / ySd;
		yVar.setMeanSdCorr(yMean, ySd, r, 9);
		
		data.variableChanged("yData");
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Type the intercept and slope into the equation for the displayed line.\n(The two templates may help you to calculate these values.)");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				
				NumVariable xVar = (NumVariable)data.getVariable("x01");
				NumVariable yVar = (NumVariable)data.getVariable("y01");
				NumValue x0 = (NumValue)xVar.valueAt(0);
				NumValue x1 = (NumValue)xVar.valueAt(1);
				NumValue y0 = (NumValue)yVar.valueAt(0);
				NumValue y1 = (NumValue)yVar.valueAt(1);
				NumValue dx = new NumValue(x1.toDouble() - x0.toDouble(), Math.max(x0.decimals, x1.decimals));
				NumValue dy = new NumValue(y1.toDouble() - y0.toDouble(), Math.max(y0.decimals, y1.decimals));
				messagePanel.insertText("When " + getXVarName() + " increases by " + dx
										+ ", the change in " + getYVarName() + " is " + dy + ". Therefore the slope is "
										+ dy + " divided by " + dx + ".");
				
//				LinearModel lineVar = (LinearModel)data.getVariable("line");
//				NumValue slope = lineVar.getSlope();
				messagePanel.insertText("\nSince " + getYVarName() + " is " + y0 + " at " + getXVarName()
												+ "=" + x0 +", the intercept is " + x0 + " times the slope less than this at "
												+ getXVarName() + "=0.");
				
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have found the correct slope and intercept.");
				break;
			case ANS_CLOSE:
			case ANS_WRONG:
//				messagePanel.insertRedHeading(result == ANS_CLOSE ? "Close!" : "Not close enough!");
				messagePanel.insertBoldText("Slope: ");
				if (slopeResult == ANS_CORRECT)
					messagePanel.insertRedText("Correct.");
				else {
					messagePanel.insertText("The slope is the change in " + getYVarName() + " when " + getXVarName() + " increased by 1. ");
					messagePanel.insertText(" (DIvide the difference between the values of " + getYVarName() + " by the change in " + getXVarName() + ".) ");
					messagePanel.insertText("Your value is ");
					if (slopeResult == ANS_CLOSE)
						messagePanel.insertRedText("close to the correct value.");
					else
						messagePanel.insertRedText("not close enough.");
				}
				messagePanel.insertBoldText("\nIntercept: ");
				if (interceptResult == ANS_CORRECT)
					messagePanel.insertRedText("Correct.");
				else {
					messagePanel.insertText("The intercept is the value of " + getYVarName() + " when " + getXVarName() + " is 0. ");
					xVar = (NumVariable)data.getVariable("x01");
					x0 = (NumValue)xVar.valueAt(0);
					messagePanel.insertText(" (Subtract " + x0 + " times the slope from the value of " + getYVarName() + " at " + getXVarName() + "=" + x0 + ".) " );
					messagePanel.insertText("Your value is ");
					if (interceptResult == ANS_CLOSE)
						messagePanel.insertRedText("close to the correct value.");
					else
						messagePanel.insertRedText("not close enough.");
				}
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 100;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = super.getData();
		
			xGenerator = new RandomNormal(10, 0.0, 1.0, 2.0);		//	+/- 2 SD
			xGenerator.setSeed(nextSeed());
			NumSampleVariable xDataVar = new NumSampleVariable("X", xGenerator, 9);
			xDataVar.generateNextSample();
		data.addVariable("xData", xDataVar);
		
			yGenerator = new RandomNormal(10, 0.0, 1.0, 2.0);		//	+/- 2 SD
			yGenerator.setSeed(nextSeed());
			NumSampleVariable yBaseVar = new NumSampleVariable("ZY", yGenerator, 9);
			yBaseVar.generateNextSample();
		data.addVariable("zy", yBaseVar);
			
			CorrelatedVariable yDataVar = new CorrelatedVariable("", data, "xData", "zy", 9);
		data.addVariable("yData", yDataVar);
		
		return data;
	}
	
	
//-----------------------------------------------------------
	
	protected void showCorrectWorking() {
		super.showCorrectWorking();
		
		LinearModel correctLine = (LinearModel)data.getVariable("line");
		NumValue slope = correctLine.getSlope();
		
		NumVariable xVar = (NumVariable)data.getVariable("x01");
		NumVariable yVar = (NumVariable)data.getVariable("y01");
		slopeTemplate.setValues((NumValue)xVar.valueAt(0), (NumValue)yVar.valueAt(0),
																(NumValue)xVar.valueAt(1), (NumValue)yVar.valueAt(1));
	
		interceptTemplate.setValues((NumValue)yVar.valueAt(0), slope, (NumValue)xVar.valueAt(0));
	}
}