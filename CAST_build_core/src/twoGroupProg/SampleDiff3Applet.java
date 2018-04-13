package twoGroupProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import distn.*;
import coreGraphics.*;
import models.*;
import imageUtils.*;

import twoGroup.*;


public class SampleDiff3Applet extends SampleDiff2Applet {
	static final private Color kPinkColor = new Color(0xFFCCCC);
	static final private Color kDarkGreen = new Color(0x006600);
	
	protected XPanel displayPanel(CoreModelDataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		thePanel.add("Center", dataPanel(data));
		thePanel.add("East", summaryPanel(data, summaryData));
		return thePanel;
	}
	
	protected SummaryDataSet getSummaryData(CoreModelDataSet sourceData) {
		SummaryDataSet summaryData = super.getSummaryData(sourceData);
		
		GroupsModelVariable model = (GroupsModelVariable)sourceData.getVariable("model");
		int meanDecimals = Math.max(model.getMean(0).decimals, model.getMean(1).decimals);
		
		NormalDistnVariable diffTheory = new NormalDistnVariable("theory");
		diffTheory.setDecimals(meanDecimals, sourceData.getSummaryDecimals());
		summaryData.addVariable("theory", diffTheory);
		setTheoryParams(sourceData, summaryData);
		
		return summaryData;
	}
	
	protected void setTheoryParams(CoreModelDataSet data, SummaryDataSet summaryData) {
		GroupsDataSet anovaData = (GroupsDataSet)data;
		
		NormalDistnVariable diffTheory = (NormalDistnVariable)summaryData.getVariable("theory");
		
		GroupsModelVariable model = (GroupsModelVariable)data.getVariable("model");
		diffTheory.setMean(model.getMean(1).toDouble() - model.getMean(0).toDouble());
		
		double s1 = model.getSD(0).toDouble();
		double s2 = model.getSD(1).toDouble();
		int n1 = anovaData.getN(0);
		int n2 = anovaData.getN(1);
		diffTheory.setSD(Math.sqrt(s1 * s1 / n1 + s2 * s2 / n2));
	}
	
	protected XPanel summaryPanel(CoreModelDataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 20));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
			topPanel.add(new Separator(-1.0, 6));			//	space of 14 pixels
			topPanel.add(parameterPanel(data));
			topPanel.add(new Separator(0.5, 20));
			topPanel.add(differencePanel(data));
		
		thePanel.add("North", topPanel);
		thePanel.add("Center", summaryPlotPanel(data, summaryData));
		
		return thePanel;
	}
	
	private XPanel parameterPanel(CoreModelDataSet data) {
		GroupsDataSet anovaData = (GroupsDataSet)data;
		GroupsModelVariable model = (GroupsModelVariable)data.getVariable("model");
		NumValue m1 = model.getMean(0);
		NumValue m2 = model.getMean(1);
		NumValue s1 = model.getSD(0);
		NumValue s2 = model.getSD(1);
		int n1 = anovaData.getN(0);
		int n2 = anovaData.getN(1);
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			topPanel.add(paramColPanel(data, m1, m2, "xEquals/mu1Black.png", "xEquals/mu2Black.png", 12,
																																								Color.black));
			topPanel.add(paramColPanel(data, s1, s2, "xEquals/sigma1.png", "xEquals/sigma2.png", 12,
																																								Color.red));
			topPanel.add(paramColPanel(data, new NumValue(n1, 0), new NumValue(n2, 0),
																									"xEquals/n1.png", "xEquals/n2.png", 12, Color.blue));
		thePanel.add("North", topPanel);
		
			XPanel sdDiffPanel = new XPanel();
			sdDiffPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				NumValue sdDiff = new NumValue(Math.sqrt(s1.toDouble() * s1.toDouble() / n1 + s2.toDouble() * s2.toDouble() / n2),
																																						s1.decimals + 2);
				FixedValueImageView valView = new FixedValueImageView("xEquals/diffMeanSD.png", 27, sdDiff,
																																sdDiff.toDouble(), this);
				valView.unboxValue();
				valView.setFont(getBigFont());
				valView.setForeground(kDarkGreen);
			sdDiffPanel.add(valView);
		thePanel.add("Center", sdDiffPanel);
		
		return thePanel;
	}
	
	private XPanel paramColPanel(DataSet data, NumValue v1, NumValue v2, String imgFile1,
																											String imgFile2, int ascent, Color c) {
		XPanel p = new XPanel();
		p.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																													VerticalLayout.VERT_CENTER, 2));
			FixedValueImageView valView = new FixedValueImageView(imgFile1, ascent, v1, v1.toDouble(), this);
			valView.unboxValue();
			valView.setFont(getBigFont());
			valView.setForeground(c);
		p.add(valView);
			valView = new FixedValueImageView(imgFile2, ascent, v2, v2.toDouble(), this);
			valView.unboxValue();
			valView.setFont(getBigFont());
			valView.setForeground(c);
		p.add(valView);
		
		return p;
	}
	
	private XPanel differencePanel(CoreModelDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
		
		String maxSummaryString = getParameter(MAX_SUMMARY_PARAM);
		
			GroupSummary2View meanDiffView = new GroupSummary2View(data, this, GroupSummary2View.X_BAR_DIFF,
																														maxSummaryString, data.getSummaryDecimals());
			meanDiffView.setForeground(Color.red);
			meanDiffView.setFont(getBigFont());
		
		thePanel.add(meanDiffView);
		
		return thePanel;
	}
	
	protected XPanel summaryPlotPanel(CoreModelDataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theAxis = new HorizAxis(this);
			theAxis.readNumLabels(getParameter(SUMMARY_AXIS_PARAM));
			theAxis.setAxisName(summaryData.getVariable("difference").name);
			theAxis.setForeground(Color.red);
		thePanel.add("Bottom", theAxis);
		
			StackedPlusNormalView summaryDotPlot = new StackedPlusNormalView(summaryData, this, theAxis, "theory");
			summaryDotPlot.setActiveNumVariable("difference");
			summaryDotPlot.setForeground(Color.red);
			summaryDotPlot.lockBackground(Color.white);
			summaryDotPlot.setDensityColor(kPinkColor);
				NormalDistnVariable diffTheory = (NormalDistnVariable)summaryData.getVariable("theory");
				String mu = diffTheory.getMean().toString();
				String sigma = diffTheory.getSD().toString();
			summaryDotPlot.setDistnLabel(new LabelValue(translate("Normal") + "(" + mu + ", " + sigma + ")"),
																																						Color.lightGray);
		thePanel.add("Center", summaryDotPlot);
		
		return thePanel;
	}
}