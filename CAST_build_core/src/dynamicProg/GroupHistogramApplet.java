package dynamicProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;

import dynamic.*;


public class GroupHistogramApplet extends XApplet {
	static final private String FREQ_AXIS_INFO_PARAM = "freqAxis";
	static final private String X_AXIS_INFO_PARAM = "xAxis";
	static final private String CLASS_BOUNDARY_PARAM = "classBoundary";
	static final private String UNIT_CLASS_WIDTH_PARAM = "unitClassWidth";
	static final private String GROUPING_PARAM = "grouping";
	static final private String X_AXIS_NAME_PARAM = "xAxisName";
	static final private String DENSITY_AXIS_NAME_PARAM = "densityAxisName";
	static final private String GROUP_CHECK_NAME_PARAM = "groupCheckName";
	
	private XLabel axisLabel;
	private HistogramView theView;
	
	private XCheckbox showCorrectCheck, groupingCheck;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(0, 0));
		add("Center", displayPanel(data));
		
		add("North", topPanel(data));
		
		add("South", controlPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		axisLabel = new XLabel(getParameter(VAR_NAME_PARAM), XLabel.LEFT, this);
		axisLabel.setFont(getBigBoldFont());
		thePanel.add(axisLabel);
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			theHorizAxis.readNumLabels(getParameter(X_AXIS_INFO_PARAM));
			theHorizAxis.setAxisName(getParameter(X_AXIS_NAME_PARAM));
		thePanel.add("Bottom", theHorizAxis);
		
			VertAxis theVertAxis = new VertAxis(this);
			theVertAxis.readNumLabels(getParameter(FREQ_AXIS_INFO_PARAM));
		thePanel.add("Left", theVertAxis);
		
			StringTokenizer st = new StringTokenizer(getParameter(CLASS_BOUNDARY_PARAM));
			double classBoundary[] = new double[st.countTokens()];
			for (int i=0 ; i<classBoundary.length ; i++)
				classBoundary[i] = Double.parseDouble(st.nextToken());
				
			double unitClassWidth = Double.parseDouble(getParameter(UNIT_CLASS_WIDTH_PARAM));
			
			boolean groupWithNext[] = new boolean[classBoundary.length - 1];	//	all false
			st = new StringTokenizer(getParameter(GROUPING_PARAM));
			while (st.hasMoreTokens()) {
				StringTokenizer st2 = new StringTokenizer(st.nextToken(), "-");
				int lowIndex = Integer.parseInt(st2.nextToken());
				int highIndex = Integer.parseInt(st2.nextToken());
				for (int i=lowIndex ; i<highIndex ; i++)
					groupWithNext[i] = true;
			}
			
			theView = new HistogramView(data, this, "y", theHorizAxis, theVertAxis,
																									unitClassWidth, classBoundary, groupWithNext);
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			groupingCheck = new XCheckbox(getParameter(GROUP_CHECK_NAME_PARAM), this);
		thePanel.add(groupingCheck);
		
			showCorrectCheck = new XCheckbox(translate("Correct diagram"), this);
		thePanel.add(showCorrectCheck);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == showCorrectCheck) {
			theView.setCorrectHeights(showCorrectCheck.getState());
			theView.repaint();
			
			if (showCorrectCheck.getState()) {
				axisLabel.setForeground(Color.red);
				axisLabel.setText(getParameter(DENSITY_AXIS_NAME_PARAM));
			}
			else {
				axisLabel.setForeground(Color.black);
				axisLabel.setText(getParameter(VAR_NAME_PARAM));
			}
			
			return true;
		}
		else if (target == groupingCheck) {
			theView.animateGrouping(groupingCheck.getState());
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}