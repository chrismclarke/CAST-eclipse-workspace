package stemLeafProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import valueList.*;

import stemLeaf.*;


public class CrossAndLeafApplet extends XApplet {
	static final private String CLASS_INFO_PARAM = "classInfo";
	static final private String AXIS_INFO_PARAM = "axisInfo";
	static final private String INVERSE_PARAM = "inverse";
	
	private XChoice crossLeafChoice;
	private int currentChoice = 0;
	
	private CrossAndLeafView theView;
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new BorderLayout(0, 5));
		
		add("Center", displayPanel(data));
		add("North", valuePanel(data));
		add("South", controlPanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
			String labelName = getParameter(LABEL_NAME_PARAM);
			if (labelName != null)
				data.addLabelVariable("label", labelName, getParameter(LABELS_PARAM));
		return data;
	}
	
	private boolean isInverse() {
		String inverseString = getParameter(INVERSE_PARAM);
		return (inverseString != null) && inverseString.equals("true");
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			horizAxis.readNumLabels(labelInfo);
			horizAxis.setAxisName(data.getVariable("y").name);
			
		thePanel.add("Bottom", horizAxis);
		
			theView = new CrossAndLeafView(data, this, horizAxis, getParameter(CLASS_INFO_PARAM));
			if (isInverse())
				theView.setShowLeaves(true);
			theView.lockBackground(Color.white);
			theView.setRetainLastSelection(true);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	private XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		thePanel.add(new OneValueView(data, "y", this));
		CoreVariable labelVar = data.getVariable("label");
		if (labelVar !=  null)
			thePanel.add(new OneValueView(data, "label", this));
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		
			XLabel l = new XLabel(translate("Display values as") + ": ", XLabel.RIGHT, this);
			l.setFont(getStandardBoldFont());
		thePanel.add(l);
		
			crossLeafChoice = new XChoice(this);
			crossLeafChoice.addItem(translate("Crosses"));
			crossLeafChoice.addItem(translate("Digits"));
			if (isInverse()) {
				crossLeafChoice.select(1);
				currentChoice = 1;
			}
		
		thePanel.add(crossLeafChoice);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == crossLeafChoice) {
			if (crossLeafChoice.getSelectedIndex() != currentChoice) {
				currentChoice = crossLeafChoice.getSelectedIndex();
				
				theView.setShowLeaves(currentChoice != 0);
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