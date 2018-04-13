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

import ssq.*;
import multivarProg.*;
import multiRegn.*;


public class TAndFApplet extends RotateApplet {
	static final private String MAX_COEFF_PARAM = "maxCoeff";
	static final private String MAX_T_PARAM = "maxT";
	static final private String MAX_SSQ_PARAM = "maxSsq";
	
	static final private NumValue kMaxPValue = new NumValue(0.9999, 4);
	static final private NumValue kMaxR2 = new NumValue(1.0, 4);
	
	static final private Color kTBackgroundColor = new Color(0xDDDDFF);
	static final private Color kFBackgroundColor = new Color(0xFFDDDD);
	
	static final private Color kFNumerColor = new Color(0x990000);
	static final private Color kFDenomColor = new Color(0x000099);
	
	static final private String kFImageFileName = "multiRegn/fFormula.png";
	static final private int kFImageAscent = 22;
	static final private int kFImageDescent = 14;
	static final private int kFImageWidth = 96;
	
	private MultiRegnDataSet data;
	private SummaryDataSet summaryData;
	
//	private Model3ResidView theView;
//	private D3Axis xAxis, yAxis, zAxis;
	
	private String xVarName[] = new String[2];
	private String yVarName;
	
	private NumValue maxCoeff[];
	private NumValue maxT;
	private int errorDF;
	
	private NumValue maxSsq, maxMsq, maxF;
	
	protected DataSet readData() {
		data = new MultiRegnDataSet(this);
		data.addBasicComponents();
		
		setCoeffNames();
		
		summaryData = getSummaryData(data);
		summaryData.setSingleSummaryFromData();
		return data;
	}
	
	protected void setCoeffNames() {
		xVarName[0] = data.getVariable("x").name;
		xVarName[1] = data.getVariable("z").name;
		yVarName = data.getVariable("y").name;
	}
	
	protected SummaryDataSet getSummaryData(MultiRegnDataSet data) {
			StringTokenizer st = new StringTokenizer(getParameter(MAX_SSQ_PARAM));
			maxSsq = new NumValue(st.nextToken());
			maxMsq = new NumValue(st.nextToken());
			maxF = new NumValue(st.nextToken());
		
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "error",
									BasicComponentVariable.kComponentKey, maxSsq.decimals, kMaxR2.decimals,
									maxMsq.decimals, maxF.decimals);
		
			int numerDF = 2;
			int denomDF = ((BasicComponentVariable)data.getVariable("resid")).getDF();

		summaryData.addVariable("fDistn", new FDistnVariable("F distn", numerDF, denomDF));
		
			String pValueKey = "f_pVal";
		summaryData.addVariable("f_pVal", new TailAreaVariable(translate("p-value") + " =", summaryData,
												"f-explained", "fDistn", TailAreaVariable.UPPER_TAIL, kMaxPValue.decimals));
		
			maxCoeff = new NumValue[3];
			int decimals[] = new int[3];
			st = new StringTokenizer(getParameter(MAX_COEFF_PARAM));
			for (int i=0 ; i<3 ; i++) {
				maxCoeff[i] = new NumValue(st.nextToken());
				decimals[i] = maxCoeff[i].decimals;
			}
			maxT = new NumValue(getParameter(MAX_T_PARAM));
			
		summaryData.addVariable("planes", new LSCoeffVariable("Planes", "ls",
																						MultiRegnDataSet.xKeys, "y", null, decimals));
		
			NumVariable yVar = (NumVariable)data.getVariable("y");
			errorDF = yVar.noOfValues() - 3;
		
		summaryData.addVariable("tDistn", new TDistnVariable("T distn", errorDF));
		
		for (int i=1 ; i<3 ; i++) {
			String coeffKey = "b" + i;
			summaryData.addVariable(coeffKey, new SingleLSCoeffVariable(coeffKey, summaryData,
																																						"planes", i));
			String coeffSEKey = coeffKey + "_se";
			summaryData.addVariable(coeffSEKey, new CoeffSEVariable("St error", "ls", "y", i,
																																							decimals[i]));
			String tValueKey = coeffKey + "_t";
			summaryData.addVariable(tValueKey, new RatioVariable("t", summaryData, coeffKey,
																																	coeffSEKey, maxT.decimals));
			pValueKey = coeffKey + "_pVal";
			summaryData.addVariable(pValueKey, new TailAreaVariable(translate("p-value") + " =", summaryData,
													tValueKey, "tDistn", TailAreaVariable.TWO_TAILED, kMaxPValue.decimals));
		}
		
		return summaryData;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		boolean hasLabel = data.getVariable("label") != null;
		if (hasLabel)
			thePanel.add(new OneValueView(data, "label", this));
		
		return thePanel;
	}
	
	private XPanel equationPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 5, 0, 0);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		
//			XLabel modelLabel = new XLabel("Least sqrs: ", XLabel.LEFT, this);
//			modelLabel.setFont(getStandardBoldFont());
//		thePanel.add(modelLabel);
		thePanel.add(new MultiLinearEqnView(data, this, "ls", yVarName, xVarName, maxCoeff, maxCoeff));
		
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		MultiRegnDataSet regnData = (MultiRegnDataSet)data;
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			D3Axis xAxis = new D3Axis(regnData.getXVarName(), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(regnData.getXAxisInfo());
			D3Axis yAxis = new D3Axis(regnData.getYVarName(), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(regnData.getYAxisInfo());
			D3Axis zAxis = new D3Axis(regnData.getZVarName(), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(regnData.getZAxisInfo());
			
			Model3ResidView tempView = new Model3ResidView(data, this, xAxis, yAxis, zAxis, "ls",
																																MultiRegnDataSet.xKeys, "y");
			tempView.setShowSelectedArrows(false);
			tempView.setSelectCrosses(false);
			theView = tempView;
			theView.lockBackground(Color.white);
			
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 20));
		
			XPanel rotatePanel = new XPanel();
			rotatePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 8));
			rotatePanel.add(RotateButton.createRotationPanel(theView, this, RotateButton.VERTICAL));
			
				rotateButton = new XButton(translate("Spin"), this);
			rotatePanel.add(rotateButton);
		
		thePanel.add(rotatePanel);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 12));
		
		thePanel.add(equationPanel(data));
		
			XPanel tPanel = new XPanel();
			tPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
			tPanel.add(tTestPanel(summaryData, 1, kTBackgroundColor));
			tPanel.add(tTestPanel(summaryData, 2, kTBackgroundColor));
			
		thePanel.add(tPanel);
			
		thePanel.add(fTestPanel(summaryData, kFBackgroundColor));
		return thePanel;
	}
	
	private XPanel tTestPanel(SummaryDataSet summaryData, int coeffIndex, Color backgroundColor) {
		XPanel thePanel = new InsetPanel(5, 2);
		thePanel.setLayout(new BorderLayout(0, 0));
		
//			XPanel blankCanvas = new XPanel();
//			if (backgroundColor != null)
//				blankCanvas.lockBackground(backgroundColor);
//		thePanel.add("Center", blankCanvas);
		
			XLabel testLabel = new XLabel(translate("t-test for") + " " + xVarName[coeffIndex - 1], XLabel.LEFT,
																														this);
			testLabel.setFont(getStandardBoldFont());
			testLabel.setForeground(TFormulaPanel.kTColor);
		
		thePanel.add("North", testLabel);
		
			String coeffKey = "b" + coeffIndex;
			String coeffSEKey = coeffKey + "_se";
			String tValueKey = coeffKey + "_t";
			String pValueKey = coeffKey + "_pVal";
			
			FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
			TFormulaPanel tFormula = new TFormulaPanel(summaryData, coeffKey, coeffSEKey,
														tValueKey, maxCoeff[coeffIndex], maxCoeff[coeffIndex], maxT,
														"multiRegn/tFormula.gif", 22, 17, 69, false, stdContext);
		thePanel.add("Center", tFormula);
		
			XPanel pValPanel = new XPanel();
			pValPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				OneValueView pValueView = new OneValueView(summaryData, pValueKey, this, kMaxPValue);
			
			pValPanel.add(pValueView);
			
		thePanel.add("East", pValPanel);
		
		if (backgroundColor != null)
			thePanel.lockBackground(backgroundColor);
		return thePanel;
	}
	
	
	private XPanel fTestPanel(SummaryDataSet summaryData, Color backgroundColor) {
//		AnovaImages.loadGroupImages(this);
		
		XPanel thePanel = new InsetPanel(5, 2);
		thePanel.setLayout(new BorderLayout(0, 0));
		
//		thePanel.add("Center", new XPanel());
		
			StringTokenizer st = new StringTokenizer(translate("F-test for * and"), "*");
			XLabel testLabel = new XLabel(st.nextToken() + xVarName[0] + st.nextToken() + " " + xVarName[1],
																													XLabel.LEFT, this);
			testLabel.setFont(getStandardBoldFont());
		
		thePanel.add("North", testLabel);
		
			String meanExplainedKey = "m-" + BasicComponentVariable.kComponentKey[BasicComponentVariable.EXPLAINED];
			String meanResidKey = "m-" + BasicComponentVariable.kComponentKey[BasicComponentVariable.RESIDUAL];
			String fKey = "f-" + BasicComponentVariable.kComponentKey[BasicComponentVariable.EXPLAINED];
			String pValueKey = "f_pVal";
		
			FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
			FCalcPanel fCalc = new FCalcPanel(summaryData, meanExplainedKey, meanResidKey,
								fKey, maxMsq, maxF, kFNumerColor, kFDenomColor, false, false,
								kFImageFileName, kFImageAscent, kFImageDescent, kFImageWidth, stdContext);
		
		thePanel.add("Center", fCalc);
				
			XPanel pValPanel = new XPanel();
			pValPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				OneValueView pValueView = new OneValueView(summaryData, pValueKey, this, kMaxPValue);
			
			pValPanel.add(pValueView);
			
		thePanel.add("East", pValPanel);
		
		if (backgroundColor != null)
			thePanel.lockBackground(backgroundColor);
		return thePanel;
	}
}