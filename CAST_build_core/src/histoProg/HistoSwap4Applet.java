package histoProg;

import java.awt.*;

import dataView.*;
import utils.*;
import histo.*;
import valueList.OneValueView;


public class HistoSwap4Applet extends VariableClass2Applet {
	static final private String INIT_GROUPING2_PARAM = "grouping2Info";
	static final private String INIT_GROUPING3_PARAM = "grouping3Info";
	static final protected String GROUPING_NAMES_PARAM = "groupingNames";
	static final protected String BOXING_PARAM = "boxing";
	
	protected XChoice groupingChoice;
	private int currentGrouping;
	private XCheckbox horizLinesCheck;
	
	protected XPanel createControls(DataSet data) {
		boolean boxingCheck, initialBoxState;
		String boxingString = getParameter(BOXING_PARAM);
		if (boxingString == null) {
			boxingCheck = false;
			initialBoxState = false;
		}
		else {
			boxingCheck = true;
			initialBoxState = boxingString.equals("true");
		}
		
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new BorderLayout(0, 0));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		
				groupingChoice = new XChoice(this);
				LabelEnumeration theValues = new LabelEnumeration(getParameter(GROUPING_NAMES_PARAM));
				groupingChoice.addItem((String)theValues.nextElement());
				groupingChoice.addItem((String)theValues.nextElement());
				groupingChoice.addItem((String)theValues.nextElement());
				groupingChoice.select(1);						//	standard grouping
				currentGrouping = 1;
			leftPanel.add(groupingChoice);
		
			if (boxingCheck) {
				horizLinesCheck = new XCheckbox(translate("Box values"), this);
				horizLinesCheck.setState(initialBoxState);
				leftPanel.add(horizLinesCheck);
			}
		controlPanel.add("West", leftPanel);
		
			XPanel rightPanel = new InsetPanel(0, 0, 8, 0);
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.RIGHT, VerticalLayout.VERT_BOTTOM, 0));
				
				OneValueView theValue = new OneValueView(data, "y", this);
				theValue.addEqualsSign();
			rightPanel.add(theValue);
			
		controlPanel.add("Center", rightPanel);
		
		return controlPanel;
	}
	
	private boolean localAction(Object target) {
		if (target == groupingChoice) {
			if (currentGrouping != groupingChoice.getSelectedIndex()) {
				currentGrouping = groupingChoice.getSelectedIndex();
				String groupingInfo;
				switch (currentGrouping) {
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
			}
			return true;
		}
		else if (target == horizLinesCheck) {
			int newBoxing = horizLinesCheck.getState() ? HistoView.BOTH_BARS : HistoView.VERT_BARS;
			theHisto.setBarType(newBoxing);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}