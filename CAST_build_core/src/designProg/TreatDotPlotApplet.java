package designProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import exper.*;
import utils.*;
import random.RandomNormal;


public class TreatDotPlotApplet extends XApplet {
	static final private String RANDOM_NORMAL1_PARAM = "random1";
	static final private String RANDOM_NORMAL2_PARAM = "random2";
	static final protected String AXIS_INFO_PARAM = "vertAxis";
	static final private String GROUP_EFFECT1_PARAM = "groupEffect1";
	static final private String GROUP_EFFECT2_PARAM = "groupEffect2";
	
	private RandomNormal generator1, generator2;
	private double[] groupEffect1, groupEffect2;
	
	private int generatorType;
	private XChoice generatorChoice;
	private XButton sampleButton;
	private DotPlotTreatView theView;
	private DataSet data;
	private VertAxis theVertAxis;
	
	public void setupApplet() {
		data = createData();
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
		add("North", topPanel(data));
	}
	
	protected DataSet createData() {
		DataSet data = new DataSet();
		synchronized (data) {
			data.addCatVariable("group", getParameter(CAT_NAME_PARAM),
									getParameter(CAT_VALUES_PARAM), getParameter(CAT_LABELS_PARAM));
			CatVariable group = data.getCatVariable();
			
			String randomInfo = getParameter(RANDOM_NORMAL1_PARAM);
			if (randomInfo != null) {
				generator1 = new RandomNormal(randomInfo);
				
				groupEffect1 = new double[group.noOfCategories()];
				StringTokenizer theEffects = new StringTokenizer(getParameter(GROUP_EFFECT1_PARAM));
				for (int i=0 ; i<group.noOfCategories() ; i++) {
					String nextItem = theEffects.nextToken();
					groupEffect1[i] = Double.parseDouble(nextItem);
				}
			}
			
			NumVariable resp1 = new NumVariable(getParameter(VAR_NAME_PARAM));
			String valueString = getParameter(VALUES_PARAM);
			if (valueString != null)
				resp1.readValues(valueString);
			else {
				double vals[] = generateResponses(data, generator1, groupEffect1);
				resp1.setValues(vals);
			}
			data.addVariable("resp1", resp1);
			
			randomInfo = getParameter(RANDOM_NORMAL2_PARAM);
			if (randomInfo != null) {
				generator2 = new RandomNormal(randomInfo);
				
				groupEffect2 = new double[group.noOfCategories()];
				StringTokenizer theEffects = new StringTokenizer(getParameter(GROUP_EFFECT2_PARAM));
				for (int i=0 ; i<group.noOfCategories() ; i++) {
					String nextItem = theEffects.nextToken();
					groupEffect2[i] = Double.parseDouble(nextItem);
				}
				
				NumVariable resp2 = new NumVariable(getParameter(VAR_NAME_PARAM));
				double vals[] = generateResponses(data, generator2, groupEffect2);
				resp2.setValues(vals);
				data.addVariable("resp2", resp2);
			}
				
		}
		return data;
	}
	
	private double[] generateResponses(DataSet data, RandomNormal generator, double[] groupEffect) {
		double vals[] = generator.generate();
		CatVariable group = data.getCatVariable();
		for (int i=0 ; i<vals.length ; i++)
			vals[i] += groupEffect[group.getItemCategory(i)];
		return vals;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		XLabel yVariateName = new XLabel(getParameter(VAR_NAME_PARAM), XLabel.LEFT, this);
		yVariateName.setFont(theVertAxis.getFont());
		thePanel.add(yVariateName);
		return thePanel;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		theVertAxis = new VertAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theVertAxis.readNumLabels(labelInfo);
		thePanel.add("Left", theVertAxis);
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		theHorizAxis.setCatLabels(data.getCatVariable());
		theHorizAxis.setAxisName(getParameter(CAT_NAME_PARAM));
		thePanel.add("Bottom", theHorizAxis);
		
		CatVariable group = data.getCatVariable();
		boolean doJitter = group.noOfValues() > group.noOfCategories();
		
		theView = new DotPlotTreatView(data, this, theVertAxis, theHorizAxis, (doJitter ? 1.0 : 0.0));
		theView.setActiveNumVariable("resp1");
		theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
		
		if (generator1 != null && generator2 != null) {
			generatorChoice = new XChoice(this);
			generatorChoice.addItem("High natural variability");
			generatorChoice.addItem("Low natural variability");
			generatorChoice.select(0);
			generatorType = 0;
			thePanel.add(generatorChoice);
		}
		
		if (generator1 != null) {
			sampleButton = new XButton(translate("Conduct experiment"), this);
			thePanel.add(sampleButton);
		}
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == generatorChoice) {
			int newGenerator = generatorChoice.getSelectedIndex();
			if (newGenerator != generatorType) {
				generatorType = newGenerator;
				theView.setActiveNumVariable(generatorType == 0 ? "resp1" : "resp2");
				theView.repaint();
			}
			return true;
		}
		else if (target == sampleButton) {
			String variableKey = generatorType == 0 ? "resp1" : "resp2";
			NumVariable v = (NumVariable)data.getVariable(variableKey);
			RandomNormal generator = (generatorType == 0 ? generator1 : generator2);
			double groupEffect[] = (generatorType == 0 ? groupEffect1 : groupEffect2);
			double vals[] = generateResponses(data, generator, groupEffect);
			v.setValues(vals);
			theView.newRandomJittering();
//			data.variableChanged(variableKey);		//		newRandomJittering() causes repaint()
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}