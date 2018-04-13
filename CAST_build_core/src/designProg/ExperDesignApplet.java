package designProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import random.RandomNormal;
import exper.*;


public class ExperDesignApplet extends XApplet {
	static final private String TREAT_EFFECT_PARAM = "treatEffect";
	static final protected String FIELD_PICT_PARAM = "fieldPict";
	static final protected String TREAT_PICT_PARAM = "treatPict";
	static final private String RESPONSE_NAME_PARAM = "responseName";
	static final private String MAX_MEAN_PARAM = "maxMean";
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String RANDOMISATION_PARAM = "randomisation";
	static final protected String AXIS_INFO_PARAM = "vertAxis";
	
	protected DataSet data;
	protected double treatmentEffect[];
	protected NumValue maxMean;
	protected int plotsPerTreat[];
	
	private FieldPlotsView theView;
	private TreatmentBiasView theBias;
	protected RandomNormal generator;
	private XButton sampleButton;
	
	private Random randomisator;
	
	public void setupApplet() {
		String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
		generator = new RandomNormal(randomInfo);
		
		StringTokenizer st = new StringTokenizer(getParameter(TREAT_EFFECT_PARAM));
		treatmentEffect = new double[st.countTokens()];
		for (int i=0 ; i<treatmentEffect.length ; i++)
			treatmentEffect[i] = Double.parseDouble(st.nextToken());
		maxMean = new NumValue(getParameter(MAX_MEAN_PARAM));
		
		DataSet data = readData();
		
		String randomisationString = getParameter(RANDOMISATION_PARAM);
		if (randomisationString != null) {
			CatVariable treat = (CatVariable)data.getVariable("treatment");
			int noOfTreats = treat.noOfCategories();
			StringTokenizer st2 = new StringTokenizer(randomisationString);
			plotsPerTreat = new int[noOfTreats];
			for (int i=0 ; i<noOfTreats ; i++)
				plotsPerTreat[i] = Integer.parseInt(st2.nextToken());
			
			long seed = Long.parseLong(st2.nextToken());
			randomisator = new Random(seed);
		}
		
		setLayout(new BorderLayout(10, 0));
		
		XPanel graphicPanel = new XPanel();
		graphicPanel.setLayout(new BorderLayout(10, 0));
		graphicPanel.add("West", displayPanel(data));
		graphicPanel.add("Center", dotPlotPanel(data));
		
		add("West", graphicPanel);
		add("Center", controlPanel(data));
	}
	
	public void setShowPicture(boolean doShowPicture) {
		if (theView != null)
			theView.setShowPicture(doShowPicture);
		if (theBias != null)
			theBias.show(doShowPicture);
	}
	
	protected DataSet readData() {
		data = new DataSet();
		
		String plotEffectName = getParameter(VAR_NAME_PARAM);
		if (plotEffectName != null)
			data.addNumVariable("plotEffect", plotEffectName, getParameter(VALUES_PARAM));
		CatVariable treatmentVar = new CatVariable(getParameter(CAT_NAME_PARAM));
		treatmentVar.readLabels(getParameter(CAT_LABELS_PARAM));
		treatmentVar.readValues(getParameter(CAT_VALUES_PARAM));
		data.addVariable("treatment", treatmentVar);
		NumVariable response = new NumVariable(getParameter(RESPONSE_NAME_PARAM));
		double val[] = new double[treatmentVar.noOfValues()];
		response.setValues(val);
		data.addVariable("response", response);
		return data;
	}
	
	private XPanel dotPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new MinAxisLayout(60));
		
		VertAxis theVertAxis = new VertAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theVertAxis.readNumLabels(labelInfo);
		thePanel.add("Left", theVertAxis);
		
		ResponseDotPlotView theView = new ResponseDotPlotView(data, this, theVertAxis, "treatment", "response", 1.0);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected FieldPlotsView getFieldView(DataSet data) {
		return new FieldPlotsView(data, this, getParameter(FIELD_PICT_PARAM), getParameter(TREAT_PICT_PARAM),
							FieldPlotsView.CAN_EDIT, "treatment", "plotEffect");
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
		theView = getFieldView(data);
		
		thePanel.add(theView);
		return thePanel;
	}
	
	protected TreatmentEffectView getEffectView(DataSet data) {
		return new TreatmentEffectView(data, this, "treatment", "response", maxMean, TreatmentEffectView.HIDE_INTERVAL);
	}
	
	protected TreatmentBiasView getBiasView(DataSet data) {
		return new TreatmentBiasView(data, this, "treatment", "plotEffect", maxMean);
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																	VerticalLayout.VERT_CENTER, 10));
		
		TreatmentEffectView effectView = getEffectView(data);
		thePanel.add(effectView);
		
		theBias = getBiasView(data);
		if (theBias != null) {
			theBias.show(false);
			thePanel.add(theBias);
		}
		
		sampleButton = new XButton(translate("Conduct experiment"), this);
		thePanel.add(sampleButton);
		
		return thePanel;
	}
	
	protected void generateResponses() {
		double vals[] = generator.generate();
		NumVariable response = (NumVariable)data.getVariable("response");
		NumVariable plotEffect = (NumVariable)data.getVariable("plotEffect");
		CatVariable treat = (CatVariable)data.getVariable("treatment");
		
		ValueEnumeration re = response.values();
		ValueEnumeration pe = plotEffect.values();
		ValueEnumeration te = treat.values();
		
		int index = 0;
		while (re.hasMoreValues() && pe.hasMoreValues() && te.hasMoreValues()) {
			LabelValue t = (LabelValue)te.nextValue();
			double p = pe.nextDouble();
			double value = p + vals[index];
			value += treatmentEffect[treat.labelIndex(t)];
			
			NumValue r = (NumValue)re.nextValue();
			r.decimals = maxMean.decimals;
			r.setValue(value);
			
			index ++;
		}
		
		data.variableChanged("response");
	}
	
	protected void randomiseTreatments(int startPlot, int[] treatCount) {
		CatVariable treat = (CatVariable)data.getVariable("treatment");
		int noOfTreats = treat.noOfCategories();
		
		int totalLeft = 0;
		int sampLeft[] = new int[noOfTreats];
		for (int i=0 ; i<noOfTreats ; i++) {
			sampLeft[i] = treatCount[i];
			totalLeft += treatCount[i];
		}
		int endPlot = startPlot + totalLeft;
		
		for (int i=startPlot ; i<endPlot ; i++) {
			double index = totalLeft * randomisator.nextDouble();
			int cumulative = 0;
			for (int j=0 ; j<noOfTreats ; j++) {
				cumulative += sampLeft[j];
				if (index < cumulative) {
					treat.setValueAt(treat.getLabel(j), i);
					sampLeft[j] --;
					break;
				}
			}
			totalLeft --;
		}
	}
	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			generateResponses();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}