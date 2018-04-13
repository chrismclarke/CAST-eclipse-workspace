package samplingProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;

import sampling.*;


public class SimRandomisation2Applet extends SimRandomisationApplet {
	
	private XButton updateButton;
	private TreatGeneratorView generatorView;
	private int nextIndex = 0;
	
	public void setupApplet() {
		TreatmentImages.loadTreatments(this);
		
		data = readData();
		readTreatCounts(data);
		
		setLayout(new ProportionLayout(0.4, 10));
		
		add(ProportionLayout.LEFT, generatorPanel(data));
		add(ProportionLayout.RIGHT, treatmentPanel(data));
	}
//---------------------------------------------------------------------

	
	private XPanel treatmentPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				generateButton = new XButton(translate("Generate next"), this);
			buttonPanel.add(generateButton);
		thePanel.add("North", buttonPanel);
		
			theView = new TreatmentPictView(data, this, 1, popnSeed, 0, rows, cols, rowCycle, maxHorizOffset,
																																maxVertOffset, "treat");
			theView.setActiveCatVariable("y");
			theView.setDrawIndices(true);
			theView.doInitialisation(this);
		
		thePanel.add("Center", theView);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(0, 10));
		
				message = new SimpleTextArea(1);
				message.lockBackground(Color.white);
				message.setFont(getBigBoldFont());
			bottomPanel.add("North", message);
				
				XPanel resetPanel = new XPanel();
				resetPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
					resetButton = new XButton(translate("Clear selection"), this);
				resetPanel.add(resetButton);
			bottomPanel.add("Center", resetPanel);
		
		thePanel.add("South", bottomPanel);
		
		return thePanel;
	}

	
	private XPanel generatorPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				updateButton = new XButton(translate("Update probs"), this);
				updateButton.disable();
			buttonPanel.add(updateButton);
		thePanel.add("North", buttonPanel);
		
		thePanel.add("Center", probPanel(data));
		
			XPanel digitsPanel = new XPanel();
			digitsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				theDigits = new RandomDigitsPanel(this, this, RandomDigitsPanel.DECIMALS,
													getParameter(RANDOM_SEED_PARAM), RandomDigitsPanel.SMALL_DIGITS, 9);
			digitsPanel.add(theDigits);
		thePanel.add("South", digitsPanel);
		
		return thePanel;
	}
	
	private XPanel probPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			VertAxis vertAxis = new VertAxis(this);
			vertAxis.readNumLabels("0 1 0.0 0.1");
		thePanel.add("Left", vertAxis);
		
			generatorView = new TreatGeneratorView(data, this, vertAxis, treatCounts, TreatmentImages.treatImage);
		generatorView.setFont(getBigBoldFont());
		thePanel.add("Center", generatorView);
		
		return thePanel;
	}

//----------------------------------------------------------------
	
	public void noteNewValue(RandomDigitsPanel valuePanel) {
		double prob = valuePanel.getDecimalValue();
		int newCat = generatorView.generateRandomCat(prob);
		
		CatVariable treatVar = (CatVariable)data.getVariable("treat");
		Value newTreatLabel = treatVar.getLabel(newCat);
		
		((TreatmentPictView)theView).setNextTreatmentLabel(newCat);
		theView.addToSample(nextIndex);
		nextIndex ++;
		
		message.lockBackground(kPaleGreen);
		message.setText("Item " + nextIndex + " gets treatment " + newTreatLabel.toString());
		
		if (nextIndex < treatVar.noOfValues())
			updateButton.enable();
	}
	
	protected void clearSample() {
		message.lockBackground(Color.white);
		message.setText("");
		
		generatorView.updateProbs();
		
		theView.clearSample();
		generateButton.enable();
		updateButton.disable();
		
		nextIndex = 0;
	}

	
	private boolean localAction(Object target) {
		if (target == updateButton) {
			message.lockBackground(Color.white);
			message.setText("");
			theView.clearHighlight();
			generatorView.updateProbs();
			
			updateButton.disable();
			generateButton.enable();
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