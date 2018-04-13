package inferenceProg;

import java.util.*;

import dataView.*;
import utils.*;
import formula.*;
import imageUtils.*;



public class FindNForPiApplet extends FindNForMeanApplet {
	
	protected void setValuesFromSliders(NumVariable seVar, NumVariable plusMinusVar,
																			NumVariable widthVar, int n, double guessParam) {
		double se = Math.sqrt(guessParam * (1 - guessParam) / n);
		double plusMinus = 2.0 * se;
		double width = 2.0 * plusMinus;
		
		NumValue seValue = (NumValue)seVar.valueAt(0);
		seValue.setValue(se);
		
		NumValue plusMinusValue = (NumValue)plusMinusVar.valueAt(0);
		plusMinusValue.setValue(plusMinus);
		
		NumValue widthValue = (NumValue)widthVar.valueAt(0);
		widthValue.setValue(width);
	}
	
	protected ParameterSlider getGuessSlider(String lowGuess, String highGuess,
																												double startGuess, int noOfSteps) {
		StringTokenizer st = new StringTokenizer(translate("Guess at value of * (and p)"), "*");
		String piGuess = st.nextToken() + MText.expandText("#pi#") + st.nextToken();
		return new ParameterSlider(new NumValue(lowGuess), new NumValue(highGuess),
									new NumValue(startGuess), noOfSteps, piGuess, this);
	}
	
	protected OneValueImageView getSeValueView(DataSet data) {
		return new OneValueImageView(data, "se", this, "ci/sePiFormula.png", 32, maxWidth);
	}
	
	protected OneValueImageView getPlusMinusValueView(DataSet data) {
		return new OneValueImageView(data, "plusMinus", this, "ci/ciPi95.png", 19, maxWidth);
	}
}