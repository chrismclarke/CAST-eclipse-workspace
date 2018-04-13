package exerciseProbProg;

import java.awt.*;

import dataView.*;
import utils.*;
import exercise2.*;
import formula.*;


import exerciseNumGraph.*;
import exerciseProb.*;


public class CardDiceTableApplet extends ExerciseApplet {
	static final private int CARD_TYPE = 0;
	static final private int DICE_TYPE = 1;
	
	static final private int DICE_TOTAL_INEQUALITY = 0;
	static final private int DICE_AT_LEAST_ONE_EQUALS = 1;
	static final private int DICE_AT_LEAST_ONE_GREATER = 2;
	static final private int DICE_BOTH_EQUAL = 3;
	
	static final private int CARD_JQK = 0;
	static final private int CARD_JQKA = 1;
	static final private int CARD_KA = 2;
	static final private int CARD_EQUALS = 3;
	static final private int CARD_INEQUALITY = 4;
	
	static final private int GE_INEQUALITY = 0;
	static final private int LE_INEQUALITY = 1;
//	static final private int EQ_INEQUALITY = 2;
	
	static final private int CLUB = 0;
	static final private int HEART = 1;
	static final private int DIAMOND = 2;
	static final private int SPADE = 3;
	
	static final private NumValue kZero = new NumValue(0, 0);
	static final private NumValue kOne = new NumValue(1, 0);
	
	static final private Value[] kCardSuit = {new LabelValue(MText.expandText("#club#")), new LabelValue(MText.expandText("#heart#")),
																						new LabelValue(MText.expandText("#diamond#")), new LabelValue(MText.expandText("#spade#"))};
	
	static final private String[] kCardString = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
	static final private Value[] kCardValue = new Value[13];
	static {
		for (int i=0 ; i<13 ; i++)
			kCardValue[i] = new LabelValue(kCardString[i]);
	}
	
	static final private Value[] kDieFace = {new LabelValue("1"), new LabelValue("2"), new LabelValue("3"),
																					new LabelValue("4"), new LabelValue("5"), new LabelValue("6")};
	
	static final private Color kDarkBlue = new Color(0x000099);
	
	private CrossTableView tableView;
	private PropnTemplatePanel propnTemplate;
	private XLabel columnHeading;
	private XVertLabel rowHeading;
	
	private ResultValuePanel resultPanel;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 10));
			
				questionPanel = new QuestionPanel(this);
			topPanel.add(questionPanel);
			
			topPanel.add(getWorkingPanels(null));
			
				FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
				propnTemplate = new PropnTemplatePanel(translate("Proportion") + " =", stdContext);
				registerStatusItem("propnTemplate", propnTemplate);
			topPanel.add(propnTemplate);
			
				resultPanel = new ResultValuePanel(this, translate("Probability") + " =", 6);
				registerStatusItem("prob", resultPanel);
			topPanel.add(resultPanel);
			
		add("North", topPanel);
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(0, 4));
			
			bottomPanel.add("North", createMarkingPanel(NO_HINTS));
			
				message = new ExerciseMessagePanel(this);
			bottomPanel.add("Center", message);
		
		add("Center", bottomPanel);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		registerParameter("qnType", "choice");
		registerParameter("cardQnType", "choice");
		registerParameter("cardInequality", "string");
		registerParameter("cardAceType", "string");
		registerParameter("cardEquals", "string");
		
		registerParameter("dieQnType", "choice");
		registerParameter("dieInequality", "string");
		registerParameter("diceTotal", "int");
		registerParameter("dieEquals", "int");
		registerParameter("cardSuit", "int");
	}
	
	private int getQnType() {
		return getIntParam("qnType");
	}
	
	private int getQnVariation() {
		if (getObjectParam("cardQnType") != null)
			return getIntParam("cardQnType");
		else
			return getIntParam("dieQnType");
	}
	
	private int getDiceInequalityType() {
		String s = getStringParam("dieInequality");
		return s.equals("at least") ? 0 : s.equals("less than or equal to") ? 1 : 2;
	}
	
	private int getDiceTotal() {
		return getIntParam("diceTotal");
	}
	
	private int getDiceEquals() {
		return getIntParam("dieEquals");
	}
	
	private int getCardSuit() {
		return getIntParam("cardSuit");
	}
	
	private int getCardInequalityType() {
		return getStringParam("cardInequality").equals("higher") ? 0 : 1;
	}
	
	private boolean isAceHigh() {
		return getStringParam("cardAceType").equals("higher");
	}
	
	private int getCardEquals() {
		String card = getStringParam("cardEquals");
		for (int i=0 ; i<13 ; i++)
			if (kCardString[i].equals(card))
				return i;
		return -1; 
	}
	
//-----------------------------------------------------------
	
	private Value[] getRowLabels() {
		switch (getQnType()) {
			case CARD_TYPE:
				return kCardSuit;
			case DICE_TYPE:
			default:
				return kDieFace;
		}
	}
	
	private Value[] getColLabels() {
		switch (getQnType()) {
			case CARD_TYPE:
				return kCardValue;
			case DICE_TYPE:
			default:
				return kDieFace;
		}
	}
	
	private String getRowHeading() {
		switch (getQnType()) {
			case CARD_TYPE:
				return "Suit";
			case DICE_TYPE:
			default:
				return "2nd die";
		}
	}
	
	private String getColHeading() {
		switch (getQnType()) {
			case CARD_TYPE:
				return "Value";
			case DICE_TYPE:
			default:
				return "1st die";
		}
	}
	
	private Font getRowFont() {
		switch (getQnType()) {
			case CARD_TYPE:
				Font f = getBigBoldFont();
				return new Font(f.getName(), f.getStyle(), f.getSize() * 3 / 2);
			case DICE_TYPE:
			default:
				return getBigBoldFont();
		}
	}
	
	private Font getColFont() {
		return getBigBoldFont();
	}
	
	private Color getRowColor(int i) {
		switch (getQnType()) {
			case CARD_TYPE:
				return (i == 1 || i == 2) ? Color.red : Color.black;
			case DICE_TYPE:
			default:
				return kDarkBlue;
		}
	}
	
	private Color getColColor(int i) {
		return kDarkBlue;
	}
	
	private Color[] getRowColors() {
		int nRows = getRowLabels().length;
		Color c[] = new Color[nRows];
		for (int i=0 ; i<nRows ; i++)
			c[i] = getRowColor(i);
		return c;
	}
	
	private Color[] getColColors() {
		int nCols = getColLabels().length;
		Color c[] = new Color[nCols];
		for (int i=0 ; i<nCols ; i++)
			c[i] = getColColor(i);
		return c;
	}
	
	
//-----------------------------------------------------------

	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel innerPanel = new XPanel();
			GridBagLayout gbl = new GridBagLayout();
			innerPanel.setLayout(gbl);
			
			columnHeading = new XLabel("", XLabel.CENTER, this);
			columnHeading.setFont(getBigBoldFont());
			columnHeading.setForeground(kDarkBlue);
			rowHeading = new XVertLabel("", XLabel.CENTER, this);
			rowHeading.setFont(getBigBoldFont());
			rowHeading.setForeground(kDarkBlue);
			tableView = new CrossTableView(new DataSet(), this);
			tableView.setFont(getBigBoldFont());
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridheight = gbc.gridwidth = 1;
			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.insets = new Insets(0,30,0,0);
			gbc.ipadx = gbc.ipady = 0;
			gbc.weightx = gbc.weighty = 0.0;
			innerPanel.add(columnHeading);
			gbl.setConstraints(columnHeading, gbc);
			
			gbc.fill = GridBagConstraints.VERTICAL;
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.insets = new Insets(30,0,0,0);
			innerPanel.add(rowHeading);
			gbl.setConstraints(rowHeading, gbc);
			
			gbc.fill=GridBagConstraints.BOTH;
			gbc.gridx = 1;
			gbc.insets = new Insets(0,0,0,0);
			gbc.weightx = gbc.weighty = 1.0;
			innerPanel.add(tableView);
			gbl.setConstraints(tableView, gbc);
		
		thePanel.add(innerPanel);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		tableView.setRowColLabels(getRowLabels(), getColLabels(), getRowFont(), getColFont(),
																													getRowColors(), getColColors());
		tableView.setSelection(null);
		tableView.invalidate();
		tableView.repaint();
		
		columnHeading.setText(getColHeading());
		
		rowHeading.setText(getRowHeading());
		
		propnTemplate.setValues(kZero, kOne);
		
		resultPanel.clear();
	}
	
	protected DataSet getData() {
		return null;
	}
	
	protected void setDataForQuestion() {
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Find the probability and type it in the answer box.");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertText("You must type a probability in the answer box.");
				break;
			case ANS_INVALID:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("Probabilities must be between zero and one.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				int nSuccess = getNSuccess(getSuccesses());
				if (nSuccess == 1)
					messagePanel.insertText("The single cell ");
				else
					messagePanel.insertText("The " + getNSuccess(getSuccesses()) + " cells ");
				messagePanel.insertText("for which " + getSuccessString());
				if (nSuccess == 1)
					messagePanel.insertText(" is ");
				else
					messagePanel.insertText(" are ");
				messagePanel.insertText("coloured yellow.\nThe probability is the proportion of cells that are yellow.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have given the correct probability. (It is the proportion of cells for which " + getSuccessString() + ".)");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("First identify the cells for which " + getSuccessString() + ".\nThe probability is the proportion of such cells.");
				break;
		}
	}
	
	private String getSuccessString() {
		String result = "";
		switch (getQnType()) {
			case CARD_TYPE:
				switch(getCardSuit()) {
					case 0:		//	red
					case 1:
						result = "the suit is a diamond or heart and ";
						break;
					case 2:		//	black
					case 3:
						result = "the suit is a club or spade and ";
						break;
					case 4:
						result = "the suit is a club and ";
						break;
					case 5:
						result = "the suit is a heart and ";
						break;
					case 6:
						result = "the suit is a diamond and ";
						break;
					case 7:
						result = "the suit is a spade and ";
						break;
					default:
						result = "";
				}
				switch (getQnVariation()) {
					case CARD_JQK:
						result += "the card value is J, Q or K";
						break;
					case CARD_JQKA:
						result += "the card value is J, Q, K or A";
						break;
					case CARD_KA:
						result += "the card value is K or A";
						break;
					case CARD_EQUALS:
						result += "the card value is " + kCardString[getCardEquals()];
						break;
					case CARD_INEQUALITY:
						int inequalityType = getCardInequalityType();
						int critical = getCardEquals();
						boolean aceHigh = isAceHigh();
						boolean isSingle;
						if (inequalityType == GE_INEQUALITY)
							isSingle = (aceHigh && critical == 0) ||(!aceHigh && critical == 12);
						else
							isSingle = (aceHigh && critical == 1) ||(!aceHigh && critical == 0);
						
						if (isSingle) {
							result += "the card value is ";
							if (inequalityType == GE_INEQUALITY)
								if (aceHigh)
									result += kCardString[0];
								else
									result += kCardString[12];
							else
								if (aceHigh)
									result += kCardString[1];
								else
									result += kCardString[0];
						}
						else {
							result += "the card value is one of (";
							if (inequalityType == GE_INEQUALITY) {
								if (aceHigh) {
									if (critical == 0)
										critical = 1;
									for (int j=critical ; j<13 ; j++)
										result += kCardString[j] + ", ";
									result += "or " + kCardString[0];
								}
								else {
									for (int j=critical ; j<12 ; j++)
										result += kCardString[j] + ", ";
									result += "or " + kCardString[12];
								}
							}
							else {				//	inequalityType == LE_INEQUALITY
								if (aceHigh) {
									if (critical == 0)
										critical = 13;
									for (int j=1 ; j<critical-1 ; j++)
										result += kCardString[j] + ", ";
									result += "or " + ((critical < 13) ? kCardString[critical] : kCardString[0]);
								}
								else {
									for (int j=0 ; j<critical ; j++)
										result += kCardString[j] + ", ";
									result += "or " + kCardString[critical];
								}
							}
							result += ")";
						}
						break;
				}
				break;
			case DICE_TYPE:
				switch (getQnVariation()) {
					case DICE_TOTAL_INEQUALITY:
						result = "the sum of the dice is ";
//						int inequalityType = getDiceInequalityType();
						switch (getDiceInequalityType()) {
							case GE_INEQUALITY:
								result += "greater than or equal to ";
								break;
							case LE_INEQUALITY:
								result += "less than or equal to ";
								break;
							default:
								result += "equal to ";
								break;
						}
						result += getDiceTotal();
						break;
					case DICE_AT_LEAST_ONE_EQUALS:
						result = "at least one of the dice has a value that equals " + getDiceEquals();
						break;
					case DICE_AT_LEAST_ONE_GREATER:
						result = "at least one of the dice is greater than " + getDiceEquals();
						break;
					case DICE_BOTH_EQUAL:
						result = "both dice have the same value";
						break;
				}
				break;
		}
		return result;
	}
	
	protected int getMessageHeight() {
		return 100;
	}
	
//-----------------------------------------------------------
	
	private double getAttempt() {
		return resultPanel.getAttempt().toDouble();
	}
	
	private double getCorrectPropn() {
		boolean[][] succ = getSuccesses();
		return getNSuccess(succ) / (double)getPopnSize(succ);
	}
	
	private int getNSuccess(boolean[][] succ) {
		int nSucc = 0;
		for (int i=0 ; i<succ.length ; i++)
			for (int j=0 ; j<succ[i].length ; j++)
				if (succ[i][j])
					nSucc ++;
		return nSucc;
	}
	
	private int getPopnSize(boolean[][] succ) {
		return succ.length * succ[0].length;
	}
	
	private boolean[][] getSuccesses() {
		boolean[][] succ = null;
		switch (getQnType()) {
			case CARD_TYPE:
				boolean suit[] = new boolean[4];
				int suitChoice = getCardSuit();
				suit[CLUB] = suitChoice == 2 || suitChoice == 3 || suitChoice == 4 || suitChoice > 7;
				suit[HEART] = suitChoice == 0 || suitChoice == 1 || suitChoice == 5 || suitChoice > 7;
				suit[DIAMOND] = suitChoice == 0 || suitChoice == 1 || suitChoice == 6 || suitChoice > 7;
				suit[SPADE] = suitChoice == 2 || suitChoice == 3 || suitChoice >= 7;
				
				succ = new boolean[4][13];
				for (int i=0 ; i<4 ; i++)
					if (suit[i])
						switch (getQnVariation()) {
							case CARD_JQK:
								for (int j=10 ; j<13 ; j++)
									succ[i][j] = true;		//	face cards
								break;
							case CARD_JQKA:
								for (int j=10 ; j<13 ; j++)
									succ[i][j] = true;		//	face cards
								succ[i][0] = true;
								break;
							case CARD_KA:
								succ[i][0] = succ[i][12] = true;
								break;
							case CARD_EQUALS:
								succ[i][getCardEquals()] = true;
								break;
							case CARD_INEQUALITY:
								int inequalityType = getCardInequalityType();
								int critical = getCardEquals();
								boolean aceHigh = isAceHigh();
								if (inequalityType == GE_INEQUALITY) {
									if (critical > 0 || !aceHigh)
										for (int j=critical ; j<13 ; j++)
											succ[i][j] = true;
									if (aceHigh)
										succ[i][0] = true;
								}
								else {				//	inequalityType == LE_INEQUALITY
									if (aceHigh && critical == 0) {
										succ[i][0] = true;
										critical = 12;
									}
									for (int j=critical ; j>0 ; j--)
										succ[i][j] = true;
									if (!aceHigh)
										succ[i][0] = true;
								}
								break;
						}
				break;
			case DICE_TYPE:
				succ = new boolean[6][6];
				switch (getQnVariation()) {
					case DICE_TOTAL_INEQUALITY:
						{
						int inequalityType = getDiceInequalityType();
						int critical = getDiceTotal();
						for (int i=0 ; i<6 ; i++)
							for (int j=0 ; j<6 ; j++)
								succ[i][j] = (inequalityType == GE_INEQUALITY) ? (i + j + 2 >= critical)
															: (inequalityType == LE_INEQUALITY) ? (i + j + 2 <= critical)
															: (i + j + 2 == critical);
						}
						break;
					case DICE_AT_LEAST_ONE_EQUALS:
						{
						int critical = getDiceEquals();
						for (int i=0 ; i<6 ; i++)
							for (int j=0 ; j<6 ; j++)
								succ[i][j] = (i + 1 == critical) || (j + 1 == critical);
						}
						break;
					case DICE_AT_LEAST_ONE_GREATER:
						{
						int critical = getDiceEquals();
						for (int i=0 ; i<6 ; i++)
							for (int j=0 ; j<6 ; j++)
								succ[i][j] = (i + 1 > critical) || (j + 1 > critical);
						}
						break;
					case DICE_BOTH_EQUAL:
						for (int i=0 ; i<6 ; i++)
							for (int j=0 ; j<6 ; j++)
								succ[i][j] = i == j;
						break;
				}
				break;
		}
		return succ;
	}
	
	public boolean noteChangedWorking() {
		boolean changed = super.noteChangedWorking();
		if (changed) {
			tableView.setSelection(null);
			tableView.repaint();
		}
		return changed;
	}
	
	protected int assessAnswer() {
		double correct = getCorrectPropn();
		double attempt = getAttempt();
		
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		else if (attempt < 0.0 || attempt > 1.0)
			return ANS_INVALID;
		else
			return (Math.abs(correct - attempt) < 0.001) ? ANS_CORRECT : ANS_WRONG;
	}
	
	protected void giveFeedback() {
		if (result == ANS_CORRECT || result == ANS_WRONG) {
			tableView.setSelection(getSuccesses());
			tableView.repaint();
		}
	}
	
	protected void showCorrectWorking() {
		boolean[][] succ = getSuccesses();
		tableView.setSelection(succ);
		tableView.repaint();
		
		NumValue numer = new NumValue(getNSuccess(succ), 0);
		NumValue denom = new NumValue(getPopnSize(succ), 0);
		propnTemplate.setValues(numer, denom);
		
		resultPanel.showAnswer(propnTemplate.getResult());
	}
	
	protected double getMark() {
		return (assessAnswer() == ANS_CORRECT) ? 1 : 0;
	}
	
}