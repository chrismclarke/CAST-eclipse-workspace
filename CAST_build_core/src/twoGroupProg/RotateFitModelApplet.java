package twoGroupProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import multivarProg.*;
import twoGroup.*;


public class RotateFitModelApplet extends RotateApplet {
	static final private String JITTER_PARAM = "jitterPropn";
	static private final String DESCRIPTION_WIDTH_PARAM = "descriptionWidth";
	static private final String SHOW_DIFF_PARAM = "showDiff";
	
	static final protected String kDefaultZAxis = "0.0 1.0 2.0 1.0";
	static final private String kMaxSummaryString = "999.99";
	static final private String kMaxCountString = "9999";
	
	static final private Color kGreenColor = new Color(0x006600);
	
	private GroupsDataSet data;
	
	private XTextArea dataQuestions;
	private XChoice dataSetChoice;
	private XLabel dataLabel;
	private D3Axis xAxis, yAxis;
	private XLabel catLabel[];
	
	protected DataSet readData() {
		data = new GroupsDataSet(this);
		
		GroupsModelVariable model = new GroupsModelVariable("model", data, "x");
		model.updateLSParams("y");
		data.addVariable("model", model);
		
		return data;
	}
	
	protected Rotate3DView getView(DataSet data, D3Axis xAxis, D3Axis yAxis, D3Axis densityAxis) {
		double jitterPropn = Double.parseDouble(getParameter(JITTER_PARAM));
		RotateAnovaPDFView theView = new RotateAnovaPDFView(data, this, xAxis, yAxis, densityAxis, "model", "x", "y",
																																		jitterPropn);
		theView.setShowData(true);
		return theView;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		xAxis = new D3Axis(((GroupsDataSet)data).getXVarName(), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		CatVariable xVar = (CatVariable)data.getVariable("x");
		xAxis.setCatScale(xVar);
		
		yAxis = new D3Axis(((GroupsDataSet)data).getYVarName(), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		yAxis.setNumScale(((GroupsDataSet)data).getYAxisInfo());
		
		D3Axis zAxis = new D3Axis("", D3Axis.Y_AXIS, D3Axis.X_AXIS, this);
		zAxis.setNumScale(kDefaultZAxis);
		
		theView = getView(data, xAxis, yAxis, zAxis);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			dataLabel = new XLabel(((GroupsDataSet)data).getDataName(), XLabel.LEFT, this);
			dataLabel.setFont(getBigBoldFont());
			thePanel.add("Center", dataLabel);
		return thePanel;
	}

//-----------------------------------------------------------
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		thePanel.add(rotationPanel());
		return thePanel;
	}
	
	private XPanel rotationPanel() {
		XPanel thePanel = RotateButton.create2DRotationPanel(theView, this, RotateButton.HORIZONTAL);
		rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
		return thePanel;
	}

//-----------------------------------------------------------
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(12, 0, 0, 0);
		thePanel.setLayout(new BorderLayout(0, 5));
		
		thePanel.add("North", questions((GroupsDataSet)data));
		thePanel.add("Center", summaryPanel(data));
		thePanel.add("South", dataChoicePanel(data));
		
		return thePanel;
	}
	
	private XTextArea questions(GroupsDataSet data) {
		String messageText[] = data.getQuestionStrings();
		int messageWidth = Integer.parseInt(getParameter(DESCRIPTION_WIDTH_PARAM));
		
		dataQuestions = new XTextArea(messageText, 0, messageWidth, this);
		dataQuestions.setFont(getStandardBoldFont());
		dataQuestions.setForeground(Color.red);
		dataQuestions.lockBackground(Color.white);
		
		return dataQuestions;
	}
	
	private XPanel differencePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
		GroupSummary2View difference = new GroupSummary2View(data, this, GroupSummary2View.MU_DIFF_HAT,
																								kMaxSummaryString, ((GroupsDataSet)data).getSummaryDecimals());
		thePanel.add(difference);
		
		return thePanel;
	}
	
	private XPanel summaryPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new ProportionLayout(0.5, 10));
			
			catLabel = new XLabel[2];
			
			topPanel.add(ProportionLayout.LEFT, oneSummaryPanel(data, 0));
			topPanel.add(ProportionLayout.RIGHT, oneSummaryPanel(data, 1));
			
		thePanel.add("Center", topPanel);
		
		String showDiffString = getParameter(SHOW_DIFF_PARAM);
		if (showDiffString == null || showDiffString.equals("true"))
			thePanel.add("South", differencePanel(data));
		
		return thePanel;
	}
	
	private XPanel oneSummaryPanel(DataSet data, int group) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 2));
			
			CatVariable xVar = (CatVariable)data.getVariable("x");
			catLabel[group] = new XLabel(xVar.getLabel(group).toString(), XLabel.CENTER, this);
			catLabel[group].setFont(getStandardBoldFont());
			thePanel.add(catLabel[group]);
			
			GroupSummaryView countView = new GroupSummaryView(data, this, GroupSummaryView.N, group, kMaxCountString, 0);
			thePanel.add(countView);
			
			GroupSummaryView meanView = new GroupSummaryView(data, this, GroupSummaryView.MU_HAT, group, kMaxSummaryString,
											((GroupsDataSet)data).getSummaryDecimals());
			meanView.setForeground(Color.blue);
			thePanel.add(meanView);
			
			GroupSummaryView sdView = new GroupSummaryView(data, this, GroupSummaryView.SIGMA_HAT, group, kMaxSummaryString,
																						((GroupsDataSet)data).getSummaryDecimals());
			sdView.setForeground(kGreenColor);
			thePanel.add(sdView);
		return thePanel;
	}
	
	private XPanel dataChoicePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 2));
			
			dataSetChoice = ((GroupsDataSet)data).dataSetChoice(this);
			thePanel.add(dataSetChoice);
			
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == dataSetChoice) {
			if (data.changeDataSet(dataSetChoice.getSelectedIndex(), null, dataQuestions)) {
				yAxis.setNumScale(data.getYAxisInfo());
				yAxis.setLabelName(data.getYVarName());
				
				CatVariable xVar = (CatVariable)data.getVariable("x");
				xAxis.setCatScale(xVar);
				xAxis.setLabelName(data.getXVarName());
				
				data.resetLSEstimates();
				GroupsModelVariable model = (GroupsModelVariable)data.getVariable("model");
				model.updateLSParams("y");
				
				data.variableChanged("x");
				
				dataLabel.setText(data.getDataName());
				
				for (int i=0 ; i<xVar.noOfCategories() ; i++)
					catLabel[i].setText(xVar.getLabel(i).toString());
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}