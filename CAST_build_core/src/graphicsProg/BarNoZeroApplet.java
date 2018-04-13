package graphicsProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;

import graphics.*;


public class BarNoZeroApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "yAxis";
	static final private String AXIS_LIMITS_PARAM = "yAxisLimits";
	
	private DynamicVertAxis valAxis;
	
	private XCheckbox expandAxisCheck;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(0, 10));
		
		add("North", controlPanel(data));
		add("Center", displayPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		data.addLabelVariable("label", getParameter(LABEL_NAME_PARAM), getParameter(LABELS_PARAM));
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel barPanel = new XPanel();
			barPanel.setLayout(new AxisLayout());
			
				StringTokenizer st = new StringTokenizer(getParameter(AXIS_LIMITS_PARAM));
				double startMin = Double.parseDouble(st.nextToken());
				double endMin = Double.parseDouble(st.nextToken());
				double startMax = Double.parseDouble(st.nextToken());
				double endMax = Double.parseDouble(st.nextToken());
				valAxis = new DynamicVertAxis(this, startMin, endMin, startMax, endMax);
				String labelInfo = getParameter(AXIS_INFO_PARAM);
				valAxis.readNumLabels(labelInfo);
			barPanel.add("Left", valAxis);
			
				HorizAxis catAxis = new HorizAxis(this);
				LabelVariable labelVariable = (LabelVariable)data.getVariable("label");
				catAxis.setLabelLabels(labelVariable);
			barPanel.add("Bottom", catAxis);
			
				VertNoZeroBarView vertChart = new VertNoZeroBarView(data, this, "y", catAxis, valAxis);
				vertChart.lockBackground(Color.white);
				valAxis.setLinkedView(vertChart);
			barPanel.add("Center", vertChart);
		
		thePanel.add("Center", barPanel);
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				XLabel yLabel = new XLabel(data.getVariable("y").name, XLabel.LEFT, this);
				yLabel.setFont(getStandardBoldFont());
			topPanel.add(yLabel);
			
		thePanel.add("North", topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
			
				XLabel catLabel = new XLabel(data.getVariable("label").name, XLabel.RIGHT, this);
				catLabel.setFont(getStandardBoldFont());
			bottomPanel.add(catLabel);
			
		thePanel.add("South", bottomPanel);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			expandAxisCheck = new XCheckbox(translate("Expand scale"), this);
		thePanel.add(expandAxisCheck);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == expandAxisCheck) {
			valAxis.animateFrames(expandAxisCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}