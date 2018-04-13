package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import models.*;
import distn.*;
import graphics3D.*;

import multivarProg.*;
import multiRegn.*;
import ssq.*;
import variance.*;


public class FTestDataApplet extends RotateApplet {
//	static final private String MEAN_DECIMALS_PARAM = "meanDecimals";
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String SSQ_NAMES_PARAM = "ssqNames";
	static final private String F_AXIS_PARAM = "fAxis";
	static final private String AREA_PROPN_PARAM = "areaProportion";
	
	static final private NumValue kMaxR2 = new NumValue(1.0, 4);
	
	static final private Color kDarkRed = new Color(0xAA0000);
	static final private Color kDarkBlue = new Color(0x000099);
	static final private Color kLightGrey = new Color(0xEEEEEE);
	static final private Color kPaleYellow = new Color(0xF5FFB8);
	
	private MultiRegnDataSet data;
	private SummaryDataSet summaryData;
	
	private D3Axis xAxis, yAxis, zAxis;
	
	private XTextArea dataDescription, dataConclusion;
	
	private NumValue maxSsq, maxMsq, maxF;
	
	private XChoice dataSetChoice;
	
	protected DataSet readData() {
		data = new MultiRegnDataSet(this);
		
		data.addBasicComponents();
		
		summaryData = getSummaryData(data);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet data) {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SSQ_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxMsq = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "error",
									BasicComponentVariable.kComponentKey, maxSsq.decimals, kMaxR2.decimals,
									maxMsq.decimals, maxF.decimals);
		
			FDistnVariable fDistn = new FDistnVariable("F distn", 99, 99);
																		// df are set later in adjustDF()
		summaryData.addVariable("fDistn", fDistn);
		
		summaryData.setSingleSummaryFromData();
		return summaryData;
	}
	
	protected XPanel topPanel(DataSet data) {
		MultiRegnDataSet multiData = (MultiRegnDataSet)data;
		
		XPanel thePanel = new InsetPanel(0, 0, 0, 8);
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

		dataSetChoice = multiData.dataSetChoice(this);
		if (dataSetChoice != null)
			thePanel.add(dataSetChoice);
			
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		MultiRegnDataSet multiData = (MultiRegnDataSet)data;
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
		thePanel.add("Center", rotatePlotPanel(data));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.FILL,
																													VerticalLayout.VERT_SPACED, 0));
			
				dataDescription = new XTextArea(multiData.getDescriptionStrings(), 0, 200, this);
				dataDescription.lockBackground(kLightGrey);
				dataDescription.setForeground(kDarkBlue);
			
			leftPanel.add(dataDescription);
			
			leftPanel.add(buttonPanel());
			
		thePanel.add("West", leftPanel);
		
		return thePanel;
	}
	
	private XPanel buttonPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		thePanel.add(new RotateButton(RotateButton.XYZ_ROTATE, theView, this));
			rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
		return thePanel;
	}
	
	private XPanel rotatePlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		CoreVariable xVar = data.getVariable("x");
		xAxis = new D3Axis(xVar == null ? "x" : xVar.name, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
		
		CoreVariable yVar = data.getVariable("y");
		yAxis = new D3Axis(yVar == null ? "y" : yVar.name, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
		yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
		
		CoreVariable zVar = data.getVariable("z");
		zAxis = new D3Axis(zVar == null ? "z" : zVar.name, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
		
		theView = new Model3ResidView(data, this, xAxis, yAxis, zAxis, "ls", MultiRegnDataSet.xKeys, "y");
		
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 20, 0, 0);
		thePanel.setLayout(new BorderLayout(0, 20));
		thePanel.add("Center", anovaTablePanel(summaryData));
		thePanel.add("South", fConclusionPanel(summaryData));
		
		return thePanel;
	}
	
	private XPanel anovaTablePanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			StringTokenizer st = new StringTokenizer(getParameter(SSQ_NAMES_PARAM));
			String ssqNames[] = new String[3];
			for (int i=0 ; i<3 ; i++)
				ssqNames[i] = st.nextToken();
			AnovaTableView table = new AnovaTableView(summaryData, this, BasicComponentVariable.kComponentKey,
										maxSsq, maxMsq, maxF, AnovaTableView.SSQ_F_PVALUE);
			table.setComponentNames(ssqNames);
		thePanel.add(table);
		
		return thePanel;
	}
	
	private XPanel fConclusionPanel(SummaryDataSet summaryData) {
		MultiRegnDataSet multiData = (MultiRegnDataSet)data;
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
		thePanel.add("Center", fDistnPanel(summaryData));
		
			XPanel conclusionPanel = new XPanel();
			conclusionPanel.setLayout(new BorderLayout(0, 3));
			
//				XLabel concLabel = new XLabel("Conclusion", XLabel.LEFT, this);
//				concLabel.setFont(getStandardBoldFont());
//			conclusionPanel.add("North", concLabel);
				
				dataConclusion = new XTextArea(multiData.getQuestionStrings(), 0, 240, this);
				dataConclusion.lockBackground(kPaleYellow);
				dataConclusion.setForeground(kDarkRed);
			
			conclusionPanel.add("Center", dataConclusion);
		
		thePanel.add("East", conclusionPanel);
		
		return thePanel;
	}
	
	private XPanel fDistnPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
			thePanel.setLayout(new FixedSizeLayout(100, 100));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new AxisLayout());
			
				HorizAxis axis = new HorizAxis(this);
				axis.readNumLabels(getParameter(F_AXIS_PARAM));
				
			mainPanel.add("Bottom", axis);
			
				FView fView = new FView(summaryData, this, axis, "fDistn", "f-explained");
				fView.setDistnLabel(new LabelValue("F(#, #)"), Color.gray);
				adjustDF(summaryData);
				double areaPropn = Double.parseDouble(getParameter(AREA_PROPN_PARAM));
				fView.setAreaProportion(areaPropn);
				
				fView.lockBackground(Color.white);
				
			mainPanel.add("Center", fView);
		
		thePanel.add(mainPanel);
		
		return thePanel;
	}
	
	private void adjustDF(SummaryDataSet summaryData) {
		SsqVariable regnSsqVar = (SsqVariable)summaryData.getVariable(BasicComponentVariable.kComponentKey[1]);
		int regnDF = regnSsqVar.getDF();
		
		SsqVariable residSsqVar = (SsqVariable)summaryData.getVariable(BasicComponentVariable.kComponentKey[2]);
		int residDF = residSsqVar.getDF();
		
		FDistnVariable fDistn = (FDistnVariable)summaryData.getVariable("fDistn");
		fDistn.setDF(regnDF, residDF);
	}
	
	private boolean localAction(Object target) {
		if (target == dataSetChoice) {
			data.changeDataSet(dataSetChoice.getSelectedIndex(), dataDescription, dataConclusion);
			xAxis.setNumScale(data.getXAxisInfo());
			xAxis.setLabelName(data.getXVarName());
			zAxis.setNumScale(data.getZAxisInfo());
			zAxis.setLabelName(data.getZVarName());
			yAxis.setNumScale(data.getYAxisInfo());
			yAxis.setLabelName(data.getYVarName());
			data.variableChanged("y");
			
			adjustDF(summaryData);
			
			summaryData.setSingleSummaryFromData();
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