package statistic2Prog;

import java.awt.*;

import dataView.*;
import utils.*;
import statistic2.*;
import formula.*;
import valueList.ProportionView;


public class StDevnApplet extends BoxDotStatApplet {
	private XChoice multipleChoice;
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = super.displayPanel(data);
		theView.setSpreadStat(SpreadCalculator.STDEV);
		theView.setSDMultiplier(1.0);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_TOP, 20));
		
		controlPanel.add(distnChoicePanel(data));
		controlPanel.add(sampleButtonPanel(data));
		
		return controlPanel;
	}
	
	protected XPanel proportionPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 4));
		
			multipleChoice = new XChoice(this);
			String plusMinusString = MText.expandText(" #plusMinus# ");
			multipleChoice.addItem(translate("Mean") + plusMinusString + "SD");
			multipleChoice.addItem(translate("Mean") + plusMinusString + "2 SD");
			multipleChoice.addItem(translate("Mean") + plusMinusString + "3 SD");
			multipleChoice.select(0);
		thePanel.add(multipleChoice);
		
			ProportionView propView = new ProportionView(data, "y", this);
			propView.setFont(getStandardBoldFont());
		thePanel.add(propView);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == multipleChoice) {
			int multipleIndex = multipleChoice.getSelectedIndex();
			double multiple = (multipleIndex == 0) ? 1.0 : (multipleIndex == 1) ? 2.0 : 3.0;
			theView.setSDMultiplier(multiple);
			theView.repaint();
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