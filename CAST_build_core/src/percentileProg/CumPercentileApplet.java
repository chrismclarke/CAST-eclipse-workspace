package percentileProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;

import percentile.*;


public class CumPercentileApplet extends DragCumPercentApplet {
	static final private String kPercentageAxis = "0 100 0 25";
	static final private String kSingleValueAxis = "0 100 50 100";
																						//	only shows a single value, initialised later
	static final private String kZeroOneAxis = "0 1 3 1";						//		
	
	protected DataSet getReferenceData(DataSet data) {
		return null;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
			XLabel propnLabel = new XLabel(translate("Cumulative percentage"), XLabel.LEFT, this);
			propnLabel.setForeground(Color.red);
		thePanel.add(propnLabel);
		return thePanel;
	}
	
	protected XPanel dataDisplayPanel(DataSet data, DataSet referenceData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			horizAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
			String longVarName = getParameter(LONG_VAR_NAME_PARAM);
			horizAxis.setAxisName((longVarName == null) ? data.getVariable("y").name : longVarName);
		thePanel.add("Bottom", horizAxis);
		
			DragMultiVertAxis vertAxis = new DragMultiVertAxis(this, 3);
			vertAxis.readNumLabels(kZeroOneAxis);
			vertAxis.readExtraNumLabels(kPercentageAxis);
			vertAxis.readExtraNumLabels(kSingleValueAxis);
			vertAxis.setStartAlternate(1);
			vertAxis.setForeground(Color.red);
		thePanel.add("Left", vertAxis);
		
			cumView = new CumFunctPercentileView(data, this, horizAxis, referenceData, "ref",
																											PropnRangeView.LESS_THAN, vertAxis);
			cumView.setFont(getSmallFont());
		thePanel.add("Center", cumView);
		return thePanel;
	}
	
	protected XPanel propnCalcPanel(DataSet data, DataSet referenceData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		return thePanel;
	}
}