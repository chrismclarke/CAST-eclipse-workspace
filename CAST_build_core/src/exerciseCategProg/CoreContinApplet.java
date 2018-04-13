package exerciseCategProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import exercise2.*;


import exerciseCateg.*;


abstract public class CoreContinApplet extends ExerciseApplet {
	static final protected Color kVariableLabelColor = new Color(0x000099);
	static final protected Color kTemplateColor = new Color(0x000099);	//	dark blue
	static final protected Color kTemplateBackground = new Color(0xFFE594);
		
	protected StaticContinTableView theTable;
	private XLabel xNameLabel;
	private XVertLabel yNameLabel;
	
	protected ResultValuePanel resultPanel;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(0, 10));
				
				questionPanel = new QuestionPanel(this);
			
			topPanel.add("North", questionPanel);
			
			topPanel.add("Center", getWorkingPanels(data));
			
		add("North", topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(0, 4));
			
				XPanel answerPanel = new XPanel();
				answerPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));

				
					resultPanel = new ResultValuePanel(this, "Answer =", 6);
					registerStatusItem("answer", resultPanel);
				answerPanel.add(resultPanel);
				
				answerPanel.add(createMarkingPanel(NO_HINTS));
		
			bottomPanel.add("North", answerPanel);
		
				XPanel messagePanel = new XPanel();
				messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
					
					message = new ExerciseMessagePanel(this);
				messagePanel.add(message);
			bottomPanel.add("Center", messagePanel);
		
		add("Center", bottomPanel);
	}
	
//-----------------------------------------------------------------
	
	protected void registerParameterTypes() {
		registerParameter("xVarName", "string");
		registerParameter("xCategories", "string");
		registerParameter("yVarName", "string");
		registerParameter("yCategories", "string");
		registerParameter("counts", "string");
		registerParameter("criticalX", "criticalX");
		registerParameter("criticalY", "criticalY");
	}
	
	protected void addTypeDelimiters() {
		addType("criticalX", ",");
		addType("criticalY", ",");
	}
	
	protected Object createConstObject(String baseType, String valueString) {
		if (baseType.equals("criticalX"))
			return CategoryParam.createConstObject(valueString, getXCategories(), true, false);
		else if (baseType.equals("criticalY"))
			return CategoryParam.createConstObject(valueString, getYCategories(), true, false);
		else
			return super.createConstObject(baseType, valueString);
	}
	
	protected Object createRandomObject(String baseType, String paramString, Object oldParam) {
		if (baseType.equals("criticalX"))
			return CategoryParam.createRandomObject(paramString, getXCategories(), true, false, this);
		else if (baseType.equals("criticalY"))
			return CategoryParam.createRandomObject(paramString, getYCategories(), true, false, this);
		else
			return super.createRandomObject(baseType, paramString, oldParam);
	}
	
	private String getXVarName() {
		return getStringParam("xVarName");
	}
	
	private Value[] getXCategories() {
		return underscoreToSpaces(getStringParam("xCategories"));
	}
	
	private Value[] underscoreToSpaces(String labelString) {
		StringTokenizer st = new StringTokenizer(labelString);
		Value label[] = new Value[st.countTokens()];
		for (int i=0 ; i<label.length ; i++) {
			String oneLabelString = st.nextToken().replace('_', ' ');
			label[i] = new LabelValue(oneLabelString);
		}
		
		return label;
	}
	
	private String getYVarName() {
		return getStringParam("yVarName");
	}
	
	private Value[] getYCategories() {
		return underscoreToSpaces(getStringParam("yCategories"));
	}
	
	private int[] getYCounts() {		//		order x1y1 x1y2 ... x1yn x2y1 ...
		StringTokenizer st = new StringTokenizer(getStringParam("counts"));
		int nCounts = st.countTokens();
		int counts[] = new int[nCounts];
		for (int i=0 ; i<nCounts ; i++)
			counts[i] = Integer.parseInt(st.nextToken());
		
		return counts;
	}
	
	private int[] getXCounts() {		//		order x1y1 x1y2 ... x1yn x2y1 ...
		int nx = getXCategories().length;
		StringTokenizer st = new StringTokenizer(getStringParam("counts"));
		int nTotal = st.countTokens();
		int ny = nTotal / nx;
		
		int counts[] = new int[nx];
		for (int i=0 ; i<nTotal ; i++)
			counts[i / ny] += Integer.parseInt(st.nextToken());
		
		return counts;
	}
	
	protected int getCriticalX() {
		Object criticalX = getObjectParam("criticalX");
		return (criticalX == null) ? -1 : ((CategoryParam)criticalX).intValue();
	}
	
	protected int getCriticalY() {
		Object criticalY = getObjectParam("criticalY");
		return (criticalY == null) ? -1 : ((CategoryParam)criticalY).intValue();
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			XPanel tablePanel = new XPanel();
			tablePanel.setLayout(new BorderLayout(6, 0));
			
				XPanel xNamePanel = new InsetPanel(30, 0, 0, 0);
				xNamePanel.setLayout(new BorderLayout(0, 0));
			
					xNameLabel = new XLabel("", XLabel.CENTER, this);
					xNameLabel.setFont(getStandardBoldFont());
					xNameLabel.setForeground(kVariableLabelColor);
				xNamePanel.add("Center", xNameLabel);
				
			tablePanel.add("North", xNamePanel);
			
				yNameLabel = new XVertLabel("", XLabel.CENTER, this);
				yNameLabel.setFont(getStandardBoldFont());
				yNameLabel.setForeground(kVariableLabelColor);
			tablePanel.add("West", yNameLabel);
			
				theTable = new StaticContinTableView(data, this, "y", "x");
			tablePanel.add("Center", theTable);
		
		thePanel.add(tablePanel);
		
		thePanel.add(getTemplatePanel());
		
		return thePanel;
	}
		
	abstract protected XPanel getTemplatePanel();
	
	protected void setDisplayForQuestion() {
		xNameLabel.setText(data.getVariable("x").name);
		yNameLabel.setText(data.getVariable("y").name);
		theTable.resetLayout();
		
		resultPanel.clear();
	}
	
	
	protected void setDataForQuestion() {
		CatVariable yVar = (CatVariable)data.getVariable("y");
		yVar.name = getYVarName();
		yVar.setLabels(getYCategories());
		
		CatVariable xVar = (CatVariable)data.getVariable("x");
		xVar.name = getXVarName();
		xVar.setLabels(getXCategories());
		
		xVar.setCounts(getXCounts());
		yVar.setCounts(getYCounts());
	}
	
//-----------------------------------------------------------
	
	abstract protected void insertMessageContent(MessagePanel messagePanel);
	
	protected int getMessageHeight() {
		return 120;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			CatVariable xVar = new CatVariable("", true);
		data.addVariable("x", xVar);
		
			CatVariable yVar = new CatVariable("", true);
		data.addVariable("y", yVar);
		
		return data;
	}
	
	protected double getAttempt() {
		return resultPanel.getAttempt().toDouble();
	}
	
	abstract protected double getCorrect();
	
	abstract protected void highlightTableRowCol();
	
	abstract protected int assessAnswer();
	
	protected void giveFeedback() {
		if (result == ANS_INCOMPLETE || result == ANS_INVALID) {
			theTable.clearPropnIndices();
			theTable.repaint();
		}
		else
			highlightTableRowCol();
	}
	
	abstract protected void showCorrectWorking();
	
	protected double getMark() {
		int ans = assessAnswer();
		return (ans == ANS_CORRECT) ? 1 : (ans == ANS_CLOSE) ? 0.8 : 0;
	}
	
}