package exerciseSDProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import random.*;
import coreVariables.*;
import exercise2.*;
import formula.*;

import exerciseSD.*;


public class CenterSpreadApplet extends ExerciseApplet {
	static final public int EQUAL_AB = 0;
	static final public int A_SMALLER = 1;
	static final public int B_SMALLER = 2;
	
	static final private int ANS_MULTI_WRONG = ANS_WRONG;
	static final private int ANS_MENU_WRONG = ANS_WRONG + 1;
	
	static final private String kYGroupKeys[] = {"ya", "yb"};
	
//	static final private Color kHeadingColor = new Color(0x990000);
	static final private Color kMeanBackgroundColor = new Color(0x9BD8F2);
	static final private Color kSdBackgroundColor = new Color(0xFFAFAF);
	
	private RandomNormal generatorA, generatorB;
	
	private HorizAxis theAxis;
	private StackedPlusSdView theView;
	
	private DiffMeanSDChoicePanel meanChoicePanel, sdChoicePanel;
	
	private XChoice meanChoice, sdChoice;
	
	private int resultMean = ANS_UNCHECKED;
	private int resultSD = ANS_UNCHECKED;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
		add("Center", getWorkingPanels(data));
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
			bottomPanel.add(createMarkingPanel(NO_HINTS));
			
				XPanel messagePanel = new XPanel();
				messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
					
					message = new ExerciseMessagePanel(this);
				messagePanel.add(message);
			bottomPanel.add(messagePanel);
		
		add("South", bottomPanel);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
//		registerParameter("index", "int");					//	always registered
		registerParameter("meanDiffType", "choice");
		registerParameter("sdDiffType", "choice");
		registerParameter("count", "string");
		registerParameter("axis", "string");
		registerParameter("varName", "string");
		registerParameter("groupNames", "string");
		registerParameter("meanOptions", "string");
		registerParameter("sdOptions", "string");
	}
	
	private int getDiffMeanType() {
		return getIntParam("meanDiffType");
	}
	
	private int getDiffSdType() {
		return getIntParam("sdDiffType");
	}
	
	public String getAxisInfo() {
		return getStringParam("axis");
	}
	
	protected String getCountString() {
		return getStringParam("count");
	}
	
	public String getVarName() {
		return getStringParam("varName");
	}
	
	public String getGroupNames() {
		return getStringParam("groupNames");
	}
	
	private String[] getOptionArray(String name) {
		StringTokenizer st = new StringTokenizer(getStringParam(name), " ");
		String result[] = new String[st.countTokens()];
		for (int i=0 ; i<result.length ; i++)
			result[i] = st.nextToken().replaceAll("_", " ");
		return result;
	}
	
	public String[] getMeanOptions() {
		return getOptionArray("meanOptions");
	}
	
	public String[] getSdOptions() {
		return getOptionArray("sdOptions");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 4));
		
			XPanel dotPlotPanel = new XPanel();
			dotPlotPanel.setLayout(new AxisLayout());
			
				theAxis = new HorizAxis(this);
			dotPlotPanel.add("Bottom", theAxis);
			
				theView = new StackedPlusSdView(data, this, theAxis, "y", "group", 9);
				theView.setShowStatistics(false);
				theView.lockBackground(Color.white);
			dotPlotPanel.add("Center", theView);
			
		thePanel.add("Center", dotPlotPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new ProportionLayout(0.5, 0));
		
				XPanel meanPanel = new InsetPanel(2, 1, 6, 1);
				meanPanel.setLayout(new BorderLayout(0, 3));
				
					XLabel centerLabel = new XLabel(translate("Centre"), XLabel.CENTER, this);
//					centerLabel.setForeground(kHeadingColor);
					centerLabel.setFont(getBigBoldFont());
				meanPanel.add("North", centerLabel);
				
					String yaName = data.getVariable("ya").name;
					String ybName = data.getVariable("yb").name;
					meanChoicePanel = new DiffMeanSDChoicePanel(this, true, getDiffMeanType(),
																									getMeanOptions(), yaName, ybName);
					registerStatusItem("meanChoice", meanChoicePanel);
				meanPanel.add("Center", meanChoicePanel);
				
					XPanel meanMenuPanel = new InsetPanel(0, 5, 0, 0);
					meanMenuPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
						meanChoice = new XChoice(translate("Summary") + ":", XChoice.HORIZONTAL, this);
						registerStatusItem("meanIndes", meanChoice);
					meanMenuPanel.add(meanChoice);
					
				meanPanel.add("South", meanMenuPanel);
				
				meanPanel.lockBackground(kMeanBackgroundColor);
			bottomPanel.add(ProportionLayout.LEFT, meanPanel);
		
				XPanel sdPanel = new InsetPanel(2, 1, 6, 1);
				sdPanel.setLayout(new BorderLayout(0, 0));
				
					XLabel spreadLabel = new XLabel(translate("Spread"), XLabel.CENTER, this);
//					spreadLabel.setForeground(kHeadingColor);
					spreadLabel.setFont(getBigBoldFont());
				sdPanel.add("North", spreadLabel);
				
					sdChoicePanel = new DiffMeanSDChoicePanel(this, false, getDiffSdType(),
																										getSdOptions(), yaName, ybName);
					registerStatusItem("sdChoice", sdChoicePanel);
				sdPanel.add("Center", sdChoicePanel);
				
					XPanel sdMenuPanel = new InsetPanel(0, 5, 0, 0);
					sdMenuPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
						sdChoice = new XChoice(translate("Summary") + ":", XChoice.HORIZONTAL, this);
						registerStatusItem("sdIndes", sdChoice);
					sdMenuPanel.add(sdChoice);
					
				sdPanel.add("South", sdMenuPanel);
				
				sdPanel.lockBackground(kSdBackgroundColor);
			bottomPanel.add(ProportionLayout.RIGHT, sdPanel);
				
		thePanel.add("South", bottomPanel);
		
		return thePanel;
	}
	
	private void addMenuOptions() {
		String yaName = data.getVariable("ya").name;
		String ybName = data.getVariable("yb").name;
		
		meanChoice.clearItems();
		meanChoice.addItem("centre(" + ybName + MText.expandText(") #approxEqual# centre(") + yaName + ")");
		meanChoice.addItem("centre(" + ybName + ") > centre(" + yaName + ")");
		meanChoice.addItem("centre(" + ybName + ") < centre(" + yaName + ")");
		
		sdChoice.clearItems();
		sdChoice.addItem("spread(" + ybName + MText.expandText(") #approxEqual# spread(") + yaName + ")");
		sdChoice.addItem("spread(" + ybName + ") > spread(" + yaName + ")");
		sdChoice.addItem("spread(" + ybName + ") < spread(" + yaName + ")");
	}
	
	protected void setDisplayForQuestion() {
		theAxis.readNumLabels(getAxisInfo());
		theAxis.setAxisName(getVarName());
		theAxis.invalidate();
		
		theView.setCrossSize(DataView.LARGE_CROSS);
		theView.repaint();
		
		CoreVariable yaVar = data.getVariable("ya");
		CoreVariable ybVar = data.getVariable("yb");
		
		sdChoicePanel.changeOptions(getDiffSdType(), getSdOptions(), yaVar.name, ybVar.name);
		sdChoicePanel.clearRadioButtons();
		sdChoicePanel.invalidate();
		
		meanChoicePanel.changeOptions(getDiffMeanType(), getMeanOptions(), yaVar.name, ybVar.name);
		meanChoicePanel.clearRadioButtons();
		meanChoicePanel.invalidate();
		
		addMenuOptions();
		meanChoice.invalidate();
		sdChoice.invalidate();
	}
	
	protected void setDataForQuestion() {
		StringTokenizer ct = new StringTokenizer(getCountString());
		int na = Integer.parseInt(ct.nextToken());
		int nb = Integer.parseInt(ct.nextToken());
		
		NumSampleVariable zaVar = (NumSampleVariable)data.getVariable("za");
		zaVar.setSampleSize(na);
		zaVar.generateNextSample();
		
		NumSampleVariable zbVar = (NumSampleVariable)data.getVariable("zb");
		zbVar.setSampleSize(nb);
		zbVar.generateNextSample();
		
		ScaledVariable yaVar = (ScaledVariable)data.getVariable("ya");
		ScaledVariable ybVar = (ScaledVariable)data.getVariable("yb");
		StringTokenizer nt = new StringTokenizer(getGroupNames());
		yaVar.name = nt.nextToken();
		ybVar.name = nt.nextToken();
		
		StackedKeyVariable groupVar = (StackedKeyVariable)data.getVariable("group");
		groupVar.setSourceVar(data, kYGroupKeys);		//		sets group counts and names
		
		int diffMeanType = getDiffMeanType();
		int diffSdType = getDiffSdType();
//		System.out.println("diffMeanType = " + diffMeanType + ", diffSdType = " + diffSdType);
		
		StringTokenizer axisT = new StringTokenizer(getAxisInfo());
		double axisMin = Double.parseDouble(axisT.nextToken());
		double axisMax = Double.parseDouble(axisT.nextToken());
		
		double centerA, centerB, rangeA, rangeB;
		double maxRange;
		if (diffMeanType == EQUAL_AB) {
			centerA = centerB = (axisMin + axisMax) / 2;
			maxRange = axisMax - axisMin;
		}
		else {
			maxRange = (axisMax - axisMin) * 2 / 3;
			if (diffMeanType == A_SMALLER) {
				centerA = (2 * axisMin + axisMax) / 3;
				centerB = (axisMin + 2 * axisMax) / 3;
			}
			else {
				centerB = (2 * axisMin + axisMax) / 3;
				centerA = (axisMin + 2 * axisMax) / 3;
			}
		}
		
		if (diffSdType == EQUAL_AB)
			rangeA = rangeB = maxRange;
		else if (diffSdType == A_SMALLER) {
			rangeA = maxRange / 2;
			rangeB = maxRange;
		}
		else {
			rangeB = maxRange / 2;
			rangeA = maxRange;
		}
		
		setScale(yaVar, zaVar, centerA, rangeA);
		setScale(ybVar, zbVar, centerB, rangeB);
	}
	
	private void setScale(ScaledVariable yVar, NumVariable zVar, double center, double range) {
		double zMin = Double.POSITIVE_INFINITY;
		double zMax = Double.NEGATIVE_INFINITY;
		ValueEnumeration ze = zVar.values();
		while (ze.hasMoreValues()) {
			double z = ze.nextDouble();
			zMin = Math.min(zMin, z);
			zMax = Math.max(zMax, z);
		}
		
		double factor = range / (zMax - zMin);
		double shift = center - range / 2 - zMin * factor;
		
		yVar.setScale(shift, factor, 9);
		yVar.clearSortedValues();
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		if (result == ANS_UNCHECKED)
			messagePanel.insertText("Select one of the three options on the left by clicking on a radio button; then choose the option from the pop-up menu below that best summarises this.\nFinally, do the same on the right.");
		else if (result == ANS_TOLD) {
			messagePanel.insertRedHeading("Answer\n");
			messagePanel.insertBoldText(translate("Centre") + ": ");
			messagePanel.insertText(meanChoicePanel.getSelectedOptionMessage());
			messagePanel.insertBoldText("\nSpread: ");
			messagePanel.insertText(sdChoicePanel.getSelectedOptionMessage());
		}
		else {
			if (resultMean == ANS_CORRECT && resultSD == ANS_CORRECT)
				messagePanel.insertRedHeading("Good!\n");
			else
				messagePanel.insertRedHeading("Wrong!\n");
			messagePanel.insertBoldText(translate("Centre") + ": ");
			switch (resultMean) {
				case ANS_INCOMPLETE:
					messagePanel.insertRedText("You must select an option on the left by clicking a radio button.");
					break;
				case ANS_CORRECT:
					if (resultSD == ANS_CORRECT)
						messagePanel.insertText("Yes. " + meanChoicePanel.getSelectedOptionMessage());
					else
						messagePanel.insertText("Your answer on the left is correct.");
					break;
				case ANS_MULTI_WRONG:
					messagePanel.insertRedText("No. " + meanChoicePanel.getCorrectOptionMessage());
					break;
				case ANS_MENU_WRONG:
					messagePanel.insertRedText("No. ");
					messagePanel.insertText(meanChoicePanel.getSelectedOptionMessage());
					messagePanel.insertText(" The radio buttons are correct but ");
					messagePanel.insertRedText("your summary is not consistent with this.");
					break;
			}
			
			messagePanel.insertBoldText("\nSpread: ");
			switch (resultSD) {
				case ANS_INCOMPLETE:
					messagePanel.insertRedText("You must select an option on the right by clicking a radio button.");
					break;
				case ANS_CORRECT:
					if (resultMean == ANS_CORRECT)
						messagePanel.insertText("Yes. " + sdChoicePanel.getSelectedOptionMessage());
					else
						messagePanel.insertText("Your answer on the right is correct.");
					break;
				case ANS_MULTI_WRONG:
					messagePanel.insertRedText("No. " + sdChoicePanel.getCorrectOptionMessage());
					break;
				case ANS_MENU_WRONG:
					messagePanel.insertRedText("No. ");
					messagePanel.insertText(sdChoicePanel.getSelectedOptionMessage());
					messagePanel.insertText(" The radio buttons are correct but ");
					messagePanel.insertRedText("your summary is not consistent with this.");
					break;
			}
		}
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
//			RandomNeatNormal generatorA = new RandomNeatNormal(10, 0.0, 1.0, 2.2);
			generatorA = new RandomNormal(10, 0.0, 1.0, 2.5);
			generatorA.setSeed(nextSeed());
			generatorA.setNeatening(0.3);
			NumSampleVariable zaVar = new NumSampleVariable("Std normal A", generatorA, 9);
			zaVar.generateNextSample();
		data.addVariable("za", zaVar);
			
			ScaledVariable yaVar = new ScaledVariable("", zaVar, "za", 0.0, 1.0, 9);
		data.addVariable("ya", yaVar);
		
//			RandomNeatNormal generatorB = new RandomNeatNormal(10, 0.0, 1.0, 2.2);
			generatorB = new RandomNormal(10, 0.0, 1.0, 2.5);
			generatorB.setSeed(nextSeed());
			generatorB.setNeatening(0.3);
			NumSampleVariable zbVar = new NumSampleVariable("Std normal B", generatorB, 9);
			zbVar.generateNextSample();
		data.addVariable("zb", zbVar);
			
			ScaledVariable ybVar = new ScaledVariable("", zbVar, "zb", 0.0, 1.0, 9);
		data.addVariable("yb", ybVar);
		
			StackedNumVariable yVar = new StackedNumVariable("Y", data, kYGroupKeys);
		data.addVariable("y", yVar);
			
			StackedKeyVariable groupVar = new StackedKeyVariable("Group", data, kYGroupKeys);
		data.addVariable("group", groupVar);
		
		return data;
	}
	
	
//-----------------------------------------------------------
	
	private boolean firstPaint = true;
	
	public void paint(Graphics g) {
		if (firstPaint) {								//		Seems necessary to get multi-line options laid out
			meanChoicePanel.invalidate();
			sdChoicePanel.invalidate();
			validate();
			repaint();
			firstPaint = false;
		}
		else
			super.paint(g);
	}
	
	private boolean localAction(Object target) {
		if (target == meanChoice || target == sdChoice) {
			noteChangedWorking();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
	
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {			//	side effect is to set resultMean and resultSD
																			//	OK for use by getMark() because they are not used in message unless overall result changes from ANS_UNCHECKED
		resultMean = meanChoicePanel.checkCorrect();
		resultSD = sdChoicePanel.checkCorrect();
		
		if (resultMean == ANS_CORRECT && meanChoice.getSelectedIndex() != getDiffMeanType())
			resultMean = ANS_MENU_WRONG;
		
		if (resultSD == ANS_CORRECT && sdChoice.getSelectedIndex() != getDiffSdType())
			resultSD = ANS_MENU_WRONG;
		
		return (resultMean == ANS_CORRECT && resultSD == ANS_CORRECT) ? ANS_CORRECT
									: (resultMean == ANS_INCOMPLETE || resultSD == ANS_INCOMPLETE) ? ANS_INCOMPLETE
									: ANS_WRONG;
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		resultMean = resultSD = ANS_TOLD;
		
		meanChoicePanel.showAnswer();
		sdChoicePanel.showAnswer();
		meanChoice.select(getDiffMeanType());
		sdChoice.select(getDiffSdType());
	}
	
	protected double getMark() {
		assessAnswer();			//	side effect sets sdResult and meanResult
		
		double meanMark = (resultMean == ANS_CORRECT) ? 0.5 : (resultMean == ANS_MENU_WRONG) ? 0.2 : 0;
		double sdMark = (resultSD == ANS_CORRECT) ? 0.5 : (resultSD == ANS_MENU_WRONG) ? 0.2 : 0;
																							//	if ANS_MENU_WRONG, the multiple choice must be correct
		return meanMark + sdMark;
	}
}