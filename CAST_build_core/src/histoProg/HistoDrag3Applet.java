package histoProg;

import java.awt.*;

import dataView.*;
import valueList.ProportionView;
import axis.*;
import utils.*;

import histo.*;


public class HistoDrag3Applet extends VariableClass2Applet {
	static final private String INIT_GROUPING2_PARAM = "grouping2Info";
	static final private String INIT_GROUPING3_PARAM = "grouping3Info";
	static final protected String GROUPING_NAMES_PARAM = "groupingNames";
	
	protected XChoice groupingChoice;
	private int currentGrouping;
	
	protected VariableClassHistoView createHistoView(DataSet data, HorizAxis theHorizAxis,
										DensityAxis2 densityAxis, double class0Start, double classWidth) {
		DragClassHistoView theView = new DragClassHistoView(data, this, theHorizAxis, densityAxis, class0Start, classWidth);
		theView.setFont(getBigBoldFont());
		return theView;
	}
	
	protected XPanel createControls(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new BorderLayout(0, 0));
		
			groupingChoice = new XChoice(this);
			LabelEnumeration theValues = new LabelEnumeration(getParameter(GROUPING_NAMES_PARAM));
			groupingChoice.addItem((String)theValues.nextElement());
			groupingChoice.addItem((String)theValues.nextElement());
			groupingChoice.addItem((String)theValues.nextElement());
			groupingChoice.select(1);						//	standard grouping
			currentGrouping = 1;
		controlPanel.add("West", groupingChoice);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				ProportionView proportion = new ProportionView(data, "y", this);
				proportion.setFont(getStandardBoldFont());
			rightPanel.add(proportion);
			
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
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}