package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import distn.*;
import valueList.*;
import coreVariables.*;
import formula.*;
import graphics3D.*;

import multivarProg.*;
import multiRegn.*;


public class TAndTApplet extends RotateApplet {
	static final private String MAX_COEFF_PARAM = "maxCoeff";
	static final private String MAX_T_PARAM = "maxT";
	
	static final private NumValue kMaxPValue = new NumValue(0.9999, 4);
	
	static final private String kXZKeys[] = {"x", "z"};
	
	static final private boolean kOnlyShowX[] = {true, true, false};
	static final private boolean kOnlyShowZ[] = {true, false, true};
	
	static final private Color kPValueBackground = new Color(0xDDDDEE);
	
	private MultiRegnDataSet data;
	private SummaryDataSet summaryData;
	
	private MultiLinearEqnView fullEquationView;
	private Model3ResidView oneExplanView;
	
	private String xVarName[] = new String[2];
	private String yVarName;
	
	private NumValue maxCoeff[];
	private NumValue maxT;
	
	private XChoice xVariableChoice;
	private int currentXVariable = 0;
	
	private XPanel jointTestPanel, singleTestPanel, singleEqnPanel;
	private CardLayout jointTestLayout, singleTestLayout, singleEqnLayout;
	
	protected DataSet readData() {
		data = new MultiRegnDataSet(this);
		
			MultipleRegnModel fullModel = (MultipleRegnModel)data.getVariable("ls");
			
			NumValue initParams[] = new NumValue[3];
			initParams[0] = fullModel.getParameter(0);
			initParams[1] = fullModel.getParameter(1);
			initParams[2] = fullModel.getParameter(2);
			MultipleRegnModel lsXZ = new MultipleRegnModel("LS: X only", data, kXZKeys,
																											initParams, fullModel.evaluateSD());
			lsXZ.updateLSParams("y", data.getXOnlyConstraints());
			data.addVariable("lsXZ", lsXZ);
		
//		printParameters(data, "after creation", "lsXZ");
		
		setCoeffNames();
		
		summaryData = getSummaryData(data);
		summaryData.setSingleSummaryFromData();
		
//		printParameters(data, "after summary", "lsXZ");
		return data;
	}
	
//	private void printParameters(DataSet data, String prefix, String lsKey) {
//			MultipleRegnModel ls = (MultipleRegnModel)data.getVariable(lsKey);
//			System.out.print(prefix + ": ");
//			for (int i=0 ; i<3 ; i++)
//				System.out.print(ls.getParameter(i).toString() + ", ");
//			System.out.print("\n");
//	}
	
	private void setCoeffNames() {
		xVarName[0] = data.getVariable("x").name;
		xVarName[1] = data.getVariable("z").name;
		yVarName = data.getVariable("y").name;
	}
	
	private SummaryDataSet getSummaryData(MultiRegnDataSet data) {
		SummaryDataSet summaryData = new SummaryDataSet(data, "error");
		
			int n = ((NumVariable)(data.getVariable("y"))).noOfValues();
			
			maxCoeff = new NumValue[3];
			int decimals[] = new int[3];
			StringTokenizer st = new StringTokenizer(getParameter(MAX_COEFF_PARAM));
			for (int i=0 ; i<3 ; i++) {
				maxCoeff[i] = new NumValue(st.nextToken());
				decimals[i] = maxCoeff[i].decimals;
			}
			maxT = new NumValue(getParameter(MAX_T_PARAM));
		
		addXZTestVariables(summaryData, decimals, n);
		addXTestVariables(summaryData, data, kXZKeys, decimals, n);
		
		return summaryData;
	}
	
	private void addXZTestVariables(SummaryDataSet summaryData, int[] decimals, int n) {
		summaryData.addVariable("t2Distn", new TDistnVariable("T distn", n - 3));
		
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
			String pValueKey = coeffKey + "_pVal";
			summaryData.addVariable(pValueKey, new TailAreaVariable(translate("p-value") + " =", summaryData,
													tValueKey, "t2Distn", TailAreaVariable.TWO_TAILED, kMaxPValue.decimals));
		}
	}
	
	private void addXTestVariables(SummaryDataSet summaryData, MultiRegnDataSet data,
																									String[] xKeyArray, int[] decimals, int n) {
		summaryData.addVariable("t1Distn", new TDistnVariable("T distn", n - 2));
		
		String lsKey = "lsXZ";
		
		for (int i=0 ; i<2 ; i++) {
				double constraints[] = (i == 0) ? data.getXOnlyConstraints()
																									: data.getZOnlyConstraints();
				String planeKey = "plane-" + ((i == 0) ? "x" : "z");
			summaryData.addVariable(planeKey, new LSCoeffVariable("Planes", lsKey,
																								xKeyArray, "y", constraints, decimals));
			
				String coeffKey = (i == 0) ? "bx" : "bz";
			summaryData.addVariable(coeffKey, new SingleLSCoeffVariable(coeffKey, summaryData,
																																							planeKey, i + 1));
				String coeffSEKey = coeffKey + "_se";
			summaryData.addVariable(coeffSEKey, new CoeffSEVariable("St error", lsKey, "y", i + 1,
																														constraints, decimals[i + 1]));
			
				String tValueKey = coeffKey + "_t";
			summaryData.addVariable(tValueKey, new RatioVariable(tValueKey, summaryData, coeffKey,
																																		coeffSEKey, maxT.decimals));
				String pValueKey = coeffKey + "_pVal";
			summaryData.addVariable(pValueKey, new TailAreaVariable(translate("p-value") + " =", summaryData,
													tValueKey, "t1Distn", TailAreaVariable.TWO_TAILED, kMaxPValue.decimals));
		}
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
			XLabel testLabel = new XLabel(translate("Variable to test") + ":", XLabel.LEFT, this);
			testLabel.setFont(getStandardBoldFont());
		thePanel.add(testLabel);
		
			xVariableChoice = new XChoice(this);
			xVariableChoice.addItem(xVarName[0]);
			xVariableChoice.addItem(xVarName[1]);
		thePanel.add(xVariableChoice);
		
		return thePanel;
	}
	
	private XPanel fullEquationPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 5);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		
			fullEquationView = new MultiLinearEqnView(data, this, "ls", yVarName, xVarName, maxCoeff, maxCoeff);
			fullEquationView.setHighlightIndex(1);
		thePanel.add(fullEquationView);
		
		thePanel.lockBackground(Color.white);
		return thePanel;
	}
	
	private XPanel marginalEquationPanel(DataSet data) {
		singleEqnPanel = new InsetPanel(0, 5);
		
		singleEqnLayout = new CardLayout();
		singleEqnPanel.setLayout(singleEqnLayout);
		
			XPanel xPanel = new XPanel();
			xPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
			
				MultiLinearEqnView xEquationView = new MultiLinearEqnView(data, this, "lsXZ",
																						yVarName, xVarName, maxCoeff, maxCoeff);
				xEquationView.setHighlightIndex(1);
				xEquationView.setDrawParameters(kOnlyShowX);
			
			xPanel.add("Center", xEquationView);
			
		singleEqnPanel.add("x", xPanel);
		
			XPanel zPanel = new XPanel();
			zPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
				MultiLinearEqnView zEquationView = new MultiLinearEqnView(data, this, "lsXZ",
																											yVarName, xVarName, maxCoeff, maxCoeff);
				zEquationView.setHighlightIndex(2);
				zEquationView.setDrawParameters(kOnlyShowZ);
				
			zPanel.add("Center", zEquationView);
				
		singleEqnPanel.add("z", zPanel);
		
		singleEqnLayout.show(singleEqnPanel, "x");
		
		singleEqnPanel.lockBackground(Color.white);
		return singleEqnPanel;
	}
	
	private XPanel view3DPanel(DataSet data, String modelKey, int startTheta0, int startTheta1) {
		MultiRegnDataSet regnData = (MultiRegnDataSet)data;
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			D3Axis xAxis = new D3Axis(regnData.getXVarName(), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(regnData.getXAxisInfo());
			D3Axis yAxis = new D3Axis(regnData.getYVarName(), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(regnData.getYAxisInfo());
			D3Axis zAxis = new D3Axis(regnData.getZVarName(), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(regnData.getZAxisInfo());
			
			Model3ResidView view3D = new Model3ResidView(data, this, xAxis, yAxis, zAxis, modelKey,
																													MultiRegnDataSet.xKeys, "y");
			view3D.setShowSelectedArrows(false);
			view3D.rotateTo(startTheta0, startTheta1);
			view3D.lockBackground(Color.white);
			if (theView == null)
				theView = view3D;
			else
				oneExplanView = view3D;
			
		thePanel.add("Center", view3D);
		return thePanel;
	}
	
	private XPanel xzPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 7));
		
		thePanel.add("Center", view3DPanel(data, "ls", 30, 30));
		thePanel.add("North", fullEquationPanel(data));
		
			jointTestPanel = new XPanel();
			jointTestLayout = new CardLayout();
			jointTestPanel.setLayout(jointTestLayout);
			
			jointTestPanel.add("x", tTestPanel(summaryData, "b1", maxCoeff[1], theView, RotateButton.XYZ_ROTATE));
			jointTestPanel.add("z", tTestPanel(summaryData, "b2", maxCoeff[2], theView, RotateButton.XYZ_ROTATE));
			jointTestLayout.show(jointTestPanel, "x");
		
		thePanel.add("South", jointTestPanel);
		
		return thePanel;
	}
	
	private XPanel xPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 7));
		
		thePanel.add("Center", view3DPanel(data, "lsXZ", 0, 0));
		thePanel.add("North", marginalEquationPanel(data));
		
			singleTestPanel = new XPanel();
			singleTestLayout = new CardLayout();
			singleTestPanel.setLayout(singleTestLayout);
			
			singleTestPanel.add("x", tTestPanel(summaryData, "bx", maxCoeff[1], oneExplanView, RotateButton.YX_ROTATE));
			singleTestPanel.add("z", tTestPanel(summaryData, "bz", maxCoeff[2], oneExplanView, RotateButton.YZ_ROTATE));
			singleTestLayout.show(singleTestPanel, "x");
		
		thePanel.add("South", singleTestPanel);
		
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10));
		
		thePanel.add(ProportionLayout.LEFT, xzPanel(data, summaryData));
		thePanel.add(ProportionLayout.RIGHT, xPanel(data, summaryData));
		
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		return thePanel;
	}
	
	private XPanel tTestPanel(SummaryDataSet summaryData, String coeffKey, NumValue maxCoeff,
																					Rotate3DView rotateView, int rotateType) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
			XPanel testPanel = new XPanel();
			testPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
			
				String coeffSEKey = coeffKey + "_se";
				String tValueKey = coeffKey + "_t";
				String pValueKey = coeffKey + "_pVal";
				
				FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
				TFormulaPanel tFormula = new TFormulaPanel(summaryData, coeffKey, coeffSEKey,
															tValueKey, maxCoeff, maxCoeff, maxT,
															"multiRegn/tFormula.gif", 22, 17, 69, false, stdContext);
			testPanel.add(tFormula);
			
				XPanel pValPanel = new InsetPanel(10, 3);
				pValPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					OneValueView pValueView = new OneValueView(summaryData, pValueKey, this, kMaxPValue);
					pValueView.setFont(getBigFont());
				pValPanel.add(pValueView);
			
				pValPanel.lockBackground(kPValueBackground);
			testPanel.add(pValPanel);
		
		thePanel.add("Center", testPanel);
		
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
			buttonPanel.add(new RotateButton(rotateType, rotateView, this));
			
		thePanel.add("West", buttonPanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == xVariableChoice) {
			int newChoice = xVariableChoice.getSelectedIndex();
			if (newChoice != currentXVariable) {
				currentXVariable = newChoice;
				String displayVar = (newChoice == 0) ? "x" : "z";
				
				jointTestLayout.show(jointTestPanel, displayVar);
				singleTestLayout.show(singleTestPanel, displayVar);
//				singleEqnLayout.show(singleEqnPanel, displayVar);
				
				double constraints[] = (newChoice == 0) ? data.getXOnlyConstraints()
																								: data.getZOnlyConstraints();
				MultipleRegnModel lsXZ = (MultipleRegnModel)data.getVariable("lsXZ");
//				lsXZ.updateLSParams("y", constraints);
				
				fullEquationView.setHighlightIndex(newChoice + 1);
				fullEquationView.repaint();
				
				oneExplanView.customRotate(new MarginRotateThread(oneExplanView, ((newChoice == 0) ? 0 : 90),
																		0, lsXZ, constraints, singleEqnLayout, singleEqnPanel, displayVar));
				
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