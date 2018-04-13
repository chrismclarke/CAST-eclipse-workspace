package inferenceProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;

import inference.*;



public class MarginOfErrorApplet extends SePGraphApplet {
	static final private String CI_INFO_PARAM = "ciAxis";
	
	private CiPropnGraphView theGraph;
	private PPlusMinusValueView pmView;
	
	private XChoice intervalTypeChoice;
	private int currentTypeChoice;
	
	protected XPanel graphPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			VertAxis ciAxis = new VertAxis(this);
			ciAxis.readNumLabels(getParameter(CI_INFO_PARAM));
			ciAxis.setForeground(kDarkRedColor);
		thePanel.add("Left", ciAxis);
		
			HorizAxis pAxis = new HorizAxis(this);
			pAxis.readNumLabels(getParameter(P_INFO_PARAM));
			pAxis.setAxisName(translate("Sample proportion") + ", p");
			pAxis.setForeground(Color.blue);
		thePanel.add("Bottom", pAxis);
		
			theGraph = new CiPropnGraphView(data, this, "x", pAxis, ciAxis);
			theGraph.lockBackground(Color.white);
		thePanel.add("Center", theGraph);
		
		return thePanel;
	}
	
	
	protected XPanel sePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
			
			intervalTypeChoice = new XChoice(this);
			intervalTypeChoice.addItem(translate("95% CI"));
			intervalTypeChoice.addItem(translate("Margin of error"));
			currentTypeChoice = 0;
		thePanel.add(intervalTypeChoice);
		
			int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
			pmView = new PPlusMinusValueView(data, this, "x", decimals);
			pmView.setForeground(kDarkRedColor);
			pmView.setFont(getBigFont());
		
		thePanel.add(pmView);
		
		return thePanel;
	}
	
	
	protected XPanel ciPanel(DataSet data) {
		return null;
	}
	
	protected void changeSampleSize(int n, int x) {
		super.changeSampleSize(n, x);
		theGraph.reset();
		data.variableChanged("x");
	}
	
	private boolean localAction(Object target) {
		if (target == intervalTypeChoice) {
			int newTypeChoiceIndex = intervalTypeChoice.getSelectedIndex();
			if (newTypeChoiceIndex != currentTypeChoice) {
				currentTypeChoice = newTypeChoiceIndex;
				theGraph.setCiNotMarginOfError(currentTypeChoice == 0);
				pmView.setCiNotMarginOfError(currentTypeChoice == 0);
			}
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