package transformProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.OneValueView;

import transform.*;


public class ZScoreApplet extends XApplet {
	static final protected String AXIS_INFO_PARAM = "horizAxis";
	static final private String N_VARIABLES_PARAM = "noOfVariables";
	static final protected String MEASUREMENT_PARAM = "measurementName";
		
	protected int noOfVariables;
	protected boolean hasLabels = false;
	protected DataSet data;
	protected OneValueView yView, zView;
	protected DataView theView;
	protected HorizAxis zAxis;
	
	private XChoice dataSetChoice;
	private int currentDataIndex;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout());
		add("North", valuePanel(data));
		add("Center", dataPanel(data));
		add("South", controlPanel(data));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		noOfVariables = Integer.parseInt(getParameter(N_VARIABLES_PARAM));
		
		for (int i=0 ; i<noOfVariables ; i++)
			data.addNumVariable("y" + i, getParameter(VAR_NAME_PARAM + i),
																									getParameter(VALUES_PARAM + i));
		addZScoreVariables(data);
		
		String labelName = getParameter(LABEL_NAME_PARAM);
		if (labelName != null) {
			hasLabels = true;
			data.addLabelVariable("label", labelName, getParameter(LABELS_PARAM));
		}
		
		return data;
	}
	
	protected void addZScoreVariables(DataSet data) {
		for (int i=0 ; i<noOfVariables ; i++)
			data.addVariable("z" + i, new ZScoreVariable(translate("z-score"), data, "y" + i, 2));
	}
	
	protected XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 0));
		
		if (hasLabels)
			thePanel.add(new OneValueView(data, "label", this));
		
		yView = new OneValueView(data, "y0", this);
		yView.setLabel(getParameter(MEASUREMENT_PARAM));
		thePanel.add(yView);
		
		
		zView = new OneValueView(data, "z0", this);
		zView.setForeground(Color.blue);
		thePanel.add(zView);
		return thePanel;
	}
	
	protected XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis xAxis = new HorizAxis(this);
		xAxis.setAxisName(getParameter(MEASUREMENT_PARAM));
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		xAxis.readNumLabels(labelInfo);
		xAxis.setForeground(Color.blue);
		thePanel.add("Bottom", xAxis);
		
		zAxis = createZAxis(data, xAxis);
		zAxis.setForeground(Color.black);
		thePanel.add("Top", zAxis);
		
		theView = coreView(data, xAxis);
		theView.setActiveNumVariable("y0");
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	protected HorizAxis createZAxis(DataSet data, HorizAxis xAxis) {
		MultiHorizAxis zAxis = new MultiHorizAxis(this, noOfVariables);
		zAxis.setChangeMinMax(true);
		zAxis.setAxisName(translate("z-score"));
		String labelInfo = findAxisString(data, "z0", xAxis.minOnAxis, xAxis.maxOnAxis);
		zAxis.readNumLabels(labelInfo);
		for (int i=1 ; i<noOfVariables ; i++) {
			labelInfo = findAxisString(data, "z" + i, xAxis.minOnAxis, xAxis.maxOnAxis);
			zAxis.readExtraNumLabels(labelInfo);
		}
		zAxis.setStartAlternate(0);
		return zAxis;
	}
	
	private String findAxisString(DataSet data, String zKey, double minX, double maxX) {
		ZScoreVariable zVar = (ZScoreVariable)data.getVariable(zKey);
		double zMin = zVar.xToZ(minX);
		double zMax = zVar.xToZ(maxX);
		
		double zRange = zMax - zMin;
		double step = (zRange < 10) ? 1 : (zRange < 20) ? 2 : 5;
		double minLabel = Math.ceil(zMin / step) * step;
		return new NumValue(zMin, 5).toString() + " "
											+ new NumValue(zMax, 5).toString() + " "
											+ (int)Math.round(minLabel) + " " + (int)Math.round(step);
	}
	
	protected DataView coreView(DataSet data, NumCatAxis theHorizAxis) {
		return new TwinAxisDotPlotView(data, this, theHorizAxis);
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		if (noOfVariables > 1) {
			dataSetChoice = new XChoice(this);
			for (int i=0 ; i<noOfVariables ; i++) {
				CoreVariable var = data.getVariable("y" + i);
				dataSetChoice.addItem(var.name);
			}
			currentDataIndex = 0;
			
			thePanel.add(dataSetChoice);
		}
		return thePanel;
	}
	
	protected void changeVariable(int i) {
		String newY = "y" + i;
		if (yView != null)
			yView.setVariableKey(newY);
		if (zView != null)
			zView.setVariableKey("z" + i);
		
		if (zAxis instanceof MultiHorizAxis
						&& ((MultiHorizAxis)zAxis).setAlternateLabels(i))
			zAxis.repaint();
		theView.setActiveNumVariable(newY);
		
		int currentSelection = data.getSelection().findSingleSetFlag();
		data.variableChanged(newY, currentSelection);
	}

	
	private boolean localAction(Object target) {
		if (target == dataSetChoice) {
			if (dataSetChoice.getSelectedIndex() != currentDataIndex) {
				currentDataIndex = dataSetChoice.getSelectedIndex();
				changeVariable(currentDataIndex);
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