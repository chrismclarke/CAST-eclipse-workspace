package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;
import models.*;
import distn.*;
import valueList.*;
import coreVariables.*;
import formula.*;

import multiRegn.*;


public class CoeffTestApplet extends CoeffCIApplet {
	static final private String MAX_T_PARAM = "maxT";
	
	static final private int kTDistnHeight = 70;
	static final private NumValue kMaxPValue = new NumValue(0.9999, 4);
	static final private String kTAxisInfo = "-5 5 -4 2";
	
	static final protected Color kDistnLabelColor = new Color(0xAAAAAA);
	static final private Color kDarkBlue = new Color(0x000099);
	static final private Color kLeftPanelColor = new Color(0xDDEEFF);
	static final private Color kRightPanelColor = new Color(0xFFE6F5);
	
	static final private Color kTFillColor = new Color(0xCCCCCC);
	static final private Color kTHighlightColor = new Color(0x0099FF);
	
	protected AccurateTailAreaView tView[] = new AccurateTailAreaView[2];
	
	private NumValue maxT;
	
	protected int errorDF;
	
	protected SummaryDataSet getSummaryData(MultiRegnDataSet data) {
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "error");
		
		maxCoeff = new NumValue[3];
		int decimals[] = new int[3];
		StringTokenizer st = new StringTokenizer(getParameter(MAX_COEFF_PARAM));
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
			summaryData.addVariable(coeffKey, new SingleLSCoeffVariable(coeffName[i], summaryData,
																																						"planes", i));
			String coeffSEKey = coeffKey + "_se";
			summaryData.addVariable(coeffSEKey, new CoeffSEVariable(translate("St error"), "ls", "y", i,
																																							decimals[i]));
			String tValueKey = coeffKey + "_t";
			summaryData.addVariable(tValueKey, new RatioVariable("t", summaryData, coeffKey,
																																		coeffSEKey, maxT.decimals));
			String pValueKey = coeffKey + "_pVal";
			summaryData.addVariable(pValueKey, new TailAreaVariable(translate("p-value") + " =", summaryData,
													tValueKey, "tDistn", TailAreaVariable.TWO_TAILED, kMaxPValue.decimals));
		}
		
		return summaryData;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 0, 0, 5);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			XLabel modelLabel = new XLabel(translate("Model") + ": ", XLabel.LEFT, this);
			modelLabel.setFont(getStandardBoldFont());
		thePanel.add(modelLabel);
		
			String xVarName[] = new String[2];
			xVarName[0] = data.getVariable("x").name;
			xVarName[1] = data.getVariable("z").name;
			String yVarName = data.getVariable("y").name;
			
		thePanel.add(new MultiLinearEqnView(data, this, "model", yVarName, xVarName, maxCoeff, maxCoeff));
		
		return thePanel;
	}
	
	protected void addDescription(DataSet data, XPanel thePanel) {
	}
	
	protected XPanel getSamplePanel() {
		XPanel samplePanel = new XPanel();
		samplePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 8));
			sampleButton = new RepeatingButton(translate("Take sample"), this);
		samplePanel.add(sampleButton);
		
		return samplePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(2, 8, 2, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new ProportionLayout(0.5, 0));
			
			innerPanel.add(ProportionLayout.LEFT, pValueCalcPanel(summaryData, 1, kLeftPanelColor));
			innerPanel.add(ProportionLayout.RIGHT, pValueCalcPanel(summaryData, 2, kRightPanelColor));
		
		thePanel.add("Center", innerPanel);
		return thePanel;
	}
	
	private XPanel pValueCalcPanel(SummaryDataSet summaryData, int coeffIndex, Color panelColor) {
		XPanel thePanel = new InsetPanel(3, 2);
		thePanel.setLayout(new BorderLayout(0, 5));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
			
				String coeffKey = "b" + coeffIndex;
				String coeffSEKey = coeffKey + "_se";
				String tValueKey = coeffKey + "_t";
				String pValueKey = coeffKey + "_pVal";
			
				OneValueView coeffView = new OneValueView(summaryData, coeffKey, this, maxCoeff[coeffIndex]);
				coeffView.setHighlightSelection(false);
			topPanel.add(coeffView);
			
				OneValueView seView = new OneValueView(summaryData, coeffSEKey, this, maxCoeff[coeffIndex]);
				seView.setHighlightSelection(false);
				seView.setForeground(TFormulaPanel.kDenomColor);
			topPanel.add(seView);
				
				FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
				TFormulaPanel tFormula = new TFormulaPanel(summaryData, coeffKey, coeffSEKey,
														tValueKey, maxCoeff[coeffIndex], maxCoeff[coeffIndex], maxT,
														"multiRegn/tFormula.gif", 22, 17, 69, stdContext);
			topPanel.add(tFormula);
			
		thePanel.add("North", topPanel);
		
		thePanel.add("Center", getTDistnPanel(summaryData, tValueKey, panelColor, coeffIndex));
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				OneValueView pValueView = new OneValueView(summaryData, pValueKey, this, kMaxPValue);
				pValueView.setForeground(kDarkBlue);
			bottomPanel.add(pValueView);
		thePanel.add("South", bottomPanel);
		
		thePanel.lockBackground(panelColor);
		return thePanel;
	}
	
	private XPanel getTDistnPanel(SummaryDataSet summaryData, String tValueKey, Color panelColor,
																																				int coeffIndex) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FixedSizeLayout(999, kTDistnHeight));
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new AxisLayout());
			
				HorizAxis tAxis = new HorizAxis(this);
				tAxis.readNumLabels(kTAxisInfo);
				tAxis.lockBackground(panelColor);
			innerPanel.add("Bottom", tAxis);
				
				AccurateTailAreaView theTView = new AccurateTailAreaView(summaryData, this, tAxis, "tDistn");
				theTView.setTailType(AccurateTailAreaView.TWO_TAILED);
				theTView.lockBackground(Color.white);
				theTView.setDistnColors(kTFillColor, kTHighlightColor);
				theTView.setActiveNumVariable(tValueKey);
				theTView.setValueLabel(new LabelValue("t"));
				theTView.setDistnLabel(new LabelValue("t(" + errorDF + " df)"), kDistnLabelColor);
				tView[coeffIndex - 1] = theTView;
				
			innerPanel.add("Center", theTView);
		
		thePanel.add(innerPanel);
		return thePanel;
	}
}