package percentileProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;

import percentile.*;


public class GroupedQuartileApplet extends XApplet {
	static final protected String X_VAR_NAME_PARAM = "xVarName";
	static final protected String X_VALUES_PARAM = "xValues";
	static final protected String X_LABELS_PARAM = "xLabels";
	
	static final protected String Y_AXIS_INFO_PARAM = "yAxis";
	static final protected String Y_VAR_NAME_PARAM = "yVarName";
	static final protected String Y_VALUES_PARAM = "yValues";
	
	protected DataSet data;
	
	protected GroupedQuartileView theView;
	
	protected XChoice displayChoice;
	protected int currentDisplay = 0;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 0));
		add("North", topPanel(data));
		add("Center", dataDisplayPanel(data));
		add("South", controlPanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		data.addCatVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM),
																													getParameter(X_LABELS_PARAM));
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
			XLabel propnLabel = new XLabel(data.getVariable("y").name, XLabel.LEFT, this);
		thePanel.add(propnLabel);
		return thePanel;
	}
	
	protected XPanel dataDisplayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
				CatVariable xVar = (CatVariable)data.getVariable("x");
			horizAxis.setCatLabels(xVar);
			horizAxis.setAxisName(xVar.name);
		thePanel.add("Bottom", horizAxis);
		
			VertAxis vertAxis = new VertAxis(this);
			vertAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
		thePanel.add("Left", vertAxis);
		
			theView = new GroupedQuartileView(data, this, vertAxis, horizAxis);
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
			displayChoice = new XChoice(this);
			displayChoice.addItem(translate("Jittered dot plots"));
			displayChoice.addItem(translate("Box plots"));
			displayChoice.addItem(translate("Box plots and quartile bands"));
			displayChoice.addItem(translate("Quartile bands only"));
		thePanel.add(displayChoice);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == displayChoice) {
			int newChoice = displayChoice.getSelectedIndex();
			if (newChoice != currentDisplay) {
				currentDisplay = newChoice;
				int displayType = (newChoice == 0) ? GroupedQuartileView.DOT_PLOT
													: (newChoice == 1) ? GroupedQuartileView.BOX_PLOT
													: (newChoice == 2) ? GroupedQuartileView.QUARTILE_AND_BOX_PLOT
																							: GroupedQuartileView.QUARTILE_PLOT;
				theView.setPlotType(displayType);
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