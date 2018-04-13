package ssqProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import models.*;

import ssq.*;


public class MeanCisApplet extends XApplet {
	static final private String JITTER_PARAM = "jitter";
	
	protected GroupsDataSet data;
	
	private XCheckbox pooledSdCheck;
	private XChoice dataCiChoice;
	private int currentDataCiChoice = 0;
	
	protected NumCatAxis numAxis;
	protected GroupMeanDotView dataView;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 10));
	
		add("North", dataCiPanel(data));
		add("Center", displayPanel(data, true));
		add("South", controlPanel(data));
	}
	
	protected GroupsDataSet getData() {
		return new GroupsDataSet(this);
	}
	
	private XPanel dataCiPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			dataCiChoice = new XChoice(this);
			dataCiChoice.addItem(translate("Data"));
			dataCiChoice.addItem("95% CIs for means");
		
		thePanel.add(dataCiChoice);
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			pooledSdCheck = new XCheckbox("Assume same sd in all groups", this);
			pooledSdCheck.disable();
		thePanel.add(pooledSdCheck);
		
		return thePanel;
	}
	
	protected XPanel titlePanel(GroupsDataSet data, boolean yIsHoriz) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XLabel titleLabel = new XLabel(data.getVariable(yIsHoriz ? "x" : "y").name, XLabel.LEFT, this);
			titleLabel.setFont(getStandardBoldFont());
		
		thePanel.add("West", titleLabel);
		thePanel.add("Center", new XPanel());	//	needed so sub-class can add label to East
		
		return thePanel;
	}
	
	protected XPanel displayPanel(GroupsDataSet data, boolean yIsHoriz) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
		thePanel.add("North", titlePanel(data, yIsHoriz));
		
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new AxisLayout());
			
				NumCatAxis catAxis;
				if (yIsHoriz)
					catAxis = new VertAxis(this);
				else
					catAxis = new HorizAxis(this);
				CatVariable xVar = (CatVariable)data.getVariable("x");
				catAxis.setCatLabels(xVar);
				if (!yIsHoriz)
					catAxis.setAxisName(xVar.name);
			
			dataPanel.add(yIsHoriz ? "Left" : "Bottom", catAxis);
			
				if (yIsHoriz)
					numAxis = new HorizAxis(this);
				else
					numAxis = new VertAxis(this);
				numAxis.readNumLabels(data.getYAxisInfo());
				if (yIsHoriz)
					numAxis.setAxisName(data.getYVarName());
			
			dataPanel.add(yIsHoriz ? "Bottom" : "Left", numAxis);
			
				double jitter = Double.parseDouble(getParameter(JITTER_PARAM));
				dataView = new GroupMeanDotView(data, this, numAxis, catAxis, "y", "x", jitter);
				dataView.lockBackground(Color.white);
				
			dataPanel.add("Center", dataView);
			
			addMarginView(dataPanel, numAxis);
		
		thePanel.add("Center", dataPanel);
		
		return thePanel;
	}
	
	protected void addMarginView(XPanel dataPanel, NumCatAxis numAxis) {
								//		to allow MeanComparisonApplet to add slider for multiple comparisons
	}

	
	private boolean localAction(Object target) {
		if (target == pooledSdCheck) {
			dataView.setMeanDisplay(pooledSdCheck.getState() ? GroupMeanDotView.CI_POOLED_BANDS
																									: GroupMeanDotView.CI_BANDS);
			dataView.repaint();
			return true;
		}
		else if (target == dataCiChoice) {
			int newChoice = dataCiChoice.getSelectedIndex();
			if (newChoice != currentDataCiChoice) {
				currentDataCiChoice = newChoice;
				if (newChoice == 0) {
					dataView.setMeanDisplay(GroupMeanDotView.MEAN_ONLY);
					pooledSdCheck.disable();
				}
				else {
					dataView.setMeanDisplay(pooledSdCheck.getState() ? GroupMeanDotView.CI_POOLED_BANDS
																									: GroupMeanDotView.CI_BANDS);
					pooledSdCheck.enable();
				}
				dataView.repaint();
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