package ssqProg;

import java.awt.*;

import dataView.*;
import utils.*;
import valueList.*;
import models.*;
import formula.*;


import ssq.*;

public class Components2Applet extends XApplet {
	static final private String COMPONENT_AXIS_PARAM = "componentAxis";
	static final private String DOT_WIDTH_PARAM = "dotPlotWidth";
	static final private String X_LABELS_PARAM = "xLabels";
	static final private String SSQ_NAMES_PARAM = "ssqNames";
	
	private CoreModelDataSet data;
	protected SummaryDataSet summaryData;
	
	protected DataWithComponentsPanel scatterPanel;
	protected ComponentPlotPanel componentPlot;
	
	protected boolean xNumNotCat;
	protected ComponentEqnPanel theEquation;
	
	protected XChoice compChoice;
	private int currentCompIndex = 0;
	
	public void setupApplet() {
		xNumNotCat = (getParameter(X_LABELS_PARAM) == null);
		data = getData();
		summaryData = getSummaryData(data);
		if (canTakeSample())
			summaryData.takeSample();
		
		setLayout(new BorderLayout(0, 0));
			
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new BorderLayout(0, 0));
			
			mainPanel.add("Center", dataPanel(data));
			mainPanel.add("East", rightPanel(data));
		
		add("Center", mainPanel);
		add("South", controlPanel(data, summaryData));
		add("North", topPanel(data));
	}
	
	protected CoreModelDataSet getData() {
		CoreModelDataSet data;
		if (xNumNotCat)
			data = new SimpleRegnDataSet(this);
		else
			data = new GroupsDataSet(this);
		
		data.addBasicComponents();
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet data) {
		return null;
	}
	
	protected boolean canTakeSample() {
		return summaryData != null;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		return thePanel;
	}
	
	protected XPanel dataPanel(DataSet data) {
		scatterPanel = getDataComponentsView(data);
		return scatterPanel;
	}
	
	protected DataWithComponentsPanel getDataComponentsView(DataSet data) {
		DataWithComponentsPanel thePanel = new DataWithComponentsPanel(this);
		thePanel.setupPanel(data, "x", "y", "ls", null, BasicComponentVariable.TOTAL, this);
		thePanel.getView().setRetainLastSelection(true);
		return thePanel;
	}
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout (FlowLayout.LEFT, 0, 0));
			topPanel.add(new XLabel(translate("Component"), XLabel.LEFT, this));
			
		thePanel.add("North", topPanel);
			
			XPanel mainPanel = new XPanel();
				int dotPlotWidth = Integer.parseInt(getParameter(DOT_WIDTH_PARAM));
			mainPanel.setLayout(new FixedSizeLayout(dotPlotWidth, 300));
			
				componentPlot = new ComponentPlotPanel(data, getParameter(COMPONENT_AXIS_PARAM),
											componentKeys(), componentColors(), getParameter(SSQ_NAMES_PARAM),
											ComponentPlotPanel.NOT_SELECTED, ComponentPlotPanel.NO_SD,
											ComponentPlotPanel.NO_HEADING, null, this);
			mainPanel.add(componentPlot);
			
		thePanel.add("Center", mainPanel);
				
		return thePanel;
	}
	
	protected String[] componentKeys() {
		return BasicComponentVariable.kComponentKey;
	}
	
	protected Color[] componentColors() {
		return BasicComponentVariable.kComponentColor;
	}
	
	protected XPanel componentChoicePanel() {
		XPanel componentPanel = new XPanel();
		componentPanel.setLayout(new VerticalLayout (VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
			compChoice = componentPlot.createComponentChoice(this);
		componentPanel.add(compChoice);
		return componentPanel;
	}
	
	protected XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout( 0, 0));
		
			XPanel equationPanel = new XPanel();
			equationPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 12));
				
				if (getParameter(LABEL_NAME_PARAM) != null)
					equationPanel.add(new OneValueView(data, "label", this));
			
			Image[] compImages;
			if (xNumNotCat) {
				AnovaImages.loadRegnImages(this);
				compImages = AnovaImages.basicRegnDevns;
			}
			else {
				AnovaImages.loadGroupImages(this);
				compImages = AnovaImages.basicGroupDevns;
			}
			
			FormulaContext bigContext = new FormulaContext(Color.black, getBigFont(), this);
			theEquation = new ComponentEqnPanel(data, componentKeys(), null,
								compImages, componentColors(), AnovaImages.kDevnWidth,
								AnovaImages.kDevnHeight, bigContext);
			equationPanel.add(theEquation);
		thePanel.add("Center", equationPanel);
		
		thePanel.add("East", componentChoicePanel());
		
		return thePanel;
	}
	
	protected void highlightEquation(int newComponentIndex) {
		if (theEquation != null)
			theEquation.highlightComponent(newComponentIndex);
	}
	
	protected void changeComponentSelected(int newComponentIndex) {
		data.clearSelection();
		
		componentPlot.setComponent(newComponentIndex);
		
		if (scatterPanel != null)
			scatterPanel.getView().changeComponentDisplay(newComponentIndex);
		highlightEquation(newComponentIndex);
	}

	
	private boolean localAction(Object target) {
		if (target == compChoice) {
			if (compChoice.getSelectedIndex() != currentCompIndex) {
				currentCompIndex = compChoice.getSelectedIndex();
				changeComponentSelected(currentCompIndex);
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