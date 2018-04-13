package multivarProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import multivar.*;


public class ScatterMatrixApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "axis";
	
	private int noOfVariables;
	private DataSet data;
	
	private HorizAxis xAxis[];
	private VertAxis yAxis[];
	
	private XCheckbox brushCheck;
	private XCheckbox groupCheck;
	private CatKey2 theKey;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new ScatterMatrixLayout(noOfVariables, 7, 7));
		xAxis = new HorizAxis[noOfVariables];
		yAxis = new VertAxis[noOfVariables];
		for (int i=0 ; i<noOfVariables ; i++) {
			if (i > 0) {
				yAxis[i] = createVertAxis(data, i);
				add("YAxis", yAxis[i]);
				add("YLabel", createVertLabel(data, i));
			}
			if (i < noOfVariables - 1) {
				xAxis[i] = createHorizAxis(data, i);
				add("XAxis", xAxis[i]);
				add("XLabel", createHorizLabel(data, i));
			}
			for (int j=0 ; j<i ; j++)
				add("Graph", createPlot(data, j, i));
		}
		
		add("XPanel", controlPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		int index = 0;
		while (true) {
			String name = getParameter(VAR_NAME_PARAM + index);
			String values = getParameter(VALUES_PARAM + index);
			if (name == null || values == null)
				break;
			data.addNumVariable("y" + index, name, values);
			index ++;
		}
		noOfVariables = index;
		String labelVarName = getParameter(LABEL_NAME_PARAM);
		if (labelVarName != null)
			data.addLabelVariable("label", labelVarName, getParameter(LABELS_PARAM));
		
		String groupVarName = getParameter(CAT_NAME_PARAM);
		if (groupVarName != null)
			data.addCatVariable("group", groupVarName, getParameter(CAT_VALUES_PARAM),
																				getParameter(CAT_LABELS_PARAM));
		return data;
	}
	
	private HorizAxis createHorizAxis(DataSet data, int index) {
		HorizAxis axis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM + index);
		axis.readNumLabels(labelInfo);
		axis.setFont(getTinyFont());
		return axis;
	}
	
	private VertAxis createVertAxis(DataSet data, int index) {
		VertAxis axis = new VertAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM + index);
		axis.readNumLabels(labelInfo);
		axis.setFont(getTinyFont());
		return axis;
	}
	
	private XLabel createVertLabel(DataSet data, int index) {
		CoreVariable v = data.getVariable("y" + index);
		XLabel label = new XLabel(v.name, XLabel.RIGHT, this);
		label.setFont(getTinyFont());
		return label;
	}
	
	private XLabel createHorizLabel(DataSet data, int index) {
		CoreVariable v = data.getVariable("y" + index);
		XLabel label = new XLabel(v.name, XLabel.CENTER, this);
		label.setFont(getTinyFont());
		return label;
	}
	
	private DataView createPlot(DataSet data, int xIndex, int yIndex) {
		String xKey = "y" + xIndex;
		String yKey = "y" + yIndex;
		BrushScatterView theView = new BrushScatterView(data, this, xAxis[xIndex], yAxis[yIndex], xKey, yKey);
		theView.lockBackground(Color.white);
		theView.setCrossSize(DataView.SMALL_CROSS);
		return theView;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
		
		brushCheck = new XCheckbox(translate("Use brush"), this);
		brushCheck.setState(false);
		thePanel.add(brushCheck);
		
		if (data.getCatVariable() != null) {
			groupCheck = new XCheckbox(translate("Show groups"), this);
			groupCheck.setState(false);
			thePanel.add(groupCheck);
			
			theKey = new CatKey2(data, "group", this, CatKey2.VERT);
			theKey.setFont(getSmallFont());
			thePanel.add(theKey);
			theKey.show(false);		//	private version of show() since hidden component is not laid out
		}
		
		return thePanel;
	}
	
	public boolean doingBrush() {
		return brushCheck.getState();
	}
	
	public boolean canShowGroups() {
		return (groupCheck == null) ? false : groupCheck.getState();
	}

	
	private boolean localAction(Object target) {
		if (target == groupCheck) {
			theKey.show(groupCheck.getState());
			data.variableChanged("group");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}