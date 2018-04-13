package timeProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import coreVariables.*;

import time.*;


public class SeasonalEffectApplet extends BasicTimeApplet {
	static final private String EFFECT_AXIS_INFO_PARAM = "effectAxisInfo";
	static final private String EFFECT_NAME_PARAM = "effectName";
	
	private MeanMedianVariable smoothedVariable;
	private SeasonTimeAxis seasonTimeAxis;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		NumVariable yVar = new NumVariable(getParameter(VAR_NAME_PARAM));
		yVar.readValues(getParameter(VALUES_PARAM));
		data.addVariable("y", yVar);
		
		boolean showEnds = false;
		smoothedVariable = new MeanMedianVariable("Running mean", data, "y", showEnds);
		smoothedVariable.setExtraDecimals(2);
		data.addVariable("smooth", smoothedVariable);
		
		data.addVariable("resid", new SumDiffVariable(translate("Residual"), data, "y", "smooth", SumDiffVariable.DIFF));
		
		StringTokenizer st = new StringTokenizer(getParameter(SEASON_PARAM));
		int noOfSeasons = Integer.parseInt(st.nextToken());
		data.addVariable("effect", new SeasonalEffectVariable("Seasonal effect", data, "resid",
																											noOfSeasons, yVar.getMaxDecimals() + 3));
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 8, ProportionLayout.VERTICAL));
		
		thePanel.add(ProportionLayout.TOP, super.displayPanel(data));
		
		thePanel.add(ProportionLayout.BOTTOM, bottomDisplayPanel(data));
		
		return thePanel;
	}
	
	protected TimeAxis horizAxis(DataSet data) {
		TimeAxis ax = super.horizAxis(data);
		if (ax instanceof SeasonTimeAxis) {
			seasonTimeAxis = (SeasonTimeAxis)ax;
			smoothedVariable.setMeanRun(seasonTimeAxis.getNoOfSeasons());
		}
		return ax;
	}
	
	protected TimeView createTimeView(TimeAxis theHorizAxis, VertAxis theVertAxis) {
		return new TimeSeasonView(getData(), this, theHorizAxis, theVertAxis);
	}
	
	protected XPanel bottomDisplayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		thePanel.add("Center", seasonEffectPanel(data));
		return thePanel;
	}
	
	private XPanel seasonEffectPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		thePanel.add("North", new XLabel(getParameter(EFFECT_NAME_PARAM), XLabel.LEFT, this));
		
			XPanel plotPanel = new XPanel();
			plotPanel.setLayout(new AxisLayout());
			
				VertAxis effectAxis = new VertAxis(this);
				effectAxis.readNumLabels(getParameter(EFFECT_AXIS_INFO_PARAM));
			plotPanel.add("Left", effectAxis);
			
				HorizAxis seasonAxis = new HorizAxis(this);
//				Value[] seasonNames = seasonTimeAxis.getSeasonNames();
				CatVariable tempSeasons = new CatVariable("temp");
				String seasonString = getParameter(SEASON_PARAM);
				seasonString = seasonString.substring(seasonString.indexOf(' ') + 1);
																			//		first item in string is no of seasons
				tempSeasons.readLabels(seasonString);
				seasonAxis.setCatLabels(tempSeasons);
				
			plotPanel.add("Bottom", seasonAxis);
				
				SeasonalEffectView thePlot = new SeasonalEffectView(data, this, seasonAxis, effectAxis,
																																		"effect", seasonTimeAxis);
				thePlot.lockBackground(Color.white);
				
			plotPanel.add("Center", thePlot);
			
		thePanel.add("Center", plotPanel);
		
		return thePanel;
	}
	
	protected XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		XLabel heading = new XLabel(translate("Moving averages to remove seasonal effect"), XLabel.LEFT, this);
		heading.setFont(getStandardBoldFont());
		thePanel.add(heading);
		return thePanel;
	}
	
	protected String getCrossKey() {
		return "y";
	}
	
	protected String[] getLineKeys() {
		String keys[] = {"y", "smooth"};
		return keys;
	}
	
	protected boolean showDataValue() {
		return false;
	}
	
	protected boolean showSmoothedValue() {
		return false;
	}
}