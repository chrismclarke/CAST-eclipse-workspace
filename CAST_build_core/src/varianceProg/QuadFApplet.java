package varianceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;
import models.*;

import variance.*;
import ssq.*;


public class QuadFApplet extends QuadSsqDistnApplet {
	static final private String AREA_PROPN_PARAM = "areaProportion";
	static final private String COMPONENT_NAME_PARAM = "componentName";
	
	static final private Color kDensityColor = new Color(0xCCCCCC);
	static final private Color kTailColor = new Color(0xFF6699);
	
	protected AnovaSummaryData getSummaryData(CoreModelDataSet sourceData) {
		AnovaSummaryData summaryData = new AnovaSummaryData(sourceData, "error", QuadComponentVariable.kComponentKey,
									maxSsq.decimals, kMaxRSquared.decimals, maxMeanSsq.decimals, maxF.decimals);
		
			FDistnVariable fDistn = new FDistnVariable("F", 1, 1);
		summaryData.addVariable("F", fDistn);
		
		adjustDF(summaryData);
		
		return summaryData;
	}
	
	protected void adjustDF(AnovaSummaryData summaryData) {
		QuadComponentVariable residComponent = (QuadComponentVariable)data.
																				getVariable(QuadComponentVariable.kComponentKey[3]);
		int residDF = residComponent.getDF();
		
		QuadComponentVariable explainedComponent = (QuadComponentVariable)data.
																				getVariable(QuadComponentVariable.kComponentKey[2]);
		int explainedDF = explainedComponent.getDF();
		
		FDistnVariable fDistn  = (FDistnVariable)summaryData.getVariable("F");
		fDistn.setDF(explainedDF, residDF);
		
		DataPlusDistnInterface fDistnView = (DataPlusDistnInterface)ssqView;
		if (fDistnView != null)
			summaryData.variableChanged("F");
	}
	
	protected boolean showSsqValue() {
		return false;
	}
	
	protected DataView getSsqDotView(SummaryDataSet summaryData, HorizAxis axis) {
		FView theView = new FView(summaryData, this, axis, "F", "f-quadratic");
		double areaPropn = Double.parseDouble(getParameter(AREA_PROPN_PARAM));
		theView.setAreaProportion(areaPropn);
		
		String labelString = translate("F distn if true model is linear");
		theView.setDistnLabel(new LabelValue(labelString), Color.gray);
		
		theView.setArrowColor(QuadComponentVariable.kQuadraticColor);
		theView.setDistnColors(kDensityColor, kTailColor);
		
		return theView;
	}
	
	protected Color getSsqColor() {
		return Color.black;
	}
	
	protected String sampleButtonText() {
		return translate("Another data set");
	}
	
	protected XPanel anovaTablePanel(AnovaSummaryData summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			AnovaTableView table = new AnovaTableView(summaryData, this,
									QuadComponentVariable.kComponentKey, maxSsq, maxMeanSsq, maxF,
									AnovaTableView.SSQ_F_PVALUE);
			String componentNames = getParameter(COMPONENT_NAME_PARAM);
			if (componentNames != null) {
				StringTokenizer st = new StringTokenizer(componentNames, "#");
				int nNames = st.countTokens();
				String componentName[] = new String[nNames];
				for (int i=0 ; i<nNames ; i++)
					componentName[i] = st.nextToken();
				table.setComponentNames(componentName);
			}
			table.setComponentColors(QuadComponentVariable.kComponentColor);
			
		thePanel.add(table);
		
		return thePanel;
	}
	
	protected void changeSampleSize(int newSizeIndex) {
		super.changeSampleSize(newSizeIndex);
		
		adjustDF(summaryData);
	}
}