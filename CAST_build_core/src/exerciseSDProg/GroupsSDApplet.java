package exerciseSDProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import random.*;
import exercise2.*;

import exerciseSD.*;


public class GroupsSDApplet extends ExerciseApplet {
	private RandomNormal generator;
	
	private HorizAxis theAxis;
	private StackedPlusSdView theView;
	
	private CombinedMeanSDChoicePanel meanChoicePanel, sdChoicePanel;
	
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
		registerParameter("scales", "string");
		registerParameter("count", "string");
		registerParameter("axis", "string");
		registerParameter("varName", "string");
		registerParameter("groupNames", "string");
		registerParameter("decimals", "int");
	}
	
	protected String getScales() {
		return getStringParam("scales");
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
	
	public int getDecimals() {
		return getIntParam("decimals");
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
				theView.lockBackground(Color.white);
			dotPlotPanel.add("Center", theView);
			
		thePanel.add("Center", dotPlotPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 0));
		
				XPanel meanPanel = new XPanel();
				meanPanel.setLayout(new BorderLayout(0, 0));
				
					XLabel meanLabel = new XLabel(translate("Mean"), XLabel.LEFT, this);
					meanLabel.setFont(getStandardBoldFont());
				meanPanel.add("North", meanLabel);
				
					meanChoicePanel = new CombinedMeanSDChoicePanel(this, data, "y", "group",
																											CombinedMeanSDChoicePanel.MEAN, getDecimals());
					registerStatusItem("meanChoice", meanChoicePanel);
				meanPanel.add("Center", meanChoicePanel);
				
			bottomPanel.add(meanPanel);
		
				XPanel sdPanel = new XPanel();
				sdPanel.setLayout(new BorderLayout(0, 0));
				
					XLabel sdLabel = new XLabel(translate("Standard deviation"), XLabel.LEFT, this);
					sdLabel.setFont(getStandardBoldFont());
				sdPanel.add("North", sdLabel);
				
					sdChoicePanel = new CombinedMeanSDChoicePanel(this, data, "y", "group",
																											CombinedMeanSDChoicePanel.SD, getDecimals());
					registerStatusItem("sdChoice", sdChoicePanel);
				sdPanel.add("Center", sdChoicePanel);
				
			bottomPanel.add(sdPanel);
				
		thePanel.add("South", bottomPanel);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		sdChoicePanel.setDecimals(getDecimals());
		sdChoicePanel.changeOptions();
		sdChoicePanel.clearRadioButtons();
		sdChoicePanel.invalidate();
		
		meanChoicePanel.setDecimals(getDecimals());
		meanChoicePanel.changeOptions();
		meanChoicePanel.clearRadioButtons();
		meanChoicePanel.invalidate();
		
		theAxis.readNumLabels(getAxisInfo());
		theAxis.setAxisName(getVarName());
		theAxis.invalidate();
		
		int decimals = getDecimals();
		theView.setSdDecimals(decimals);
		theView.setCrossSize(DataView.LARGE_CROSS);
		theView.repaint();
	}
	
	protected void setDataForQuestion() {
		StringTokenizer ct = new StringTokenizer(getCountString());
		int nGroups = ct.countTokens();
		int groupCount[] = new int[nGroups];
		int n = 0;
		int index = 0;
		while (ct.hasMoreTokens()) {
			int thisCount = Integer.parseInt(ct.nextToken());
			groupCount[index ++] = thisCount;
			n += thisCount;
		}
		
		NumSampleVariable zVar = (NumSampleVariable)data.getVariable("base");
		zVar.setSampleSize(n);
		zVar.generateNextSample();
		
		CatVariable groupVar = (CatVariable)data.getVariable("group");
		groupVar.readLabels(getGroupNames());
		groupVar.setCounts(groupCount);
		
		ScaledGroupVariable yVar = (ScaledGroupVariable)data.getVariable("y");
		double mean[] = new double[nGroups];
		double sd[] = new double[nGroups];
		StringTokenizer axisT = new StringTokenizer(getAxisInfo());
		double axisMin = Double.parseDouble(axisT.nextToken());
		double axisMax = Double.parseDouble(axisT.nextToken());
		Random random = new Random(nextSeed());
		
		StringTokenizer scaleT = new StringTokenizer(getScales());
		for (int i=0 ; i<nGroups ; i++) {
			String scaleString = scaleT.nextToken();
			if (scaleString.charAt(0) == '?') {
				mean[i] = Double.parseDouble(scaleString.substring(1));
				sd[i] = 0.0;
			}
			else {
				double propn = Double.parseDouble(scaleString);
				double width = propn * (axisMax - axisMin);
				double min = axisMin + random.nextDouble() * (axisMax - axisMin - width);
				mean[i] = min + width * 0.5;
				sd[i] = width / 6;
			}
		}
		yVar.setScale(mean, sd, getDecimals());
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Select the value above that is closest to the standard deviation of the combined data set.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText(meanChoicePanel.getSelectedOptionMessage() + ".\n");
				messagePanel.insertText(sdChoicePanel.getSelectedOptionMessage() + ".");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText(meanChoicePanel.getSelectedOptionMessage() + ".\n");
				messagePanel.insertText(sdChoicePanel.getSelectedOptionMessage() + ".");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertRedText("You must select options for both the mean and standard deviation by clicking one of the radio buttons in each group.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				if (meanChoicePanel.checkCorrect() == ANS_CORRECT)
					messagePanel.insertText("The mean is indeed " + meanChoicePanel.getCorrectValue()
																																										+ ", but...\n");
				else
					messagePanel.insertRedText(meanChoicePanel.getCorrectOptionMessage() + "\n");
				if (sdChoicePanel.checkCorrect() == ANS_CORRECT)
					messagePanel.insertText("However the standard deviation is indeed " + sdChoicePanel.getCorrectValue() + ".");
				else
					messagePanel.insertRedText(sdChoicePanel.getSelectedOptionMessage());
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 150;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			generator = new RandomNormal(10, 0.0, 1.0, 3.0);
			generator.setSeed(nextSeed());
			NumSampleVariable baseVar = new NumSampleVariable("Std normal", generator, 9);
			baseVar.generateNextSample();
		data.addVariable("base", baseVar);
			
			CatVariable groupVar = new CatVariable("Group", CatVariable.USES_REPEATS);
		data.addVariable("group", groupVar);
		
			ScaledGroupVariable yVar = new ScaledGroupVariable(getVarName(), baseVar,
																				"base", groupVar, "group", null, null, 9);
															//						scaling will be set later by setWorkingForQuestion()
		data.addVariable("y", yVar);
		
		return data;
	}
	
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
		int meanResult = meanChoicePanel.checkCorrect();
		int sdResult = sdChoicePanel.checkCorrect();
		
		return (meanResult == ANS_CORRECT && sdResult == ANS_CORRECT) ? ANS_CORRECT
									: (meanResult == ANS_INCOMPLETE || sdResult == ANS_INCOMPLETE) ? ANS_INCOMPLETE
									: ANS_WRONG;
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		meanChoicePanel.showAnswer();
		sdChoicePanel.showAnswer();
	}
	
	protected double getMark() {
		double meanMark = meanChoicePanel.checkCorrect() == ANS_CORRECT ? 0.5 : 0;
		double sdMark = sdChoicePanel.checkCorrect() == ANS_CORRECT ? 0.5 : 0;
		
		return meanMark + sdMark;
	}
}