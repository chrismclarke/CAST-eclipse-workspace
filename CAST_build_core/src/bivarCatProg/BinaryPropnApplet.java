package bivarCatProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;

import bivarCat.*;


public class BinaryPropnApplet extends XApplet {
	static final protected String X_VAR_NAME_PARAM = "xVarName";
	static final protected String X_VALUES_PARAM = "xValues";
	static final protected String X_LABELS_PARAM = "xLabels";
	
	static final protected String Y_VAR_NAME_PARAM = "yVarName";
	static final protected String Y_VALUES_PARAM = "yValues";
	static final private String Y_LABELS_PARAM = "yLabels";
	
	static final protected String MAX_PROB_PARAM = "maxProb";
	static final private String PROPN_AXIS_INFO_PARAM = "propnAxis";
	
	private XLabel propnLabel;
	private CatVariable xCatVariable, yCatVariable;
	
	private BinaryBarView barView;
	private XChoice scaleChoice;
	private int currentScaleChoice = 0;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(7, 0));
		
		add("Center", displayPanel(data));
		
		add("East", keyPanel(data));
		
		add("South", controlPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		xCatVariable = new CatVariable(getParameter(X_VAR_NAME_PARAM),
																							Variable.USES_REPEATS);
		xCatVariable.readLabels(getParameter(X_LABELS_PARAM));
		xCatVariable.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("x", xCatVariable);
		
		yCatVariable = new CatVariable(getParameter(Y_VAR_NAME_PARAM), Variable.USES_REPEATS);
		yCatVariable.readLabels(getParameter(Y_LABELS_PARAM));
		yCatVariable.readValues(getParameter(Y_VALUES_PARAM));
		data.addVariable("y", yCatVariable);
		
		return data;
	}
	
	private XPanel keyPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			CatKey2View theKey = new CatKey2View(data, this, "y", ContinTableView.OUTER);
			theKey.setColors(BinaryBarView.catColors);
			theKey.setReversedCategories();
			
			barView.setLinkedKey(theKey);
			
		thePanel.add(theKey);
		return thePanel;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("Center", barchartPanel(data));
		
			propnLabel = new XLabel("", XLabel.LEFT, this);
			setVertAxisLabel(true);
			propnLabel.setFont(getStandardBoldFont());
		thePanel.add("North", propnLabel);
		
			XLabel xCatLabel = new XLabel(xCatVariable.name, XLabel.RIGHT, this);
			xCatLabel.setFont(getStandardBoldFont());
		thePanel.add("South", xCatLabel);
		
		return thePanel;
	}
	
	private void setVertAxisLabel(boolean zeroOneScale) {
		propnLabel.setText(zeroOneScale ? translate("Proportions")
																		: translate("Proportion") + " (" + yCatVariable.getLabel(0).toString() + ")");
	}
	
	private XPanel barchartPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			VertAxis vertAxis = new VertAxis(this);
			vertAxis.readNumLabels(getParameter(PROPN_AXIS_INFO_PARAM));
		thePanel.add("Left", vertAxis);
		
			HorizAxis catAxis = new HorizAxis(this);
			catAxis.setCatLabels(xCatVariable);
		thePanel.add("Bottom", catAxis);
		
			double maxProbScale = Double.parseDouble(getParameter(MAX_PROB_PARAM));
			barView = new BinaryBarView(data, this, vertAxis, catAxis, "x", "y", maxProbScale);
			barView.lockBackground(Color.white);
		thePanel.add("Center", barView);
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
			scaleChoice = new XChoice(this);
			scaleChoice.addItem(translate("Stacked bar chart"));
			scaleChoice.addItem(translate("Propns for") + " " + yCatVariable.getLabel(0).toString());
		
		thePanel.add(scaleChoice);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == scaleChoice) {
			if (scaleChoice.getSelectedIndex() != currentScaleChoice) {
				currentScaleChoice = scaleChoice.getSelectedIndex();
				barView.animateChange(currentScaleChoice == 0);
				setVertAxisLabel(currentScaleChoice == 0);
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