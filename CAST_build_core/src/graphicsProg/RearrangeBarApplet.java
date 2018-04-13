package graphicsProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;

import cat.*;
import graphics.*;


public class RearrangeBarApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "yAxis";
	static final private String GROUPED_AXIS_INFO_PARAM = "groupedYAxis";
	static final private String GROUP_NAME_PARAM = "groupName";
	static final private String GROUP_VALUES_PARAM = "groupValues";
	static final private String GROUP_LABELS_PARAM = "groupLabels";
	static final private String SHORT_CAT_LABELS_PARAM = "shortCatLabels";
	
	static final private Color kGroupColor[] = CatKey3View.kCatColour;
	
	private GroupedHorizAxis horizAxis;
	private GroupedVertAxis vertAxis;
	private RearrangeBarView barChart;
	
	private XCheckbox setInRowCheck;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(0, 10));
		
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		data.addCatVariable("x", getParameter(CAT_NAME_PARAM), getParameter(CAT_VALUES_PARAM), getParameter(CAT_LABELS_PARAM));
		data.addCatVariable("group", getParameter(GROUP_NAME_PARAM), getParameter(GROUP_VALUES_PARAM), getParameter(GROUP_LABELS_PARAM));
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				XLabel yLabel = new XLabel(data.getVariable("y").name, XLabel.LEFT, this);
			topPanel.add(yLabel);
			
		thePanel.add("North", topPanel);
		
			XPanel barPanel = new XPanel();
			barPanel.setLayout(new AxisLayout());
			
				CatVariable xVar = (CatVariable)data.getVariable("x");
				CatVariable groupVar = (CatVariable)data.getVariable("group");
				
				vertAxis = new GroupedVertAxis(this);
				vertAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
					StringTokenizer st = new StringTokenizer(getParameter(GROUPED_AXIS_INFO_PARAM));
					NumValue startLabel = new NumValue(st.nextToken());
					NumValue step = new NumValue(st.nextToken());
					double maxLabel = Double.parseDouble(st.nextToken());
				vertAxis.setupGroupedAxis(groupVar.noOfCategories(), startLabel, step, maxLabel);
				vertAxis.setStartAlternate(1);
				
			barPanel.add("Left", vertAxis);
			
				horizAxis = new GroupedHorizAxis(this);
				horizAxis.setCatLabels(xVar);
				Value shortLabel[] = new Value[xVar.noOfCategories()];
				st = new StringTokenizer(getParameter(SHORT_CAT_LABELS_PARAM));
				for (int i=0 ; i<shortLabel.length ; i++)
					shortLabel[i] = new LabelValue(st.nextToken());
				horizAxis.setupGroupedAxis(groupVar.noOfCategories(), shortLabel);
			barPanel.add("Bottom", horizAxis);
			
				barChart = new RearrangeBarView(data, this, "y", "x",
																		"group", horizAxis, vertAxis, kGroupColor);
				barChart.lockBackground(Color.white);
				barChart.setFont(getBigBoldFont());
			barPanel.add("Center", barChart);
		
		thePanel.add("Center", barPanel);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			setInRowCheck = new XCheckbox(translate("Arrange in single row"), this);
		thePanel.add(setInRowCheck);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == setInRowCheck) {
			barChart.setArrangeInRow(setInRowCheck.getState());
			if (horizAxis.setAlternateLabels(setInRowCheck.getState() ? 1 : 0))
				horizAxis.repaint();
			if (vertAxis.setAlternateLabels(setInRowCheck.getState() ? 0 : 1))
				vertAxis.repaint();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}