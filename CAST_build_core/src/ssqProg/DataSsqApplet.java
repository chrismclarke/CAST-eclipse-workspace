package ssqProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import imageUtils.*;

import ssq.*;


public class DataSsqApplet extends XApplet {
	static final private String X_LABELS_PARAM = "xLabels";
	
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String DESCRIPTION_PARAM = "descriptionWidth";
	
	static final protected NumValue kMaxRSquared = new NumValue(1.0, 3);
	
	protected CoreModelDataSet data;
	protected AnovaSummaryData summaryData;
	protected NumValue maxSsq;
	
	private DataWithComponentsPanel scatterPanel;
	private XChoice dataSetChoice;
	private XTextArea descriptionArea;
	
	public void setupApplet() {
		data = getData();
		
			maxSsq = new NumValue(getParameter(MAX_SSQ_PARAM));
		summaryData = getSummaryData(data);
			summaryData.setSingleSummaryFromData();
		
		setLayout(new BorderLayout(20, 5));
		
			scatterPanel = new DataWithComponentsPanel(this);
			scatterPanel.setupPanel(data, "x", "y", "ls",
										null, DataWithComponentView.NO_COMPONENT_DISPLAY, this);
		add("Center", scatterPanel);
			
		add("East", rightPanel(summaryData));
		
		add("South", bottomPanel(summaryData));
	}
	
	private CoreModelDataSet getData() {
		CoreModelDataSet data;
		boolean xNumNotCat = (getParameter(X_LABELS_PARAM) == null);
		if (xNumNotCat)
			data = new SimpleRegnDataSet(this);
		else
			data = new GroupsDataSet(this);
		
		data.addBasicComponents();
		
		return data;
	}
	
	protected AnovaSummaryData getSummaryData(CoreModelDataSet data) {
		return new AnovaSummaryData(data, "y", BasicComponentVariable.kComponentKey,
																									maxSsq.decimals, kMaxRSquared.decimals);
	}
	
	protected XPanel rightPanel(AnovaSummaryData summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
			AnovaTableView theTable = new AnovaTableView(summaryData, this,
								BasicComponentVariable.kComponentKey, maxSsq, null, null, AnovaTableView.SSQ_ONLY);
		thePanel.add(theTable);
		thePanel.add(new OneValueImageView(summaryData, "rSquared", this, "xEquals/rSquared.png", 14, kMaxRSquared));
		return thePanel;
	}
	
	protected XPanel bottomPanel(AnovaSummaryData summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
			dataSetChoice = data.dataSetChoice(this);
		if (dataSetChoice != null)
			thePanel.add("East", dataSetChoice);
			
			int descriptionWidth = Integer.parseInt(getParameter(DESCRIPTION_PARAM));
			descriptionArea = new XTextArea(data.getDescriptionStrings(), 0, descriptionWidth, this);
			descriptionArea.lockBackground(Color.white);
		thePanel.add("Center", descriptionArea);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == dataSetChoice) {
			if (data.changeDataSet(dataSetChoice.getSelectedIndex(), descriptionArea)) {
				scatterPanel.changeAxes(data.getXAxisInfo(), data.getYAxisInfo());
				data.variableChanged("y");
				summaryData.setSingleSummaryFromData();
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