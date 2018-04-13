package histoProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import valueList.OneValueView;

import histo.*;


public class HistoSwapApplet extends VariableClassHistoApplet {
	static final private String INIT_GROUPING2_PARAM = "grouping2Info";
	static final private String CHECK_NAME_PARAM = "checkbox";
	
	private XCheckbox swapCheck;
	
	protected int initialDensityAxisLabel() {
		return DensityAxis.NO_LABELS;
	}
	
	protected VariableClassHistoView createHistoView(DataSet data, HorizAxis theHorizAxis,
										DensityAxis densityAxis, double class0Start, double classWidth) {
		VariableClassHistoView histoView = super.createHistoView(data, theHorizAxis,
																	densityAxis, class0Start, classWidth);
		histoView.setRetainLastSelection(true);
		return histoView;
	}
	
	protected XPanel createControls(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 2));
		
		OneValueView theValue = new OneValueView(data, "y", this);
		theValue.addEqualsSign();
		controlPanel.add(theValue);
		
		String checkName = getParameter(CHECK_NAME_PARAM);
		swapCheck = new XCheckbox(checkName, this);
		controlPanel.add(swapCheck);
		swapCheck.setState(false);
		
		return controlPanel;
	}
	
	private boolean localAction(Object target) {
		if (target == swapCheck) {
			String groupingInfo =  getParameter(swapCheck.getState()
																? INIT_GROUPING2_PARAM : INIT_GROUPING_PARAM);
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