package exper2Prog;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import random.*;
import coreVariables.*;
import models.*;

import exper2.*;


public class AdjustRssApplet extends XApplet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String X_LABELS_PARAM = "xLabels";
	
	static final private String MEAN_VALUES_PARAM = "meanValues";
	static final private String MEAN_RSS_PARAM = "meanRss";
	static final private String RANDOM_SEED_PARAM = "randomSeed";
	
	protected DataSet data;
	
	private ParameterSlider meanRssSlider;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 5));
	
		add("Center", displayPanel(data));
		add("South", controlPanel());
		
		setMeanRss();
	}
	
	private void standardiseErrors(double[] errors, CatVariable xCatVar) {
		int nGroups = xCatVar.noOfCategories();
		double errorMean[] = new double[nGroups];
		int n[] = new int[nGroups];
		for (int i=0 ; i<errors.length ; i++) {
			int cat = xCatVar.getItemCategory(i);
			errorMean[cat] += errors[i];
			n[cat] ++;
		}
		for (int i=0 ; i<nGroups ; i++)
			errorMean[i] /= n[i];
		
		for (int i=0 ; i<errors.length ; i++) {
			int cat = xCatVar.getItemCategory(i);
			errors[i] -= errorMean[cat];
		}
		
		double rss = 0.0;
		for (int i=0 ; i<errors.length ; i++)
			rss += errors[i] * errors[i];
		double s = Math.sqrt(rss / (errors.length - nGroups));
		
		for (int i=0 ; i<errors.length ; i++)
			errors[i] /= s;
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
			CatVariable xCatVar = new CatVariable(getParameter(X_VAR_NAME_PARAM));
			xCatVar.readLabels(getParameter(X_LABELS_PARAM));
			xCatVar.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("x", xCatVar);
		
			int n = xCatVar.noOfValues();
//			int nGroups = xCatVar.noOfCategories();
			
		data.addNumVariable("groupMean", "GroupMean", getParameter(MEAN_VALUES_PARAM));
			
			NumVariable errorVar = new NumVariable(translate("Error"));
			RandomNormal generator = new RandomNormal(n, 0.0, 1.0, 3.0);
			generator.setSeed(Long.parseLong(getParameter(RANDOM_SEED_PARAM)));
			double errors[] = generator.generate();
			standardiseErrors(errors, xCatVar);
			errorVar.setValues(errors);
		data.addVariable("error", errorVar);
		
			ScaledVariable scaledError = new ScaledVariable("ScaledError", errorVar, "error",
																																				0.0, 1.0, 9);
		data.addVariable("scaledError", scaledError);
		
			SumDiffVariable yVar = new SumDiffVariable(getParameter(Y_VAR_NAME_PARAM), data,
																					"groupMean", "scaledError", SumDiffVariable.SUM);
		data.addVariable("y", yVar);
		
			GroupsModelVariable factorModel = new GroupsModelVariable("Factor", data, "x");
			factorModel.updateLSParams("y");
		data.addVariable("model", factorModel);
		
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			XPanel scatterPanel = new XPanel();
			scatterPanel.setLayout(new AxisLayout());
				
					NumVariable yVar = (NumVariable)data.getVariable("y");
					CatVariable xVar = (CatVariable)data.getVariable("x");
					
					HorizAxis xAxis = new HorizAxis(this);
//					int nXCats = xVar.noOfCategories();
					xAxis.setCatLabels(xVar);
					xAxis.setAxisName(xVar.name);
				
				scatterPanel.add("Bottom", xAxis);
				
					VertAxis yAxis = new VertAxis(this);
					String labelInfo = getParameter(Y_AXIS_INFO_PARAM);
					yAxis.readNumLabels(labelInfo);
				
				scatterPanel.add("Left", yAxis);
					
					DragFactorMeansView theView = new DragFactorMeansView(data, this, xAxis, yAxis, null, "x", "y", "model");
					theView.setCanDrag(false);
					theView.setCrossSize(DataView.LARGE_CROSS);
					theView.lockBackground(Color.white);
				
				scatterPanel.add("Center", theView);
				
		thePanel.add("Center", scatterPanel);
			
			XLabel yVariateName = new XLabel(yVar.name, XLabel.LEFT, this);
			yVariateName.setFont(yAxis.getFont());
			
		thePanel.add("North", yVariateName);
		
		return thePanel;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL,
													VerticalLayout.VERT_CENTER, 20));
		
			StringTokenizer st = new StringTokenizer(getParameter(MEAN_RSS_PARAM));
			NumValue minValue = new NumValue(st.nextToken());
			NumValue maxValue = new NumValue(st.nextToken());
			NumValue startValue = new NumValue(st.nextToken());
			meanRssSlider = new ParameterSlider(minValue, maxValue, startValue,
																			"Mean resid ssq", this);
			meanRssSlider.setFont(getStandardBoldFont());
		thePanel.add(meanRssSlider);
		
		return thePanel;
	}
	
	private void setMeanRss() {
		ScaledVariable scaledError = (ScaledVariable)data.getVariable("scaledError");
		double s = Math.sqrt(meanRssSlider.getParameter().toDouble());
		scaledError.setParam(1, s);
		data.variableChanged("scaledError");
	}
	
	private boolean localAction(Object target) {
		if (target == meanRssSlider) {
			setMeanRss();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}