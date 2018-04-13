package glmAnovaProg;

import java.awt.*;

import dataView.*;
import utils.*;


public class PolynomialApplet extends AnovaPolynomialApplet {
	private XChoice polyOrderChoice;
	private int currentOrder;
	
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = super.displayPanel(data);
		dataView.setShowLowerPower(false);
		dataView.setLastExplanatory(-1);
		return thePanel;
	}
	
	protected XPanel anovaTablePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XLabel orderLabel = new XLabel(translate("Polynomial degree"), XLabel.LEFT, this);
			orderLabel.setFont(getStandardBoldFont());
		thePanel.add(orderLabel);
		
			polyOrderChoice = new XChoice(this);
			for (int i=0 ; i<polyDegree ; i++)
				if (i == 0)
					polyOrderChoice.addItem("0 (" + translate("Constant") + ")");
				else if (i == 1)
					polyOrderChoice.addItem("1 (" + translate("Linear") + ")");
				else if (i == 2)
					polyOrderChoice.addItem("2 (" + translate("Quadratic") + ")");
				else if (i == 3)
					polyOrderChoice.addItem("3 (" + translate("Cubic") + ")");
				else
					polyOrderChoice.addItem(String.valueOf(i));
		thePanel.add(polyOrderChoice);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == polyOrderChoice) {
			int newChoice = polyOrderChoice.getSelectedIndex();
			if (newChoice != currentOrder) {
				currentOrder = newChoice;
				dataView.setLastExplanatory(newChoice - 1);
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}