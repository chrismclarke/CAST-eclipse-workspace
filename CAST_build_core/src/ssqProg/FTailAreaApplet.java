package ssqProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import coreGraphics.*;
import models.*;

import variance.*;
import ssq.*;


public class FTailAreaApplet extends CoreSsqDistnApplet {
	static final private String AREA_PROPN_PARAM = "areaProportion";
	static final private String COMPONENT_NAME_PARAM = "componentName";
	
	protected AnovaSummaryData getSummaryData(CoreModelDataSet sourceData) {
		AnovaSummaryData summaryData = super.getSummaryData(sourceData);
		
			FDistnVariable fDistn = new FDistnVariable("F", 1, 1);
		summaryData.addVariable("F", fDistn);
		
			adjustDF(summaryData);
		
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
		if (fDistnView != null)
			summaryData.variableChanged("F");
	}
	
	protected int componentType() {
		return DataWithComponentView.NO_COMPONENT_DISPLAY;
	}
	
	protected XPanel ssqAndDistnPanel(AnovaSummaryData summaryData) {
		return ssqPanel(summaryData);
	}
	
	protected DataView getSsqDotView(SummaryDataSet summaryData, HorizAxis axis) {
//		AccurateTailAreaView theView = new AccurateTailAreaView(summaryData, null, this,
//																									axis, "F", DataView.BUFFERED);
//		theView.setActiveNumVariable("f-explained");
		
		FView theView = new FView(summaryData, this, axis, "F", "f-explained");
		double areaPropn = Double.parseDouble(getParameter(AREA_PROPN_PARAM));
		theView.setAreaProportion(areaPropn);
		
		String labelString = xNumNotCat ? translate("F distn if true slope is zero")
																		: translate("F distn if all groups are identical");
		theView.setDistnLabel(new LabelValue(labelString), Color.gray);
		
		return theView;
	}
	
	protected Color getSsqColor() {
		return Color.black;
	}
	
	protected XPanel anovaTablePanel(AnovaSummaryData summaryData) {
		XPanel thePanel = new InsetPanel(0, 0, 0, 8);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			AnovaTableView table = new AnovaTableView(summaryData, this,
									BasicComponentVariable.kComponentKey, maxSsq, maxMeanSsq, maxF,
									AnovaTableView.SSQ_F_PVALUE);
			String componentNames = getParameter(COMPONENT_NAME_PARAM);
			if (componentNames != null) {
				StringTokenizer st = new StringTokenizer(componentNames, "#");
				String componentName[] = new String[3];
				for (int i=0 ; i<3 ; i++)
					componentName[i] = st.nextToken();
				table.setComponentNames(componentName);
			}
		table.setFont(getBigFont());
		thePanel.add(table);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(CoreModelDataSet data, AnovaSummaryData summaryData) {
		XPanel thePanel = super.controlPanel(data, summaryData);
		thePanel.add(getSampleSizePanel());
		thePanel.add(getEffectSizeSlider(data));
		thePanel.add(getSampleButton(translate("Another data set")));
		return thePanel;
	}
	
	protected void changeSampleSize(int newSizeIndex) {
		super.changeSampleSize(newSizeIndex);
		
		adjustDF(summaryData);
	}
}