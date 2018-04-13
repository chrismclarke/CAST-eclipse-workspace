package loessProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.*;
import models.*;

import regnProg.*;
import regn.*;
import loess.*;


public abstract class CoreDiagnosticApplet extends MultipleScatterApplet {
	static final private String RANDOM_PARAM = "random";
	
	private XChoice randomOrDataChoice;
	private XButton sampleButton;
	private int currentChoiceIndex;
	
	protected void addSimulationVariables(DataSet data, String xKey, String yKey) {
			NumVariable xVar = (NumVariable)data.getVariable(xKey);
			NumVariable yVar = (NumVariable)data.getVariable(yKey);
			int nVals = xVar.noOfValues();
			
			LinearModel modelVariable = new LinearModel("model", data, xKey);
			modelVariable.setLSParams(yKey, 4, 4, 0);
		data.addVariable("model", modelVariable);
		
			StringTokenizer randomTokenizer = new StringTokenizer(getParameter(RANDOM_PARAM));
			double truncation = Double.parseDouble(randomTokenizer.nextToken());
			RandomNormal generator = new RandomNormal(nVals, 0.0, 1.0, truncation);
			if (randomTokenizer.hasMoreTokens()) {
				long randomSeed = Long.parseLong(randomTokenizer.nextToken());
				generator.setSeed(randomSeed);
			}
			NumSampleVariable error = new NumSampleVariable("error", generator, 10);
			error.setSampleSize(nVals);
			error.generateNextSample();
		data.addVariable("error", error);
		
			SwitchResponseVariable response = new SwitchResponseVariable(yVar.name, data,
																	xKey, yKey, "error", "model");
		data.addVariable("response", response);
			
			LSLinearModel lsLine = new LSLinearModel("least sqrs line", data, xKey, "response", 4, 4, 0);
		data.addVariable("lsLine", lsLine);
		
			ResidValueVariable resid = new ResidValueVariable(residualName(), data, xKey,
																						"response", "lsLine", 4);
		data.addVariable("resid", resid);
		
			NormalScoreVariable nScore = new NormalScoreVariable(translate("Normal scores"), data, "resid", 4);
		data.addVariable("nscore", nScore);
	}
	
	protected String residualName() {
		return translate("Residual");			//		so that ProbPlotRandomApplet can call them "Ordered residuals"
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		return new SummaryDataSet(sourceData, "error");
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
		randomOrDataChoice = new XChoice(this);
		randomOrDataChoice.addItem(translate("Actual Data"));
		randomOrDataChoice.addItem(translate("Random Normal Data"));
		currentChoiceIndex = 0;
		thePanel.add(randomOrDataChoice);
		
		sampleButton = getSampleButton();
		sampleButton.disable();
		thePanel.add(sampleButton);
		
		return thePanel;
	}
	
	protected XButton getSampleButton() {
		return new XButton(translate("Take sample"), this);
	}
	
	protected void changeDataSample(boolean toData) {
		SwitchResponseVariable resp = (SwitchResponseVariable)data.getVariable("response");
		resp.setDataShow(toData);
		data.variableChanged("response");
		if (toData == SwitchResponseVariable.SHOW_RANDOM)
			sampleButton.enable();
		else
			sampleButton.disable();
	}
	
	private boolean localAction(Object target) {
		if (target == randomOrDataChoice) {
			int newChoiceIndex = randomOrDataChoice.getSelectedIndex();
			if (newChoiceIndex != currentChoiceIndex) {
				currentChoiceIndex = newChoiceIndex;
				boolean showData = (newChoiceIndex == 1) ? SwitchResponseVariable.SHOW_RANDOM
																									: SwitchResponseVariable.SHOW_DATA;
				changeDataSample(showData);
			}
			return true;
		}
		else if (target == sampleButton)
			summaryData.takeSample();
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}