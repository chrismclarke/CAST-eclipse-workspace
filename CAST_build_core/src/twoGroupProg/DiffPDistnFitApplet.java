package twoGroupProg;

import java.awt.*;

import dataView.*;
import distn.*;
import utils.*;
import models.*;

import twoGroup.*;
import bivarCat.*;

public class DiffPDistnFitApplet extends DiffDistnFitApplet {
	static final private String kMaxSummaryString = "999.99";
	
	private ContinFitView theView;
	private XCheckbox proportionCheck;
	
	protected CoreModelDataSet getData() {
		return new ContinTableDataSet(this);
	}
	
	protected String getDifferenceName() {
		return "Difference in proportions";
	}
	
	protected void setFittedDistn(CoreModelDataSet sourceData, DataSet differenceData) {
		ContinTableDataSet continData = (ContinTableDataSet)sourceData;
		double p1 = continData.getPropn(0);
		double p2 = continData.getPropn(1);
		double pDiff = p2 - p1;
		double pDiffSD = Math.sqrt(p1 * (1.0 - p1) / continData.getN(0) + p2 * (1.0 - p2) / continData.getN(1));
		
		NumVariable pDiffVar = (NumVariable)differenceData.getVariable("diff");
		NormalDistnVariable fitDistn = (NormalDistnVariable)differenceData.getVariable("fit");
		
		pDiffVar.setValueAt(new NumValue(pDiff, continData.getSummaryDecimals()), 0);
		fitDistn.setMean(pDiff);
		fitDistn.setSD(pDiffSD);
		fitDistn.setDecimals(continData.getSummaryDecimals());
	}
	
	protected XPanel displayPanel(CoreModelDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.8, 5, ProportionLayout.VERTICAL,
																								ProportionLayout.TOTAL));
			theView = new ContinFitView(data, this, "x", "y", null, data.getSummaryDecimals(), 0);
		
		thePanel.add(ProportionLayout.TOP, theView);
		
			XPanel controlPanel = new XPanel();
			controlPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				proportionCheck = new XCheckbox(translate("Proportions"), this);
				controlPanel.add(proportionCheck);
				
		thePanel.add(ProportionLayout.BOTTOM, controlPanel);	
		return thePanel;
	}
	
	protected XPanel differencePanel(CoreModelDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
		GroupSummary2View diffHat = new GroupSummary2View(data, this, GroupSummary2View.PI_DIFF_HAT,
																											kMaxSummaryString, data.getSummaryDecimals());
		diffHat.setForeground(Color.red);
		thePanel.add(diffHat);
		
		GroupSummary2View diffSDHat = new GroupSummary2View(data, this, GroupSummary2View.SD_PDIFF_HAT,
																											kMaxSummaryString, data.getSummaryDecimals());
		diffSDHat.setForeground(Color.red);
		thePanel.add(diffSDHat);
		
		return thePanel;
	}
	
	protected XPanel getGroupSummaryPanel(CoreModelDataSet data, int group) {
		return new GroupPSummaryPanel(this, (ContinTableDataSet)data, group, GroupSummaryPanel.VERTICAL);
	}

	
	private boolean localAction(Object target) {
		if (target == proportionCheck) {
			theView.setDisplayType(TwoWayView.XMAIN,
						proportionCheck.getState() ? TwoWayView.PROPN_IN_X : TwoWayView.COUNT, false);
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