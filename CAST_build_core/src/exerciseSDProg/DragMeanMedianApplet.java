package exerciseSDProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import random.*;
import coreVariables.*;
import exercise2.*;

import exerciseSD.*;


public class DragMeanMedianApplet extends ExerciseApplet {
//	static final private String VAR_NAME_PARAM = "varName";
	
	static final private double kLowTailProb = 0.005;
	static final private double kMinWidthPropn = 0.6;
	static final private double kCloseSlopFactor = 2.0;
	
	static final private double kSkewFactor = 0.05;
	static final private double kMeanErrorFactor = 2.0;
	
//	private String[] varName;
	
	private RandomGamma generator;
	
	private HorizAxis theAxis;
	private DragMeanMedStackedView theView;
	
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
		registerParameter("shape", "const");
		registerParameter("count", "int");
		registerParameter("axis", "string");
		registerParameter("decimals", "int");
		registerParameter("varName", "string");
	}
	
	protected double getShapeValue() {
		return getDoubleParam("shape");
	}
	
	public String getAxisInfo() {
		return getStringParam("axis");
	}
	
	protected int getCount() {
		return getIntParam("count");
	}
	
	public int getDecimals() {
		return getIntParam("decimals");
	}
	
	public String getVarName() {
		return getStringParam("varName");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			theAxis = new HorizAxis(this);
		thePanel.add("Bottom", theAxis);
		
			theView = new DragMeanMedStackedView(data, this, theAxis, "y", 0);
			theView.lockBackground(Color.white);
			registerStatusItem("meanMedian", theView);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		theAxis.readNumLabels(getAxisInfo());
		theAxis.setAxisName(getVarName());
		theAxis.invalidate();
		
		theView.setMeanMedianDecimals(getDecimals());
		theView.resetMeanMedian();
		theView.setCrossSize(getCount() > 40 ? DataView.MEDIUM_CROSS : DataView.LARGE_CROSS);
	}
	
	protected void setDataForQuestion() {
		NumSampleVariable coreVar = (NumSampleVariable)data.getVariable("base");
		RandomGamma gammaGenerator = (RandomGamma)coreVar.getGenerator();
		
		double shape = getShapeValue();
		double absShape = Math.abs(shape);
		gammaGenerator.setShape(absShape);
		
//		System.out.println("new shape = " + shape);
		
		double lowQuantile = GammaDistnVariable.gammaQuant(kLowTailProb, absShape);
		double highQuantile = GammaDistnVariable.gammaQuant(1.0 - kLowTailProb, absShape);
		gammaGenerator.setTruncation(lowQuantile, highQuantile);
		
//		System.out.println("lowQuantile = " + lowQuantile + ", highQuantile = " + highQuantile);
		
		int n = getCount();
		coreVar.setSampleSize(n);
		coreVar.generateNextSample();
		
		StringTokenizer st = new StringTokenizer(getAxisInfo());
		double axisMin = Double.parseDouble(st.nextToken());
		double axisMax = Double.parseDouble(st.nextToken());
		
		Random uniformGenerator = new Random(nextSeed());
		double minWidth = kMinWidthPropn * (axisMax - axisMin);
		double dataMin = axisMin + uniformGenerator.nextDouble() * (axisMax - axisMin - minWidth);
		double dataMax = dataMin + minWidth + uniformGenerator.nextDouble() * (axisMax - dataMin - minWidth);
		
		if (shape < 0.0) {
			double temp = dataMax;
			dataMax = dataMin;
			dataMin = temp;
		}
		
		double factor = (dataMax - dataMin) / (highQuantile - lowQuantile);
		double shift = dataMin - lowQuantile * factor;
		
		ScaledVariable yVar = (ScaledVariable)data.getVariable("y");
		yVar.setScale(shift, factor, getDecimals());
		yVar.clearSortedValues();
		
		data.variableChanged("base");
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		NumValue correctMean = theView.getCorrectMean();
		NumValue correctMedian = theView.getCorrectMedian();
		
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Drag the two arrows to show your best guess at the values of the mean and median.\nYou should be able to determine the median fairly accurately (count the crosses). However your mean can be a little further from its correct value.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				double slop = theView.getColumnSlop();
				if (Math.abs(correctMean.toDouble() - correctMedian.toDouble()) < slop)
					messagePanel.insertText("The distribution is reasonably symmetric (or at least not particularly skew), so the mean and median should be close to each other.");
				else if (correctMean.toDouble() < correctMedian.toDouble())
					messagePanel.insertText("Since the distribution has a long tail to the left, the mean of these data should be less than the median.");
				else
					messagePanel.insertText("Since the distribution has a long tail to the right, the mean of these data should be greater than the median.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("Your guess of the mean and median are as close as could be expected by eye.");
				messagePanel.insertText("\nThe exact value of the median is " + correctMedian
																										+ " and the mean is " + correctMean + ".");
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Close!\n");
				messagePanel.insertText("Your guess of the mean and median are reasonably close to the correct values but you should be able to do better.");
				setWrongMessages(messagePanel, correctMean, correctMedian);
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Not close enough!");
				setWrongMessages(messagePanel, correctMean, correctMedian);
				break;
		}
	}
	
	private void setWrongMessages(MessagePanel messagePanel, NumValue correctMean, NumValue correctMedian) {
		double attemptMean = theView.getMeanGuess().toDouble();
		double attemptMedian = theView.getMedianGuess().toDouble();
		
		if (medianResult == ANS_WRONG || medianResult == ANS_CLOSE) {
			if (attemptMedian < correctMedian.toDouble())
				messagePanel.insertText("\nYour estimate of the median is too low. (" + (getCount() / 2) + " crosses should be on each side of the median.)");
			else
				messagePanel.insertText("\nYour estimate of the median is too high. (" + (getCount() / 2) + " crosses should be on each side of the median.)");
		}
		else
			messagePanel.insertText("\nYour estimate of the median is close enough to the correct value (" + correctMedian + ").");
		
		if (isSkew && wrongSkewness) {
			if (correctMean.toDouble() < correctMedian.toDouble())
				messagePanel.insertText("\nFrom the shape of the distribution, the mean of these data should be less than the median.");
			else
				messagePanel.insertText("\nFrom the shape of the distribution, the mean of these data should be greater than the median.");
		}
		else if (meanResult == ANS_WRONG || meanResult == ANS_CLOSE) {
			double approxSlop = theView.getColumnSlop() * kCloseSlopFactor;
			if (!isSkew && Math.abs(attemptMean - attemptMedian) > approxSlop)
				messagePanel.insertText("\nThe mean and median should be closer to each other.");
			else if (attemptMean < correctMean.toDouble())
				messagePanel.insertText("\nYour estimate of the mean is too low. (The point of balance of the crosses is the mean.)");
			else
				messagePanel.insertText("\nYour estimate of the mean is too high. (The point of balance of the crosses is the mean.)");
		}
		else
			messagePanel.insertText("\nYour estimate of the mean is close enough to the correct value (" + correctMean + ").");
	}
	
	protected int getMessageHeight() {
		return 150;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			generator = new RandomGamma(10, 1.0, 1.0, 3.0);
			generator.setSeed(nextSeed());
			NumSampleVariable baseVar = new NumSampleVariable("Std normal", generator, 9);
			baseVar.generateNextSample();
		data.addVariable("base", baseVar);
		
			ScaledVariable yVar = new ScaledVariable(getVarName(), baseVar,
																																"base", 0.0, 1.0, 9);
		data.addVariable("y", yVar);
		
		return data;
	}
	
	
//-----------------------------------------------------------
	
	private int medianResult, meanResult;
	private boolean isSkew, wrongSkewness;
	
	private double getDataRange() {
		NumVariable yVar = (NumVariable)data.getVariable("y");
		ValueEnumeration ye = yVar.values();
		double yMin = ye.nextDouble();
		double yMax = yMin;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			yMin = Math.min(yMin, y);
			yMax = Math.max(yMax, y);
		}
		return yMax - yMin;
	}
	
	protected int assessAnswer() {
		double correctMedian = theView.getCorrectMedian().toDouble();
		double attemptMedian = theView.getMedianGuess().toDouble();
		
		double exactSlop = theView.getColumnSlop();
		
		double absMedianError = Math.abs(attemptMedian - correctMedian);
		medianResult = (absMedianError <= exactSlop) ? ANS_CORRECT
											: (absMedianError <= exactSlop * kCloseSlopFactor) ? ANS_CLOSE
											: ANS_WRONG;
		
		double correctMean = theView.getCorrectMean().toDouble();
		double attemptMean = theView.getMeanGuess().toDouble();
		
		double dataRange = getDataRange();
		isSkew = Math.abs(correctMedian - correctMean) / dataRange > kSkewFactor;
		
		wrongSkewness = ((correctMedian - correctMean) > 0) != ((attemptMedian - attemptMean) > 0);
		if (isSkew && wrongSkewness)
			meanResult = ANS_WRONG;
		else {
			double absMeanError = Math.abs(attemptMean - correctMean);
			double meanSlop = exactSlop * kMeanErrorFactor
											* dataRange / (theAxis.maxOnAxis - theAxis.minOnAxis);
			meanResult = (absMeanError <= meanSlop) ? ANS_CORRECT
											: (absMeanError <= meanSlop * kCloseSlopFactor) ? ANS_CLOSE
											: ANS_WRONG;
		}
		
		if (meanResult == ANS_CORRECT && medianResult == ANS_CORRECT)
			return ANS_CORRECT;
		else if (meanResult == ANS_WRONG || medianResult == ANS_WRONG)
			return ANS_WRONG;
		else
			return ANS_CLOSE;
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		theView.showCorrectMeanMedian();
		theView.repaint();
	}
	
	protected double getMark() {
		assessAnswer();
		
		double mark = 0.0;
		if (medianResult == ANS_CORRECT)
			mark += 0.5;
		else if (medianResult == ANS_CLOSE)
			mark += 0.4;
			
		if (meanResult == ANS_CORRECT)
			mark += 0.5;
		else if (meanResult == ANS_CLOSE)
			mark += 0.4;
			
		return mark;
	}
	
}