package simulationProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import valueList.*;

import simulation.*;


public class RandomCat2Applet extends RandomCatApplet {
	static final private String MODEL_COUNT_PARAM = "noOfModels";
	static final private String MODEL_NAME_PARAM = "modelName";
	static final private String MODEL_LABELS_PARAM = "modelLabels";
	static final private String MODEL_PROBS_PARAM = "modelProbs";
	static final private String MAX_CAT_PARAM = "maxCats";
	
	private XChoice modelChoice;
	private int modelIndex = 0;
	
	private XLabel catValueLabel;
	private OneValueView catValueView;
	
	private double[][] modelProbs;
	private String[] modelLabels;
	private String[] modelName;
	
	protected PseudoRandCatVariable createCatVariable(DataSet data) {
		int noOfModels = Integer.parseInt(getParameter(MODEL_COUNT_PARAM));
		modelProbs = new double[noOfModels][];
		modelLabels = new String[noOfModels];
		modelName = new String[noOfModels];
		for (int i=0 ; i<noOfModels ; i++) {
			modelLabels[i] = getParameter(MODEL_LABELS_PARAM + (i+1));
			modelName[i] = getParameter(MODEL_NAME_PARAM + (i+1));
			StringTokenizer st = new StringTokenizer(getParameter(MODEL_PROBS_PARAM + (i+1)));
			int noOfCategories = st.countTokens();
			modelProbs[i] = new double[noOfCategories];
			for (int j=0 ; j<noOfCategories ; j++)
				modelProbs[i][j] = Double.parseDouble(st.nextToken());
		}
		
		PseudoRandCatVariable catVar = new PseudoRandCatVariable(getParameter(MODEL_NAME_PARAM + "1"),
																				data, "random", modelProbs[0]);
		catVar.readLabels(modelLabels[0]);
		return catVar;
	}
	
	protected XPanel catValuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 20));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(0, 0));
				 XLabel keyLabel = new XLabel("Key", XLabel.LEFT, this);
				 keyLabel.setFont(getStandardBoldFont());
			topPanel.add("North", keyLabel);
				StringTokenizer st = new StringTokenizer(getParameter(MAX_CAT_PARAM), "*");
				int maxLabels = Integer.parseInt(st.nextToken());
				String maxCatLabel = st.nextToken();
				CatKeyProbView catKey = new CatKeyProbView(data, this, "randomCat", maxCatLabel, maxLabels);
//				catKey.lockBackground(Color.yellow);
			topPanel.add("Center", catKey);
		thePanel.add("North", topPanel);
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL,
																		VerticalLayout.VERT_CENTER, 0));
				catValueLabel = new XLabel(modelName[0], XLabel.LEFT, this);
				catValueLabel.setFont(getBigBoldFont());
			bottomPanel.add(catValueLabel);
			
				XPanel valuePanel = new XPanel();
				valuePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				
					catValueView = new OneValueView(data, "randomCat", this);
					catValueView.setNameDraw(false);
					catValueView.setFont(getBigBoldFont());
				valuePanel.add(catValueView);
			bottomPanel.add(valuePanel);
		
		thePanel.add("Center", bottomPanel);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		
		thePanel.add(new XLabel(translate("Model") + ":", XLabel.LEFT, this));
		
		modelChoice = new XChoice(this);
		for (int i=0 ; i<modelName.length ; i++)
			modelChoice.addItem(modelName[i]);
		thePanel.add(modelChoice);
		
		return thePanel;
	}
	
	protected void resetCatVariable() {
		PseudoRandCatVariable catVar = (PseudoRandCatVariable)data.getVariable("randomCat");
		catVar.setProbs(modelProbs[modelIndex]);
		data.variableChanged("randomCat", catVar.noOfValues() - 1);
	}

	
	private boolean localAction(Object target) {
		if (target == modelChoice) {
			int newSelection = modelChoice.getSelectedIndex();
			if (modelIndex != newSelection) {
				modelIndex = newSelection;
				catValueLabel.setText(modelName[modelIndex]);
				PseudoRandCatVariable catVar = (PseudoRandCatVariable)data.getVariable("randomCat");
				catVar.name = modelName[modelIndex];
				catVar.readLabels(modelLabels[modelIndex]);
				catValueView.reset(null);
				validate();
				resetCatVariable();
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else 
			return localAction(evt.target);
	}
}