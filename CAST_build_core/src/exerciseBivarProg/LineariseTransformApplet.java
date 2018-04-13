package exerciseBivarProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import random.*;
import coreGraphics.*;
import coreVariables.*;
import exercise2.*;


public class LineariseTransformApplet extends ExerciseApplet {
	static final private int LOG_X = 0;
	static final private int LOG_Y = 1;
	static final private int QUAD_Y = 2;
	
	private RandomRectangular xGenerator;
	private RandomNormal yGenerator;
	
	private XLabel yVarNameLabel;
	private HorizAxis xAxis;
	private VertAxis yAxis;
	private ScatterView theView;
	
	private XChoice transformChoice;
	private int currentTransformChoice = 0;
	
	private XPanel xSliderPanel, ySliderPanel;
	private CardLayout xSliderPanelLayout, ySliderPanelLayout;
	
	private XNoValueSlider transformXSlider, transformYSlider;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
		add("Center", getWorkingPanels(data));
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
				XPanel sliderPanel = new InsetPanel(0, 0, 0, 10);
				sliderPanel.setLayout(new ProportionLayout(0.5, 20));
				
				sliderPanel.add(ProportionLayout.LEFT, getYSliderPanel());
				sliderPanel.add(ProportionLayout.RIGHT, getXSliderPanel());
				
			bottomPanel.add(sliderPanel);
			
				XPanel choicePanel = new XPanel();
				choicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					transformChoice = new XChoice("Transformation:", XChoice.HORIZONTAL, this);
					transformChoice.addItem("Log X");
					transformChoice.addItem("Log Y");
					transformChoice.addItem("Neither works");
					registerStatusItem("transformChoice", transformChoice);
					
				choicePanel.add(transformChoice);
					
			bottomPanel.add(choicePanel);
			
			bottomPanel.add(createMarkingPanel(NO_HINTS));
			
				XPanel messagePanel = new XPanel();
				messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
					
					message = new ExerciseMessagePanel(this);
				messagePanel.add(message);
			bottomPanel.add(messagePanel);
		
		add("South", bottomPanel);
	}
	
	private XPanel getYSliderPanel() {
		ySliderPanel = new InsetPanel(0, 0, 0, 10);
		ySliderPanelLayout = new CardLayout();
		ySliderPanel.setLayout(ySliderPanelLayout);
			
		ySliderPanel.add("hide", new XPanel());
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new BorderLayout(0, 0));
			
				transformYSlider = new XNoValueSlider("Y", "Log Y", null, 0, 100, 0, this);
//				transformYSlider.setSnapToExtremes();
			mainPanel.add("Center", transformYSlider);
	
		ySliderPanel.add("show", mainPanel);
		return ySliderPanel;
	}
	
	private XPanel getXSliderPanel() {
		xSliderPanel = new InsetPanel(0, 0, 0, 10);
		xSliderPanelLayout = new CardLayout();
		xSliderPanel.setLayout(xSliderPanelLayout);
			
		xSliderPanel.add("hide", new XPanel());
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new BorderLayout(0, 0));
			
				transformXSlider = new XNoValueSlider("X", "Log X", null, 0, 100, 0, this);
//				transformXSlider.setSnapToExtremes();
			mainPanel.add("Center", transformXSlider);
	
		xSliderPanel.add("show", mainPanel);
		return xSliderPanel;
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
//		registerParameter("index", "int");					//	always registered
		registerParameter("count", "int");
		registerParameter("corr", "const");
		registerParameter("transformType", "choice");
		registerParameter("quadConcaveUp", "boolean");
		registerParameter("ordersOfMagnitude", "const");
	}
	
	private int getCount() {
		return getIntParam("count");
	}
	
	private NumValue getCorr() {
		return getNumValueParam("corr");
	}
	
	private int getTransformType() {
		return getIntParam("transformType");
	}
	
	private boolean isConcaveUp() {
		return getBooleanParam("quadConcaveUp");
	}
	
	private double getOrdersOfMagnitude() {
		return getDoubleParam("ordersOfMagnitude");
	}
	
	private String getXVarName() {
		return "Explanatory, X";
	}
	
	private String getYVarName() {
		return "Response, Y";
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new InsetPanel(60, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
			
			yVarNameLabel = new XLabel("", XLabel.LEFT, this);
		thePanel.add("North", yVarNameLabel);
			
			XPanel displayPanel = new XPanel();
			displayPanel.setLayout(new AxisLayout());
			
				yAxis = new VertAxis(this);
			displayPanel.add("Left", yAxis);
			
				xAxis = new HorizAxis(this);
			displayPanel.add("Bottom", xAxis);
			
				theView = new ScatterView(data, this, xAxis, yAxis, "x", "y");
				theView.lockBackground(Color.white);
			displayPanel.add("Center", theView);
		
		thePanel.add("Center", displayPanel);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		transformChoice.select(0);
		currentTransformChoice = 0;
		
		ySliderPanelLayout.show(ySliderPanel, "hide");
		xSliderPanelLayout.show(xSliderPanel, "hide");
		transformXSlider.setValue(0);
		transformYSlider.setValue(0);
		
		String xKey = "x";
		String yKey = "y";
		
		switch (getTransformType()) {
			case LOG_X:
				xKey = "xExp";
				break;
			case LOG_Y:
				yKey = "yExp";
				break;
			case QUAD_Y:
				yKey = "yQuad";
				break;
		}
		setupAxis(xAxis, xKey);
		setupAxis(yAxis, yKey);
		
		yVarNameLabel.setText(getYVarName());
		
		xAxis.setAxisName(getXVarName());
		
		theView.changeVariables(yKey, xKey);
	
		data.variableChanged("x");
		data.variableChanged("y");
	}
	
	private void setupAxis(NumCatAxis axis, String varKey) {
		NumVariable theVar = (NumVariable)data.getVariable(varKey);
		double max = Double.NEGATIVE_INFINITY;
		double min = Double.POSITIVE_INFINITY;
		for (int i=0 ; i<theVar.noOfValues() ; i++) {
			double xi = theVar.doubleValueAt(i);
			max = Math.max(max, xi);
			min = Math.min(min, xi);
		}
		
		axis.readNumLabels(min + " " + max + " " + (max + 1) + " 1");		//	no labels
	}
	
	protected void setDataForQuestion() {
		NumSampleVariable xUnscaledVar = (NumSampleVariable)data.getVariable("xUnscaled");
		ScaledVariable xVar = (ScaledVariable)data.getVariable("x");
		generateData(xUnscaledVar);
		scaleVariable(xUnscaledVar, xVar, getOrdersOfMagnitude(), getTransformType() == LOG_X);
		
		NumSampleVariable yBaseVar = (NumSampleVariable)data.getVariable("yBase");
		CorrelatedVariable yUnscaledVar = (CorrelatedVariable)data.getVariable("yUnscaled");
		ScaledVariable yVar = (ScaledVariable)data.getVariable("y");
		
		double corr = getCorr().toDouble();
		if (getTransformType() == QUAD_Y)
			yUnscaledVar.setMeanSdCorr(0.0, 1.0, 0.0, 9);
		else
			yUnscaledVar.setMeanSdCorr(0.0, 1.0, corr, 9);
		generateData(yBaseVar);
		scaleVariable(yUnscaledVar, yVar, getOrdersOfMagnitude(), getTransformType() == LOG_Y);
			
		if (getTransformType() == QUAD_Y) {
			QuadraticVariable quadOffsetVar = (QuadraticVariable)data.getVariable("quadOffset");
			double quadScale = 0.8 / Math.pow(1.0 - Math.abs(corr), 0.75);
			if (isConcaveUp())
				quadOffsetVar.changeParameters(0.25 * quadScale, -quadScale, quadScale);
			else
				quadOffsetVar.changeParameters(0.0, quadScale, -quadScale);
			
			NumVariable yQuadUnscaledVar = (NumVariable)data.getVariable("yQuadUnscaled");
			ScaledVariable yQuadVar = (ScaledVariable)data.getVariable("yQuad");
			scaleVariable(yQuadUnscaledVar, yQuadVar, getOrdersOfMagnitude(), false);
		}
	}
	
	private void generateData(NumSampleVariable sampleVar) {
		int n = getCount();
		sampleVar.setSampleSize(n);
		sampleVar.generateNextSample();
	}
	
	private void scaleVariable(NumVariable unscaledVar, ScaledVariable scaledVar,
																							double ordersOfMagnitude, boolean scaleForExp) {
		double min = Double.POSITIVE_INFINITY;
		double max = 0.0;
		for (int i=0 ; i<unscaledVar.noOfValues() ; i++) {
			double x = unscaledVar.doubleValueAt(i);
			min = Math.min(min, x);
			max = Math.max(max, x);
		}
		
//		double targetMin = Math.min(min, 0.0);
//		double targetMax = 1.0;
		double targetMin = scaleForExp ? 0 : Math.pow(10, -ordersOfMagnitude);
		double targetMax = scaleForExp ? ordersOfMagnitude : 1.0;
		double scale = (targetMax - targetMin) / (max - min);
		scaledVar.setScale(targetMin - scale * min, scale, 9);
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Use the pop-up menu to select a transformation to linearise the relationship.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText(getCorrectMessage(getTransformType()));
				messagePanel.insertText("\nUse the sliders to investigate the effect of transforming Y and X.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText(getCorrectMessage(getTransformType()));
				if (getTransformType() != QUAD_Y)
					messagePanel.insertText("\n(Use the slider to verify the effect of the transformation.)");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText(getWrongMessage(getTransformType(), currentTransformChoice));
					messagePanel.insertText("\n(Use the two sliders to see the effect of the transformations.)");
				break;
		}
	}
	
	private String getCorrectMessage(int correctTransformType) {
		switch (correctTransformType) {
			case QUAD_Y:
				return "Log transformations of X or Y cannot linearise a relationship that goes "
																			+ (isConcaveUp() ? "down then up." : "up then down.");
			case LOG_Y:
				return "Y is more variable on the " + (getCorr().toDouble() > 0 ? "right" : "left")
								+ " of the scatterplot (when its mean is high). Only a transformation of Y can simultaneously linearise the relationship and give constant response variability.";
			case LOG_X:
				return "Y is equally variable on the left and right of the scatterplot, so a transformation of X may be used to linearise the relationship.";
		}
		return null;
	}
	
	private String getWrongMessage(int correctTransformType, int selectedTransformType) {
		switch (correctTransformType) {
			case QUAD_Y:
				return "Log transformations of X or Y cannot linearise a relationship that goes "
																			+ (isConcaveUp() ? "down then up." : "up then down.");
			case LOG_Y:
				if (selectedTransformType == LOG_X)
					return "Y is more variable on the " + (getCorr().toDouble() > 0 ? "right" : "left")
								+ " of the scatterplot. A transformation of X cannot fix this problem.";
				else		//	QUAD_Y
					return "Since the relationship goes steadily " + (getCorr().toDouble() > 0 ? "up" : "down")
								+ ", a transformation of either Y or X could potentially linearise the relationship.";
			case LOG_X:
				if (selectedTransformType == LOG_Y)
					return "Y is equally variable for low X and high X, but a transformation of Y would make it "
												+ (getCorr().toDouble() > 0 ? "more" : "less") + " variable at high X.";
				else		//	QUAD_Y
					return "Since the relationship goes steadily " + (getCorr().toDouble() > 0 ? "up" : "down")
							+ ", a transformation of either Y or X could potentially linearise the relationship.";
		}
		return null;
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			xGenerator = new RandomRectangular(10, 0.0, 1.0);
			xGenerator.setSeed(nextSeed());
			NumSampleVariable xUnscaledVar = new NumSampleVariable("", xGenerator, 9);
		data.addVariable("xUnscaled", xUnscaledVar);
		
			ScaledVariable xVar = new ScaledVariable("", xUnscaledVar, "xUnscaled", 0.0, 1.0, 9);
		data.addVariable("x", xVar);
		
			yGenerator = new RandomNormal(10, 0.0, 1.0, 2.5);		//	+/- 2.5 SD
			yGenerator.setSeed(nextSeed());
			NumSampleVariable yBaseVar = new NumSampleVariable("ZY", yGenerator, 9);
		data.addVariable("yBase", yBaseVar);
			
			CorrelatedVariable yUnscaledVar = new CorrelatedVariable("", data, "xUnscaled", "yBase", 9);
		data.addVariable("yUnscaled", yUnscaledVar);
		
			ScaledVariable yVar = new ScaledVariable("", yUnscaledVar, "yUnscaled", 0.0, 1.0, 9);
		data.addVariable("y", yVar);
		
			LogVariable xExpVar = new LogVariable("", data, "x", 9);
			xExpVar.setInverse();		//		10 to power instead of log10()
		data.addVariable("xExp", xExpVar);
		
			LogVariable yExpVar = new LogVariable("", data, "y", 9);
			yExpVar.setInverse();		//		10 to power instead of log10()
		data.addVariable("yExp", yExpVar);
		
			QuadraticVariable quadOffsetVar = new QuadraticVariable("", xUnscaledVar, 0.0, 0.0, 0.0, 9);
		data.addVariable("quadOffset", quadOffsetVar);
		
			SumDiffVariable yQuadUnscaledVar = new SumDiffVariable("", data, "y", "quadOffset",
																																						SumDiffVariable.SUM);
		data.addVariable("yQuadUnscaled", yQuadUnscaledVar);
		
			ScaledVariable yQuadVar = new ScaledVariable("", yQuadUnscaledVar, "yQuadUnscaled", 0.0, 1.0, 9);
		data.addVariable("yQuad", yQuadVar);
		
		return data;
	}
	
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
		return (transformChoice.getSelectedIndex() == getTransformType()) ? ANS_CORRECT : ANS_WRONG;
	}
	
	protected void giveFeedback() {
		if (result == ANS_WRONG || getTransformType() == LOG_X)
			xSliderPanelLayout.show(xSliderPanel, "show");
		
		if (result == ANS_WRONG || getTransformType() == LOG_Y)
			ySliderPanelLayout.show(ySliderPanel, "show");
	}
	
	protected void showCorrectWorking() {
		currentTransformChoice = getTransformType();
		transformChoice.select(currentTransformChoice);
		
		xSliderPanelLayout.show(xSliderPanel, "show");
		ySliderPanelLayout.show(ySliderPanel, "show");
	}
	
	protected double getMark() {
		return (assessAnswer() == ANS_CORRECT) ? 1 : 0;
	}
	
	
//-----------------------------------------------------------
	
	private boolean localAction(Object target) {
		if (target == transformChoice) {
			int newChoice = transformChoice.getSelectedIndex();
			if (newChoice != currentTransformChoice) {
				currentTransformChoice = newChoice;
				noteChangedWorking();
			}
			return true;
		}
		else if (target == transformXSlider) {
			int oldPowerIndex = xAxis.getPowerIndex();
			int newPowerIndex = 300 - transformXSlider.getValue();
			if (oldPowerIndex > 250 && oldPowerIndex <= 250)
				xAxis.setAxisName("Transformed explanatory, Log X");
			else if (oldPowerIndex <= 250 && oldPowerIndex > 250)
				xAxis.setAxisName(getXVarName());
				
			xAxis.setPowerIndex(newPowerIndex);			//		does repaint()
			
			theView.repaint();
			return true;
		}
		else if (target == transformYSlider) {
			int oldPowerIndex = yAxis.getPowerIndex();
			int newPowerIndex = 300 - transformYSlider.getValue();
			if (oldPowerIndex > 250 && oldPowerIndex <= 250)
				yVarNameLabel.setText("Transformed response, Log Y");
			else if (oldPowerIndex <= 250 && oldPowerIndex > 250)
				yVarNameLabel.setText(getYVarName());
			
			yAxis.setPowerIndex(newPowerIndex);
			
			theView.repaint();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}