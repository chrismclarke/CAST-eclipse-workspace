package experProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import random.*;
import models.*;
import valueList.*;

import map.*;
import exper.*;


public class SelectTreatmentsApplet extends XApplet {
	static final private String LURKING_VAR_NAME_PARAM = "lurkingVarName";
	static final private String LURKING_VALUES_PARAM = "lurkingValues";
	static final private String LURKING_MIN_MAX_PARAM = "lurkingMinMax";
	
	static final private String UNIT_NAME_PARAM = "unitName";
	static final private String CIRCLE_COLS_PARAM = "circleCols";
	
	static final private String TREAT_VAR_NAME_PARAM = "treatVarName";
	static final private String TREAT_LABELS_PARAM = "treatLabels";
	static final private String LURKING_REGN_PARAM = "lurkingRegn";
	static final private String RANDOM_SEED_PARAM = "randomSeed";
	static final private String NO_IN_LOW_GROUP_PARAM = "noInLowGroup";
	
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_REGN_PARAM = "yRegn";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	static final private String EFFECT_PARAM = "treatEffect";
	static final private String EFFECT_NAME_PARAM = "effectName";
	static final private String MAX_DIFF_PARAM = "maxDiff";
	
	static final private Color kKeyColors[] = {Color.red, Color.blue};
	
	static final private Color kDiffBackground = new Color(0xEDF2FF);
	static final private Color kDiffColor = new Color(0x990000);
	
	private double treatmentEffect[];
	protected NumValue maxDiff;
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	protected XButton takeSampleButton, allocateTreatsButton;
	protected XChoice effectChoice;
	protected int effectIndex = 0;
	
	protected AllocateToGroupsView allocateView;
	private VertAxis yAxis;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		initialiseNullSample(data, summaryData);
		
		setLayout(new BorderLayout(10, 0));
		
		add("West", treatAllocatePanel(data));
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new BorderLayout(10, 0));
			
			rightPanel.add("West", controlPanel());
			rightPanel.add("Center", responsePanel(data));
			rightPanel.add("North", theoryPanel(data));
			rightPanel.add("South", diffMeanPanel(data));
			
		add("Center", rightPanel);
	}
	
	protected DataSet getData() {
		data = new DataSet();
		
			NumVariable lurkingVar = new NumVariable(getParameter(LURKING_VAR_NAME_PARAM));
			lurkingVar.readValues(getParameter(LURKING_VALUES_PARAM));
		data.addVariable("lurking", lurkingVar);
		
			LinearModel lurkingModel = new LinearModel("Treat model", data, "lurking",
																														getParameter(LURKING_REGN_PARAM));
		data.addVariable("treatModel", lurkingModel);
		
			StringTokenizer st = new StringTokenizer(getParameter(RANDOM_SEED_PARAM));
			int nValues = lurkingVar.noOfValues();
			String randomParams = String.valueOf(nValues) + " 0.0 1.0 " + st.nextToken() + " 3.0";
			RandomNormal treatGenerator = new RandomNormal(randomParams);
			NumSampleVariable lurkingError = new NumSampleVariable("treatError", treatGenerator, 10);
			lurkingError.setSampleSize(nValues);
			lurkingError.generateNextSample();
		data.addVariable("selectError", lurkingError);
		
		data.addVariable("treatSelect", new ResponseVariable("treatSelect", data, "lurking",
																																"selectError", "treatModel", 9));
		
			int noInLowGroup = Integer.parseInt(getParameter(NO_IN_LOW_GROUP_PARAM));
		data.addVariable("treat", new GroupByYVariable(getParameter(TREAT_VAR_NAME_PARAM), data,
														"treatSelect", getParameter(TREAT_LABELS_PARAM), noInLowGroup));
		
		
			LinearModel yModel = new LinearModel(getParameter(Y_VAR_NAME_PARAM), data,
																								"lurking", getParameter(Y_REGN_PARAM));
		data.addVariable("yModel", yModel);
			
			StringTokenizer st2 = new StringTokenizer(getParameter(EFFECT_PARAM));
			treatmentEffect = new double[st2.countTokens()];
			for (int i=0 ; i<treatmentEffect.length ; i++)
				treatmentEffect[i] = Double.parseDouble(st2.nextToken());
		
			randomParams = String.valueOf(nValues) + " 0.0 1.0 " + st.nextToken() + " 3.0";
			RandomNormal yGenerator = new RandomNormal(randomParams);
			NumSampleVariable yError = new NumSampleLsVariable("yError", yGenerator, 10, data,
																																						getLsKeys(), "y");
			yError.setSampleSize(nValues);
		data.addVariable("yError", yError);
		
			ResponseVariable resp = new ResponseVariable(getParameter(Y_VAR_NAME_PARAM),
								data, "lurking", "yError", "yModel", "treat", treatmentEffect[0], 10);
		data.addVariable("y", resp);
		
			String[] kTreatKey = {"treat"};
			maxDiff = new NumValue(getParameter(MAX_DIFF_PARAM));
			MultipleRegnModel lsRaw = new MultipleRegnModel("LS without covars", data, kTreatKey,
																																createCloneArray(maxDiff, 2));
		data.addVariable("ls", lsRaw);
		
		return data;
	}
	
	protected String[] getLsKeys() {
		String[] keys = {"ls"};
		return keys;
	}
	
	protected NumValue[] createCloneArray(NumValue v, int repeats) {
		NumValue tempArray[] = new NumValue[repeats];
		for (int i=0 ; i<repeats ; i++)
			tempArray[i] = new NumValue(v);
		return tempArray;
	}
	
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "yError");
		
			String treatEstName = translate("Estimated") + " " + getParameter(EFFECT_NAME_PARAM);
		summaryData.addVariable("treatEst", new MultiRegnParamVariable(treatEstName, "ls", 1));
		
		return summaryData;
	}
	
	private void initialiseNullSample(DataSet data, SummaryDataSet summaryData) {
		NumSampleVariable yError = (NumSampleVariable)data.getVariable("yError");
		yError.generateNextSample();
		
		summaryData.takeSample();		//	so one value is added to each summary variable
		clearYErrorValues(data);
		String[] lsKeys = getLsKeys();
		for (int i=0 ; i<lsKeys.length ; i++) {
			MultipleRegnModel lsModel = (MultipleRegnModel)data.getVariable(lsKeys[i]);
			lsModel.setParameter(1, Double.NaN);			//		assumes models all have treatment effect first
		}
		summaryData.redoLastSummary();
	}
	
	private void clearYErrorValues(DataSet data) {
		NumVariable yError = (NumVariable)data.getVariable("yError");
		ValueEnumeration ye = yError.values();
		while (ye.hasMoreValues())
			((NumValue)ye.nextValue()).setValue(Double.NaN);
		data.variableChanged("yError");
	}
	
	private XPanel treatAllocatePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
			XPanel keyPanel = new XPanel();
			keyPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
			
				XLabel lurkingLabel = new XLabel(data.getVariable("lurking").name, XLabel.LEFT, this);
				lurkingLabel.setFont(getStandardBoldFont());
			keyPanel.add(lurkingLabel);
			
				StringTokenizer st = new StringTokenizer(getParameter(LURKING_MIN_MAX_PARAM), "#");
				NumKeyView lurkingKeyView = new NumKeyView(data, this, st.nextToken(), st.nextToken(), kKeyColors);
			keyPanel.add(lurkingKeyView);
			
		thePanel.add("South", keyPanel);
		
			int circleCols = Integer.parseInt(getParameter(CIRCLE_COLS_PARAM));
			allocateView = new AllocateToGroupsView(data, this, "treat", "lurking", lurkingKeyView,
																			new LabelValue(getParameter(UNIT_NAME_PARAM)), circleCols);
			allocateView.setFont(getStandardBoldFont());
		thePanel.add("Center", allocateView);
		
		return thePanel;
	}
	
	protected XPanel controlPanel() {
		XPanel thePanel = new InsetPanel(14, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 40));
		
			StringTokenizer st = new StringTokenizer(translate("Allocate*Treatments"), "*");
			allocateTreatsButton = new XButton(st.nextToken() + "\n" + st.nextToken(), this);
		thePanel.add(allocateTreatsButton);
		
			st = new StringTokenizer(translate("Run*Experiment"), "*");
			takeSampleButton = new XButton(st.nextToken() + "\n" + st.nextToken(), this);
			takeSampleButton.disable();
		thePanel.add(takeSampleButton);
		
		return thePanel;
	}
	
	protected XPanel responsePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		thePanel.add("Center", responsePlotPanel(data));
		thePanel.add("North", responseLabelPanel(data));
	
		return thePanel;
	}
	
	private XPanel responseLabelPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		XLabel yVariateName = new XLabel(getParameter(Y_VAR_NAME_PARAM), XLabel.LEFT, this);
		yVariateName.setFont(yAxis.getFont());
		thePanel.add(yVariateName);
		return thePanel;
	}
	
	private XPanel responsePlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			yAxis = new VertAxis(this);
			yAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
		thePanel.add("Left", yAxis);
		
			HorizAxis treatAxis = new HorizAxis(this);
			treatAxis.setCatLabels((CatVariable)data.getVariable("treat"));
			
		thePanel.add("Bottom", treatAxis);
		
			TreatDotView yView = new TreatDotView(data, this, yAxis, treatAxis, "y", "treat", 1.0);
			yView.lockBackground(Color.white);
		thePanel.add("Center", yView);
		
		return thePanel;
	}
	
	protected XPanel diffMeanPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
			
			XPanel innerPanel = new InsetPanel(12, 4);
			innerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
				OneValueView diffValue = new OneValueView(summaryData, "treatEst", this, maxDiff);
				diffValue.setFont(getStandardBoldFont());
				diffValue.setForeground(kDiffColor);
			innerPanel.add(diffValue);
			
			innerPanel.lockBackground(kDiffBackground);
		thePanel.add(innerPanel);
		return thePanel;
	}
	
	private XPanel theoryPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
			
			XPanel innerPanel = new InsetPanel(12, 4);
			innerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
				StringTokenizer st = new StringTokenizer(getParameter(EFFECT_PARAM));
				if (st.countTokens() == 1) {
					XLabel theoryLabel = new XLabel(translate("Theoretical") + " " + getParameter(EFFECT_NAME_PARAM) + " = "
																										+ getParameter(EFFECT_PARAM), XLabel.LEFT, this);
					theoryLabel.setFont(getStandardBoldFont());
					theoryLabel.setForeground(kDiffColor);
					innerPanel.add(theoryLabel);
				}
				else {
					effectChoice = new XChoice(translate("Theoretical") + " " + getParameter(EFFECT_NAME_PARAM) + " =",
																											XChoice.HORIZONTAL, this);
					while (st.hasMoreTokens())
						effectChoice.addItem(st.nextToken());
					innerPanel.add(effectChoice);
				}
			
			innerPanel.lockBackground(kDiffBackground);
		thePanel.add(innerPanel);
		return thePanel;
	}
	
	protected void frameChanged(DataView theView) {
		if (theView.getCurrentFrame() == AllocateToGroupsView.kEndFrame)
			takeSampleButton.enable();
	}
	
	private boolean localAction(Object target) {
		if (target == allocateTreatsButton) {
			allocateTreatsButton.disable();
			NumSampleVariable lurkingError = (NumSampleVariable)data.getVariable("selectError");
			lurkingError.generateNextSample();
			data.variableChanged("selectError");
			clearYErrorValues(data);
			String[] lsKeys = getLsKeys();
			for (int i=0 ; i<lsKeys.length ; i++) {
				MultipleRegnModel lsModel = (MultipleRegnModel)data.getVariable(lsKeys[i]);
				lsModel.setParameter(1, Double.NaN);
			}
			summaryData.redoLastSummary();
			allocateView.doGroupingAnimation();
			return true;
		}
		else if (target == takeSampleButton) {
			takeSampleButton.disable();
			summaryData.takeSample();
			
			allocateTreatsButton.enable();
			return true;
		}
		else if (target == effectChoice) {
			int newChoice = effectChoice.getSelectedIndex();
			if (newChoice != effectIndex) {
				effectIndex = newChoice;
				ResponseVariable resp = (ResponseVariable)data.getVariable("y");
				resp.setEffect(treatmentEffect[newChoice]);
				data.variableChanged("y");
				String[] lsKeys = getLsKeys();
				for (int i=0 ; i<lsKeys.length ; i++) {
					MultipleRegnModel lsModel = (MultipleRegnModel)data.getVariable(lsKeys[i]);
					lsModel.updateLSParams("y");
				}
				summaryData.redoLastSummary();
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