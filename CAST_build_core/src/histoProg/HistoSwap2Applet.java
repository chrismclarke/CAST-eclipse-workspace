package histoProg;

import java.awt.*;

import dataView.*;
import utils.*;
import histo.*;
import valueList.OneValueView;


public class HistoSwap2Applet extends VariableClassHistoApplet {
	static final private String INIT_GROUPING2_PARAM = "grouping2Info";
	static final private String INIT_GROUPING3_PARAM = "grouping3Info";
	static final protected String GROUPING_NAMES_PARAM = "groupingNames";
	
	protected XChoice groupingChoice;
//	private XCheckbox horizLinesCheck;
	
	protected int initialDensityAxisLabel() {
		return DensityAxis.DENSITY_LABELS;
	}
	
	protected XPanel createControls(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 2));
		
		groupingChoice = new XChoice(this);
		LabelEnumeration theValues = new LabelEnumeration(getParameter(GROUPING_NAMES_PARAM));
		groupingChoice.addItem((String)theValues.nextElement());
		groupingChoice.addItem((String)theValues.nextElement());
		groupingChoice.addItem((String)theValues.nextElement());
		controlPanel.add(groupingChoice);
		groupingChoice.select(1);						//	standard grouping
		
		OneValueView theValue = new OneValueView(data, "y", this);
		controlPanel.add(theValue);
		
		return controlPanel;
	}
	
	private boolean localAction(Object target) {
		if (target == groupingChoice) {
			String groupingInfo;
			switch (groupingChoice.getSelectedIndex()) {
				case 0:
					groupingInfo = getParameter(INIT_GROUPING2_PARAM);
					break;
				case 1:
					groupingInfo = getParameter(INIT_GROUPING_PARAM);
					break;
				default:
					groupingInfo = getParameter(INIT_GROUPING3_PARAM);
					break;
			}
			theHisto.setGrouping(groupingInfo);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}