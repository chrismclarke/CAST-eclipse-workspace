package twoGroupProg;

import java.awt.*;

import axis.*;
import dataView.*;
import distn.*;
import utils.*;
import coreGraphics.*;
import models.*;
import imageUtils.*;

import twoGroup.*;


public class DiffDistnErrorApplet extends XApplet {
	static private final String DESCRIPTION_WIDTH_PARAM = "descriptionWidth";
	static private final String POOLED_PARAM = "pooled";
	
	static final private String kMaxSummaryString = "999.99";
	static final private int kSummaryDecimals = 3;
	
	static final private Color kPinkColor = new Color(0xFFCCCC);
	static final private Color kDarkGreenColor = new Color(0x006600);
	
	private XChoice dataSetChoice;
	
	private XTextArea dataDescriptions;
	private XLabel responseNameLabel;
	protected XLabel catLabel[];
	
	private VerticalDotView theView;
	private VertAxis theNumAxis;
	private HorizAxis theGroupAxis;
	protected HorizAxis theSummaryAxis;
	
	protected CoreModelDataSet data;
	protected DataSet errorData;
	private SummaryDataSet summaryData = null;
	
	private boolean usePooledSD = false;
	
	public void setupApplet() {
		String pooledString = getParameter(POOLED_PARAM);
		if (pooledString != null && pooledString.equals("true"))
			usePooledSD = true;
		
		data = getData();
		if (usePooledSD) {
			summaryData = getPooledSummaryData(data);
			summaryData.setSingleSummaryFromData();
			summaryData.setSelection(0);
		}
		errorData = getErrorData(data);
		setErrorDistn(data, errorData);
		
		setLayout(new ProportionLayout(0.5, 3));
		add(ProportionLayout.LEFT, leftPanel(data));
		add(ProportionLayout.RIGHT, rightPanel(data));
	}
	
	protected CoreModelDataSet getData() {
		return new GroupsDataSet(this);
	}
	
	private SummaryDataSet getPooledSummaryData(CoreModelDataSet data) {
		SummaryDataSet summaryData = new SummaryDataSet(data, "x");
		summaryData.addVariable("pooledS", new PooledSSummary("pooledS", "y", "x", kSummaryDecimals));
		return summaryData;
	}
	
	private DataSet getErrorData(CoreModelDataSet sourceData) {
		DataSet errorData = new DataSet();
			NumVariable dummy = new NumVariable("Dummy");
		errorData.addVariable("dummy", dummy);
			NormalDistnVariable error = new NormalDistnVariable("Error distribution");
		errorData.addVariable("errorDistn", error);
		return errorData;
	}
	
	protected void setErrorDistn(CoreModelDataSet sourceData, DataSet errorData) {
		GroupsDataSet anovaData = (GroupsDataSet)sourceData;
		double s1 = anovaData.getSD(0);
		double s2 = anovaData.getSD(1);
		double n1 = anovaData.getN(0);
		double n2 = anovaData.getN(1);
		double errorSD;
		if (usePooledSD)
			errorSD = Math.sqrt((1.0 / n1 + 1.0 / n2) * (s1 * s1 * (n1 - 1) + s2 * s2 * (n2 - 1)) / (n1 + n2 - 2));
		else
			errorSD = Math.sqrt(s1 * s1 / n1 + s2 * s2 / n2);
		
		NormalDistnVariable errorDistn = (NormalDistnVariable)errorData.getVariable("errorDistn");
		errorDistn.setMean(0.0);
		errorDistn.setSD(errorSD);
		errorDistn.setDecimals(anovaData.getSummaryDecimals());
	}
	
	private XPanel leftPanel(CoreModelDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		thePanel.add("Center", displayPanel(data));
		
			XPanel lowerPanel = new XPanel();
			lowerPanel.setLayout(new BorderLayout());
			lowerPanel.add("Center", descriptions(data));
			lowerPanel.add("South", dataChoicePanel(data));
			
		thePanel.add("South", lowerPanel);
		
		return thePanel;
	}
	
	protected XPanel displayPanel(CoreModelDataSet data) {
		GroupsDataSet anovaData = (GroupsDataSet)data;
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
		
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new AxisLayout());
			
			theNumAxis = new VertAxis(this);
			theNumAxis.readNumLabels(anovaData.getYAxisInfo());
			dataPanel.add("Left", theNumAxis);
			
			theGroupAxis = new HorizAxis(this);
			CatVariable groupVariable = anovaData.getCatVariable();
			theGroupAxis.setCatLabels(groupVariable);
			theGroupAxis.setAxisName(anovaData.getXVarName());
			dataPanel.add("Bottom", theGroupAxis);
			
			theView = new VerticalDotView(anovaData, this, theNumAxis, theGroupAxis, "y", "x", null, 0.4);
			theView.setMeanDisplay(VerticalDotView.MEAN_CHANGE);
			dataPanel.add("Center", theView);
			theView.lockBackground(Color.white);
		
		thePanel.add("Center", dataPanel);
		
			responseNameLabel = new XLabel(anovaData.getYVarName(), XLabel.LEFT, this);
			responseNameLabel.setFont(theNumAxis.getFont());
		thePanel.add("North", responseNameLabel);
		return thePanel;
	}
	
	private XTextArea descriptions(CoreModelDataSet data) {
		String messageText[] = data.getDescriptionStrings();
		int messageWidth = Integer.parseInt(getParameter(DESCRIPTION_WIDTH_PARAM));
		
		dataDescriptions = new XTextArea(messageText, 0, messageWidth, this);
		dataDescriptions.setFont(getStandardFont());
		dataDescriptions.lockBackground(Color.white);
		dataDescriptions.setForeground(Color.red);
		
		return dataDescriptions;
	}
	
	protected XPanel rightPanel(CoreModelDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 5));
		
		thePanel.add("North", summaryPanel(data));
		thePanel.add("Center", errorView(errorData));
		
		return thePanel;
	}
	
	private XPanel errorView(DataSet errorData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			theSummaryAxis = new HorizAxis(this);
			theSummaryAxis.readNumLabels(data.getSummaryAxisInfo());
			theSummaryAxis.setAxisName(errorData.getVariable("errorDistn").name);
			theSummaryAxis.setForeground(Color.red);
		thePanel.add("Bottom", theSummaryAxis);
		
			JitterPlusNormalView summaryDotPlot = new JitterPlusNormalView(errorData, this, theSummaryAxis, "errorDistn", 0.0);
			summaryDotPlot.setActiveNumVariable("dummy");
			summaryDotPlot.setShowDensity (DataPlusDistnInterface.CONTIN_DISTN);
			summaryDotPlot.setDensityColor(kPinkColor);
			summaryDotPlot.lockBackground(Color.white);
		thePanel.add("Center", summaryDotPlot);
		
		return thePanel;
	}
	
	private XPanel summaryPanel(CoreModelDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new ProportionLayout(0.5, 10));
			
			catLabel = new XLabel[2];
			
			topPanel.add(ProportionLayout.LEFT, oneSummaryPanel(data, 0));
			topPanel.add(ProportionLayout.RIGHT, oneSummaryPanel(data, 1));
			
		thePanel.add("Center", topPanel);
		thePanel.add("South", differencePanel(data));
		
		return thePanel;
	}
	
	protected XPanel differencePanel(CoreModelDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
		GroupSummary2View diffHat = new GroupSummary2View(data, this, GroupSummary2View.MU_DIFF_HAT,
																												kMaxSummaryString, data.getSummaryDecimals());
		diffHat.setForeground(Color.red);
		thePanel.add(diffHat);
		
		if (usePooledSD) {
			OneValueImageView pooledSDView = new OneValueImageView(summaryData, "pooledS",
																	this, "groups/sPooled.png", 9, new NumValue(kMaxSummaryString));
			pooledSDView.setForeground(kDarkGreenColor);
			pooledSDView.setHighlightSelection(false);
			thePanel.add(pooledSDView);
												
			GroupSummary2View diffSDHat = new GroupSummary2View(data, this, GroupSummary2View.SD_POOLED_DIFF_HAT,
																											kMaxSummaryString, data.getSummaryDecimals());
			diffSDHat.setForeground(Color.red);
			thePanel.add(diffSDHat);
		}
		else {
			GroupSummary2View diffSDHat = new GroupSummary2View(data, this, GroupSummary2View.SD_DIFF_HAT,
																											kMaxSummaryString, data.getSummaryDecimals());
			diffSDHat.setForeground(Color.red);
			thePanel.add(diffSDHat);
		}
		
		return thePanel;
	}
	
	private XPanel oneSummaryPanel(CoreModelDataSet data, int group) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 2));
			
			CatVariable xVar = (CatVariable)data.getVariable("x");
			catLabel[group] = new XLabel(xVar.getLabel(group).toString(), XLabel.CENTER, this);
			catLabel[group].setFont(getStandardBoldFont());
			thePanel.add(catLabel[group]);
			
			thePanel.add(getGroupSummaryPanel(data, group));
			
		return thePanel;
	}
	
	protected XPanel getGroupSummaryPanel(CoreModelDataSet data, int group) {
		return new GroupSummaryPanel(this, (GroupsDataSet)data, group, GroupSummaryPanel.VERTICAL);
	}
	
	private XPanel dataChoicePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 2));
			
			dataSetChoice = ((CoreModelDataSet)data).dataSetChoice(this);
			thePanel.add(dataSetChoice);
			
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == dataSetChoice) {
			if (data.changeDataSet(dataSetChoice.getSelectedIndex(), dataDescriptions, null)) {
				
				if (theNumAxis != null) {
					theNumAxis.readNumLabels(((GroupsDataSet)data).getYAxisInfo());
					theNumAxis.repaint();
				}
				
				CatVariable xVar = (CatVariable)data.getVariable("x");
				if (theGroupAxis != null) {
					theGroupAxis.setCatLabels(xVar);
					theGroupAxis.setAxisName(data.getXVarName());
					theGroupAxis.repaint();
				}
				
				data.variableChanged("x");
				
				if (responseNameLabel != null)
					responseNameLabel.setText(data.getYVarName());
				
				for (int i=0 ; i<xVar.noOfCategories() ; i++)
					catLabel[i].setText(xVar.getLabel(i).toString());
				
				setErrorDistn(data, errorData);
				theSummaryAxis.readNumLabels(data.getSummaryAxisInfo());
				theSummaryAxis.repaint();
				errorData.variableChanged("errorDistn");
				
				if (usePooledSD) {
					summaryData.redoLastSummary();
//					summaryData.variableChanged("pooledS");
				}
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