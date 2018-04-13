package dotPlotProg;

import java.awt.*;
import java.util.*;

import valueList.OneValueView;
import dataView.*;
import utils.*;


public class StackDotPlot3Applet extends StackDotPlot2Applet {
	static final private String DATA_NAMES_PARAM = "dataNames";
	
	private XChoice dataSetChoice;
	private int currentDataSetIndex = 0;
	
	public void setupApplet() {
		data = readLabelledData();
		
		setLayout(new BorderLayout());
		
		add("Center", oneStackedDotPlot(data, DataView.MEDIUM_CROSS));
		add("North", topPanel(data));
	}
	
	private XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
//		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
		
		if (data.getVariable("label") != null) {
			XPanel valuePanel = new XPanel();
			valuePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			valuePanel.add(new OneValueView(data, "label", this));
			thePanel.add(valuePanel);
		}
		
		String dataNameString = getParameter(DATA_NAMES_PARAM);
		if (dataNameString != null) {
			StringTokenizer st = new StringTokenizer(dataNameString, "#");
			dataSetChoice = new XChoice(translate("Data set") + ":", XChoice.HORIZONTAL, this);
			while (st.hasMoreTokens())
				dataSetChoice.addItem(st.nextToken());
			
			XPanel dataChoicePanel = new XPanel();
			dataChoicePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			dataChoicePanel.add(dataSetChoice);
			
			thePanel.add(dataChoicePanel);
		}
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == dataSetChoice) {
			int newChoice = dataSetChoice.getSelectedIndex();
			if (newChoice != currentDataSetIndex) {
				currentDataSetIndex = newChoice;
				
				String suffix = "";
				if (newChoice > 0)
					suffix += (newChoice + 1);
				
				NumVariable yVar = (NumVariable)data.getVariable("y");
				yVar.readValues(getParameter(VALUES_PARAM + suffix));
				yVar.name = getParameter(VAR_NAME_PARAM + suffix);
				
				LabelVariable labelVar = (LabelVariable)data.getVariable("label");
				if (labelVar != null) {
					labelVar.readValues(getParameter(LABELS_PARAM + suffix));
					labelVar.name = getParameter(LABEL_NAME_PARAM + suffix);
				}
				
				yAxis.readNumLabels(getParameter(AXIS_INFO_PARAM + suffix));
				if (showingVarName())
					yAxis.setAxisName(yVar.name);
				
				data.variableChanged("y");
				
				yAxis.invalidate();
				validate();
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