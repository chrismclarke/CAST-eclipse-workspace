package corrProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import coreGraphics.*;
import models.*;

//import scatterProg.*;
import regn.*;
import regnView.*;
import corr.*;


public class MultiXApplet extends ScatterApplet {
	static final private String PARAM_DECIMALS_PARAM = "paramDecimals";
	static final private String PARAM_MAX_PARAM = "biggestParams";
	static final private String EXTRA_X_PIXELS_PARAM = "extraXPixels";
	static final private String ALLOW_LS_DISPLAY_PARAM = "allowLSDisplay";
	
	static final private String kXAxisStart = "horiz";
	static final private String kXAxisEnd = "Axis";
	static final private String kXStart = "x";
	static final private String kXNameEnd = "VarName";
	static final private String kXValuesEnd = "Values";
	
	private int interceptDecimals, sdDecimals;
	private int slopeDecimals[];
	private NumValue maxResidSD;
	
	private XChoice xScaleChoice;
	private MultiHorizAxis localHorizAxis;
	private LSResidView theView;
	private CorrelationView correlationView;
	
	private XPanel optionDisplayPanel;
	private CardLayout displayLayout;
	private LinearEquationView lsEquation;
	private XCheckbox displayTypeCheck;
	private ModelSDView residSD;
	
	protected DataSet readData() {
		DataSet data = readCoreData();
		
		xScaleChoice = new XChoice(this);
		xScaleChoice.addItem(getParameter(X_VAR_NAME_PARAM));
		
		int noOfXVars = 1;
		while (true) {
			String varName = getParameter(kXStart + noOfXVars + kXNameEnd);
			String values = getParameter(kXStart + noOfXVars + kXValuesEnd);
			String axisInfo = getParameter(kXAxisStart + noOfXVars + kXAxisEnd);
			if (varName == null || values == null || axisInfo == null)
				break;
			data.addNumVariable(kXStart + noOfXVars, varName, values);
			xScaleChoice.addItem(varName);
			noOfXVars ++;
		}
		
		xScaleChoice.select(0);
		
		localHorizAxis = new MultiHorizAxis(this, noOfXVars);
		localHorizAxis.readNumLabels(getParameter(X_AXIS_INFO_PARAM));
		for (int i=1 ; i<noOfXVars ; i++) {
			String axisInfo = getParameter(kXAxisStart + i + kXAxisEnd);
			localHorizAxis.readExtraNumLabels(axisInfo);
		}
		localHorizAxis.setChangeMinMax(true);
		
		slopeDecimals = new int[noOfXVars];
		StringTokenizer st = new StringTokenizer(getParameter(PARAM_DECIMALS_PARAM));
		interceptDecimals = Integer.parseInt(st.nextToken());
		sdDecimals = Integer.parseInt(st.nextToken());
		for (int i=0 ; i<noOfXVars ; i++)
			slopeDecimals[i] = Integer.parseInt(st.nextToken());
		
		LinearModel lsModel = new LinearModel("LS line", data, "x");
		lsModel.setLSParams("y", interceptDecimals, slopeDecimals[0], sdDecimals);
		data.addVariable("model", lsModel);
		
		maxResidSD = new NumValue(getMaxResidSD(data), sdDecimals);
		
		return data;
	}
	
	private double getMaxResidSD(DataSet data) {
		NumVariable yVar = (NumVariable)data.getVariable("y");
		
		ValueEnumeration e = yVar.values();
		double sx = 0.0;
		double sxx = 0.0;
		int nVals = 0;
		while (e.hasMoreValues()) {
			double val = e.nextDouble();
			sx += val;
			sxx += val * val;
			nVals++;
		}
		return Math.sqrt((sxx - sx * sx / nVals) / (nVals - 2));
	}
	
	protected HorizAxis createHorizAxis(DataSet data) {
		return localHorizAxis;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis,
																						VertAxis theVertAxis) {
		theView = new LSResidView(data, this, theHorizAxis, theVertAxis, "x", "y", "model");
		return theView;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new BorderLayout(0, 10));
		
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
			
				XLabel choiceLabel = new XLabel(translate("Explanatory variable"), XLabel.RIGHT, this);
				choiceLabel.setFont(getStandardBoldFont());
			
			choicePanel.add(choiceLabel);
			choicePanel.add(xScaleChoice);
		
		controlPanel.add("North", choicePanel);
		controlPanel.add("Center", infoPanel(data));
		
		return controlPanel;
	}
	
	private XPanel infoPanel(DataSet data) {
		String allowLSString = getParameter(ALLOW_LS_DISPLAY_PARAM);
		if (allowLSString != null && allowLSString.equals("false"))
			return correlationPanel(data);
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel checkPanel = new XPanel();
			checkPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
			displayTypeCheck = new XCheckbox(translate("Least squares line"), this);
			checkPanel.add(displayTypeCheck);
		
		thePanel.add("East", checkPanel);
		
			optionDisplayPanel = new XPanel();
			displayLayout = new CardLayout();
			optionDisplayPanel.setLayout(displayLayout);
			optionDisplayPanel.add("Corr", correlationPanel(data));
			optionDisplayPanel.add("LS", lsPanel(data));
			displayLayout.show(optionDisplayPanel, "Corr");
		
		thePanel.add("Center", optionDisplayPanel);
		return thePanel;
	}
	
	private XPanel correlationPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			correlationView = new CorrelationView(data, "x", "y", CorrelationView.NO_FORMULA, this);
		
		thePanel.add(correlationView);
		return thePanel;
	}
	
	private XPanel lsPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																		VerticalLayout.VERT_CENTER, 5));
			
			StringTokenizer st = new StringTokenizer(getParameter(PARAM_MAX_PARAM));
			NumValue maxIntercept = new NumValue(st.nextToken());
			NumValue maxSlope = new NumValue(st.nextToken());
			String yName = data.getVariable("y").name;
			String xName = data.getVariable("x").name;
			
			lsEquation = new LinearEquationView(data, this, "model", yName, xName, maxIntercept, maxIntercept,
																																				maxSlope, maxSlope);
			
			lsEquation.setExtraPixels(Integer.parseInt(getParameter(EXTRA_X_PIXELS_PARAM)));
			
		thePanel.add(lsEquation);
		
			residSD = new ModelSDView(data, this, "model", maxResidSD, translate("sd of residuals") + " =");
			
		thePanel.add(residSD);
		
		return thePanel;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = super.topPanel(data);
		if (yVariateName != null)
			yVariateName.setFont(getStandardBoldFont());
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == xScaleChoice) {
			int newScale = xScaleChoice.getSelectedIndex();
			if (localHorizAxis.setAlternateLabels(newScale)) {
				String newXKey = kXStart;
				if (newScale > 0)
					newXKey += newScale;
				NumVariable newXVar = (NumVariable)data.getVariable(newXKey);
//				String newXName = newXVar.name;
				localHorizAxis.repaint();
				theView.changeVariables(null, newXKey);
				correlationView.changeVariables(null, newXKey);
				
				LinearModel lsModel = (LinearModel)data.getVariable("model");
				lsModel.setXKey(newXKey);
				lsModel.setLSParams("y", interceptDecimals, slopeDecimals[newScale], sdDecimals);
				if (lsEquation != null) {
					lsEquation.setExplanName(newXVar.name);
					lsEquation.repaint();
					residSD.redrawValue();
				}
			}
			return true;
		}
		else if (target == displayTypeCheck) {
			boolean lsNotCorr = displayTypeCheck.getState();
			displayLayout.show(optionDisplayPanel, lsNotCorr ? "LS" : "Corr");
			theView.setVisibleResids(lsNotCorr);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}