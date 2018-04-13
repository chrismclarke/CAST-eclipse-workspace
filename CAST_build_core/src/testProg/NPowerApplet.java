package testProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import distn.*;


public class NPowerApplet extends DecisionsApplet {
	static final private String SIG_LEVEL_PARAM = "significanceLevel";
	static final private String CRITICAL_MEAN_DECIMALS_PARAM = "criticalMeanDecimals";
	
	private double sigLevel;
	
	private NumValue nLow, nHigh, nStart;
	
	private ParameterSlider nSlider;
	private XLabel criticalMean;
	
	protected void readNullDistn() {
		StringTokenizer st = new StringTokenizer(getParameter(NULL_HYPOTH_PARAM));
		nullMean = new NumValue(st.nextToken());
		popnSd = new NumValue(st.nextToken());
		
		st = new StringTokenizer(getParameter(SAMPLE_SIZE_PARAM));
		nLow = new NumValue(st.nextToken());
		nHigh = new NumValue(st.nextToken());
		nStart = new NumValue(st.nextToken());
		
		meanSd = new NumValue(popnSd.toDouble() / Math.sqrt(nStart.toDouble()), 9);
	}
	
	protected void readCriticalValues() {
		sigLevel = Double.parseDouble(getParameter(SIG_LEVEL_PARAM));
		
		int criticalDecimals = Integer.parseInt(getParameter(CRITICAL_MEAN_DECIMALS_PARAM));
		criticalStart = new NumValue(getCriticalMean(), criticalDecimals);
	}
	
	protected XPanel criticalValuePanel() {
		XPanel thePanel = new InsetPanel(20, 0);
		thePanel.setLayout(new BorderLayout(30, 0));
		
			nSlider = new ParameterSlider(nLow, nHigh, nStart, "", this);
			nSlider.setTitle("n", this);
			nSlider.setFont(getStandardBoldFont());
			nSlider.setForeground(kDecisionColor);
			
		thePanel.add("Center", nSlider);
		
			XPanel headingPanel = new XPanel();
			headingPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																															VerticalLayout.VERT_CENTER, 0));
				XLabel heading = new XLabel(translate("Sample size") + ":", XLabel.LEFT, this);
				heading.setFont(getBigBoldFont());
				heading.setForeground(kDecisionColor);
			headingPanel.add(heading);
			
		thePanel.add("West", headingPanel);
		
			XPanel criticalMeanPanel = new InsetPanel(50, 0, 0, 0);
			criticalMeanPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																															VerticalLayout.VERT_CENTER, 0));
				XLabel criticalLabel = new XLabel(translate("Critical value for mean") + ":", XLabel.CENTER, this);
				criticalLabel.setFont(getBigBoldFont());
				criticalLabel.setForeground(kDecisionColor);
			criticalMeanPanel.add(criticalLabel);
			
				criticalMean = new XLabel(criticalStart.toString(), XLabel.CENTER, this);
				criticalMean.setFont(getBigBoldFont());
				criticalMean.setForeground(kDecisionColor);
			criticalMeanPanel.add(criticalMean);
			
		thePanel.add("East", criticalMeanPanel);
		
		return thePanel;
	}
	
	private void setSampleSize(double n) {
		meanSd.setValue(popnSd.toDouble() / Math.sqrt(n));
		
		NormalDistnVariable nullUpper = (NormalDistnVariable)data.getVariable("nullUpper");
		nullUpper.setSD(meanSd.toDouble());
		
		NormalDistnVariable nullLower = (NormalDistnVariable)data.getVariable("nullLower");
		nullLower.setSD(meanSd.toDouble());
		
		NormalDistnVariable altUpper = (NormalDistnVariable)data.getVariable("altUpper");
		altUpper.setSD(meanSd.toDouble());
		
		NormalDistnVariable altLower = (NormalDistnVariable)data.getVariable("altLower");
		altLower.setSD(meanSd.toDouble());
	}
	
	protected double getCriticalMean() {
		double z = NormalTable.quantile(1.0 - sigLevel);
		
		return nullMean.toDouble() + z * meanSd.toDouble();
	}

	
	private boolean localAction(Object target) {
		if (target == nSlider) {
			setSampleSize(nSlider.getParameter().toDouble());
			criticalStart.setValue(getCriticalMean());
			setCriticalMeanValue(criticalStart.toDouble());
			criticalMean.setText(criticalStart.toString());
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}