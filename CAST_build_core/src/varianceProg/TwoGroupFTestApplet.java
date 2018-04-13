package varianceProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;
import coreGraphics.*;

import variance.*;


public class TwoGroupFTestApplet extends TwoGroupFApplet {
	
	protected XPanel dataPanel(GroupsDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
		thePanel.add("Center", super.dataPanel(data));
		
			CatVariable groupVariable = data.getCatVariable();
			XLabel groupLabel = new XLabel(groupVariable.name, XLabel.LEFT, this);
			
		thePanel.add("North", groupLabel);
		
		return thePanel;
	}
	
	protected XPanel fDistnPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
			
			HorizAxis ssqAxis = new HorizAxis(this);
			ssqAxis.readNumLabels(getParameter(SUMMARY_AXIS_PARAM));
			ssqAxis.setAxisName("F ratio");
		thePanel.add("Bottom", ssqAxis);
		
			AccurateTailAreaView theView = new AccurateTailAreaView(summaryData, this, ssqAxis, "fDistn");
			theView.setActiveNumVariable("F");
			theView.setDistnLabel(new LabelValue("F(" + numerDF + ", " + denomDF + ")"), Color.gray);
			theView.setTailType(AccurateTailAreaView.TWO_TAILED);
			theView.setDensityScaling(0.9);
			theView.setShowValueArrow(false);
			theView.lockBackground(Color.white);
			
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
		thePanel.add(new VariancePValueView(summaryData, "F", "fDistn", this));
		return thePanel;
	}
}