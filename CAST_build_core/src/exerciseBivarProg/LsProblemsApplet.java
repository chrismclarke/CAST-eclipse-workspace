package exerciseBivarProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import models.*;
import random.*;
import exercise2.*;

import exerciseBivar.*;


public class LsProblemsApplet extends ExerciseApplet {
	static final private int NO_EXTREME = 0;
	static final private int OUTLIER_EXTREME = 1;
	static final private int LEVERAGE_EXTREME = 2;
	static final private int EXTRAPOLATE_EXTREME = 3;
	static final private int EXTRAP_OUTLIER_EXTREME = 4;
	
	static final private int EXTRAP_CHECK = 0;
	static final private int NONLIN_CHECK = 1;
	static final private int LEVERAGE_CHECK = 2;
	static final private int OUTLIER_CHECK = 3;
	
	private RandomRectangular xGenerator;
	private RandomNormal errorGenerator;
	
	private XLabel yNameLabel;
	private HorizAxis xAxis;
	private VertAxis yAxis;
	private ScatterAndLineView theView;
	
	private TrueFalseTextPanel problemCheck[] = null;
	private int checkPermutation[] = {0, 1, 2, 3};
	
	private Random random01;
	
//================================================
	
	protected void createDisplay() {
		random01 = new Random(nextSeed());
		
		setLayout(new BorderLayout(0, 8));
		
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
	
//===============================================================
	
	private class PredictionChoice extends IntChoice {
		private NumValue predictionX;
		
		public PredictionChoice(String extremeType, NumValue predictionX) {
			super(extremeType);
			this.predictionX = predictionX;
		}
		
		public PredictionChoice(String interpString, String extrapString, PredictionChoice oldChoice,
																																								ExerciseApplet applet) {
			super("0", "4", oldChoice, applet);
			
			int extremeType = intValue();
			String predictionString = (extremeType == NO_EXTREME || extremeType == OUTLIER_EXTREME)
																																				? interpString : extrapString;
			
			StringTokenizer pst = new StringTokenizer(predictionString, ":");
			NumValue low = new NumValue(pst.nextToken());
			NumValue high = new NumValue(pst.nextToken());
			int decimals = pst.hasMoreTokens() ? Integer.parseInt(pst.nextToken())
																						: Math.max(low.decimals, high.decimals);
			for (int i=0 ; i<decimals ; i++) {
				low.setValue(low.toDouble() * 10);
				high.setValue(high.toDouble() * 10);
			}
			for (int i=0 ; i<-decimals ; i++) {
				low.setValue(low.toDouble() / 10);
				high.setValue(high.toDouble() / 10);
			}
			low.decimals = high.decimals = 0;
			
			int min = (int)Math.round(low.toDouble());
			int max = (int)Math.round(high.toDouble());
			int value = new RandomInteger(min, max, 1, applet.nextSeed()).generateOne();
			
			double x = value;
			for (int i=0 ; i<decimals ; i++)
				x /= 10.0;
			for (int i=0 ; i<-decimals ; i++)
				x *= 10.0;
			predictionX = new NumValue(x, Math.max(decimals, 0));
		}
		
		public NumValue getPredictionX() {
			return predictionX;
		}
		
		public String toString() {
			return predictionX.toString();
		}
	}
	
//===============================================================
	
	protected void addTypeDelimiters() {
		addType("predictionChoice", "*");
	}
	
	protected Object createConstObject(String baseType, String valueString) {
		if (baseType.equals("predictionChoice")) {
			StringTokenizer pst = new StringTokenizer(valueString, ",");
			String extremeType = pst.nextToken();
			NumValue predictionX = new NumValue(pst.nextToken());
			return new PredictionChoice(extremeType, predictionX);
		}
		else
			return super.createConstObject(baseType, valueString);
	}
	
	protected Object createRandomObject(String baseType, String paramString, Object oldParam) {
		if (baseType.equals("predictionChoice")) {
			StringTokenizer pst = new StringTokenizer(paramString, ",");
			return new PredictionChoice(pst.nextToken(), pst.nextToken(), (PredictionChoice)oldParam, this);
		}
		else
			return super.createRandomObject(baseType, paramString, oldParam);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
//		registerParameter("index", "int");					//	always registered
		registerParameter("xVarName", "string");
		registerParameter("xAxis", "string");
		registerParameter("xPlusAxis", "string");
		registerParameter("yVarName", "string");
		registerParameter("yAxis", "string");
		registerParameter("yPlusAxis", "string");
		registerParameter("yOutlierAxis", "string");
		registerParameter("count", "int");
		registerParameter("predictionType", "predictionChoice");
		registerParameter("linearParams", "string");
		registerParameter("quadParams", "string");
		registerParameter("curvature", "choice");
		registerParameter("forceExtendedAxes", "choice");
		registerParameter("extrapolationStatement", "string");
		registerParameter("nonlinStatement", "string");
		registerParameter("leverageStatement", "string");
		registerParameter("outlierStatement", "string");
	}
	
	protected String getXVarName() {
		return getStringParam("xVarName");
	}
	
	private String getXAxis() {
		return getStringParam("xAxis");
	}
	
	private String getXExtendedAxis() {
		return getStringParam("xPlusAxis");
	}
	
	protected String getYVarName() {
		return getStringParam("yVarName");
	}
	
	private String getYAxis() {
		return getStringParam("yAxis");
	}
	
	private String getYExtendedAxis() {
		return getStringParam("yPlusAxis");
	}
	
	private String getYOutlierAxis() {
		return getStringParam("yOutlierAxis");
	}
	
	private int getCount() {
		return getIntParam("count");
	}
	
	private int getExtremeType() {
		return ((PredictionChoice)getObjectParam("predictionType")).intValue();
	}
	
	private String getLinearParams() {
		return getStringParam("linearParams");
	}
	
	private String getQuadParams() {
		return getStringParam("quadParams");
	}
	
	private boolean hasCurvature() {
		return getIntParam("curvature") == 1;
	}
	
	private boolean forceExtendedAxes() {
		return getIntParam("forceExtendedAxes") == 1;
	}
	
	private NumValue getPredictionX() {
		return ((PredictionChoice)getObjectParam("predictionType")).getPredictionX();
	}
	
	private String getExtrapStatement() {
		return getStringParam("extrapolationStatement");
	}
	
	private String getNonlinStatement() {
		return getStringParam("nonlinStatement");
	}
	
	private String getLeverageStatement() {
		return getStringParam("leverageStatement");
	}
	
	private String getOutlierStatement() {
		return getStringParam("outlierStatement");
	}
	
	//............
	
	private String getDisplayXAxis() {
		int extremeType = getExtremeType();
		if (extremeType == LEVERAGE_EXTREME || extremeType == EXTRAPOLATE_EXTREME
															|| extremeType == EXTRAP_OUTLIER_EXTREME || forceExtendedAxes())
			return getXExtendedAxis();
		else
			return getXAxis();
	}
	
	private String getDisplayYAxis() {
		int extremeType = getExtremeType();
		if (extremeType == LEVERAGE_EXTREME || extremeType == EXTRAPOLATE_EXTREME
															|| extremeType == EXTRAP_OUTLIER_EXTREME || forceExtendedAxes())
			return getYExtendedAxis();
		else if (extremeType == OUTLIER_EXTREME)
			return getYOutlierAxis();
		else
			return getYAxis();
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
		thePanel.add("Center", getScatterPanel(data));
		thePanel.add("South", getCheckboxPanel());
		
		return thePanel;
	}
	
	private XPanel getScatterPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(110, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
			yNameLabel = new XLabel("", XLabel.LEFT, this);
		thePanel.add("North", yNameLabel);
			
			XPanel displayPanel = new XPanel();
			displayPanel.setLayout(new AxisLayout());
			
				xAxis = new HorizAxis(this);
			displayPanel.add("Bottom", xAxis);
			
				yAxis = new VertAxis(this);
			displayPanel.add("Left", yAxis);
			
				theView = new ScatterAndLineView(data, this, xAxis, yAxis, "x", "y", "ls");
				theView.lockBackground(Color.white);
			displayPanel.add("Center", theView);
			
		thePanel.add("Center", displayPanel);
		
		return thePanel;
	}
	
	private XPanel getCheckboxPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 3));
		
		problemCheck = new TrueFalseTextPanel[4];
		for (int i=0 ; i<4 ; i++) {
			problemCheck[i] = new TrueFalseTextPanel(this, true);
			registerStatusItem("problem" + i, problemCheck[i]);
			thePanel.add(problemCheck[i]);
		}
			
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		xAxis.readNumLabels(getDisplayXAxis());
		xAxis.setAxisName(getXVarName());
		xAxis.invalidate();
		
		yAxis.readNumLabels(getDisplayYAxis());
		yNameLabel.setText(getYVarName());
		yAxis.invalidate();
		
		theView.setPredictionX(getPredictionX().toDouble());
		
		permute(checkPermutation, random01);
		
		int extremeType = getExtremeType();
		
		int extrapIndex = checkPermutation[EXTRAP_CHECK];
		problemCheck[extrapIndex].changeStatement(getExtrapStatement());
		problemCheck[extrapIndex].setCorrect(extremeType == EXTRAPOLATE_EXTREME || extremeType == EXTRAP_OUTLIER_EXTREME);
		
		int nonlinIndex = checkPermutation[NONLIN_CHECK];
		problemCheck[nonlinIndex].changeStatement(getNonlinStatement());
		problemCheck[nonlinIndex].setCorrect(hasCurvature());
		
		int leverageIndex = checkPermutation[LEVERAGE_CHECK];
		problemCheck[leverageIndex].changeStatement(getLeverageStatement());
		problemCheck[leverageIndex].setCorrect(getExtremeType() == LEVERAGE_EXTREME);
		
		int outlierIndex = checkPermutation[OUTLIER_CHECK];
		problemCheck[outlierIndex].changeStatement(getOutlierStatement());
		problemCheck[outlierIndex].setCorrect(extremeType == OUTLIER_EXTREME || extremeType == EXTRAP_OUTLIER_EXTREME);
		
		for (int i=0 ; i<4 ; i++)
			problemCheck[i].setState(false);
			
		data.variableChanged("x");
	}
	
	protected void setDataForQuestion() {
		NumSampleVariable xVar = (NumSampleVariable)data.getVariable("x");
		RandomRectangular xGenerator = (RandomRectangular)xVar.getGenerator();
		StringTokenizer st = new StringTokenizer(getXAxis());
		double xMin = Double.parseDouble(st.nextToken());
		double xMax = Double.parseDouble(st.nextToken());
		xGenerator.setMinMax(xMin, xMax);
		xVar.name = getXVarName();
		
		int n = getCount();
		xVar.setSampleSize(n);
		xVar.generateNextSample();
		
		int extremeType = getExtremeType();
		if (extremeType == LEVERAGE_EXTREME) {
			double leverageX = getExtremeValue(getXAxis(), getXExtendedAxis(), 0.8, 1.0);
			((NumValue)xVar.valueAt(n - 1)).setValue(leverageX);
		}
		
		NumSampleVariable errorVar = (NumSampleVariable)data.getVariable("error");
		errorVar.setSampleSize(n);
		errorVar.generateNextSample();
		
		LinearModel yLinearModel = (LinearModel)data.getVariable("linearModel");
		yLinearModel.setParameters(getLinearParams());
		
		QuadraticModel yQuadModel = (QuadraticModel)data.getVariable("quadModel");
		yQuadModel.setParameters(getQuadParams());
		
		ResponseVariable yLinearVar = (ResponseVariable)data.getVariable("yLinear");
		ResponseVariable yQuadVar = (ResponseVariable)data.getVariable("yQuad");
		
//		NumValue lastValue = (extremeType == LEVERAGE_EXTREME) ? (NumValue)yLinearVar.valueAt(n - 1)
//																								: null;		//	from linear model for high leverage
		
		NumVariable yVar = (NumVariable)data.getVariable("y");
		yVar.clearData();
		for (int i=0 ; i<n-1 ; i++)
			yVar.addValue(hasCurvature() ? yQuadVar.valueAt(i) : yLinearVar.valueAt(i));
		
		switch (extremeType) {
			case LEVERAGE_EXTREME:
				yVar.addValue(yLinearVar.valueAt(n - 1));
				break;
			case OUTLIER_EXTREME:
			case EXTRAP_OUTLIER_EXTREME:
				double outlier = getExtremeValue(getYAxis(), getYExtendedAxis(), 0.0, 0.5);
				yVar.addValue(new NumValue(outlier, yVar.getMaxDecimals()));
				
				double p = random01.nextDouble() / 2;
				if (yLinearModel.getSlope().toDouble() < 0)
					p = 1 - p;
				double outlierX = p * xMax + (1 - p) * xMin;
				((NumValue)xVar.valueAt(n - 1)).setValue(outlierX);
				break;
			default:				//		NO_EXTREME or EXTRAPOLATE_EXTREME
				yVar.addValue(hasCurvature() ? yQuadVar.valueAt(n - 1) : yLinearVar.valueAt(n - 1));
				break;
		}
		
		LinearModel lsModel = (LinearModel)data.getVariable("ls");
		lsModel.setLSParams("y", 9, 9, 0);
	}
	
	private double getExtremeValue(String axisInfo, String axisPlusInfo, double pLow, double pHigh) {
		StringTokenizer st = new StringTokenizer(axisInfo);
		st.nextToken();
		double yMax = Double.parseDouble(st.nextToken());
		
		st = new StringTokenizer(axisPlusInfo);
		st.nextToken();
		double yMaxPlus = Double.parseDouble(st.nextToken());
		
		double p = pLow + random01.nextDouble() * (pHigh - pLow);
		
		return yMax + p * (yMaxPlus - yMax);
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			xGenerator = new RandomRectangular(10, 0.0, 1.0);
			xGenerator.setSeed(nextSeed());
			NumSampleVariable xVar = new NumSampleVariable("", xGenerator, 9);
			xVar.generateNextSample();
		data.addVariable("x", xVar);
		
			errorGenerator = new RandomNormal(10, 0.0, 1.0, 2.5);		//	+/- 2.5 SD
			errorGenerator.setSeed(nextSeed());
			NumSampleVariable errorVar = new NumSampleVariable(translate("Error"), errorGenerator, 9);
			errorVar.generateNextSample();
		data.addVariable("error", errorVar);
		
			QuadraticModel yQuadModel = new QuadraticModel("Model", data, "x");
		data.addVariable("quadModel", yQuadModel);
		
			ResponseVariable yQuadVar = new ResponseVariable("", data, "x", "error", "quadModel", 9);
		data.addVariable("yQuad", yQuadVar);
		
			LinearModel yLinearModel = new LinearModel("Model", data, "x");
		data.addVariable("linearModel", yLinearModel);
		
			ResponseVariable yLinearVar = new ResponseVariable("", data, "x", "error", "linearModel", 9);
		data.addVariable("yLinear", yLinearVar);
		
			NumVariable yVar = new NumVariable("");
		data.addVariable("y", yVar);
		
			LinearModel lsModel = new LinearModel("Least sqrs", data, "x");
		data.addVariable("ls", lsModel);
		
		return data;
	}
	
	public void setFixedQuestionSeed(long seed) {
		super.setFixedQuestionSeed(seed);
		random01.setSeed(nextSeed());
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		int problemIndex[] = new int[4];
		for (int i=0 ; i<4 ; i++)
			problemIndex[checkPermutation[i]] = i;
		
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Which of the above statements are true?\n(Click the checkboxes to toggle them.)");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				int extremeType = getExtremeType();
				for (int i=0 ; i<4 ; i++) {
					switch (problemIndex[i]) {
						case EXTRAP_CHECK:
							messagePanel.insertBoldBlueText("Extrapolation: ");
							insertCoreExtrapolationMessage(messagePanel, extremeType);
							break;
						case NONLIN_CHECK:
							messagePanel.insertBoldBlueText("Curvature: ");
							insertCoreCurvatureMessage(messagePanel, extremeType);
							break;
						case LEVERAGE_CHECK:
							messagePanel.insertBoldBlueText("Leverage: ");
							insertCoreLeverageMessage(messagePanel, extremeType);
							break;
						case OUTLIER_CHECK:
							messagePanel.insertBoldBlueText("Outlier: ");
							insertCoreOutlierMessage(messagePanel, extremeType);
							break;
					}
					if (i < 3)
						messagePanel.insertText("\n");
				}
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have identified the correct and wrong statements.");
				break;
			case ANS_WRONG:
				for (int i=0 ; i<4 ; i++) {
					switch (problemIndex[i]) {
						case EXTRAP_CHECK:
							insertExtrapolationError(messagePanel);
							break;
						case NONLIN_CHECK:
							insertCurvatureError(messagePanel);
							break;
						case LEVERAGE_CHECK:
							insertLeverageError(messagePanel);
							break;
						case OUTLIER_CHECK:
							insertOutlierError(messagePanel);
							break;
					}
					if (i < 3)
						messagePanel.insertText("\n");
				}
				break;
		}
	}
	
	private void insertExtrapolationError(MessagePanel messagePanel) {
		int extremeType = getExtremeType();
		boolean isExtrap = (extremeType == EXTRAPOLATE_EXTREME)
																			|| (extremeType == EXTRAP_OUTLIER_EXTREME);
		boolean attempt = problemCheck[checkPermutation[EXTRAP_CHECK]].isSelected();
		
		messagePanel.insertBoldBlueText("Extrapolation: ");
		if (isExtrap == attempt) {
			messagePanel.insertText("Correct, ");
			if (isExtrap)
				messagePanel.insertText("this is extrapolation beyond existing data.");
			else
				messagePanel.insertText("this is not extrapolation.");
		}
		else {
			messagePanel.insertBoldRedText("Wrong. ");
			insertCoreExtrapolationMessage(messagePanel, extremeType);
		}
	}
	
	private void insertCoreExtrapolationMessage(MessagePanel messagePanel, int extremeType) {
		boolean isExtrap = (extremeType == EXTRAPOLATE_EXTREME)
																			|| (extremeType == EXTRAP_OUTLIER_EXTREME);
		if (isExtrap) {
			messagePanel.insertText("Since there are no data points with " + getXVarName() + " as high as " + getPredictionX() + ", this ");
			messagePanel.insertBoldText("is");
			messagePanel.insertText(" extrapolation. We have no information about the shape of the relationship near " + getPredictionX() + ".");
		}
		else {
			if (extremeType == LEVERAGE_EXTREME) {
				messagePanel.insertText("Although the prediction is made outside the range of most of the data, one observation has been made with " + getXVarName() + " near " + getPredictionX() + ". This is therefore ");
				messagePanel.insertBoldText("not");
				messagePanel.insertText(" strictly extrapolation.");
			}
			else {
				messagePanel.insertText("Since there are other observations with " + getXVarName() + " close to " + getPredictionX() + ", this is ");
				messagePanel.insertBoldText("not");
				messagePanel.insertText(" extrapolation.");
			}
		}
	}
	
	private void insertCurvatureError(MessagePanel messagePanel) {
		int extremeType = getExtremeType();
		boolean isNonlinear = hasCurvature();
		boolean attempt = problemCheck[checkPermutation[NONLIN_CHECK]].isSelected();
		
		messagePanel.insertBoldBlueText("Curvature: ");
		if (isNonlinear == attempt) {
			messagePanel.insertText("Correct, ");
			if (isNonlinear)
				messagePanel.insertText("there is curvature in the scatterplot.");
			else
				messagePanel.insertText("the relationship seems linear.");
		}
		else {
			messagePanel.insertBoldRedText("Wrong. ");
			insertCoreCurvatureMessage(messagePanel, extremeType);
		}
	}
	
	private void insertCoreCurvatureMessage(MessagePanel messagePanel, int extremeType) {
		boolean isNonlinear = hasCurvature();
		if (isNonlinear) {
			messagePanel.insertText("Most crosses lie close to a ");
			messagePanel.insertBoldText("curve");
			messagePanel.insertText(" not a straight line.");
		}
		else {
			messagePanel.insertText("Most crosses lie close to a straight line so there is ");
			messagePanel.insertBoldText("no evidence");
			messagePanel.insertText(" of curvature.");
		}
	}
	
	private void insertOutlierError(MessagePanel messagePanel) {
		int extremeType = getExtremeType();
		boolean isOutlier = (extremeType == OUTLIER_EXTREME)
																			|| (extremeType == EXTRAP_OUTLIER_EXTREME);
		boolean attempt = problemCheck[checkPermutation[OUTLIER_CHECK]].isSelected();
		
		messagePanel.insertBoldBlueText("Outlier: ");
		if (isOutlier == attempt) {
			messagePanel.insertText("Correct, ");
			if (isOutlier)
				messagePanel.insertText("there is an outlier.");
			else
				messagePanel.insertText("there are no outliers.");
		}
		else {
			messagePanel.insertBoldRedText("Wrong. ");
			insertCoreOutlierMessage(messagePanel, extremeType);
		}
	}
	
	private void insertCoreOutlierMessage(MessagePanel messagePanel, int extremeType) {
		boolean isOutlier = (extremeType == OUTLIER_EXTREME)
																			|| (extremeType == EXTRAP_OUTLIER_EXTREME);
		if (isOutlier) {
			messagePanel.insertText("One cross does not seem to follow the same trend as the other crosses. The " + getYVarName() + " is very unusual so it ");
			messagePanel.insertBoldText("is");
			messagePanel.insertText(" an outlier.");
		}
		else {
			if (extremeType == LEVERAGE_EXTREME) {
				messagePanel.insertText("One observation has an unusual value of " + getXVarName() + " (high leverage), but its ");
				messagePanel.insertBoldText("response");
				messagePanel.insertText(" must be unusual for it to be called an outlier. ");
				
				if (hasCurvature()) {
					messagePanel.insertText("There is too little data around " + getXVarName() + " = " + getPredictionX() + " to tell whether its response is unusual (despite the curvature) so it is ");
					messagePanel.insertBoldText("not");
					messagePanel.insertText(" called an outlier.");
				}
				else {
					messagePanel.insertText("Its residual is small so it would ");
					messagePanel.insertBoldText("not");
					messagePanel.insertText(" be called an outlier.");
				}
			}
			else {
				messagePanel.insertText("All crosses lie close to the same " + (hasCurvature() ? "curve" : "straight line") + " so there are ");
				messagePanel.insertBoldText("none");
				messagePanel.insertText(" that would be classified as outliers.");
			}
		}
	}
	
	private void insertLeverageError(MessagePanel messagePanel) {
		int extremeType = getExtremeType();
		boolean isLeverage = extremeType == LEVERAGE_EXTREME;
		boolean attempt = problemCheck[checkPermutation[LEVERAGE_CHECK]].isSelected();
		
		messagePanel.insertBoldBlueText("Leverage: ");
		if (isLeverage == attempt) {
			messagePanel.insertText("Correct, ");
			if (isLeverage)
				messagePanel.insertText("there is a high leverage point with an unusual value of " + getXVarName() + ".");
			else
				messagePanel.insertText("there are no high leverage points.");
		}
		else {
			messagePanel.insertBoldRedText("Wrong. ");
			insertCoreLeverageMessage(messagePanel, extremeType);
		}
	}
	
	private void insertCoreLeverageMessage(MessagePanel messagePanel, int extremeType) {
		boolean isLeverage = extremeType == LEVERAGE_EXTREME;
		if (isLeverage) {
			messagePanel.insertText("One observation has an extreme value of " + getXVarName() + " and this makes it a ");
			messagePanel.insertBoldText("high leverage");
			messagePanel.insertText(" point.");
		}
		else {
			messagePanel.insertText("No crosses have values of " + getXVarName() + " that are very different from the rest, so there are ");
			messagePanel.insertBoldText("no");
			messagePanel.insertText(" high leverage points.");
		}
	}
	
	protected int getMessageHeight() {
		return 180;
	}
	
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
		int localResult = ANS_CORRECT;
		for (int i=0 ; i<4 ; i++)
			localResult = Math.max(localResult, problemCheck[i].checkCorrect());
		return localResult;
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		for (int i=0 ; i<4 ; i++)
			problemCheck[i].showAnswer();
	}
	
	protected double getMark() {
		int nWrong = 0;
		for (int i=0 ; i<4 ; i++)
			if (problemCheck[i].checkCorrect() != ANS_CORRECT)
				nWrong ++;
		return (nWrong == 0) ? 1 : (nWrong == 1) ? 0.5 : 0;
	}
	
}