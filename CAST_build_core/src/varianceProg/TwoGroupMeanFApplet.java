package varianceProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import coreGraphics.*;
import distn.*;
import models.*;
import formula.*;

import ssq.*;


public class TwoGroupMeanFApplet extends TwoGroupFApplet {
	static final private String COMPONENT_NAME_PARAM = "componentName";
	static final private String AREA_PROPN_PARAM = "areaProportion";
	
	private String meanExplainedKey, meanResidKey, fKey;
	
	protected GroupsDataSet readData() {
		GroupsDataSet data = new GroupsDataSet(this);
		
		data.addBasicComponents();
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(CoreModelDataSet data) {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SUMMARIES_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxMsq = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
		maxRSquared = new NumValue(st.nextToken());
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "error",
												BasicComponentVariable.kComponentKey, maxSsq.decimals,
												maxRSquared.decimals, maxMsq.decimals, maxF.decimals);
		
		numerDF = 1;
		denomDF = Integer.parseInt(getParameter(DF_PARAM));
			
		meanExplainedKey = "m-" + BasicComponentVariable.kComponentKey[BasicComponentVariable.EXPLAINED];
		meanResidKey = "m-" + BasicComponentVariable.kComponentKey[BasicComponentVariable.RESIDUAL];
		fKey = "f-" + BasicComponentVariable.kComponentKey[BasicComponentVariable.EXPLAINED];

		summaryData.addVariable("fDistn", new FDistnVariable("F distn", numerDF, denomDF));
		
		return summaryData;
	}
	
	protected double getFProportion() {
		return 0.35;
	}
	
	protected XPanel dataPanel(GroupsDataSet data) {
		DataWithComponentsPanel thePanel = new DataWithComponentsPanel(this);
		thePanel.setupPanel(data, "x", "y", "ls", "model",
																		DataWithComponentView.NO_COMPONENT_DISPLAY, this);
		
		return thePanel;
	}
	
	protected XPanel fDistnPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
			
			HorizAxis ssqAxis = new HorizAxis(this);
			ssqAxis.readNumLabels(getParameter(SUMMARY_AXIS_PARAM));
			ssqAxis.setAxisName(translate("F ratio"));
		thePanel.add("Bottom", ssqAxis);
		
			StackedPlusNormalView theView = new StackedPlusNormalView(summaryData, this, ssqAxis, "fDistn",
																						StackedPlusNormalView.ACCURATE_STACK_ALGORITHM);
			theView.setActiveNumVariable(fKey);
			double areaProportion = Double.parseDouble(getParameter(AREA_PROPN_PARAM));
			theView.setAreaProportion(areaProportion);
			theView.setDistnLabel(new LabelValue("F(" + numerDF + ", " + denomDF + ")"), Color.gray);
			theView.lockBackground(Color.white);
			
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected XPanel summaryValuePanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_SPACED, 10));
		
			AnovaTableView table = new AnovaTableView(summaryData, this,
															BasicComponentVariable.kComponentKey, maxSsq, maxMsq, maxF,
															AnovaTableView.SSQ_AND_F);
			StringTokenizer st = new StringTokenizer(getParameter(COMPONENT_NAME_PARAM), "#");
			String componentName[] = new String[3];
			for (int i=0 ; i<3 ; i++)
				componentName[i] = st.nextToken();
			table.setComponentNames(componentName);
			table.setComponentColors(BasicComponentVariable.kComponentColor);
		thePanel.add(table);
		
			FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
			Color explainedColor = BasicComponentVariable.kComponentColor[BasicComponentVariable.EXPLAINED];
			Color residColor = BasicComponentVariable.kComponentColor[BasicComponentVariable.RESIDUAL];
		thePanel.add(new FCalcPanel(summaryData, meanExplainedKey, meanResidKey,
																	fKey, maxMsq, maxF, explainedColor, residColor, stdContext));
		
		return thePanel;
	}
}