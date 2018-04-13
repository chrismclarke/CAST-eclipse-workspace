package catProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import cat.*;


public class QuantBarApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "vertAxis";
	static final private String TIME_INFO_PARAM = "timeAxis";
	static final private String TIME_NAME_PARAM = "timeAxisName";
	static final private String NO_OF_VARS_PARAM = "noOfVars";
	
	private DataSet data;
	private int noOfVariables;
	private QuantBarView theView;
	
	private MultiVertAxis theVertAxis;
	private XLabel yVariateLabel;
	private String vertAxisInfo[];
	
	private XChoice dataChoice;
	private int currentDataSet = 0;
	private XChoice viewChoice;
	private int currentView = QuantBarView.BAR_VIEW;
	
	public void setupApplet() {
		dataChoice = new XChoice(this);
		
		data = readData();
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		
		add("South", controlPanel(data));
		
		add("North", topPanel(data));
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		noOfVariables = Integer.parseInt(getParameter(NO_OF_VARS_PARAM));
		vertAxisInfo = new String[noOfVariables];
		
		String varNameString = getParameter(VAR_NAME_PARAM);
		data.addNumVariable("y", varNameString, getParameter(VALUES_PARAM));
		dataChoice.addItem(varNameString);
		vertAxisInfo[0] = getParameter(AXIS_INFO_PARAM);
		
		for (int i=2 ; i<=noOfVariables ; i++) {
			varNameString = getParameter(VAR_NAME_PARAM + i);
			data.addNumVariable("y" + i, varNameString, getParameter(VALUES_PARAM + i));
			dataChoice.addItem(varNameString);
			vertAxisInfo[i-1] = getParameter(AXIS_INFO_PARAM + i);
		}
		
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		theVertAxis = vertAxis(data);
		thePanel.add("Left", theVertAxis);
		
		IndexTimeAxis theHorizAxis = horizAxis(data);
		thePanel.add("Bottom", theHorizAxis);
		
		theView = new QuantBarView(data, this, "y", theHorizAxis, theVertAxis);
		
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	private MultiVertAxis vertAxis(DataSet data) {
		MultiVertAxis localAxis = new MultiVertAxis(this, noOfVariables);
		localAxis.readNumLabels(vertAxisInfo[0]);
		for (int i=1 ; i<noOfVariables ; i++)
			localAxis.readExtraNumLabels(vertAxisInfo[i]);
		localAxis.setChangeMinMax(true);
		return localAxis;
	}
	
	private IndexTimeAxis horizAxis(DataSet data) {
		IndexTimeAxis valueAxis = new IndexTimeAxis(this, data.getNumVariable().noOfValues());
		valueAxis.setTimeScale(getParameter(TIME_INFO_PARAM));
		
		valueAxis.setAxisName(getParameter(TIME_NAME_PARAM));
		return valueAxis;
	}
	
	private XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			yVariateLabel = new XLabel(getParameter(VAR_NAME_PARAM), XLabel.LEFT, this);
			yVariateLabel.setFont(theVertAxis.getFont());
				
		thePanel.add("Center", yVariateLabel);
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			viewChoice = new XChoice(this);
			viewChoice.addItem(translate("Bar chart"));
			viewChoice.addItem(translate("Time series"));
			
		thePanel.add(viewChoice);
		thePanel.add(dataChoice);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == viewChoice) {
			if (viewChoice.getSelectedIndex() != currentView) {
				currentView = viewChoice.getSelectedIndex();
				theView.setViewType(currentView);
			}
			return true;
		}
		if (target == dataChoice) {
			if (dataChoice.getSelectedIndex() != currentDataSet) {
				currentDataSet = dataChoice.getSelectedIndex();
				theVertAxis.setAlternateLabels(currentDataSet);
				String newVarKey = currentDataSet == 0 ? "y" : ("y" + (currentDataSet + 1));
				theView.setNumVariable(newVarKey);
				NumVariable yVar = (NumVariable)data.getVariable(newVarKey);
				yVariateLabel.setText(yVar.name);
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