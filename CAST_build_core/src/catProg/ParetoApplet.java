package catProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;

import cat.*;


public class ParetoApplet extends XApplet {
	static final private String PROPN_AXIS_INFO_PARAM = "propnAxis";
	static final private String MAX_PROB_PARAM = "maxProb";
	
	private ParetoView barView;
	private XCheckbox scaleCheck;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(7, 0));
		
		add("Center", displayPanel(data));
		
		add("South", controlPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		CatVariable yCatVariable = new CatVariable(getParameter(CAT_NAME_PARAM), Variable.USES_REPEATS);
		yCatVariable.readLabels(getParameter(CAT_LABELS_PARAM));
		yCatVariable.readValues(getParameter(CAT_VALUES_PARAM));
		data.addVariable("y", yCatVariable);
		
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("Center", barchartPanel(data));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout());
			
				XLabel propnLabel = new XLabel(translate("Proportion"), XLabel.LEFT, this);
			topPanel.add("West", propnLabel);
				XLabel cumLabel = new XLabel(translate("Cumulative proportion"), XLabel.RIGHT, this);
			topPanel.add("East", cumLabel);
			
		thePanel.add("North", topPanel);
		
		return thePanel;
	}
	
	private XPanel barchartPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			VertAxis propnAxis = new VertAxis(this);
			propnAxis.readNumLabels(getParameter(PROPN_AXIS_INFO_PARAM));
		thePanel.add("Left", propnAxis);
		
			VertAxis cumAxis = new VertAxis(this);
			cumAxis.readNumLabels(getParameter(PROPN_AXIS_INFO_PARAM));
		thePanel.add("Right", cumAxis);
		
			HorizAxis catAxis = new HorizAxis(this);
			CatVariable yCatVariable = (CatVariable)data.getVariable("y");
			catAxis.setCatLabels(yCatVariable);
			catAxis.setAxisName(yCatVariable.name);
		thePanel.add("Bottom", catAxis);
		
			double maxProbScale = Double.parseDouble(getParameter(MAX_PROB_PARAM));
			barView = new ParetoView(data, this, "y", catAxis, propnAxis, cumAxis, maxProbScale);
			barView.lockBackground(Color.white);
		thePanel.add("Center", barView);
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
			scaleCheck = new XCheckbox(translate("Separate scale for cumulative propns"), this);
		
		thePanel.add(scaleCheck);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == scaleCheck) {
			barView.animateChange(scaleCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}