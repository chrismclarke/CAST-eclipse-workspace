package simulationProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import valueList.*;

import simulation.*;


public class RandomCatApplet extends XApplet {
	static final private String RANDOM_TEMPLATE_PARAM = "randomTemplate";
	
	private NumValue randomTemplate;
	private double[] prob = {0.6, 0.4};
	
	private XButton resetButton;
	private RepeatingButton nextButton;
	private ParameterSlider probSlider;
	
	private OneValueView catView;
	
	protected DataSet data;
	
	protected RandomCatGeneratorView valueView;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(20, 0));
		add("West", leftPanel(data));
		add("Center", mainPanel(data));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		randomTemplate = new NumValue(getParameter(RANDOM_TEMPLATE_PARAM));
		U01RandomVariable rand = new U01RandomVariable(getParameter(VAR_NAME_PARAM),
																						randomTemplate.decimals);
		data.addVariable("random", rand);
		rand.generateNext();
		
		data.addVariable("randomCat", createCatVariable(data));
		
		data.setSelection(0);
		
		return data;
	}
	
	protected PseudoRandCatVariable createCatVariable(DataSet data) {
		PseudoRandCatVariable catVar = new PseudoRandCatVariable(getParameter(CAT_NAME_PARAM),
																						data, "random", prob);
		catVar.readLabels(getParameter(CAT_LABELS_PARAM));
		return catVar;
	}
	
	private XPanel leftPanel(DataSet data) {
		XPanel leftPanel = new XPanel();
		leftPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																		VerticalLayout.VERT_CENTER, 10));
		
		leftPanel.add(new OneValueView(data, "random", this, randomTemplate));
		
			nextButton = new RepeatingButton(translate("Next value"), this);
		leftPanel.add(nextButton);
		
			resetButton = new XButton(translate("Reset"), this);
		leftPanel.add(resetButton);
		
		return leftPanel;
	}
	
	private XPanel mainPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20,20));
		
		thePanel.add("Center", dotPlotPanel(data));
		
		thePanel.add("East", catValuePanel(data));
		
		thePanel.add("South", controlPanel(data));
		
		return thePanel;
	}
	
	protected XPanel catValuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																		VerticalLayout.VERT_CENTER, 10));
			catView = new OneValueView(data, "randomCat", this);
		thePanel.add(catView);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		PseudoRandCatVariable catVar = (PseudoRandCatVariable)data.getVariable("randomCat");
		String cat1Label = ((LabelValue)catVar.getLabel(0)).label;
		probSlider = new ParameterSlider(new NumValue(0.0, 2), new NumValue(1.0, 2),
						new NumValue(catVar.getCumProbs()[0], 2), "P(" + cat1Label + ") ", this);
		thePanel.add("Center", probSlider);
		
		return thePanel;
	}
	
	private XPanel dotPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			VertAxis vertAxis = new VertAxis(this);
			vertAxis.readNumLabels("0 1 0.0 0.1");
		thePanel.add("Left", vertAxis);
		
			valueView = new RandomCatGeneratorView(data, this, vertAxis);
			valueView.setActiveNumVariable("random");
//			valueView.lockBackground(Color.white);
		thePanel.add("Center", valueView);
		return thePanel;
	}
	
	protected void resetCatVariable() {
		double newProb = probSlider.getParameter().toDouble();
		prob[0] = newProb;
		prob[1] = 1.0 - newProb;
		PseudoRandCatVariable catVar = (PseudoRandCatVariable)data.getVariable("randomCat");
		catVar.setProbs(prob);
		data.variableChanged("randomCat", catVar.noOfValues() - 1);
	}

	
	private boolean localAction(Object target) {
		if (target == nextButton) {
			U01RandomVariable rand = (U01RandomVariable)data.getVariable("random");
			rand.generateNext();
			data.valuesAdded(rand.noOfValues());
			resetCatVariable();
			
			return true;
		}
		else if (target == resetButton) {
			U01RandomVariable rand = (U01RandomVariable)data.getVariable("random");
			rand.resetGenerator();
			data.valuesAdded(rand.noOfValues());
			resetCatVariable();
			
			return true;
		}
		else if (target == probSlider) {
			resetCatVariable();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}