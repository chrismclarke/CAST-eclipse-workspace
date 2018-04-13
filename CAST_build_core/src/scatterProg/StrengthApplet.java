package scatterProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import coreVariables.*;
import coreGraphics.*;

import scatter.*;


public class StrengthApplet extends ScatterApplet {
	static final private String QUAD1_PARAM = "quad1";
	static final private String QUAD2_PARAM = "quad2";
	static final private String QUAD3_PARAM = "quad3";
	
	protected ScatterMoveView theView;
	private QuadraticVariable y2Variable;
	
	private XButton animateButton;
	private XSlider animateSlider;
	private XChoice dataTypeChoice;
	private int currentChoice;
	
	protected String paramString[];
	
//	public void setupApplet() {
//		ScrollImages.loadScroll(this);
//		super.setupApplet();
//	}
	
	protected DataSet readData() {
		DataSet data = readCoreData();
		
		paramString = new String[3];
		paramString[0] = getParameter(QUAD1_PARAM);
		paramString[1] = getParameter(QUAD2_PARAM);
		paramString[2] = getParameter(QUAD3_PARAM);
		
		NumVariable xVariable = (NumVariable)data.getVariable("x");
		y2Variable = new QuadraticVariable(getParameter(Y_VAR_NAME_PARAM),
																	xVariable, paramString[0]);
		data.addVariable("y2", y2Variable);
		return data;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		theView = new ScatterMoveView(data, this, theHorizAxis, theVertAxis, "x", "y", "x", "y2");
		return theView;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new BorderLayout(5, 2));
		
		XPanel buttonPanel = new XPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 3));
		
		dataTypeChoice = createChoice(data);
		if (dataTypeChoice != null) {
			dataTypeChoice.select(0);
			currentChoice = 0;
			buttonPanel.add(dataTypeChoice);
		}
		
		animateButton = new XButton(translate("Animate"), this);
		buttonPanel.add(animateButton);
		
		controlPanel.add("West", buttonPanel);
		
		String text[] = getSliderText();
		animateSlider = new XNoValueSlider(text[0], text[1], text[2], 0, ScatterMoveView.kMaxFrame,
																																		0, this);
		controlPanel.add("Center", animateSlider);
		
		return controlPanel;
	}
	
	protected XChoice createChoice(DataSet data) {
		XChoice assocTypeChoice = new XChoice(this);
		assocTypeChoice.addItem("Positive");
		assocTypeChoice.addItem("Negative");
		assocTypeChoice.addItem("Curved");
		
		return assocTypeChoice;
	}
	
	protected String[] getSliderText() {
		String text[] = {"weak", "strong", "Association"};
		return text;
	}
	
	protected void changeDataType(String newParams) {
		y2Variable.setParameters(newParams);
	}

	
	private boolean localAction(Object target) {
		if (target == animateSlider) {
			theView.setFrame(animateSlider.getValue());
			return true;
		}
		else if (target == animateButton) {
			theView.doAnimation(animateSlider);
			return true;
		}
		else if (target == dataTypeChoice && dataTypeChoice != null) {
			int newDataIndex = dataTypeChoice.getSelectedIndex();
			if (newDataIndex != currentChoice) {
				changeDataType(paramString[newDataIndex]);
				synchronized (data) {
					data.variableChanged("y2");
					theView.setFrame(0, animateSlider);
				}
				currentChoice = newDataIndex;
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