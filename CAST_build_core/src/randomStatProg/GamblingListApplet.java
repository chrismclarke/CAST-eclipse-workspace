package randomStatProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.ScrollValueList;
import random.RandomNormal;
import coreGraphics.*;


public class GamblingListApplet extends XApplet {
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String DECIMALS_PARAM = "decimals";
	static final private String VERT_AXIS_PARAM = "vertAxis";
	
	private RandomNormal generator;
	private DataSet data;
	
	private XButton takeSampleButton;
	
	public void setupApplet() {
//		ScrollImages.loadScroll(this);
		
		data = readData();
		
		setLayout(new BorderLayout(10, 0));
		
		XPanel dataPanel = new XPanel();
		dataPanel.setLayout(new ProportionLayout(0.75, 10, ProportionLayout.HORIZONTAL,
																						ProportionLayout.TOTAL));
		dataPanel.add("Left", valueList(data));
		dataPanel.add("Right", dotPlotPanel(data));
		
		add("Center", dataPanel);
		
		add("East", buttonPanel());
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
		data.addLabelVariable("label", getParameter(LABEL_NAME_PARAM), getParameter(LABELS_PARAM));
		
		String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
		generator = new RandomNormal(randomInfo);
		double vals[] = generateData();
		
		NumVariable y = new NumVariable(getParameter(VAR_NAME_PARAM));
		y.setValues(vals);
		int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
		y.setDecimals(decimals);
		data.addVariable("y", y);
		
		return data;
	}
	
	private double[] generateData() {
		return generator.generate();
	}
	
	private XPanel dotPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		VertAxis vertAxis = new VertAxis(this);
		vertAxis.readNumLabels(getParameter(VERT_AXIS_PARAM));
		
		thePanel.add("Left", vertAxis);
		
		StackedDotPlotView theView = new StackedDotPlotView(data, this, vertAxis);
		theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	private ScrollValueList valueList(DataSet data) {
		ScrollValueList theList = new ScrollValueList(data, this, ScrollValueList.HEADING);
		theList.addVariableToList("y", ScrollValueList.INVERSE_RANK);
		theList.addVariableToList("y", ScrollValueList.RAW_VALUE);
		theList.addVariableToList("label", ScrollValueList.RAW_VALUE);
		theList.sortByVariable("y", ScrollValueList.SMALL_LAST);
		return theList;
	}
	
	private XPanel buttonPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		takeSampleButton = new XButton(translate("Sample"), this);
		thePanel.add(takeSampleButton);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			double vals[] = generateData();
			data.getNumVariable().setValues(vals);
			data.variableChanged("y");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}