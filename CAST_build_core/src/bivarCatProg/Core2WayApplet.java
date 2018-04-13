package bivarCatProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import bivarCat.*;


abstract public class Core2WayApplet extends XApplet {
	static final protected String X_VAR_NAME_PARAM = "xVarName";
	static final protected String X_VALUES_PARAM = "xValues";
	static final protected String X_LABELS_PARAM = "xLabels";
	
	static final protected String Y_VAR_NAME_PARAM = "yVarName";
	static final protected String Y_VALUES_PARAM = "yValues";
	static final private String Y_LABELS_PARAM = "yLabels";
	
	static final private String FREQ_UNITS_PARAM = "freqUnits";
	
	static final private String VERT_SCALE_PARAM = "vertScale";
																//	0 = count, 1 = propnWithinX, 2 = propnWithinY
	static final private String SHOW_PERCENTAGES_PARAM = "showPercentages";

	static final protected String DECIMALS_PARAM = "decimals";		//		For propns in table
	
	protected Variable xVariable, yVariable;
	
	protected boolean canScaleCount = false;
	protected boolean canScalePropnInX = false;
	protected boolean canScalePropnInY = false;
	protected boolean showPercentages = true;
	
	protected int initialVertScale;
	protected XChoice vertScaleChoice;
	private String initialVertScaleName;
	
	private int vertScaleMenuMap[] = new int[5];
	
	public void setupApplet() {
		DataSet data = readData();
		
		readOptions();
		
		addDisplayComponents(data);
		
		if (initialVertIndex() != 0 && vertScaleChoice != null) {
			vertScaleChoice.select(initialVertIndex());
			changeVertScale(getVertScaleChoice());
		}
	}
	
	abstract protected void addDisplayComponents(DataSet data);
	
	protected int initialVertIndex() {
		return 0;
	}
	
	private String getCountName() {
		String freqUnits = getParameter(FREQ_UNITS_PARAM);
		String countName = translate("Frequency");
		if (freqUnits == null)
			return countName;
		else
			return countName + " (" + freqUnits + ")";
	}
	
	protected void readOptions() {
		StringTokenizer vertScaleInfo = new StringTokenizer(getParameter(VERT_SCALE_PARAM));
		while (vertScaleInfo.hasMoreTokens()) {
			int index = Integer.parseInt(vertScaleInfo.nextToken());
			if (index == 0) {
				if (initialVertScaleName == null)
					initialVertScaleName = getCountName();
				canScaleCount = true;
			}
			else if (index == 1) {
				if (initialVertScaleName == null)
					initialVertScaleName = translate("Propn within") + " " + xVariable.name;
				canScalePropnInX = true;
			}
			else if (index == 2) {
				if (initialVertScaleName == null)
					initialVertScaleName = translate("Propn within") + " " + yVariable.name;
				canScalePropnInY = true;
			}
		}
		
		initialVertScale = canScaleCount ? TwoWayView.COUNT : 
									canScalePropnInX ? TwoWayView.PROPN_IN_X : TwoWayView.PROPN_IN_Y;
		String showPercentString = getParameter(SHOW_PERCENTAGES_PARAM);
		if (showPercentString != null && showPercentString.equals("false"))
			showPercentages = false;
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		CatVariable xCatVariable = new CatVariable(getParameter(X_VAR_NAME_PARAM),
																							Variable.USES_REPEATS);
		xCatVariable.readLabels(getParameter(X_LABELS_PARAM));
		xCatVariable.readValues(getParameter(X_VALUES_PARAM));
		xVariable = xCatVariable;
		data.addVariable("x", xVariable);
		
		CatVariable yCatVariable = new CatVariable(getParameter(Y_VAR_NAME_PARAM), Variable.USES_REPEATS);
		yCatVariable.readLabels(getParameter(Y_LABELS_PARAM));
		yCatVariable.readValues(getParameter(Y_VALUES_PARAM));
		yVariable = yCatVariable;
		data.addVariable("y", yVariable);
		
		return data;
	}
	
	protected XPanel vertScalePanel(DataSet data, String labelForChoice) {
		int noOfOptions = 0;
		if (canScaleCount)
			noOfOptions ++;
		if (canScalePropnInX) {
			noOfOptions ++;
			if (showPercentages)
				noOfOptions ++;
		}
		if (canScalePropnInY) {
			noOfOptions ++;
			if (showPercentages)
				noOfOptions ++;
		}
		
		XPanel vertScaleChoicePanel = new XPanel();
		vertScaleChoicePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		if (noOfOptions == 1) {
			XLabel vertLabel = new XLabel(initialVertScaleName, XLabel.LEFT, this);
			vertLabel.setFont(getBigFont());
			vertScaleChoicePanel.add(vertLabel);
		}
		else {
			if (labelForChoice != null) {
				XLabel theLabel = new XLabel(labelForChoice, XLabel.LEFT, this);
				theLabel.setFont(getStandardBoldFont());
				vertScaleChoicePanel.add(theLabel);
			}
			
			vertScaleChoice = new XChoice(this);
			int menuIndex = 0;
			if (canScaleCount) {
				vertScaleMenuMap[menuIndex ++] = TwoWayView.COUNT;
				vertScaleChoice.addItem(getCountName());
			}
			if (canScalePropnInX) {
				vertScaleMenuMap[menuIndex ++] = TwoWayView.PROPN_IN_X;
				vertScaleChoice.addItem(translate("Propn within") + " " + xVariable.name);
				if (showPercentages) {
					vertScaleMenuMap[menuIndex ++] = TwoWayView.PERCENT_IN_X;
					vertScaleChoice.addItem(translate("Percent within") + " " + xVariable.name);
				}
			}
			if (canScalePropnInY) {
				vertScaleMenuMap[menuIndex ++] = TwoWayView.PROPN_IN_Y;
				vertScaleChoice.addItem(translate("Propn within") + " " + yVariable.name);
				if (showPercentages) {
					vertScaleMenuMap[menuIndex ++] = TwoWayView.PERCENT_IN_Y;
					vertScaleChoice.addItem(translate("Percent within") + " " + yVariable.name);
				}
			}
			vertScaleChoice.select(0);
			vertScaleChoicePanel.add(vertScaleChoice);
		}
		return vertScaleChoicePanel;
	}
	
	protected int getVertScaleChoice() {		//	translates choice index into TwoWayView vertScale
		int newVertIndex = vertScaleChoice.getSelectedIndex();
		return vertScaleMenuMap[newVertIndex];
	}
	
	abstract protected void changeVertScale(int newVertScale);
	
	private boolean localAction(Object target) {
		if (target == vertScaleChoice) {
			changeVertScale(getVertScaleChoice());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}