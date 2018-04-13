package transformProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.OneValueView;


public class PowerTransformApplet extends DotLabelApplet {
//	private XChoice jitterStackChoice;
	private XChoice axisValueType;
	
	TransformHorizAxis theHorizAxis;
	
	protected XPanel bottomPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 8, 0, 0);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
//		addStackJitterChoice(thePanel);
		
		axisValueType = new XChoice(this);
		axisValueType.addItem(translate("Transformed values"));
		axisValueType.addItem(translate("Raw values"));
		axisValueType.select(0);
		thePanel.add(axisValueType);
		
		return thePanel;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 0, 0, 6);
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 2));
			OneValueView labelValue = new OneValueView(data, "label", this);
			labelValue.addEqualsSign();
		thePanel.add(labelValue);
		
			OneValueView yValue = new OneValueView(data, "y", this);
			yValue.addEqualsSign();
		thePanel.add(yValue);
		return thePanel;
	}
	
	protected XPanel createView(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		theHorizAxis = new TransformHorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		theHorizAxis.setTransValueDisplay(NumCatAxis.TRANS_VALUES);
		thePanel.add("Bottom", theHorizAxis);
		
		DataView theView = coreView(data, theHorizAxis);
		theHorizAxis.setLinkedData(data, true);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == axisValueType) {
			theHorizAxis.setTransValueDisplay(axisValueType.getSelectedIndex() == 1);
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