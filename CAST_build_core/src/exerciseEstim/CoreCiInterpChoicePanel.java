package exerciseEstim;

import java.awt.*;

import dataView.*;
import exercise2.*;


abstract public class CoreCiInterpChoicePanel extends MultichoicePanel {
	private int nWrongOptions;
	
	protected String sampleValues, popnValues, newValue, newSample, samples,
																				sampleStat, popnParam, success, units;
	protected NumValue lowLimit, highLimit;
	
	public CoreCiInterpChoicePanel(ExerciseApplet exerciseApplet, int nDisplayOptions,
																											int nWrongOptions) {
		super(exerciseApplet, nDisplayOptions);
		this.nWrongOptions = nWrongOptions;
		optionInfo = new OptionInformation[nDisplayOptions];
		doParamSetup("", "", "", "", "", "", "", "", null, null, "");
		setupPanel();
	}
	
	private void doParamSetup(String sampleValues, String popnValues, String newValue,
									String newSample, String samples, String sampleStat, String popnParam,
									String success, NumValue lowLimit, NumValue highLimit, String units) {
		this.sampleValues = sampleValues;
		this.popnValues = popnValues;
		this.newValue = newValue;
		this.newSample = newSample;
		this.samples = samples;
		this.sampleStat = sampleStat;
		this.popnParam = popnParam;
		this.success = success;
		this.lowLimit = lowLimit;
		this.highLimit = highLimit;
		this.units = units;
		
		setupChoices();
	}
	
	public void setupChoices() {
		optionInfo[0] = createOptionInfo(0);	//	correct answer
		
		int wrongPermutation[] = exerciseApplet.createPermutation(nWrongOptions);
		for (int i=1 ; i<optionInfo.length ; i++)
			optionInfo[i] = createOptionInfo(wrongPermutation[i] + 1);
																	//	random wrong answers out of nWrongOptions possibles
		randomiseOptions();
		findCorrectOption();
	}
	
	abstract protected OptionInformation createOptionInfo(int optionIndex);
	
	protected Component createOptionPanel(int optionIndex, ExerciseApplet exerciseApplet) {
		return new OptionLongTextPanel(optionInfo, optionIndex, exerciseApplet);
	}
	
	public void changeOptions(String sampleValues, String popnValues, String newValue,
								String newSample, String samples, String sampleStat, String popnParam,
								String success, NumValue lowLimit, NumValue highLimit, String units) {
		doParamSetup(sampleValues, popnValues, newValue, newSample, samples, sampleStat,
																			popnParam, success, lowLimit, highLimit, units);
		
		for (int i=0 ; i<option.length ; i++)
			((OptionLongTextPanel)option[i]).changeContent();
	}
}