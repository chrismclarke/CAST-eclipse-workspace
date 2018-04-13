package exper2Prog;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import models.*;

import exper2.*;


public class DragFactorMeansApplet extends XApplet {
	static final protected String Y_VAR_NAME_PARAM = "yVarName";
	static final protected String Y_VALUES_PARAM = "yValues";
	static final protected String Y_AXIS_INFO_PARAM = "yAxis";
	
	static final private String X_ALTERNATIVES_PARAM = "xAlternatives";
	static final protected String X_VAR_NAME_PARAM = "xVarName";
	static final protected String X_VALUES_PARAM = "xValues";
	static final protected String X_LABELS_PARAM = "xLabels";
	
	static final private String MODEL_TYPE_PARAM = "modelType";
	static final private String INITIALISATION_PARAM = "initialise";
	static final private String SHOW_RSS_PARAM = "showRss";
	static final protected String MAX_RSS_PARAM = "maxRss";
	static final private String SHOW_RESID_PARAM = "showResiduals";
	
	static final private String kAllModelKeys[] = {"mean", "linear", "quadratic", "factor"};
	
	protected DataSet data;
	
	private String modelKeys[];
	private XChoice modelChoice;
	private int currentModelChoice = 0;
	
	private MultiHorizAxis multiXAxis;
	
	private CoreOneFactorView theView;
	protected FactorRssValueView rssValueView;
	
	private XChoice axisChoice;
	private int currentAxisChoice = 0;
	
	private XButton lsButton;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(0, 0));
		
		add("Center", displayPanel(data));
		add("South", rssPanel(data));
		add("East", rightPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
			NumVariable yVar = new NumVariable(getParameter(Y_VAR_NAME_PARAM));
			yVar.readValues(getParameter(Y_VALUES_PARAM));
		data.addVariable("y", yVar);
		
			CatVariable xCatVar = new CatVariable(getParameter(X_VAR_NAME_PARAM));
			xCatVar.readLabels(getParameter(X_LABELS_PARAM));
			xCatVar.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("xCat", xCatVar);
		
		data.addNumVariable("xNum", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM));
		
		String initString = getParameter(INITIALISATION_PARAM);
		boolean lsInit = initString.equals("ls");
		double initValue = lsInit ? 0.0 : Double.parseDouble(initString);
		
			MeanOnlyModel meanModel = new MeanOnlyModel(translate("Mean only"), data);
			if (lsInit)
				meanModel.updateLSParams("y");
			else
				meanModel.setMean(new NumValue(initValue));
		data.addVariable(kAllModelKeys[0], meanModel);
		
			LinearModel linModel = new LinearModel(translate("Linear"), data, "xNum");
			if (lsInit)
				linModel.updateLSParams("y");
			else {
				linModel.setIntercept(new NumValue(initValue));
				linModel.setSlope(new NumValue(0.0));
			}
		data.addVariable(kAllModelKeys[1], linModel);
		
			QuadraticModel quadModel = new QuadraticModel(translate("Quadratic"), data, "xNum");
			if (lsInit)
				quadModel.updateLSParams("y");
			else {
				quadModel.setIntercept(new NumValue(initValue));
				quadModel.setSlope(new NumValue(0.0));
				quadModel.setCurvature(new NumValue(0.0));
			}
		data.addVariable(kAllModelKeys[2], quadModel);
		
			GroupsModelVariable factorModel = new GroupsModelVariable(xCatVar.noOfCategories() + "-" + translate("means"), data, "xCat");
			if (lsInit)
				factorModel.updateLSParams("y");
			else
				for (int i=0 ; i<xCatVar.noOfCategories() ; i++)
					factorModel.setMean(new NumValue(initValue), i);
		data.addVariable(kAllModelKeys[3], factorModel);
				
			StringTokenizer st = new StringTokenizer(getParameter(MODEL_TYPE_PARAM));
			int nModels = st.countTokens();
			if (nModels > 1)
				modelChoice = new XChoice(this);
			modelKeys = new String[nModels];
			currentModelChoice = 0;
			int index = 0;
			while (st.hasMoreTokens()) {
				modelKeys[index] = st.nextToken();
				if (modelChoice != null)
					modelChoice.addItem(data.getVariable(modelKeys[index]).name);
				index ++;
			}
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			XPanel scatterPanel = new XPanel();
			scatterPanel.setLayout(new AxisLayout());
				
					NumVariable yVar = (NumVariable)data.getVariable("y");
					CatVariable xVar = (CatVariable)data.getVariable("xCat");
					
					String xAltString = getParameter(X_ALTERNATIVES_PARAM);
					int nAlternatives = (xAltString == null) ? 1 : Integer.parseInt(xAltString);
					HorizAxis xAxis;
					if (nAlternatives == 1) {
						xAxis = new HorizAxis(this);
						int nXCats = xVar.noOfCategories();
						xAxis.readNumLabels("-0.5 " + (nXCats - 0.5) + " " + nXCats + " 1");
																			//	so xAxis can be indexed numerially or categorically
						xAxis.setCatLabels(xVar);
						xAxis.setAxisName(xVar.name);
					}
					else {
						xAxis = multiXAxis = new MultiHorizAxis(this, nAlternatives);
						int nXCats = xVar.noOfCategories();
						multiXAxis.readNumLabels("-0.5 " + (nXCats - 0.5) + " " + nXCats + " 1");
																			//	so xAxis can be indexed numerially or categorically
						multiXAxis.setCatLabels(xVar);
						CatVariable tempX = new CatVariable("");
						for (int i=2 ; i<=nAlternatives ; i++) {
							tempX.readLabels(getParameter(X_LABELS_PARAM + i));
							multiXAxis.readExtraCatLabels(tempX);
						}
					}
				
				scatterPanel.add("Bottom", xAxis);
				
					VertAxis yAxis = new VertAxis(this);
					String labelInfo = getParameter(Y_AXIS_INFO_PARAM);
					yAxis.readNumLabels(labelInfo);
				
				scatterPanel.add("Left", yAxis);
					
					theView = getMeansView(data, xAxis, yAxis);
					theView.lockBackground(Color.white);
					String showResidParam = getParameter(SHOW_RESID_PARAM);
					if (showResidParam != null && showResidParam.equals("false"))
						theView.setShowResiduals(false);
				
				scatterPanel.add("Center", theView);
				
		thePanel.add("Center", scatterPanel);
			
			XLabel yVariateName = new XLabel(yVar.name, XLabel.LEFT, this);
			yVariateName.setFont(yAxis.getFont());
			
		thePanel.add("North", yVariateName);
		
		if (nAlternatives > 1) {
			XPanel axisChoicePanel = new XPanel();
			axisChoicePanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
			
				axisChoice = new XChoice(this);
				axisChoice.addItem(getParameter(X_VAR_NAME_PARAM));
				for (int i=2 ; i<=nAlternatives ; i++)
					axisChoice.addItem(getParameter(X_VAR_NAME_PARAM + i));
			axisChoicePanel.add(axisChoice);
			
			thePanel.add("South", axisChoicePanel);
		}
		
		return thePanel;
	}
	
	protected CoreOneFactorView getMeansView(DataSet data, HorizAxis xAxis, VertAxis yAxis) {
		return new DragFactorMeansView(data, this, xAxis, yAxis, "xNum", "xCat", "y", modelKeys[currentModelChoice]);
	}
	
	protected XPanel rssPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
		
		String showRssString = getParameter(SHOW_RSS_PARAM);
		if (showRssString != null && showRssString.equals("true")) {
			rssValueView = new FactorRssValueView(data, this,
								"xNum", "xCat", "y", modelKeys[currentModelChoice], new NumValue(getParameter(MAX_RSS_PARAM)));
			thePanel.add(rssValueView);
			
			lsButton = new XButton(translate("Least squares"), this);
			thePanel.add(lsButton);
		}
		
		return thePanel;
	}
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
		if (modelChoice != null) {
			XLabel modelLabel = new XLabel(translate("Model"), XLabel.LEFT, this);
			modelLabel.setFont(getStandardBoldFont());
			thePanel.add(modelLabel);
			thePanel.add(modelChoice);
		}
		
		return thePanel;
	}
	
	private void setLeastSquares(String modelKey) {
		CoreModelVariable model = (CoreModelVariable)data.getVariable(modelKey);
		model.updateLSParams("y");
	}
	
	private int keyToIndex(String modelKey){
		return modelKey.equals("mean") ? 0
						: modelKey.equals("linear") ? 1
						: modelKey.equals("quadratic") ? 2
						: 3;							//	factor
	}
	
	private void updateModel(int newChoice) {
		String currentModel = modelKeys[currentModelChoice];
		String newModel = modelKeys[newChoice];
		int currentIndex = keyToIndex(currentModel);
		int newIndex = keyToIndex(newModel);
		
		
		if (newIndex < currentIndex)
			setLeastSquares(newModel);
		else if (newIndex == 3) {			//		model -> factor
			CoreModelVariable lm = (CoreModelVariable)data.getVariable(modelKeys[currentModelChoice]);
			GroupsModelVariable fm = (GroupsModelVariable)data.getVariable(modelKeys[newChoice]);
			CatVariable xCatVar = (CatVariable)data.getVariable("xCat");
			for (int i=0 ; i<xCatVar.noOfCategories() ; i++)
				fm.setMean(lm.evaluateMean(new NumValue(i)), i);
		}
		else if (newIndex == 2) {		//		mean or linear -> quadratic
			QuadraticModel qm = (QuadraticModel)data.getVariable(newModel);
			qm.setCurvature(0.0);
			if (currentIndex == 1) {		//	linear
				LinearModel lm = (LinearModel)data.getVariable(currentModel);
				qm.setIntercept(lm.getIntercept().toDouble());
				qm.setSlope(lm.getSlope().toDouble());
			}
			else {
				MeanOnlyModel mm = (MeanOnlyModel)data.getVariable(currentModel);
				qm.setIntercept(mm.getMean().toDouble());
				qm.setSlope(0.0);
			}
		}
		else {		//		mean -> linear
			MeanOnlyModel mm = (MeanOnlyModel)data.getVariable(currentModel);
			LinearModel lm = (LinearModel)data.getVariable(newModel);
			lm.setIntercept(mm.getMean().toDouble());
			lm.setSlope(0.0);
		}
	}
	
	private boolean localAction(Object target) {
		if (target == axisChoice) {
			int newChoice = axisChoice.getSelectedIndex();
			if (newChoice != currentAxisChoice) {
				currentAxisChoice = newChoice;
				multiXAxis.setAlternateLabels(newChoice);
				multiXAxis.repaint();
			}
			return true;
		}
		else if (target == modelChoice) {
			int newChoice = modelChoice.getSelectedIndex();
			if (newChoice != currentModelChoice) {
				updateModel(newChoice);
				currentModelChoice = newChoice;
				theView.setModelKey(modelKeys[currentModelChoice]);
				if (rssValueView != null)
					rssValueView.setModelKey(modelKeys[currentModelChoice]);
				data.variableChanged(modelKeys[currentModelChoice]);
			}
			return true;
		}
		else if (target == lsButton) {
			setLeastSquares(modelKeys[currentModelChoice]);
			data.variableChanged(modelKeys[currentModelChoice]);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}