package glmAnovaProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import coreVariables.*;
import glmAnova.*;


public class ResidSeqChangeApplet extends XApplet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String X_VARIABLES_PARAM = "xVariables";
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String FIT_DECIMALS_PARAM = "fitDecimals";
	
	static final private Color kDarkRed = new Color(0x990000);
	
	static final private String kXNameEnd = "VarName";
	static final private String kXValuesEnd = "Values";
	
	private String kFittedValHeading, kResidualsHeading;
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	private String xKeys[];
	private String fitKeys[];
	private String residKeys[];
	
	private ResidSequenceView residFitView;
	
	@SuppressWarnings("unused")
	private NumValue maxSsq, maxMsq, maxF;
	@SuppressWarnings("unused")
	private int maxDF;
	
	private XChoice fitResidChoice;
	private int currentFitOrResid;
	private XLabel headingLabel;
	
	public void setupApplet() {
		kFittedValHeading = translate("Fitted values from various models");
		kResidualsHeading = translate("Residuals from various models");
		
		data = readData();
		summaryData = createSummaryData(data);
		summaryData.setSingleSummaryFromData();
		
		setLayout(new BorderLayout(10, 0));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 10));
			mainPanel.add(topPanel());
			mainPanel.add(residFitPanel(data));
			
		add("Center", mainPanel);
		add("South", bottomPanel());
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		NumVariable yVar = new NumVariable(getParameter(Y_VAR_NAME_PARAM));
		yVar.readValues(getParameter(Y_VALUES_PARAM));
		data.addVariable("y", yVar);
		
		int fitDecimals = Integer.parseInt(getParameter(FIT_DECIMALS_PARAM));
		NumVariable yCopyVar = new NumVariable("y");
		yCopyVar.readValues(getParameter(Y_VALUES_PARAM));
		yCopyVar.setDecimals(fitDecimals);						//	only affects display
		data.addVariable("fitY", yCopyVar);
		
		StringTokenizer st = new StringTokenizer(getParameter(X_VARIABLES_PARAM));
		int nx = st.countTokens();
		xKeys = new String[nx];
		for (int i=0 ; i<nx ; i++) {
			xKeys[i] = st.nextToken();
			data.addNumVariable(xKeys[i], getParameter(xKeys[i] + kXNameEnd),
																									getParameter(xKeys[i] + kXValuesEnd));
		}
		
		int paramDecimals[] = new int[nx + 1];		//	params not printed, so all zero is OK
		double constraints[] = new double[nx + 1];
		String xSequence = "";
		
		fitKeys = new String[nx + 2];
		residKeys = new String[nx + 2];
		
		for (int i=0 ; i<xKeys.length+2 ; i++) {
			String fitKey, residKey;
			if (i == xKeys.length + 1) {
				fitKey = "fitY";
				residKey = "zero";
				data.addNumVariable(residKey, "", yVar.noOfValues() + "@0");
			}
			else {
				String fitName, residName;
				if (i == 0) {
					fitName = "y\u0302(0) = y\u0305";		//	yHat(0) = yBar
					residName = "e(0) = y - y\u0305";		//	e(0) = y - yBar
				}
				else {
					xSequence += xKeys[i - 1];
					fitName = "y\u0302(" + xSequence + ")";
					residName = "e(" + xSequence + ")";
				}
				fitKey = "fit" + xSequence;
				residKey = "resid" + xSequence;
				String lsKey = "ls" + xSequence;
				
				constraints[i] = Double.NaN;
				MultipleRegnModel lsModel = new MultipleRegnModel(lsKey, data, xKeys);
				lsModel.setLSParams("y", constraints, paramDecimals, 9);
				data.addVariable(lsKey, lsModel);
				
				FittedValueVariable fitVar = new FittedValueVariable(fitName, data, xKeys,
																																	lsKey, fitDecimals);
				data.addVariable(fitKey, fitVar);
				
				SumDiffVariable residVar = new SumDiffVariable(residName, data, "fitY",
																													fitKey, SumDiffVariable.DIFF);
				data.addVariable(residKey, residVar); 
			}
			
			fitKeys[i] = fitKey;
			residKeys[i] = residKey;
		}
		
		return data;
	}
	
	private SummaryDataSet createSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SSQ_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxDF = Integer.parseInt(st.nextToken());
		maxMsq = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
		
		return summaryData;
	}
	
	private XPanel residFitPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			residFitView = new ResidSequenceView(data, this, fitKeys, residKeys, xKeys, maxSsq);
			residFitView.setFont(getBigFont());
//			residFitView.setSelectedColumn(2);
		thePanel.add("Center", residFitView);
		
		return thePanel;
	}
	
	private XPanel bottomPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			fitResidChoice = new XChoice(this);
			fitResidChoice.addItem(translate("Fitted values"));
			fitResidChoice.addItem(translate("Residuals"));
		thePanel.add(fitResidChoice);
		
		return thePanel;
	}
	
	private XPanel topPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			headingLabel = new XLabel(kFittedValHeading, XLabel.CENTER, this);
			headingLabel.setFont(getBigBoldFont());
			headingLabel.setForeground(kDarkRed);
		thePanel.add("Center", headingLabel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == fitResidChoice) {
			int newChoice = fitResidChoice.getSelectedIndex();
			if (newChoice != currentFitOrResid) {
				currentFitOrResid = newChoice;
				residFitView.setValueDisplayType(newChoice == 0 ? ResidSequenceView.FITTED_VALUES
																												: ResidSequenceView.RESIDUALS);
				residFitView.repaint();
				
				headingLabel.setText(newChoice == 0 ? kFittedValHeading : kResidualsHeading);
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