package ssqProg;

import java.awt.*;

import dataView.*;
import utils.*;
import formula.*;


import ssq.*;

public class ComponentsR2Applet extends ComponentsSsqApplet {
	
	static final private NumValue kMaxRSquared = new NumValue(1.0, 3);
	
	private XButton sampleButton;
	
	protected XPanel leftControlPanel(DataSet data, double initialR2) {
		XPanel thePanel = super.leftControlPanel(data, initialR2);
		
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				sampleButton = new XButton(translate("Another data set"), this);
			
			buttonPanel.add(sampleButton);
		
		thePanel.add(buttonPanel);
		return thePanel;
	}
	
	protected XPanel ssqPanel(DataSet data) {
		XPanel thePanel = super.ssqPanel(data);
		
			FormulaContext bigContext = new FormulaContext(Color.black, getBigFont(), this);
			thePanel.add(new RSquaredPanel(summaryData, "explained", "total", "rSquared",
																					maxSsq, kMaxRSquared, xNumNotCat, bigContext));
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
//		AdjustedSsqVariable yVar = (AdjustedSsqVariable)data.getVariable("y");
//		yVar.noteVariableChange("z");
		summaryData.takeSample();
		return true;
	}
	return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}