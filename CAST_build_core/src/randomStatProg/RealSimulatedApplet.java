package randomStatProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import boxPlot.*;
import random.RandomNormal;


public class RealSimulatedApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String SIM_NAME_PARAM = "simName";
	
	private DataSet data;
	private RandomNormal generator;
	
	private XButton takeSampleButton;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout());
		
		XPanel dataPanel = new XPanel();
		dataPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL,
																								ProportionLayout.TOTAL));
		dataPanel.add(ProportionLayout.TOP, displayPanel(data, "y"));
		dataPanel.add(ProportionLayout.BOTTOM, displayPanel(data, "sim"));
		
		add("Center", dataPanel);
		add("East", controlPanel(data));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		NumVariable y = new NumVariable(getParameter(VAR_NAME_PARAM));
		y.readValues(getParameter(VALUES_PARAM));
		data.addVariable("y", y);
		
		String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
		generator = new RandomNormal(randomInfo);
		double vals[] = generateData();
		data.addNumVariable("sim", getParameter(SIM_NAME_PARAM), vals);
		
		return data;
	}
	
	private XPanel displayPanel(DataSet data, String variableKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		Variable v = (Variable)data.getVariable(variableKey);
		theHorizAxis.setAxisName(v.name);
		thePanel.add("Bottom", theHorizAxis);
		
		BoxAndDotView theView = new BoxAndDotView(data, this, theHorizAxis);
		theView.setActiveNumVariable(variableKey);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 15));
		
		takeSampleButton = new XButton(translate("Take sample"), this);
		controlPanel.add(takeSampleButton);
		
		return controlPanel;
	}
	
	private double[] generateData() {
		return generator.generate();
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			double vals[] = generateData();
			((NumVariable)data.getVariable("sim")).setValues(vals);
			data.variableChanged("sim");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}