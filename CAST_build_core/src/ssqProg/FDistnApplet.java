package ssqProg;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;
import models.*;

import ssq.*;


public class FDistnApplet extends CoreSsqDistnApplet {
	static final private String AREA_PROPN_PARAM = "areaProportion";
	
//	static final private Color kDensityColor = new Color(0x66CCFF);
	
	protected AnovaSummaryData getSummaryData(CoreModelDataSet sourceData) {
		AnovaSummaryData summaryData = super.getSummaryData(sourceData);
		
			FDistnVariable fDistn = new FDistnVariable("F", 1, 1);
		summaryData.addVariable("F", fDistn);
		
			adjustDF(summaryData);
		
		summaryData.setAccumulate(true);
		
		return summaryData;
	}
	
	private void adjustDF(AnovaSummaryData summaryData) {
		BasicComponentVariable residComponent = (BasicComponentVariable)data.
																				getVariable(BasicComponentVariable.kComponentKey[2]);
		int residDF = residComponent.getDF();
		
		BasicComponentVariable explainedComponent = (BasicComponentVariable)data.
																				getVariable(BasicComponentVariable.kComponentKey[1]);
		int explainedDF = explainedComponent.getDF();
		
		FDistnVariable fDistn  = (FDistnVariable)summaryData.getVariable("F");
		fDistn.setDF(explainedDF, residDF);
		
		DataPlusDistnInterface fDistnView = (DataPlusDistnInterface)ssqView;
		if (fDistnView != null) {
			adjustFDistnLabel(fDistnView, explainedDF, residDF);
			summaryData.variableChanged("F");
		}
	}
	
	private void adjustFDistnLabel(DataPlusDistnInterface fView, int explainedDF, int residDF) {
		fView.setDistnLabel(new LabelValue("F(" + explainedDF + ", " + residDF + " df)"),
																																									Color.gray);
	}
	
	protected int componentType() {
		return DataWithComponentView.NO_COMPONENT_DISPLAY;
	}
	
	protected XPanel ssqAndDistnPanel(AnovaSummaryData summaryData) {
		return ssqPanel(summaryData);
	}
	
	protected DataView getSsqDotView(SummaryDataSet summaryData, HorizAxis axis) {
		StackedPlusNormalView theView = new StackedPlusNormalView(summaryData, this,
															axis, "F", StackedPlusNormalView.ACCURATE_STACK_ALGORITHM);
		theView.setActiveNumVariable("f-explained");
		theView.setShowDensity(DataPlusDistnInterface.CONTIN_DISTN);
		double areaPropn = Double.parseDouble(getParameter(AREA_PROPN_PARAM));
		theView.setAreaProportion(areaPropn);
//		theView.setDensityColor(kDensityColor);
		
		FDistnVariable fDistn = (FDistnVariable)summaryData.getVariable("F");
		adjustFDistnLabel(theView, fDistn.getDF1(), fDistn.getDF2());
		
		return theView;
	}
	
	protected Color getSsqColor() {
		return Color.black;
	}
	
	protected XPanel anovaTablePanel(AnovaSummaryData summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			AnovaTableView table = new AnovaTableView(summaryData, this,
									BasicComponentVariable.kComponentKey, maxSsq, maxMeanSsq, maxF,
									AnovaTableView.SSQ_AND_F);
			table.setFont(getBigFont());
			
		thePanel.add(table);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(CoreModelDataSet data, AnovaSummaryData summaryData) {
		XPanel thePanel = super.controlPanel(data, summaryData);
		thePanel.add(getSampleSizePanel());
		thePanel.add(getSampleButton(translate("Take sample")));
		return thePanel;
	}
	
	protected void changeSampleSize(int newSizeIndex) {
		super.changeSampleSize(newSizeIndex);
		
		adjustDF(summaryData);
	}
}