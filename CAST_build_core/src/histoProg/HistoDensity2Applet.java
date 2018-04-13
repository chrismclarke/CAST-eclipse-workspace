package histoProg;

import java.awt.*;

import dataView.*;
import utils.*;
import valueList.*;

import histo.*;


public class HistoDensity2Applet extends VariableClass2Applet {

	private XChoice axisTypeChoice;
	private int currentAxisType;
	
	protected XPanel createControls(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		
		axisTypeChoice = new XChoice(this);
		axisTypeChoice.addItem(translate("Frequency"));
		axisTypeChoice.addItem(translate("Relative frequency"));
		axisTypeChoice.addItem(translate("Density"));
//		axisTypeChoice.addItem("No labels");
		axisTypeChoice.select(0);
		currentAxisType = 0;
		controlPanel.add(axisTypeChoice);
		
		OneValueView theValue = new OneValueView(data, "y", this);
		theValue.addEqualsSign();
		controlPanel.add(theValue);
		
		return controlPanel;
	}
	
	private boolean localAction(Object target) {
		if (target == axisTypeChoice) {
			if (axisTypeChoice.getSelectedIndex() != currentAxisType) {
				currentAxisType = axisTypeChoice.getSelectedIndex();
				int labelType;
				switch (currentAxisType) {
					case 0:
						labelType = DensityAxis2.COUNT_LABELS;
						break;
					case 1:
						labelType = DensityAxis2.REL_FREQ_LABELS;
						break;
					case 2:
						labelType = DensityAxis2.DENSITY_LABELS;
						break;
					default:
						labelType = DensityAxis2.NO_LABELS;
				}
				theDensityAxis.changeLabelType(labelType);
				densityAxisNameLabel.setText(theDensityAxis.getAxisName());
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