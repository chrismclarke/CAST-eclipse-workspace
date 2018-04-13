package varianceProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import distn.*;
import models.*;
import formula.*;

import ssq.*;
import variance.*;


public class TwoGroupF2Applet extends TwoGroupFApplet {
	static final private String COMPONENT_NAME_PARAM = "componentName";
	
	protected SummaryDataSet getSummaryData(CoreModelDataSet data) {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SUMMARIES_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxMsq = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
		maxRSquared = new NumValue(st.nextToken());
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "error",
												TwoGroupComponentVariable.kComponentKey, maxSsq.decimals,
												maxRSquared.decimals);
		
			String var0Key = TwoGroupComponentVariable.kComponentKey[TwoGroupComponentVariable.WITHIN_0];
			SsqVariable mss = new SsqVariable("Group 1 var", data, var0Key, maxMsq.decimals, SsqVariable.MEAN_SSQ);
		summaryData.addVariable("var0", mss);
		
			String var1Key = TwoGroupComponentVariable.kComponentKey[TwoGroupComponentVariable.WITHIN_1];
			mss = new SsqVariable("Group 2 var", data, var1Key, maxMsq.decimals, SsqVariable.MEAN_SSQ);
		summaryData.addVariable("var1", mss);
		
																//	swap numerator and denominator from TwoGroupFApplet
																//	to match F ratio in AnovaTableView
			SsqRatioVariable f = new SsqRatioVariable("F ratio", var0Key,
										var1Key, maxF.decimals, SsqRatioVariable.MEAN_SSQ);
		summaryData.addVariable("F", f);

		st = new StringTokenizer(getParameter(DF_PARAM));
		numerDF = Integer.parseInt(st.nextToken());
		denomDF = Integer.parseInt(st.nextToken());
		
		summaryData.addVariable("fDistn", new FDistnVariable("F distn", numerDF, denomDF));
		
		return summaryData;
	}
	
	protected double getFProportion() {
		return 0.35;
	}
	
	protected XPanel dataPanel(GroupsDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new AxisLayout());
				
				VertAxis yAxis = new VertAxis(this);
				String labelInfo = data.getYAxisInfo();
				yAxis.readNumLabels(labelInfo);
			mainPanel.add("Left", yAxis);
			
				HorizAxis theGroupAxis = new HorizAxis(this);
				CatVariable groupVariable = data.getCatVariable();
				theGroupAxis.setCatLabels(groupVariable);
			mainPanel.add("Bottom", theGroupAxis);
			
				DataView theView = new TwoGroupSpreadDotView(data, this, yAxis, theGroupAxis, "y", "x");
				theView.lockBackground(Color.white);
				
			mainPanel.add("Center", theView);
		
		thePanel.add("Center", mainPanel);
		
			XLabel yVarName = new XLabel(data.getVariable("y").name, XLabel.LEFT, this);
			yVarName.setFont(yAxis.getFont());
		thePanel.add("North", yVarName);
		
		return thePanel;
	}
	
	protected XPanel summaryValuePanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_SPACED, 10));
		
			AnovaTableView table = new AnovaTableView(summaryData, this,
															TwoGroupComponentVariable.kComponentKey, maxSsq, maxMsq, maxF,
															AnovaTableView.SSQ_AND_F);
			StringTokenizer st = new StringTokenizer(getParameter(COMPONENT_NAME_PARAM), "#");
			String componentName[] = new String[4];
			for (int i=0 ; i<4 ; i++)
				componentName[i] = st.nextToken();
			table.setComponentNames(componentName);
			table.setComponentColors(TwoGroupComponentVariable.kComponentColor);
			table.setFDenom(TwoGroupComponentVariable.WITHIN_0, TwoGroupComponentVariable.WITHIN_1);
		thePanel.add(table);
		
			FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
			Color var0Color = TwoGroupComponentVariable.kComponentColor[TwoGroupComponentVariable.WITHIN_0];
			Color var1Color = TwoGroupComponentVariable.kComponentColor[TwoGroupComponentVariable.WITHIN_1];
		thePanel.add(new FCalcPanel(summaryData, "var0", "var1",
																		"F", maxMsq, maxF, var0Color, var1Color, stdContext));
		
		return thePanel;
	}
}