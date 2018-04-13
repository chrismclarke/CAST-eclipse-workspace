package stdErrorProg;

import java.awt.*;

import dataView.*;
import utils.*;
import distn.*;
import coreGraphics.*;


public class MeanMedTheoryApplet extends ErrorDistnTheoryApplet {
	static final private String MEDIAN_ERROR_NAME_PARAM = "medianErrorName";
	
	private NumValue medianErrorSD, medianErrorMean;
	
	private SimpleDistnView medianDistnView;
	
	protected DataSet getData() {
		DataSet data = super.getData();
		
			int errorSdDecimals = Integer.parseInt(getParameter(ERROR_SD_DECIMALS_PARAM));
			int errorMeanDecimals;
			if (isNormalPopn)
				errorMeanDecimals = 0;
			else
				errorMeanDecimals = errorSdDecimals;
		
		NormalDistnVariable medianDistn = new NormalDistnVariable(showError ? getParameter(MEDIAN_ERROR_NAME_PARAM)
																																											: translate("Median"));
			medianErrorSD = new NumValue(0.0, errorSdDecimals);
			medianErrorMean = new NumValue(0.0, errorMeanDecimals);
			medianDistn.setDecimals(errorMeanDecimals, errorSdDecimals);
		
		data.addVariable(showError ? "medianErrorDistn" : "medianDistn", medianDistn);
		
		return data;
	}
	
	protected void setTheoryParameters(DataSet data) {
		super.setTheoryParameters(data);
		
		int sampleSize = (int)Math.round(sampleSizeSlider.getParameter().toDouble());
		
		String varName = showError ? "medianErrorDistn" : "medianDistn";
		NormalDistnVariable medianDistn = (NormalDistnVariable)data.getVariable(varName);
		
		if (isNormalPopn) {
			medianErrorSD.setValue(1.253 * modelSD.toDouble() / Math.sqrt(sampleSize));
			double varMean = showError ? 0.0 : modelMean.toDouble();
			medianErrorMean.setValue(varMean);
			medianDistn.setSD(medianErrorSD.toDouble());
			medianDistn.setMean(varMean);
		}
		else {
			medianErrorSD.setValue(modelSD.toDouble() / Math.sqrt(sampleSize));
			double varMean = modelMean.toDouble() * Math.log(2.0);
			if (showError)
				varMean -= modelMean.toDouble();
			medianErrorMean.setValue(varMean);
			medianDistn.setSD(medianErrorSD.toDouble());
			medianDistn.setMean(varMean);
		}
			
		LabelValue label = new LabelValue(translate("mean") + " = " + medianErrorMean.toString()
																					+ ", " + translate("sd") + " = " + medianErrorSD.toString());
		medianDistnView.setLabel(label, Color.gray);
		
		data.variableChanged(varName);
	}
	
	protected XPanel popnTitlePanel(Color c) {
		return titlePanel("Population*distribution", c);		//	titlePanel() calls translate()
	}
	
	protected XPanel errorDistnTitlePanel(Color c) {
		return titlePanel(showError ? "Error*distribution*(approx)" : "Distribution*of estimate*(approx)", c);		//	titlePanel() calls translate()
	}
	
	protected boolean saveDistnView(SimpleDistnView view) {
		if (!super.saveDistnView(view))
			medianDistnView = view;
		return true;
	}
	
	protected XPanel errorPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																												ProportionLayout.TOTAL));
		String axisString = getParameter(showError ? ERROR_AXIS_PARAM : MEAN_MED_AXIS_PARAM);
		
		thePanel.add(ProportionLayout.LEFT, distributionPanel(data, showError ? "meanErrorDistn" : "meanDistn", kErrorColor,
																																		kNormalErrorColor, axisString));
		thePanel.add(ProportionLayout.RIGHT, distributionPanel(data, showError ? "medianErrorDistn" : "medianDistn", kErrorColor,
																																		kNormalErrorColor, axisString));
		
		return thePanel;
	}
}