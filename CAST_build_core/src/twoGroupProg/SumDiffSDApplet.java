package twoGroupProg;

import java.awt.*;

import dataView.*;
import utils.*;
import distn.*;
import coreVariables.*;
import formula.*;

import twoGroup.*;


public class SumDiffSDApplet extends SumTwoSDApplet {
	static final private String DIFF_AXIS_INFO_PARAM = "diffAxis";
	
	static final private Color kDiffColor = new Color(0x006600);					//	dark green
	static final private Color kDiffDensityColor = new Color(0x66FF99);	//	light green
	
	protected int noOfSubPanels() {
		return 4;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = super.getSummaryData(sourceData);
		
			String diffName = MText.expandText("Difference, X#sub1# - X#sub2#");
			SumDiffVariable diff = new SumDiffVariable(diffName, summaryData, "x1", "x2", SumDiffVariable.DIFF);
		summaryData.addVariable("diff", diff);
		
			int decimals = maxSD.decimals;
			NormalDistnVariable diffDistn = new NormalDistnVariable("diff theory");
			diffDistn.setDecimals(decimals);
		summaryData.addVariable("diffTheory", diffDistn);
		
		return summaryData;
	}
	
	protected void setTheoryParameters(SummaryDataSet summaryData) {
		super.setTheoryParameters(summaryData);
		
		NormalDistnVariable sumDistn = (NormalDistnVariable)summaryData.getVariable("sumTheory");
		
		NormalDistnVariable diffDistn = (NormalDistnVariable)summaryData.getVariable("diffTheory");
		diffDistn.setMean(y1Mean - y2Mean);
		diffDistn.setSD(sumDistn.getSD().toDouble());
	}
	
	protected XPanel subDataPanel(DataSet data, SummaryDataSet summaryData, int index) {
		if (index == 3) {
			String axisInfo = getParameter(DIFF_AXIS_INFO_PARAM);
			return oneDataPanel(summaryData, "diff", "diffTheory", axisInfo, kDiffColor, kDiffDensityColor);
		}
		else
			return super.subDataPanel(data, summaryData, index);
	}
	
	protected XPanel subSummaryPanel(DataSet data, SummaryDataSet summaryData, int index) {
		if (index == 3) {
			XPanel panel3 = new XPanel();
			panel3.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 8));
			
				SDImageView sdView = new SDImageView(summaryData, this, "diff/sigma1Minus2.gif", 20, "diffTheory", maxSD);
				sdView.setForeground(kDiffColor);
				sdView.setFont(getBigBoldFont());
			panel3.add(sdView);
			
			return panel3;
		}
		else
			return super.subSummaryPanel(data, summaryData, index);
	}
}