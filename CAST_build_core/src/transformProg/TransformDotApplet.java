package transformProg;

import java.awt.*;

import dataView.*;
import axis.*;
import boxPlot.*;
import random.RandomNormal;


public class TransformDotApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String RANDOM_NORMAL_PARAM = "random";
	
	TransformHorizAxis theHorizAxis;
//	private XChoice axisValueType;
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new BorderLayout());
		add("Center", createView(data));
		
		XPanel controlPanel = createControls(data);
		if (controlPanel != null)
			add("South", controlPanel);
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
		RandomNormal generator = new RandomNormal(randomInfo);
		double vals[] = generator.generate();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), vals);
		return data;
	}
	
	private XPanel createView(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		theHorizAxis = new TransformHorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
//		theHorizAxis.setTransValueDisplay(NumCatAxis.TRANS_VALUES);
		thePanel.add("Bottom", theHorizAxis);
		
		DataView theView = coreView(data, theHorizAxis);
		theHorizAxis.setLinkedData(data, true);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	protected DataView coreView(DataSet data, HorizAxis theHorizAxis) {
		return new BoxAndDotView(data, this, theHorizAxis);
	}
	
	protected XPanel createControls(DataSet data) {
		return null;
	}
	
/*
	protected XPanel createControls(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		
		axisValueType = new XChoice(this);
		axisValueType.addItem("Transformed values");
		axisValueType.addItem("Raw values");
		axisValueType.select(0);
		controlPanel.add(axisValueType);
		
		return controlPanel;
	}
	
	public boolean action(Event evt, Object what) {
		if (evt.target == axisValueType) {
			theHorizAxis.setTransValueDisplay(axisValueType.getSelectedIndex() == 1);
			return true;
		}
		return false;
	}
*/
}