package experProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import exper.*;


public class PairedTreatmentsApplet extends CoreMultiFactorApplet {
	
	static final private String SEED_INFO_PARAM = "randomSeeds";
	static final private String PAIRED_TEXT_PARAM = "pairedText";
	
	private long popnRandomSeed;
	private Random permGenerator;
	
	private XButton randomiseButton;
	private XChoice schemeChoice;
	private int currentSchemeChoice;
	
	private PairedPictView designView;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(10, 10));
		
		add("Center", displayPanel(data));
		add("South", controlPanel());
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
			CatVariable treat1Var = (CatVariable)data.getVariable("treat1");
		
		StringTokenizer popnTok = new StringTokenizer(getParameter(SEED_INFO_PARAM));
		long genderSeed = Long.parseLong(popnTok.nextToken());
		popnRandomSeed = Long.parseLong(popnTok.nextToken());
		long randomisationSeed = Long.parseLong(popnTok.nextToken());
		permGenerator = new Random(randomisationSeed);
		
			CatVariable genderVar = new CatVariable(getParameter(CAT_NAME_PARAM));
			genderVar.readLabels(getParameter(CAT_LABELS_PARAM));
			
			Random genderGenerator = new Random(genderSeed);
			int popnSize = treat1Var.noOfValues();
			int values[] = new int[popnSize];
			for (int i=0 ; i<popnSize ; i++)
				values[i] = (genderGenerator.nextDouble() <= 0.5) ? 0 : 1;
			genderVar.setValues(values);
		
		data.addVariable("gender", genderVar);
		
			RandomisedNumVariable permVar = new RandomisedNumVariable("perm");
			double perm[] = new double[popnSize];
			for (int i=0 ; i<popnSize ; i++)
				perm[i] = i;
			permVar.setValues(perm);
			permVar.generateNextSample();
		data.addVariable("perm", permVar);
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			CatVariable treat = (CatVariable)data.getVariable("treat2");
			XLabel treatmentLabel = new XLabel(treat.name, XLabel.LEFT, this);
			treatmentLabel.setFont(getBigBoldFont());
		thePanel.add("North", treatmentLabel);
		
			designView = new PairedPictView(data, this, popnRandomSeed, "perm", "treat2", "treat1");
			designView.doInitialisation(this);
			designView.setFont(getBigBoldFont());
			designView.setActiveCatVariable("gender");
		
		thePanel.add("Center", designView);
		return thePanel;
	}
	
	protected XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			randomiseButton = new XButton(translate("Randomise"), this);
		thePanel.add(randomiseButton);
		
			schemeChoice = new XChoice(this);
			schemeChoice.addItem(translate("Completely randomised"));
				
				String pairedTextString = getParameter(PAIRED_TEXT_PARAM);
				if (pairedTextString == null)
					pairedTextString = translate("Paired");
			schemeChoice.addItem(pairedTextString);
			currentSchemeChoice = 0;
		thePanel.add(schemeChoice);
		
		return thePanel;
	}
	
	public void finishAnimation() {
		randomiseButton.enable();
	}
	
	private void doRandomisation() {
		randomiseButton.disable();
		
		RandomisedNumVariable perm = (RandomisedNumVariable)data.getVariable("perm");
		if (currentSchemeChoice == 0)
			perm.generateNextSample();
		else {
			int nBlocks = perm.noOfValues() / 2;
			for (int i=0 ; i<nBlocks ; i++) {
				int zz = permGenerator.nextDouble() > 0.5 ? 1 : 0;
				((NumValue)perm.valueAt(i)).setValue(i + nBlocks * zz);
				((NumValue)perm.valueAt(nBlocks + i)).setValue(i + nBlocks * (1 - zz));
			}
		}
		designView.doSampleAnimation();
	}
	
	private boolean localAction(Object target) {
		if (target == randomiseButton) {
			doRandomisation();
			return true;
		}
		else if (target == schemeChoice) {
			if (schemeChoice.getSelectedIndex() != currentSchemeChoice) {
				currentSchemeChoice = schemeChoice.getSelectedIndex();
				doRandomisation();
				designView.setShowingBlocks(currentSchemeChoice == 1);
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