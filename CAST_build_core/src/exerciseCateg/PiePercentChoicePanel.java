package exerciseCateg;

import java.awt.*;

import dataView.*;
import random.*;
import exercise2.*;


public class PiePercentChoicePanel extends MultichoicePanel{
	static final private int kOptions = 5;
	static final private long kRandomSeedOffset = 485867238764l;
	
	private RandomInteger random[];
	private int correctPercent;
	
//================================================
	
	private class PercentOptionInfo extends NumOptionInformation {
		PercentOptionInfo(NumValue percent, boolean correct) {
			super(percent, correct);
			setUnitString("%");
		}
		
		public String getMessageString() {
			double attempt = getOptionValue().toDouble();
			
			String message;
			if (isCorrect())
				message = "The categories use up " + correctPercent + " percent of the pie chart's area.";
			else if (attempt < correctPercent) {
				message = "The categories use up more than ";
				message += getOptionValue();
				message += " percent of the pie chart's area.";
			}
			else {
				message = "The categories use up less than ";
				message += getOptionValue();
				message += " percent of the pie chart's area.";
			}
			return message;
		}
	}
	
//================================================
	
	public PiePercentChoicePanel(ExerciseApplet exerciseApplet) {
		super(exerciseApplet, kOptions);
		optionInfo = new NumOptionInformation[kOptions];
		
		random = new RandomInteger[kOptions];
		random[0] = new RandomInteger(5, 10, 1, exerciseApplet.nextSeed());
		random[1] = new RandomInteger(20, 30, 1, exerciseApplet.nextSeed());
		random[2] = new RandomInteger(42, 58, 1, exerciseApplet.nextSeed());
		random[3] = new RandomInteger(70, 80, 1, exerciseApplet.nextSeed());
		random[4] = new RandomInteger(90, 95, 1, exerciseApplet.nextSeed());
		
		setupChoices(0);				//		will be changed from 0 to correct option by changeOptions()
		setupPanel();
	}
	
	public void setRandomSeed(long seed) {
		for (int i=0 ; i<random.length ; i++)
			random[i].setSeed(seed + i * kRandomSeedOffset);
	}
	
	public int getCorrectPercent() {
		return correctPercent;
	}
	
	public int getSelectedPercent() {
		int selectedIndex = getSelectedOption();
		NumValue selectedVal = ((NumOptionInformation)optionInfo[selectedIndex]).getOptionValue();
		return (int)Math.round(selectedVal.toDouble());
	}
	
	public void setupChoices(int correctOption) {
		for (int i=0 ; i<kOptions ; i++) {
			int percent = (i == correctOption) ? correctPercent : random[i].generateOne();
			optionInfo[i] = new PercentOptionInfo(new NumValue(percent, 0), i == correctOption);
		}
		
//		randomiseOptions();
		findCorrectOption();
	}
	
	protected Component createOptionPanel(int optionIndex, ExerciseApplet exerciseApplet) {
		return new OptionShortTextPanel(optionInfo, optionIndex, exerciseApplet);
	}
	
	public void changeOptions(int correctPercent) {
		this.correctPercent = correctPercent;
		int correctOption = (correctPercent < 15) ? 0
												: (correctPercent < 36) ? 1
												: (correctPercent < 64) ? 2
												: (correctPercent < 85) ? 3 : 4;
		setupChoices(correctOption);
		for (int i=0 ; i<option.length ; i++)
			((OptionShortTextPanel)option[i]).changeContent();
	}
}