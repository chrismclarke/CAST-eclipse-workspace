package boxPlotProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import coreVariables.*;

import boxPlot.*;


public class WeightedBoxApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String VAR_NAME2_PARAM = "varName2";
	static final private String VALUES2_PARAM = "values2";
	static final private String OUTLIERS_PARAM = "showOutliers";
	static final private String WEIGHT_PARAM = "initialWt";
	
	static final private int kMaxSteps = 40;
	
	private DataSet data;
	
	private SimpleBoxDotView theView;
	
	private XNoValueSlider weightSlider;
	private XChoice boxPlotDisplayChoice;
	private int currentBoxPlotDisplay = 0;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout());
			
		String outliersString = getParameter(OUTLIERS_PARAM);
		if (outliersString != null && outliersString.equals("choice"))
			add("North", displayChoicePanel());
			
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y1", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		data.addNumVariable("y2", getParameter(VAR_NAME2_PARAM), getParameter(VALUES2_PARAM));
		
		String wtString = getParameter(WEIGHT_PARAM);
		double wt = Double.parseDouble(wtString);
		WeightedMeanVariable weighted = new WeightedMeanVariable("weighted", data, "y1", "y2", wt);
		data.addVariable("y3", weighted);
		
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		
		theView = new SimpleBoxDotView(data, this, theHorizAxis);
		theView.lockBackground(Color.white);
		theView.setShadeDotPlot(true);
		theView.setActiveNumVariable("y3");
		String outliersString = getParameter(OUTLIERS_PARAM);
		if (outliersString != null)
			theView.setShowOutliers(outliersString.equals("true"));
		
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(30, 0);
		thePanel.setLayout(new BorderLayout());
		weightSlider = new XNoValueSlider(getParameter(VAR_NAME_PARAM),
										getParameter(VAR_NAME2_PARAM), null, 0, kMaxSteps, 0, this);
		thePanel.add("Center", weightSlider);
		return thePanel;
	}
	
	private XPanel displayChoicePanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			boxPlotDisplayChoice = new XChoice(this);
			boxPlotDisplayChoice.addItem(translate("Basic box plot"));
			boxPlotDisplayChoice.addItem(translate("Box plot showing outliers"));
		thePanel.add("Center", boxPlotDisplayChoice);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == weightSlider) {
			int value = weightSlider.getValue();
			WeightedMeanVariable weighted = (WeightedMeanVariable)data.getVariable("y3");
			weighted.setWeight(((double)(kMaxSteps - value)) / kMaxSteps);
			data.variableChanged("y3");
			return true;
		}
		else if (target == boxPlotDisplayChoice) {
			int newChoice = boxPlotDisplayChoice.getSelectedIndex();
			if (newChoice != currentBoxPlotDisplay) {
				currentBoxPlotDisplay = newChoice;
				theView.setShowOutliers(newChoice == 1);
				data.variableChanged("y3");
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