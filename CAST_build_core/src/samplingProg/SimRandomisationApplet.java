package samplingProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import imageUtils.*;

import cat.*;
import sampling.*;


public class SimRandomisationApplet extends SimulateSampleApplet {
	static final private String TREAT_NAME_PARAM = "treatName";
	static final private String TREAT_LABELS_PARAM = "treatLabels";
	static final private String TREAT_COUNTS_PARAM = "treatCounts";
	
	protected int treatCounts[];
	private int nextTreatment, remaining;
	
	private ImageSwapCanvas nextTreatCanvas;
	
	public void setupApplet() {
		TreatmentImages.loadTreatments(this);
		
		data = readData();
		readTreatCounts(data);
		
		setLayout(new BorderLayout(20, 0));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new BorderLayout(0, 3));
			
			mainPanel.add("Center", displayPanel(data));
			
				message = new SimpleTextArea(1);
				message.lockBackground(Color.white);
				message.setBorders(6, 5);
				message.setFont(getBigBoldFont());
			mainPanel.add("South", message);
			
		add("Center", mainPanel);
		
		add("East", controlPanel());
	}
	
	protected void readTreatCounts(DataSet data) {
		CatVariable treatVar = (CatVariable)data.getVariable("treat");
		int nTreats = treatVar.noOfCategories();
		treatCounts = new int[nTreats];
		StringTokenizer st = new StringTokenizer(getParameter(TREAT_COUNTS_PARAM));
		int nextIndex = 1;
		while (st.hasMoreTokens()) {
			treatCounts[nextIndex] = Integer.parseInt(st.nextToken());
			nextIndex ++;
		}
		nextTreatment = 1;
		remaining = treatCounts[1];
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		CatVariable tr = new CatVariable(getParameter(TREAT_NAME_PARAM));
		tr.readLabels(getParameter(TREAT_LABELS_PARAM));
		
		int popnSize = rows * cols;
		int values[] = new int[popnSize];
		for (int i=0 ; i<popnSize ; i++)
			values[i] = 0;
		tr.setValues(values);
		
		data.addVariable("treat", tr);
		
		return data;
	}

//---------------------------------------------------------------------

	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			theView = new TreatmentPictView(data, this, 1, popnSeed, 0, rows, cols,
																				rowCycle, maxHorizOffset, maxVertOffset, "treat");
			theView.setActiveCatVariable("y");
			theView.setDrawIndices(true);
			theView.doInitialisation(this);
		
		thePanel.add("Center", theView);
		return thePanel;
	}

	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_SPACED, 10));
		
		thePanel.add(countsPanel());
		
		thePanel.add(nextTreatPanel());
		
			XPanel generatePanel = new XPanel();
			generatePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
			
				generateButton = new XButton(translate("Random index"), this);
			generatePanel.add(generateButton);
		
				theDigits = new RandomDigitsPanel(this, this, RandomDigitsPanel.NO_DECIMALS,
													getParameter(RANDOM_SEED_PARAM), RandomDigitsPanel.SMALL_DIGITS, maxSigDigit());
			generatePanel.add(theDigits);
			
		thePanel.add(generatePanel);
		
			resetButton = new XButton(translate("Clear selection"), this);
		thePanel.add(resetButton);
		
		return thePanel;
	}
	
	private XPanel nextTreatPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 0));
		
			XPanel labelPanel = new XPanel();
			labelPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				
				XLabel nextLabel = new XLabel(translate("Next") + " " + getParameter(TREAT_NAME_PARAM) + ": ", XLabel.RIGHT, this);
				nextLabel.setFont(getStandardBoldFont());
			labelPanel.add(nextLabel);
		thePanel.add(labelPanel);
		
			XPanel treatPanel = new XPanel();
			treatPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				
				nextTreatCanvas = new ImageSwapCanvas(TreatmentImages.treatImage, TreatmentImages.kWidth, TreatmentImages.kHeight, this);
				nextTreatCanvas.showVersion(0);
			treatPanel.add(nextTreatCanvas);
		thePanel.add(treatPanel);
			
		return thePanel;
	}
	
	private XPanel countsPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		FreqTableView freqView = new FreqTableView(data, this, "treat", FreqTableView.NO_DRAG, 3,
											FreqTableView.SHORT_HEADINGS, FreqTableView.NO_RELFREQ, translate("Count"), false);
		thePanel.add(freqView);
		return thePanel;
	}
	
	private int maxSigDigit() {
		int digit = rows * cols - 1;
		while (digit > 9)
			digit /= 10;
		return digit;
	}

//----------------------------------------------------------------
	
	public void noteNewValue(RandomDigitsPanel valuePanel) {
		int newIndex = valuePanel.getValue();
		if (newIndex >= rows * cols) {
			message.lockBackground(kPaleRed);
			message.setText(translate("Ignored: no item has index") + " " + newIndex);
		}
		else {
			boolean successful = theView.addToSample(newIndex);
			if (successful) {
				CatVariable treatVar = (CatVariable)data.getVariable("treat");
				message.lockBackground(kPaleGreen);
				Value nextTreatLabel = treatVar.getLabel(nextTreatment);
				StringTokenizer st = new StringTokenizer(translate("Item * gets treatment"), "*");
				message.setText(st.nextToken() + newIndex + st.nextToken() + " " + nextTreatLabel.toString());
				remaining --;
				if (remaining <= 0) {
					if (nextTreatment < treatCounts.length - 2) {
						nextTreatment ++;
						remaining = treatCounts[nextTreatment];
						((TreatmentPictView)theView).setNextTreatmentLabel(nextTreatment);
						nextTreatCanvas.showVersion(nextTreatment - 1);
					}
					else {
						finaliseRandomisation(treatVar);
						generateButton.disable();
						nextTreatCanvas.showVersion(-1);
						return;
					}
				}
			}
			else {
				message.lockBackground(kPaleOrange);
				StringTokenizer st = new StringTokenizer(translate("Ignored: item * already has treatment"));
				message.setText(st.nextToken() + newIndex + st.nextToken());
			}
		}
		generateButton.enable();
	}
	
	private void finaliseRandomisation(CatVariable treatVar) {
		Value noTreatLabel = treatVar.getLabel(0);
		Value lastTreatLabel = treatVar.getLabel(treatVar.noOfCategories() - 1);
		for (int i=0 ; i<treatVar.noOfValues() ; i++)
			if (treatVar.valueAt(i) == noTreatLabel)
				treatVar.setValueAt(lastTreatLabel, i);
	}
	
	protected void clearSample() {
		super.clearSample();
		nextTreatment = 1;
		remaining = treatCounts[1];
		generateButton.enable();
		nextTreatCanvas.showVersion(0);
	}
}