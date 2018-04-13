package exerciseCategProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import exercise2.*;


public class ContinDiscCatApplet extends ExerciseApplet {
	static final private int CONTINUOUS = 0;
	static final private int DISCRETE = 1;
	static final private int ORDINAL = 2;
	static final private int NOMINAL = 3;
	
	static final private String kTypeString[] = {"numerical continuous", "numerical discrete", "categorical ordinal", "categorical nominal"};
	
	private String varDescription[];
	private String shortVarName[];
	private String varMessage[];
	private int varType[];
	
	private int permutation[];
	
	private VariableTextPanel varNames[] = new VariableTextPanel[4];
	private XChoice choices[] = new XChoice[4];
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(0, 10));
			
				questionPanel = new QuestionPanel(this);
			topPanel.add("North", questionPanel);
			
			topPanel.add("Center", getWorkingPanels(null));
			
		add("North", topPanel);
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(0, 4));
			
			bottomPanel.add("North", createMarkingPanel(NO_HINTS));
			
				XPanel messagePanel = new XPanel();
				messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
					
					message = new ExerciseMessagePanel(this);
				messagePanel.add(message);
			bottomPanel.add("Center", messagePanel);
		
		add("Center", bottomPanel);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
//		registerParameter("index", "int");					//	always registered
		registerParameter("continVars", "string");
		registerParameter("discreteVars", "string");
		registerParameter("ordinalVars", "string");
		registerParameter("nominalVars", "string");
	}
	
	private String getContinVars() {
		return getStringParam("continVars");
	}
	
	private String getDiscreteVars() {
		return getStringParam("discreteVars");
	}
	
	private String getOrdinalVars() {
		return getStringParam("ordinalVars");
	}
	
	private String getNominalVars() {
		return getStringParam("nominalVars");
	}
	
	
//=================================================
	
class VariableTextPanel extends MessagePanel {
	private int varIndex;
	
	VariableTextPanel(ExerciseApplet exerciseApplet, int varIndex) {
		super(null, exerciseApplet, NO_SCROLL);
		this.varIndex = varIndex;
		changeContent();
	}
	
	protected void fillContent() {
		insertBoldText(shortVarName[permutation[varIndex]]);
		insertText("\n" + varDescription[permutation[varIndex]]);
	}
}
	
//=================================================
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		GridBagLayout gbl = new GridBagLayout();
		thePanel.setLayout(gbl);
		
			GridBagConstraints gbVarName = new GridBagConstraints();
			gbVarName.anchor = GridBagConstraints.CENTER;
			gbVarName.fill = GridBagConstraints.BOTH;
			gbVarName.gridheight = gbVarName.gridwidth = 1;
			gbVarName.gridx = 0;
			gbVarName.insets = new Insets(2,2,2,2);
			gbVarName.ipadx = gbVarName.ipady = 0;
			gbVarName.weightx = gbVarName.weighty = 1.0;
 
			GridBagConstraints gbChoice = new GridBagConstraints();
			gbChoice.anchor = GridBagConstraints.CENTER;
			gbChoice.fill = GridBagConstraints.NONE;
			gbChoice.gridheight = gbChoice.gridwidth = 1;
			gbChoice.gridx = 1;
			gbChoice.insets = new Insets(0,10,0,0);
			gbChoice.ipadx = gbChoice.ipady = 0;
			gbChoice.weightx = gbChoice.weighty = 0.0;
		
		for (int i=0 ; i<4 ; i++) {
			varNames[i] = new VariableTextPanel(this, i);
			thePanel.add(varNames[i]);
			gbVarName.gridy = i;
			gbl.setConstraints(varNames[i], gbVarName);
			
				choices[i] = new XChoice(this);
				choices[i].addItem("Numerical continuous");
				choices[i].addItem("Numerical discrete");
				choices[i].addItem("Categorical ordinal");
				choices[i].addItem("Categorical nominal");
			thePanel.add(choices[i]);
			gbChoice.gridy = i;
			gbl.setConstraints(choices[i], gbChoice);
			
			registerStatusItem("typeChoice" + i, choices[i]);
		}
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		for (int i=0 ; i<4 ; i++) {
			varNames[i].changeContent();
			varNames[i].setTextBackground(Color.white);
				
			choices[i].select(0);
		}
	}
	
	
	protected void setDataForQuestion() {
		StringTokenizer stContin = new StringTokenizer(getContinVars(), " ");
		StringTokenizer stDiscrete = new StringTokenizer(getDiscreteVars(), " ");
		StringTokenizer stOrdinal = new StringTokenizer(getOrdinalVars(), " ");
		StringTokenizer stNominal = new StringTokenizer(getNominalVars(), " ");
		
		int nVars = stContin.countTokens() + stDiscrete.countTokens() + stOrdinal.countTokens()
																																			+ stNominal.countTokens();
		shortVarName = new String[nVars];
		varDescription = new String[nVars];
		varMessage = new String[nVars];
		varType = new int[nVars];
		int index = addVars(stContin, CONTINUOUS, 0);
		index = addVars(stDiscrete, DISCRETE, index);
		index = addVars(stOrdinal, ORDINAL, index);
		index = addVars(stNominal, NOMINAL, index);
		
		permutation = createPermutation(nVars);
	}
	
	private int addVars(StringTokenizer st, int type, int index) {
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			s = s.replaceAll("_", " ");				//	since the editor replaces spaces with underscores in a string_array
			StringTokenizer st2 = new StringTokenizer(s, "*");
			shortVarName[index] = st2.nextToken();
			varDescription[index] = st2.nextToken();
			varMessage[index] = st2.nextToken();
			varType[index] = type;
			index ++;
		}
		return index;
	}
	
	protected DataSet getData() {
		return null;
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Use the pop-up menus to classify the variables.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer");
				for (int i=0 ; i<4 ; i++) {
					messagePanel.insertBoldBlueText("\n" + shortVarName[permutation[i]] + " is ");
					messagePanel.insertBoldBlueText(kTypeString[varType[permutation[i]]] + ": ");
					messagePanel.insertText(varMessage[permutation[i]]);
				}
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have chosen the correct variable types.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!");
				for (int i=0 ; i<4 ; i++) {
					boolean correct = varType[permutation[i]] == choices[i].getSelectedIndex();
					if (!correct) {
						messagePanel.insertBoldBlueText("\n" + shortVarName[permutation[i]] + ": ");
						messagePanel.insertText(varMessage[permutation[i]]);
					}
				}
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 150;
	}
	
//-----------------------------------------------------------
	
	public boolean noteChangedWorking() {
		boolean changed = super.noteChangedWorking();
		if (changed) {
			for (int i=0 ; i<4 ; i++)
				varNames[i].setTextBackground(Color.white);
		}
		return changed;
	}
	
	protected int assessAnswer() {
		for (int i=0 ; i<4 ; i++)
			if (varType[permutation[i]] != choices[i].getSelectedIndex())
				return ANS_WRONG;
		return ANS_CORRECT;
	}
	
	protected void giveFeedback() {
		for (int i=0 ; i<4 ; i++) {
			boolean correct = varType[permutation[i]] == choices[i].getSelectedIndex();
			varNames[i].setTextBackground(correct ? kCorrectAnswerBackground : kWrongAnswerBackground);
		}
	}
	
	protected void showCorrectWorking() {
		for (int i=0 ; i<4 ; i++) {
			choices[i].select(varType[permutation[i]]);
			varNames[i].setTextBackground(Color.white);
		}
	}
	
	protected double getMark() {
		int nWrong = 0;
		for (int i=0 ; i<4 ; i++)
			if (varType[permutation[i]] != choices[i].getSelectedIndex())
				nWrong ++;
		return (nWrong == 0) ? 1 : (nWrong == 1) ? 0.5 : 0;
	}
	
//-------------------------------------------------------------------------
	
	private boolean localAction(Object target) {
		for (int i=0 ; i<4 ; i++)
			if (target == choices[i]) {
				noteChangedWorking();
				return true;
			}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
	
}