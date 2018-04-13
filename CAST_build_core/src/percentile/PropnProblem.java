package percentile;

import dataView.*;
import random.*;


public class PropnProblem extends CorePercentileProblem {
	private RandomUniform sampSizeGenerator;
	private RandomRectangular boundaryGenerator;
	
	private NumValue propnLess = new NumValue(0.0, 4);
	private NumValue correctPropn = new NumValue(0.0, 4);
	private NumValue percentage = new NumValue(0.0, 2);
	private NumValue expectedCount = new NumValue(0.0, 2);
	private NumValue returnPeriod = new NumValue(0.0, 2);
	
	public PropnProblem(DataSet data, String aboutDataText, String[] questionText,
												String aboveBelowStrings, int longestQuestionIndex,
												NumValue[] boundaryMinMax, int[] sampSizeMinMax, int answerDecimals) {
		super(data, aboutDataText, questionText, aboveBelowStrings, longestQuestionIndex,
																																					answerDecimals);
		
		createRandomGenerators(boundaryMinMax, sampSizeMinMax);
	}
	
	private void createRandomGenerators(NumValue[] boundaryMinMax, int[] sampSizeMinMax) {
		sampSizeGenerator = new RandomUniform(1, sampSizeMinMax[0], sampSizeMinMax[1]);
		boundaryGenerator = new RandomRectangular(1, boundaryMinMax[0].toDouble(), boundaryMinMax[1].toDouble());
		
		randomValues = new Value[3];
		longestValues = new Value[3];
	
		randomValues[0] = belowLabel;
		randomValues[1] = new NumValue(boundaryMinMax[0].toDouble(),
														Math.max(boundaryMinMax[0].decimals, boundaryMinMax[1].decimals));
		randomValues[2] = new NumValue(1, 0);
		
		longestValues[0] = aboveLabel;
		longestValues[1] = boundaryMinMax[1];
		longestValues[2] = new NumValue(sampSizeMinMax[1], 0);
	}
	
	public void solveExercise() {
		message.setText(2);
		answer.setToCorrectAnswer(exactAnswer);
		((PropnWorkingPanel)working).setReferenceValue((NumValue)randomValues[1]);
	}
	
	public void changeQuestion() {
		changeQuestionInfo();
		
		double r1 = boundaryGenerator.generateOne();
		double r2 = sampSizeGenerator.generateOne();
		((NumValue)randomValues[1]).setValue(r1);
		((NumValue)randomValues[2]).setValue(r2);
		
		double refVal = ((NumValue)randomValues[1]).toDouble();
		boolean lower = randomValues[0] == belowLabel;
		
		NumVariable yVar = (NumVariable)data.getVariable("y");
		int n = yVar.noOfValues();
		ValueEnumeration ye = yVar.values();
		int count = 0;
		
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			if (y < refVal)
				count ++;
		}
		
		double pLess = count / (double)n;
		propnLess.setValue(pLess);
		double pCorrect = lower ? pLess : (1.0 - pLess);
		correctPropn.setValue(pCorrect);
		percentage.setValue(pCorrect * 100.0);
		expectedCount.setValue(pCorrect * r2);
		returnPeriod.setValue(1.0 / pCorrect);
		double exactSlop = 0.0;
		double approxSlop = 0.0;
		if (questionIndex == 0) {		//	percentage
			messageArray[2] = "Correct! (The exact answer is " + percentage.toString() + ")";
			messageArray[3] = "Close but you could do better! The exact answer is " + percentage.toString();
			messageArray[4] = "Wrong. Multiply the proportion by 100.";
			
			String propnString = lower ? correctPropn.toString()
						: ("(1 - " + propnLess.toString() + ") = " + correctPropn.toString());
			messageArray[5] = "The percentage is " + propnString + " times 100 = "
																																+ percentage.toString();
			exactAnswer = percentage;
			exactSlop = 0.5001;
			approxSlop = 0.5001;
		}
		else if (questionIndex == 1) {		//	expected count
			String nString = randomValues[2].toString();
			messageArray[2] = "Correct! (The exact answer is " + expectedCount.toString() + ")";
			messageArray[3] = "Close but you could do better! The exact answer is " + expectedCount.toString();
			messageArray[4] = "Wrong. Multiply the proportion by " + nString + ".";
			
			String propnString = lower ? correctPropn.toString()
						: ("(1 - " + propnLess.toString() + ") = " + correctPropn.toString());
			messageArray[5] = "The expected number is " + propnString + " times " + nString + " = "
																														+ expectedCount.toString();
			exactAnswer = expectedCount;
			exactSlop = (n > 10) ? n * 0.005001 : 0.05;
			approxSlop = 2 * exactSlop;
		}
		else if (questionIndex == 2) {		//	return period
			messageArray[2] = "Correct! (The exact answer is " + returnPeriod.toString() + ")";
			messageArray[3] = "Close but you could do better! The exact answer is " + returnPeriod.toString();
			messageArray[4] = "Wrong. The expected time is the inverse of the proportion.";
			
			String propnString = lower ? correctPropn.toString()
																								: ("(1 - " + propnLess.toString() + ")");
			messageArray[5] = "The expected time till the first occurrence is 1 / " + propnString
																									+ " = " + returnPeriod.toString();
			exactAnswer = returnPeriod;
			exactSlop = (returnPeriod.toDouble() > 10) ? 0.5001
									: (returnPeriod.toDouble() > 2) ? 0.05001 : 0.005001;
			approxSlop = 2 * exactSlop;
		}
		double ans = exactAnswer.toDouble();
		lowExactAnswer = ans - exactSlop;
		highExactAnswer = ans + exactSlop;
		lowApproxAnswer = ans - approxSlop;
		highApproxAnswer = ans + approxSlop;
		
		if (message != null) {
			message.changeText(messageArray);
			message.setText(0);
			answer.reset();
		}
	}
	
}