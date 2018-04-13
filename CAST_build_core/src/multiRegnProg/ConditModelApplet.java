package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import multiRegn.*;


public class ConditModelApplet extends CoreRegnPlaneApplet {
	static final private String X_LIMITS_PARAM = "xLimits";
	static final private String Z_LIMITS_PARAM = "zLimits";
	
	static final private String explanKey[] = {"x", "z"};
	
	private XCheckbox[] paramCheck;
	private boolean[] paramInModel;
	
	private XChoice conditChoice;
	private int currentConditChoice = ConditModel3View.Z_CONDIT;
	
	private XPanel sliderPanel;
	private CardLayout sliderCardLayout;
	private ParameterSlider valueSlider[] = new ParameterSlider[2];
	private String sliderName[] = {"X", "Z"};
	private NumValue[] sliderMin = new NumValue[2];
	private NumValue[] sliderMax = new NumValue[2];
	private NumValue[] sliderStart = new NumValue[2];
	
	private ConditLinearEqnView conditEqn;
	
	
	protected DataSet readData() {
		data = super.readData();
		
		readSliderLimits(getParameter(X_LIMITS_PARAM), 0);
		readSliderLimits(getParameter(Z_LIMITS_PARAM), 1);
		
		return data;
	}
	
	private void readSliderLimits(String limits, int paramIndex) {
		StringTokenizer st = new StringTokenizer(limits);
		sliderMin[paramIndex] = new NumValue(st.nextToken());
		sliderMax[paramIndex] = new NumValue(st.nextToken());
		sliderStart[paramIndex] = new NumValue(st.nextToken());
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = super.topPanel(data);
		if (paramInModel != null)
			theEqn.setDrawParameters(paramInModel);
		theEqn.setHighlightIndex(2 - currentConditChoice);
		return thePanel;
	}
	
	protected Rotate3DView getRotatingView(DataSet data, D3Axis yAxis, D3Axis xAxis, D3Axis zAxis) {
		ConditModel3View view = new ConditModel3View(data, this, xAxis, yAxis, zAxis, "model", explanKey, "y");
		view.setCondit(currentConditChoice, sliderStart[currentConditChoice].toDouble());
		return view;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		thePanel.add("Center", super.controlPanel(data));
		
			XPanel eqnPanel = new XPanel();
			eqnPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																				VerticalLayout.VERT_CENTER, 0));
				conditEqn = new ConditLinearEqnView(data, this, "model", yName, explanName, minParam, maxParam);
				conditEqn.setCondit(currentConditChoice, sliderStart[currentConditChoice].toDouble());
				conditEqn.setForeground(Color.red);
				conditEqn.setFont(getStandardBoldFont());
			eqnPanel.add(conditEqn);
			
		thePanel.add("East", eqnPanel);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																				VerticalLayout.VERT_CENTER, 0));
			topPanel.add(parameterPanel(data));
		
		thePanel.add("Center", topPanel);
		thePanel.add("South", conditChoicePanel(data));
		
		return thePanel;
	}
	
	private XPanel conditChoicePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 10));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				conditChoice = new XChoice(this);
				conditChoice.addItem("Predict for " + explanName[0] + " = ...");
				conditChoice.addItem("Predict for " + explanName[1] + " = ...");
				conditChoice.select(currentConditChoice);
				conditChoice.setForeground(Color.red);
			topPanel.add(conditChoice);
		
		thePanel.add(topPanel);
		
			sliderPanel = new XPanel();
				sliderCardLayout = new CardLayout();
			sliderPanel.setLayout(sliderCardLayout);
				for (int i=0 ; i<2 ; i++) {
					XPanel innerPanel = new XPanel();
					innerPanel.setLayout(new BorderLayout(5, 0));
						valueSlider[i] = new ParameterSlider(sliderMin[i], sliderMax[i], sliderStart[i],
												explanName[i], ParameterSlider.SHOW_MIN_MAX, this);
					innerPanel.add("Center", valueSlider[i]);
					sliderPanel.add(sliderName[i], innerPanel);
					valueSlider[i].setForeground(Color.red);
				}
		
		thePanel.add(sliderPanel);
			sliderCardLayout.show(sliderPanel, sliderName[currentConditChoice]);
			
		return thePanel;
	}
	
	private XPanel parameterPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 4));
		
		XLabel heading = new XLabel("Parameters in Model", XLabel.LEFT, this);
		heading.setFont(getStandardBoldFont());
		thePanel.add(heading);
		
		paramCheck = new XCheckbox[explanName.length];
		paramInModel = new boolean[explanName.length + 1];
		paramInModel[0] = true;
		for (int i=0 ; i<explanName.length ; i++) {
			paramCheck[i] = new XCheckbox(explanName[i], this);
			paramCheck[i].setState(true);
			paramInModel[i + 1] = true;
			thePanel.add(paramCheck[i]);
		}
		setBestParams();			//		paramInModel[] must be set before calling function
		if (theEqn != null)
			theEqn.setDrawParameters(paramInModel);
		return thePanel;
	}
	
	private void setBestParams() {
		double[] fixedB = new double[explanKey.length + 1];
		for (int i=0 ; i<fixedB.length ; i++)
			fixedB[i] = paramInModel[i] ? Double.NaN : 0.0;
		MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
		model.updateLSParams("y", fixedB);
//		for (int i=0 ; i<3 ; i++)
//			System.out.print(model.getParameter(i).toDouble() + ", ");
//		System.out.println(model.evaluateSD().toDouble());
//				Useful for finding all LS parameters in order to set model for PlaneSampleApplet
	}
	
	
	private boolean localAction(Object target) {
		for (int i=0 ; i<paramCheck.length ; i++)
			if (target == paramCheck[i]) {
				paramInModel[i + 1] = paramCheck[i].getState();
				setBestParams();
				theEqn.setDrawParameters(paramInModel);
				data.variableChanged("model");
				
				return true;
			}
		
		for (int i=0 ; i<valueSlider.length ; i++)
			if (target == valueSlider[i]) {
				ConditModel3View conditView = (ConditModel3View)theView;
				conditView.setCondit(currentConditChoice, valueSlider[i].getParameter().toDouble());
				conditEqn.setCondit(currentConditChoice, valueSlider[i].getParameter().toDouble());
				return true;
			}
		
		if (target == conditChoice) {
			int newChoice = conditChoice.getSelectedIndex();
			if (newChoice != currentConditChoice) {
				currentConditChoice = newChoice;
				ConditModel3View conditView = (ConditModel3View)theView;
				sliderCardLayout.show(sliderPanel, sliderName[newChoice]);
				conditView.setCondit(newChoice, valueSlider[newChoice].getParameter().toDouble());
				conditEqn.setCondit(newChoice, valueSlider[newChoice].getParameter().toDouble());
				theEqn.setHighlightIndex(2 - currentConditChoice);
			}
		}	//	 and still return false
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (localAction(evt.target))
			return true;
		else
			return super.action(evt, what);
	}
}