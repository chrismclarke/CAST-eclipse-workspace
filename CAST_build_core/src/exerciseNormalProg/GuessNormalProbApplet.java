package exerciseNormalProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import exercise2.*;

import exerciseNormal.*;


public class GuessNormalProbApplet extends MeanSDExerciseApplet implements Interval123Constants {
//	static final private String VAR_NAME_PARAM = "varName";
	
	static final private int kProbIndex[] = {10, 9, 7, 5, 3, 1, 0,
																								0, 1, 3, 5, 7, 9, 10,
																								4, 2, 0, 6, 8, 10};
	
	static final private String kProbChoiceString[] = {"Almost certain", "0.975", "0.95", "0.85", "0.70", "0.50", "0.30", "0.15", "0.05", "0.025", "Almost impossible"};
	
	static final private Color kAnswerBackground = new Color(0xEEEEDD);
	
//	private String varName[];
	
	private HorizAxis theAxis;
	private Pdf123View theView;
	
	private XPanel pdfPanel;
	private CardLayout pdfPanelLayout;
	
	private XChoice probChoice;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
		add("Center", getWorkingPanels(data));
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			bottomPanel.add(createMarkingPanel(hasOption("hints") ? ALLOW_HINTS : NO_HINTS));
			
				XPanel messagePanel = new XPanel();
				messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
					
					message = new ExerciseMessagePanel(this);
				messagePanel.add(message);
			bottomPanel.add(messagePanel);
		
		add("South", bottomPanel);
	}
	
//-----------------------------------------------------------
	
/*
	protected void readQuestions() {
		super.readQuestions();
		int nQuestions = question.length;
		varName = new String[nQuestions];
		
		for (int i=0 ; i<nQuestions ; i++)
			varName[i] = getParameter(VAR_NAME_PARAM + i);
	}
	
	public String getVarName() {
		return varName[questionVersion];
	}
*/
	
	protected void registerParameterTypes() {
		registerParameter("varName", "string");
		registerParameter("jointMeanSD", "jointMeanSD");
		registerParameter("mean", "mean");
		registerParameter("sd", "sd");
		registerParameter("axis", "string");
		registerParameter("interType", "int");
		registerParameter("interval", "interval");
	}
	
	protected Object expandQuestionToken(String type, String paramString, Object oldParam) {
		if (type.equals("interval")) {
			NumValue meanVal = getMean();
			NumValue sdVal = getSD();
			double mean = meanVal.toDouble();
			double sd = sdVal.toDouble();
			NumValue tempVal = new NumValue(0.0, Math.max(meanVal.decimals, sdVal.decimals));
			int intervalType = intervalType();
			String result;
			if (intervalType <= LESS_THAN_3) {
				tempVal.setValue(mean + sd * (intervalType - 3));
				result = "less than " + tempVal;
			}
			else if (intervalType <= GREATER_THAN_3) {
				tempVal.setValue(mean + sd * (intervalType - GREATER_THAN_MINUS3 - 3));
				result = "greater than " + tempVal;
			}
			else {
				result = (intervalType <= BETWEEN_3) ? "between " : "outside the range ";
				int factor = (intervalType <= BETWEEN_3) ? (intervalType - BETWEEN_1 + 1) : (intervalType - OUTSIDE_1 + 1);
				tempVal.setValue(mean - sd * factor);
				result += tempVal;
				result += (intervalType <= BETWEEN_3) ? " and " : " to ";
				tempVal.setValue(mean + sd * factor);
				result += tempVal;
			}
			return result;
		}
		
		return super.expandQuestionToken(type, paramString, oldParam);
	}
	
	public String getVarName() {
		return getStringParam("varName");
	}
	
	protected NumValue getMean() {
		return ((MeanSD)getObjectParam("jointMeanSD")).getMean();
	}
	
	protected NumValue getSD() {
		return ((MeanSD)getObjectParam("jointMeanSD")).getSD();
	}
	
	public String getAxisInfo() {
		return getStringParam("axis");
	}
	
	public int getDecimals() {
		return ((MeanSD)getObjectParam("jointMeanSD")).getDecimals();
	}
	
	protected int intervalType() {
		return getIntParam("interType");
	}
	
//-----------------------------------------------------------
	
	
	protected DataSet getData() {
		DataSet data = new DataSet();
			NormalDistnVariable normalDistn = new NormalDistnVariable("Distn");
		data.addVariable("distn", normalDistn);
		return data;
	}
	
//-------------------------------------------------------------------------
	
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			pdfPanel = new XPanel();
			pdfPanelLayout = new CardLayout();
			pdfPanel.setLayout(pdfPanelLayout);
			
			pdfPanel.add("blank", blankPanel());
			
			pdfPanel.add("pdf", normalPdfPanel(data));
			
		thePanel.add("Center", pdfPanel);
		
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				XPanel innerPanel = new InsetPanel(20, 5);
				innerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					probChoice = new XChoice(translate("Probability") + " =", XChoice.HORIZONTAL, this);
					for (int i=0 ; i<kProbChoiceString.length ; i++)
						probChoice.addItem(kProbChoiceString[i]);
					registerStatusItem("probChoice", probChoice);
					
				innerPanel.add(probChoice);
			
				innerPanel.lockBackground(kAnswerBackground);
			choicePanel.add(innerPanel);
		
		thePanel.add("South", choicePanel);
		return thePanel;
	}
	
	private XPanel blankPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0,0));
		
//		if (hasOption("hints"))
			thePanel.add("Center",
						new XPanel() {
							private LabelValue kDefaultMessage[] = {new LabelValue("Answer the question by"),
																																		new LabelValue("selecting a probability from"),
																																		new LabelValue("the pop-up menu below.")};
							private Color kDefaultMessageColor = new Color(0x660000);
							private Color kDefaultMessageBackground = new Color(0xCCFFFF);
							private int kLineGap = 2;
							private int kTopBottomBorder = 6;
							private int kLeftRightBorder = 10;
							
							public void paint(Graphics g) {
								super.paint(g);
								
								g.setFont(getBigBoldFont());
								
								FontMetrics fm = g.getFontMetrics();
								int ascent = fm.getAscent();
								int descent = fm.getDescent();
								int lineHt = ascent + descent + kLineGap;
								
								int textWidth = 0;
								int nLines = kDefaultMessage.length;
								for (int i=0 ; i<nLines ; i++)
									textWidth = Math.max(textWidth, kDefaultMessage[i].stringWidth(g));
								
								int boxHeight = 2 * kTopBottomBorder + nLines * lineHt - kLineGap;
								int boxWidth = 2 * kLeftRightBorder + textWidth;
								int boxTop = (getSize().height - boxHeight) / 2;
								int boxLeft = (getSize().width - boxWidth) / 2;
								
								g.setColor(kDefaultMessageBackground);
								g.fillRect(boxLeft, boxTop, boxWidth, boxHeight);
								g.setColor(kDefaultMessageColor);
								g.drawRect(boxLeft, boxTop, boxWidth, boxHeight);
								
								int baseline = boxTop + kTopBottomBorder + ascent;
								int centre = getSize().width / 2;
								for (int i=0 ; i<nLines ; i++) {
									kDefaultMessage[i].drawCentred(g, centre, baseline);
									baseline += lineHt;
								}
							}
			 			});
		
		return thePanel;
	}
	
	private XPanel normalPdfPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			theAxis = new HorizAxis(this);
			theAxis.readNumLabels(getAxisInfo());
			theAxis.setAxisName(getVarName());
		thePanel.add("Bottom", theAxis);
			
			theView = new Pdf123View(data, this, theAxis, "distn");
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		theAxis.readNumLabels(getAxisInfo());
		theAxis.setAxisName(getVarName());
		theAxis.invalidate();
		
		theView.reset();
		
		if (!hasHints)
			pdfPanelLayout.show(pdfPanel, "blank");
		
		probChoice.select(0);
	}
	
	protected void setDataForQuestion() {
		NormalDistnVariable normalDistn = (NormalDistnVariable)data.getVariable("distn");
		double mean = getMean().toDouble();
		double sd = getSD().toDouble();
		normalDistn.setMean(mean);
		normalDistn.setSD(sd);
	}
	
//-------------------------------------------------------------------------
	
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Use the pop-up menu to select the correct probability.");
				if (hasHints)
					messagePanel.insertText("\nThe lines at #sigma#, 2#sigma# and 3#sigma# from #mu# on the above normal curve should help.");
				else
					messagePanel.insertText("\nIt might help to sketch the normal curve on paper first.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The probability equals the blue area under the normal curve.");
				break;
			case ANS_CORRECT:
				messagePanel.insertHeading("Correct!\n");
				messagePanel.insertText("The probability equals the blue area under the normal curve.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Not close enough!\n");
				messagePanel.insertText("The lines at #sigma#, 2#sigma# and 3#sigma# from #mu# on the above normal curve should help you to find the probability.");
				break;
		}
	}
	
	
	protected int getMessageHeight() {
		return 100;
	}
	
//-------------------------------------------------------------------------
	
	
	public boolean noteChangedWorking() {
		boolean changed = super.noteChangedWorking();
		if (changed) {
			theView.setIntervalType(NO_INTERVAL);
			theView.repaint();
		}
		return changed;
	}
	
	protected int assessAnswer() {
		int selectedProbIndex = probChoice.getSelectedIndex();
		int correctProbIndex = kProbIndex[intervalType()];
		
		return (selectedProbIndex == correctProbIndex) ? ANS_CORRECT : ANS_WRONG;
	}
	
	protected void giveFeedback() {
		if (result == ANS_CORRECT) {
			theView.setIntervalType(intervalType());
			pdfPanelLayout.show(pdfPanel, "pdf");
		}
		else {
			theView.setIntervalType(NO_INTERVAL);
			pdfPanelLayout.show(pdfPanel, "pdf");
		}
		theView.repaint();
	}
	
	protected void showCorrectWorking() {
		theView.setIntervalType(intervalType());
		pdfPanelLayout.show(pdfPanel, "pdf");
		theView.repaint();
		
		probChoice.select(kProbIndex[intervalType()]);
	}
	
	protected double getMark() {
		return (assessAnswer() == ANS_CORRECT) ? 1 : 0;
	}
	
	public void showHints(boolean hasHints) {
		super.showHints(hasHints);
		pdfPanelLayout.show(pdfPanel, hasHints ? "pdf" : "blank");
	}
	
//-------------------------------------------------------------------------
	
	private boolean localAction(Object target) {
		if (target == probChoice) {
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