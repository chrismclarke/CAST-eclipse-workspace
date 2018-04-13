package matrixProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import models.*;

import matrix.*;


public class FactorFittedValApplet extends GenericFittedValApplet {
	static final private String FIT_DECIMALS_PARAM = "fitDecimals";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	
	private XButton lsButton;
	private XChoice baselineChoice;
	private int currentBaseline;
	
	protected ModelTerm xTerms[];
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL));
		
		xTerms = findXTerms(data, "b");
		ModelTerm leftFitTerm = findGenericFittedTerm(data);
		ModelTerm rightFitTerm = findActualFittedTerm(data);
		
		add(ProportionLayout.BOTTOM, createFittedValEquation(data, leftFitTerm, xTerms, rightFitTerm));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(10, 0));
			
			topPanel.add("Center", displayPanel(data, xTerms));
				
			topPanel.add("East", controlPanel(data, xTerms));
		
		add(ProportionLayout.TOP, topPanel);
	}
	
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		addLsModel(data);
		
		int fitDecimals = Integer.parseInt(getParameter(FIT_DECIMALS_PARAM));
		FittedValueVariable fit = new FittedValueVariable("Fit", data, "x1", "ls", fitDecimals);
		data.addVariable("fit", fit);
		
		return data;
	}

//--------------------------------------------------------------------------------
	
	protected ModelTerm[] findXTerms(DataSet data, String paramSymbol) {
		return findOneFactorXTerms(data, paramSymbol);
	}
	
	protected ModelTerm findActualFittedTerm(DataSet data) {
		return new VariateTerm(data, "fit", false, null, 0, "fit", Color.black);
	}

//--------------------------------------------------------------------------------
	
	
	protected CoreMatrixView createBetaMatrixView(DataSet data, ModelTerm[] xTerms) {
		CoreMatrixView  matrix = new LsFactorCoeffMatrixView(data, this, "ls");
		
		matrix.setMatrixColumns(xTerms);
		matrix.setFont(getMatrixFont());
		matrix.setRowGap(getRowGap());
		
		return matrix;
	}
	
	protected MatrixFormulaValue createLeftFitView(DataSet data, ModelTerm leftFitTerm) {
		MatrixFormulaValue leftFit = super.createLeftFitView(data, leftFitTerm);
		leftFit.setGroupKey("x1");
		return leftFit;
	}
	
	protected XPanel displayPanel(DataSet data, ModelTerm[] xTerms) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			XPanel scatterPanel = new XPanel();
			scatterPanel.setLayout(new AxisLayout());
				
				NumVariable yVar = (NumVariable)data.getVariable("y");
				CatVariable xVar = (CatVariable)data.getVariable("x1");
				
				HorizAxis xAxis = new HorizAxis(this);
				xAxis.setCatLabels(xVar);
				xAxis.setAxisName(xVar.name);
				
				scatterPanel.add("Bottom", xAxis);
				
				VertAxis yAxis = new VertAxis(this);
				String labelInfo = getParameter(Y_AXIS_INFO_PARAM);
				yAxis.readNumLabels(labelInfo);
				yAxis.setAxisName(yVar.name);
				
				scatterPanel.add("Left", yAxis);
				
				DataView theView = new DragOneFactorView(data, this, xAxis, yAxis, "x1", "y", "ls", xTerms);
				theView.lockBackground(Color.white);
				theView.setFont(getBigFont());
				
				scatterPanel.add("Center", theView);
				
		thePanel.add("Center", scatterPanel);
			
			XLabel yVariateName = new XLabel(yVar.name, XLabel.LEFT, this);
			yVariateName.setFont(yAxis.getFont());
			
		thePanel.add("North", yVariateName);
		
		return thePanel;
	}
	
	
	protected XPanel controlPanel(DataSet data, ModelTerm[] xTerms) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 30));
			
			lsButton = new XButton(translate("Least squares"), this);
		thePanel.add(lsButton);
		
			if (xTerms.length == 2) {		//	has baseline category
				XPanel choicePanel = new XPanel();
				choicePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 2));
				
					XLabel baselineLabel = new XLabel(translate("Baseline") + ":", XLabel.LEFT, this);
					baselineLabel.setFont(getStandardBoldFont());
				choicePanel.add(baselineLabel);
				
					baselineChoice = new XChoice(this);
					CatVariable xVar = (CatVariable)data.getVariable("x1");
					for (int i=0 ; i<xVar.noOfCategories() ; i++)
						baselineChoice.addItem(xVar.getLabel(i).toString());
					currentBaseline = ((FactorTerm)xTerms[1]).getBaselineLevel();
					baselineChoice.select(currentBaseline);
				
				choicePanel.add(baselineChoice);
				
				thePanel.add(choicePanel);
			}
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == lsButton) {
			MultipleRegnModel lsFit = (MultipleRegnModel)data.getVariable("ls");
			lsFit.updateLSParams("y");
			data.variableChanged("ls");
			return true;
		}
		else if (target == baselineChoice) {
			int newChoice = baselineChoice.getSelectedIndex();
			if (newChoice != currentBaseline) {
				currentBaseline = newChoice;
				((FactorTerm)xTerms[1]).setBaselineLevel(currentBaseline);
				((ConstantTerm)xTerms[0]).setStartSubscript(currentBaseline + 1);
				data.variableChanged("ls");
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