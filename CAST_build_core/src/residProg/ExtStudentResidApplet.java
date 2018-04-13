package residProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;
import formula.*;


import resid.*;


public class ExtStudentResidApplet extends DeletedResidApplet {
	static final protected String SD_LEFT_UP_PARAM = "sdLeftUp";
	static final protected String MAX_VALUES_PARAM = "maxValues";
	
	static final public Color kFormulaBackgroundColor = new Color(0xFAE8D6);
	
	private NumValue maxResid, maxAntileverage, maxS, maxStdRes;
	
	private DeletedResidSView dataView;
	
	private XChoice residTypeChoice;
	private int currentResidType = 0;
	
	private StdResidFormulaPanel resFormula;
	
	protected SimpleRegnDataSet readData() {
		SimpleRegnDataSet data = super.readData();
			
			StringTokenizer st = new StringTokenizer(getParameter(MAX_VALUES_PARAM));
			maxResid = new NumValue(st.nextToken());
			maxAntileverage = new NumValue(st.nextToken());
			maxS = new NumValue(st.nextToken());
			maxStdRes = new NumValue(st.nextToken());
		
		DeletedResidVariable delResid = (DeletedResidVariable)data.getVariable("delResid");
		delResid.setDecimals(maxResid.decimals);
		
		data.addVariable("stdResid", new StdResidValueVariable("Std resid", data, "x", "y",
																														"ls", maxStdRes.decimals));
		
		data.addVariable("extStudentResid", new ExtStudentResidVariable("Deleted resid", data,
																					"x", "y", "ls", "deletedLS", maxStdRes.decimals));
		
		data.addVariable("deletedS", new DeletedSDVariable("Deleted s", data, "y", "deletedLS"));
			DeletedSDVariable ordinaryS = new DeletedSDVariable("Deleted s", data, "y", "deletedLS");
			ordinaryS.setDontDelete(true);			//	messy way to get a constant!
		data.addVariable("ordinaryS", ordinaryS);
		data.addVariable("antiLeverage", new LeverageValueVariable("Anti-leverage", data, "x",
												"ls", LeverageValueVariable.ANTI_LEVERAGE, maxAntileverage.decimals));
		return data;
	}
	
	protected XPanel topPanel(SimpleRegnDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10));
		
		thePanel.add(ProportionLayout.LEFT, new XLabel(data.getYVarName(), XLabel.LEFT, this));
			
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
				residTypeChoice = new XChoice(this);
				residTypeChoice.addItem(translate("Ordinary standardised residual"));
				residTypeChoice.addItem(translate("Externally studentised residual"));
			
			choicePanel.add(residTypeChoice);
		thePanel.add(ProportionLayout.RIGHT, choicePanel);
		
		return thePanel;
	}
	
	protected DeletedResidView getDataView(SimpleRegnDataSet data, HorizAxis dataXAxis,
																																		VertAxis dataYAxis) {
		StringTokenizer st = new StringTokenizer(getParameter(SD_LEFT_UP_PARAM));
		int sdLeft = Integer.parseInt(st.nextToken());
		int sdUp = Integer.parseInt(st.nextToken());
		dataView = new DeletedResidSView(data, this, dataXAxis, dataYAxis, "x", "y", "ls", "deletedLS", sdLeft, sdUp);
		return dataView;
	}
	
	protected XPanel bottomPanel(SimpleRegnDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel innerPanel = new InsetPanel(10, 3, 10, 4);
			innerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
				FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
				resFormula = new StdResidFormulaPanel(data, "delResid", "antiLeverage", "deletedS",
									"extStudentResid", maxResid, maxAntileverage, maxS, maxStdRes,
									"multiRegn/studentResFormula.gif", "multiRegn/extStudentResFormula.gif",
									32, 18, 147, stdContext);
				changeResidType(false);
			
			innerPanel.add(resFormula);
		
			innerPanel.lockBackground(kFormulaBackgroundColor);
		thePanel.add(innerPanel);
		return thePanel;
	}
	
	private void changeResidType(boolean useDeletedS) {
		String residKey = useDeletedS ? "extStudentResid" : "stdResid";
		resFormula.changeResidType(residKey, useDeletedS ? "deletedS" : "ordinaryS", useDeletedS);
		residView.changeVariables(residKey, "x");
		dataView.setShowDeletedSD(useDeletedS);
		dataView.repaint();
	}

	
	private boolean localAction(Object target) {
		if (target == residTypeChoice) {
			int newChoice = residTypeChoice.getSelectedIndex();
			if (newChoice != currentResidType) {
				currentResidType = newChoice;
				changeResidType(newChoice == 1);
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

