package ssqProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import imageUtils.*;

import ssq.*;


public class DataFApplet extends DataSsqApplet {
	static final private String MAX_MSSQ_PARAM = "maxMeanSsq";
	static final private String MAX_F_PARAM = "maxF";
	
	private NumValue maxMeanSsq, maxF;
	
	protected AnovaSummaryData getSummaryData(CoreModelDataSet data) {
		maxMeanSsq = new NumValue(getParameter(MAX_MSSQ_PARAM));
		maxF = new NumValue(getParameter(MAX_F_PARAM));
		
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "y",
								BasicComponentVariable.kComponentKey, maxSsq.decimals, kMaxRSquared.decimals,
								maxMeanSsq.decimals, maxF.decimals);
		return summaryData;
	}
	
	protected XPanel bottomPanel(AnovaSummaryData summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
			AnovaTableView theTable = new AnovaTableView(summaryData, this,
									BasicComponentVariable.kComponentKey, maxSsq, maxMeanSsq, maxF,
									AnovaTableView.SSQ_F_PVALUE);
		thePanel.add(theTable);
		return thePanel;
	}
	
	protected XPanel rightPanel(AnovaSummaryData summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
		thePanel.add(new OneValueImageView(summaryData, "rSquared", this, "xEquals/rSquared.png", 14, kMaxRSquared));
		
		return thePanel;
	}
	
}