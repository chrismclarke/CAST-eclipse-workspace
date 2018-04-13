package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import coreVariables.*;
import formula.*;
import graphics3D.*;
import imageUtils.*;


import ssq.*;
import multivarProg.*;
import multiRegn.*;


public class DataSeqAnovaApplet extends RotateApplet {
	static final private String MAX_COEFF_PARAM = "maxCoeff";
	static final private String MAX_T_PARAM = "maxT";
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String COMPONENT_DECIMALS_PARAM = "componentDecimals";
	
//	static final private NumValue kMaxPValue = new NumValue(0.9999, 4);
	static final private NumValue kMaxR2 = new NumValue(1.0, 4);
	
	static final private String kYVarName = "Y";
	static final private String kXVarName[] = {"X", "Z"};
	
//	static final private boolean kOnlyShowX[] = {true, true, false};
//	static final private boolean kOnlyShowZ[] = {true, false, true};
	
	static final private Color kAnovaBackgroundColor = new Color(0xFFDDDD);
	static final protected Color kTestBackgroundColor = new Color(0xDDDDFF);
	static final private Color kFColor = new Color(0x0000CC);
	
	static final private Color kSsqColors[] = {Color.black, Color.black, kFColor, Color.black};
	
	static final private int kDescriptionWidth = 150;
	static final private int kQuestionWidth = 450;
	
	private String kXZComponentNames[];
	private String kZXComponentNames[];
	
	private MultiRegnDataSet data;
	protected SummaryDataSet summaryData;
	
	protected MultiLinearEqnView fullEquationView;
//	private Model3ResidView oneExplanView;
	
	private D3Axis xAxis, yAxis, zAxis;
	
	protected NumValue maxSsq, maxMsq, maxF;
	protected NumValue maxCoeff[];
	private NumValue maxT;
	
	private XTextArea dataDescription, dataConclusion;
	
	private XPanel testPanel;
	private CardLayout testLayout;
	
	protected XChoice dataSetChoice;
	private XChoice orderChoice;
	private int currentOrder = 0;
	
	public void setupApplet() {
		kXZComponentNames = new String[4];
		kXZComponentNames[0] = translate("Total");
		kXZComponentNames[1] = "X";
		kXZComponentNames[2] = "Z " + translate("after") + " X";
		kXZComponentNames[3] = translate("Residual");
		
		kZXComponentNames = new String[4];
		kZXComponentNames[0] = translate("Total");
		kZXComponentNames[1] = "Z";
		kZXComponentNames[2] = "X " + translate("after") + " Z";
		kZXComponentNames[3] = translate("Residual");
		
		super.setupApplet();
	}
	
	private NumValue[] copyParams(MultipleRegnModel ls) {
		int nParam = ls.noOfParameters();
		NumValue paramCopy[] = new NumValue[nParam];
		for (int i=0 ; i<nParam ; i++)
			paramCopy[i] = new NumValue(ls.getParameter(i));
		return paramCopy;
	}
	
	protected DataSet readData() {
		data = new MultiRegnDataSet(this);
		
		MultipleRegnModel ls = (MultipleRegnModel)data.getVariable("ls");
		
		MultipleRegnModel lsX = new MultipleRegnModel("X only", data, MultiRegnDataSet.xKeys,
																																						copyParams(ls));
		lsX.updateLSParams("y", data.getXOnlyConstraints());
		data.addVariable("lsX", lsX);
		
		MultipleRegnModel lsZ = new MultipleRegnModel("Z only", data, MultiRegnDataSet.xKeys,
																																						copyParams(ls));
		lsZ.updateLSParams("y", data.getZOnlyConstraints());
		data.addVariable("lsZ", lsZ);
		
		int componentDecimals = Integer.parseInt(getParameter(COMPONENT_DECIMALS_PARAM));
		SeqXZComponentVariable.addComponentsToData(data, "x", "z", "y", "lsX", "lsZ", "ls", componentDecimals);
		
		summaryData = getSummaryData(data);
		
		return data;
	}
	
	private SummaryDataSet getSummaryData(MultiRegnDataSet data) {
			StringTokenizer st = new StringTokenizer(getParameter(MAX_SSQ_PARAM));
			maxSsq = new NumValue(st.nextToken());
			maxMsq = new NumValue(st.nextToken());
			maxF = new NumValue(st.nextToken());
		
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "error",
									SeqXZComponentVariable.kAllComponentKeys, maxSsq.decimals, kMaxR2.decimals,
									maxMsq.decimals, maxF.decimals);
		
			int n = ((NumVariable)(data.getVariable("y"))).noOfValues();
			
			maxCoeff = new NumValue[3];
			int decimals[] = new int[3];
			st = new StringTokenizer(getParameter(MAX_COEFF_PARAM));
			for (int i=0 ; i<3 ; i++) {
				maxCoeff[i] = new NumValue(st.nextToken());
				decimals[i] = maxCoeff[i].decimals;
			}
			maxT = new NumValue(getParameter(MAX_T_PARAM));
		
		addTTestVariables(summaryData, decimals, n);
		summaryData.setSingleSummaryFromData();
		
		return summaryData;
	}
	
	protected void addTTestVariables(SummaryDataSet summaryData, int[] decimals, int n) {
		summaryData.addVariable("planeXZ", new LSCoeffVariable("Planes", "ls",
																						MultiRegnDataSet.xKeys, "y", null, decimals));
		
		for (int i=1 ; i<3 ; i++) {
			String coeffKey = "b" + i;
			summaryData.addVariable(coeffKey, new SingleLSCoeffVariable(coeffKey, summaryData,
																																						"planeXZ", i));
			String coeffSEKey = coeffKey + "_se";
			summaryData.addVariable(coeffSEKey, new CoeffSEVariable("St error", "ls", "y", i,
																																							decimals[i]));
			String tValueKey = coeffKey + "_t";
			summaryData.addVariable(tValueKey, new RatioVariable("t", summaryData, coeffKey,
																																	coeffSEKey, maxT.decimals));
		}
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
		
			XLabel dataSetLabel = new XLabel(translate("Data set") + ":", XLabel.LEFT, this);
			dataSetLabel.setFont(getStandardBoldFont());
		thePanel.add(dataSetLabel);
		
			dataSetChoice = ((MultiRegnDataSet)data).dataSetChoice(this);
		thePanel.add(dataSetChoice);
		
		return thePanel;
	}
	
	protected XPanel fullEquationPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 5, 0, 0);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
			fullEquationView = new MultiLinearEqnView(data, this, "ls", kYVarName, kXVarName, maxCoeff, maxCoeff);
			fullEquationView.setHighlightIndex(2);
		thePanel.add(fullEquationView);
		
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
		thePanel.add("West", leftPanel(data));
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new BorderLayout(0, 10));
			
			rightPanel.add("Center", view3DPanel(data));
			rightPanel.add("South", fullEquationPanel(data));
		
		thePanel.add("Center", rightPanel);
		
		return thePanel;
	}
	
	protected boolean canChangeOrder() {
		return true;
	}
	
	protected XPanel leftPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FixedSizeLayout(kDescriptionWidth, 0));
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new BorderLayout(0, 20));
		
				MultiRegnDataSet modelData = (MultiRegnDataSet)data;
				dataDescription = new XTextArea(modelData.getDescriptionStrings(), 0, kDescriptionWidth, this);
				dataDescription.lockBackground(Color.white);
			
			innerPanel.add("North", dataDescription);
			
			innerPanel.add("Center", new XPanel());
			
			if (canChangeOrder()) {
				XPanel orderChoicePanel = new XPanel();
				orderChoicePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 1));
				
					XLabel orderLabel = new XLabel(translate("Fitting order") + ":", XLabel.LEFT, this);
					orderLabel.setFont(getStandardBoldFont());
					
				orderChoicePanel.add(orderLabel);
				
					orderChoice = new XChoice(this);
					orderChoice.addItem("X " + translate("then") + " Z");
					orderChoice.addItem("Z " + translate("then") + " X");
					
				orderChoicePanel.add(orderChoice);
				
				innerPanel.add("South", orderChoicePanel);
			}
		
		thePanel.add(innerPanel);
		
		return thePanel;
	}
	
	protected XPanel view3DPanel(DataSet data) {
		MultiRegnDataSet regnData = (MultiRegnDataSet)data;
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			xAxis = new D3Axis(regnData.getXVarName(), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(regnData.getXAxisInfo());
			yAxis = new D3Axis(regnData.getYVarName(), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(regnData.getYAxisInfo());
			zAxis = new D3Axis(regnData.getZVarName(), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(regnData.getZAxisInfo());
			
			Model3ResidView view3D = new Model3ResidView(data, this, xAxis, yAxis, zAxis, "ls",
																		MultiRegnDataSet.xKeys, "y");
			view3D.setShowSelectedArrows(false);
			view3D.lockBackground(Color.white);
			theView = view3D;
			
		thePanel.add("Center", view3D);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(4, 0, 0, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		thePanel.add(RotateButton.createRotationPanel(theView, this, RotateButton.VERTICAL));
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 7, 0, 0);
		thePanel.setLayout(new BorderLayout(10, 0));
		
//			dataConclusion = new XTextArea(regnData.getQuestionStrings(), 0, kQuestionWidth, this, DataView.BUFFERED);
//			dataConclusion.lockBackground(Color.white);
//		thePanel.add("South", dataConclusion);
		thePanel.add("South", conclusionPanel(data));
		
			testPanel = new XPanel();
			testLayout = new CardLayout();
			testPanel.setLayout(testLayout);
			
			testPanel.add("xThenZ", tTestPanel(summaryData, "b2",
											SeqXZComponentVariable.kXZComponentKey, kXZComponentNames, "f-zAfterX",
											"multiRegn/tZFormula.gif", "multiRegn/fZFormula.gif", maxCoeff[2]));
			testPanel.add("zThenX", tTestPanel(summaryData, "b1",
											SeqXZComponentVariable.kZXComponentKey, kZXComponentNames, "f-xAfterZ",
											"multiRegn/tXFormula.gif", "multiRegn/fXFormula.gif", maxCoeff[1]));
			testLayout.show(testPanel, "xThenZ");
		
		thePanel.add("Center", testPanel);
		
		return thePanel;
	}
	
	protected XPanel conclusionPanel(DataSet data) {
		MultiRegnDataSet regnData = (MultiRegnDataSet)data;
		XPanel thePanel = new InsetPanel(0, 6, 0, 0);
		thePanel.setLayout(new BorderLayout(10, 0));
		
			XLabel conclusionLabel = new XLabel(translate("Conclusion") + ":", XLabel.LEFT, this);
			conclusionLabel.setFont(getStandardBoldFont());
			
		thePanel.add("West", conclusionLabel);
		
			dataConclusion = new XTextArea(regnData.getQuestionStrings(), 0, kQuestionWidth, this);
			dataConclusion.lockBackground(Color.white);
		
		thePanel.add("Center", dataConclusion);
		
		return thePanel;
	}
	
	private XPanel tTestPanel(SummaryDataSet summaryData, String coeffKey,
												String[] anovaComponentKeys, String[] componentNames, String fKey,
												String tFormulaFile, String fFormulaFile, NumValue maxCoeff) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 8));
		
			XPanel anovaPanel = new InsetPanel(0, 7, 0, 0);
			anovaPanel.setLayout(new BorderLayout(0, 0));
		
				AnovaTableView anovaTable = new AnovaTableView(summaryData, this,
											anovaComponentKeys, maxSsq, maxMsq, maxF, AnovaTableView.SSQ_F_PVALUE);
				anovaTable.setComponentColors(kSsqColors);
				anovaTable.setHilite(2, Color.yellow);
				anovaTable.setComponentNames(componentNames);
			anovaPanel.add("Center", anovaTable);
		
			anovaPanel.lockBackground(kAnovaBackgroundColor);
		thePanel.add("North", anovaPanel);
		
			XPanel testPanel = new InsetPanel(0, 2);
			testPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 0));
			
				String coeffSEKey = coeffKey + "_se";
				String tValueKey = coeffKey + "_t";
				
				FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
				TFormulaPanel tFormula = new TFormulaPanel(summaryData, coeffKey, coeffSEKey,
																			tValueKey, maxCoeff, maxCoeff, maxT, tFormulaFile,
																			23, 18, 83, true, stdContext);
			testPanel.add(tFormula);
			
				OneValueImageView fView = new OneValueImageView(summaryData, fKey, this, fFormulaFile, 18, maxF);
				fView.setForeground(kFColor);
			testPanel.add(fView);
		
			testPanel.lockBackground(kTestBackgroundColor);
		thePanel.add("Center", testPanel);
		
		return thePanel;
	}
	
	protected void changeDisplaysForNewData(MultiRegnDataSet data) {
		xAxis.setNumScale(data.getXAxisInfo());
		xAxis.setLabelName(data.getXVarName());
		zAxis.setNumScale(data.getZAxisInfo());
		zAxis.setLabelName(data.getZVarName());
		yAxis.setNumScale(data.getYAxisInfo());
		yAxis.setLabelName(data.getYVarName());
	}
	
	private boolean localAction(Object target) {
		if (target == orderChoice) {
			if (orderChoice.getSelectedIndex() != currentOrder) {
				currentOrder = orderChoice.getSelectedIndex();
				testLayout.show(testPanel, (currentOrder == 0) ? "xThenZ" : "zThenX");
				fullEquationView.setHighlightIndex((currentOrder == 0) ? 2 : 1);
				fullEquationView.repaint();
			}
			return true;
		}
		else if (target == dataSetChoice) {
			MultiRegnDataSet regnData = (MultiRegnDataSet)data;
			int newDataIndex = dataSetChoice.getSelectedIndex();
			if (regnData.changeDataSet(newDataIndex)) {
				dataDescription.setText(newDataIndex);
				dataConclusion.setText(newDataIndex);
				
				changeDisplaysForNewData(regnData);
				
				data.variableChanged("y");
			
				summaryData.setSingleSummaryFromData();
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