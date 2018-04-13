package simulationProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;

import simulation.*;


public class PseudoRandomApplet extends XApplet {
	static final private String MAX_DIGITS_PARAM = "maxDigits";
	static final private String GENERATOR_COUNT_PARAM = "noOfGenerators";
	static final private String GENERATOR_PARAM = "generator";
	static final private String GENERATOR_NAME_PARAM = "generatorName";
	
	private XButton resetButton;
	private RepeatingButton nextButton;
	private XChoice generatorChoice, displayChoice;
	private int oldGeneratorChoice = 0;
	private int oldDisplayChoice = 0;
	
	private long seed[];
	private int seedDigits[];
	private int plus[];
	private int times[];
	
	private DataSet data;
	
	private HiliteLastDotPlotView valueView;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 20));
		add("Center", displayPanel(data));
		add("South", choicePanel());
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		int noOfGenerators = Integer.parseInt(getParameter(GENERATOR_COUNT_PARAM));
		seed = new long[noOfGenerators];
		seedDigits = new int[noOfGenerators];
		plus = new int[noOfGenerators];
		times = new int[noOfGenerators];
		for (int i=0 ; i<noOfGenerators ; i++) {
			StringTokenizer st = new StringTokenizer(getParameter(GENERATOR_PARAM + (i+1)));
			seed[i] = Long.parseLong(st.nextToken());
			seedDigits[i] = Integer.parseInt(st.nextToken());
			plus[i] = Integer.parseInt(st.nextToken());
			times[i] = Integer.parseInt(st.nextToken());
		}
		PseudoRandomVariable random = new PseudoRandomVariable("pseudoRandom", seed[0],
																			seedDigits[0], plus[0], times[0]);
		data.addVariable("random", random);
		
		data.addVariable("scaledRandom", new ScaledRandomVariable("scaledRandom", data, "random"));
		
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new BorderLayout(0, 10));
			
				StringTokenizer st = new StringTokenizer(getParameter(MAX_DIGITS_PARAM));
				int maxSeedDigits = Integer.parseInt(st.nextToken());
				int maxResultDigits = Integer.parseInt(st.nextToken());
				int maxTimesDigits = Integer.parseInt(st.nextToken());
				int maxPlusDigits = Integer.parseInt(st.nextToken());
				
				PseudoRandomView calcView = new PseudoRandomView(data, this, maxSeedDigits, maxResultDigits,
																											maxTimesDigits, maxPlusDigits, "random");
			leftPanel.add("Center", calcView);
			leftPanel.add("South", buttonPanel());
			
		thePanel.add("West", leftPanel);
		
		thePanel.add("Center", dotPlotPanel(data));
		
		return thePanel;
	}
	
	private XPanel dotPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			VertAxis vertAxis = new VertAxis(this);
			vertAxis.readNumLabels("0 1 0.0 0.1");
		thePanel.add("Left", vertAxis);
		
			valueView = new HiliteLastDotPlotView(data, this, vertAxis);
			valueView.setActiveNumVariable("scaledRandom");
			valueView.lockBackground(Color.white);
		thePanel.add("Center", valueView);
		return thePanel;
	}
	
	private XPanel buttonPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
		nextButton = new RepeatingButton(translate("Next value"), this);
		thePanel.add(nextButton);
		
		resetButton = new XButton(translate("Reset"), this);
		thePanel.add(resetButton);
		
		return thePanel;
	}
	
	private XPanel choicePanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
		displayChoice = new XChoice(this);
		displayChoice.addItem(translate("Jittered"));
		displayChoice.addItem(translate("Time series"));
		thePanel.add(displayChoice);
		
		generatorChoice = new XChoice(this);
		for (int i=0 ; i<seed.length ; i++)
			generatorChoice.addItem(getParameter(GENERATOR_NAME_PARAM + (i+1)));
		thePanel.add(generatorChoice);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == nextButton) {
			PseudoRandomVariable rand = (PseudoRandomVariable)data.getVariable("random");
			rand.generateNext();
			data.valuesAdded(rand.noOfValues());
			
			return true;
		}
		else if (target == resetButton) {
			PseudoRandomVariable rand = (PseudoRandomVariable)data.getVariable("random");
			rand.resetGenerator();
			data.valuesAdded(rand.noOfValues());
			
			return true;
		}
		else if (target == displayChoice) {
			int newSelection = displayChoice.getSelectedIndex();
			if (oldDisplayChoice != newSelection) {
				oldDisplayChoice = newSelection;
				valueView.setTimeSeries(newSelection > 0);
			}
			return true;
		}
		else if (target == generatorChoice) {
			PseudoRandomVariable rand = (PseudoRandomVariable)data.getVariable("random");
			int newSelection = generatorChoice.getSelectedIndex();
			if (oldGeneratorChoice != newSelection) {
				oldGeneratorChoice = newSelection;
				rand.resetGenerator(seed[newSelection], seedDigits[newSelection],
																plus[newSelection], times[newSelection]);
				data.valuesAdded(rand.noOfValues());
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