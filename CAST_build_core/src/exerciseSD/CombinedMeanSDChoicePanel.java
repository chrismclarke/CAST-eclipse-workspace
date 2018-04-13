package exerciseSD;

import java.awt.*;
import java.util.*;

import dataView.*;
import formula.*;

import exercise2.*;


public class CombinedMeanSDChoicePanel extends MultichoicePanel {
	static final public int MEAN = 0;
	static final public int SD = 1;
	
	static final private int kOptions = 3;
	static final private double kDifferentSdFactor = 0.6;
	
	private DataSet data;
	private String yKey, groupKey;
	private int decimals;
	private int meanSdType;
	
	private boolean hasSingleton;
	private boolean meansDifferent, sdsDifferent;
	
	private NumValue correctSD;
	private double withinGroupSD, minSD, maxSD;
	
	private NumValue correctMean;
	private double minMean, maxMean, meanWithoutSingletons;
	
//================================================
	
	private class CombinedSdOptionInfo extends NumOptionInformation {
		CombinedSdOptionInfo(NumValue sd, boolean correct) {
			super(sd, correct);
		}
		
		public String getMessageString() {
			double attempt = getOptionValue().toDouble();
			double correct = correctSD.toDouble();
			
			String message;
			if (isCorrect())
				message = "The combined standard deviation is " + correctSD;
			else if (hasSingleton) {
				if (meansDifferent) {			//		outlier
					message = "The outlier will increase the standard deviation";
					if (attempt == minSD)
						message += ".";
					else if (attempt < minSD)
						message += ", not decrease it.";
					else if (attempt > correct)
						message += MText.expandText(", but not by this much. (Think about x#bar# #plusMinus# s and x#bar# #plusMinus# 2s.");
					else				//		(attempt < correct)
						message += MText.expandText(" by more than this. (Think about x#bar# #plusMinus# s and x#bar# #plusMinus# 2s.");
				}
				else {										//		not outlier
					message = "The single value is not an outlier, so it will have little effect on the st devn. ";
					if (attempt < correct)
						message += " The combined st devn is more than ";
					else
						message += " The combined st devn is less than ";
					message += getOptionValue();
				}
			}
			else if (meansDifferent) {
				message = "When groups with different means are combined, the st devn is usually bigger than the group st devns.";
				if (attempt <= minSD)
					message += " It must be bigger than the smallest group st devn.";
				else if (attempt > correct)
					message += " However it cannot be as high as this.";
				else
					message += " The variation between group means increases the st devn more than this.";
			}
			else if (sdsDifferent) {
				message = "The group means are similar, so the overall st devn should be similar in the middle of the group st devns.";
				if (attempt > correct)		// 		!hasSingleton and !meansDifferent and sdsDifferent
					message += " Your guess is too high.";
				else
					message += " Your guess is too low.";
			}
			else {		// 		!hasSingleton and !meansDifferent and !sdsDifferent
				message = "The group means and st devns are similar, so the overall st devn should be similar to those inside the groups.";
				if (attempt > correct)
					message += " Your guess is too high.";
				else
					message += " Your guess is too low.";
			}
			return message;
		}
	}
	
//================================================
	
	private class CombinedMeanOptionInfo extends NumOptionInformation {
		CombinedMeanOptionInfo(NumValue mean, boolean correct) {
			super(mean, correct);
		}
		
		public String getMessageString() {
			double attempt = getOptionValue().toDouble();
			double correct = correctMean.toDouble();
			
			String message;
			if (isCorrect())
				message = "The combined mean is " + correctMean;
			else if (hasSingleton) {
				if (meansDifferent) {			//		outlier
					message = "An outlier will pull the mean towards it";
					if (attempt == meanWithoutSingletons)
						message += ".";
					else if (attempt < meanWithoutSingletons == meanWithoutSingletons < correct)
						message += ", not push it in the opposite direction.";
					else if (attempt > correct == meanWithoutSingletons < correct)
						message += ", but not by this much. (It pulls by about 1/n times the distance from the old mean.)";
					else				//		(attempt < correct)
						message += " by more than this. (It pulls by about 1/n times the distance from the old mean.)";
				}
				else {										//		not outlier
					message = "The single value is not an outlier; it will pull the mean towards it but by very little. ";
					if (attempt < correct)
						message += " The combined mean is more than ";
					else
						message += " The combined mean is less than ";
					message += getOptionValue();
				}
			}
			else {		//	!hasSingleton
				message = "When groups are combined, the mean is usually close to the average of the group means";
				if (attempt <= minMean)
					message += ". It must be more than the smallest group mean.";
				else if (attempt >= maxMean)
					message += ". It must be less than the largest group mean.";
				else if (attempt < correct)
					message += ", but your guess is too low.";
				else
					message += ", but your guess is too high.";
			}
			return message;
		}
	}
	
//================================================
	
	
	public CombinedMeanSDChoicePanel(ExerciseApplet exerciseApplet, DataSet data, String yKey,
																											String groupKey, int meanSdType, int decimals) {
		super(exerciseApplet, kOptions);
		this.data = data;
		this.yKey = yKey;
		this.groupKey = groupKey;
		this.meanSdType = meanSdType;
		this.decimals = decimals;
		
		if (meanSdType == MEAN)
			setUnselectedString("You must select a value for the mean by clicking a radio button.");
		else
			setUnselectedString("You must select a value for the standard deviation by clicking a radio button.");
		
		optionInfo = new NumOptionInformation[kOptions];
		setupChoices();
		setupPanel();
	}
	
	public void setDecimals(int decimals) {
		this.decimals = decimals;
	}
	
	public NumValue getCorrectValue() {
		return (meanSdType == MEAN) ? correctMean : correctSD;
	}
	
	public void setupChoices() {
		CatVariable groupVar = (CatVariable)data.getVariable(groupKey);
		int nGroups = groupVar.noOfCategories();
		
		double sy[] = new double[nGroups];
		double syy[] = new double[nGroups];
		int n[] = new int[nGroups];
		
		NumVariable yVar = (NumVariable)data.getVariable(yKey);
		ValueEnumeration ye = yVar.values();
		ValueEnumeration ge = groupVar.values();
		
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			int group = groupVar.labelIndex(ge.nextValue());
			sy[group] += y;
			syy[group] += y * y;
			n[group] ++;
		}
		
		double syOverall = 0.0;
		double syyOverall = 0.0;
		int nOverall = 0;
		double syOutlier = 0.0;
		int nOutlier = 0;
		
		double mean[] = new double[nGroups];
		double sd[] = new double[nGroups];
		
		minSD = Double.POSITIVE_INFINITY;
		maxSD = 0.0;
		
		minMean = Double.POSITIVE_INFINITY;
		maxMean = Double.NEGATIVE_INFINITY;
		
		meansDifferent = false;
		
		double ssWithin = 0.0;
		int dfWithin = 0;
		double sWithin = 0.0;
		double nWithin = 0;
		
		for (int i=0 ; i<nGroups ; i++) {
			syOverall += sy[i];
			syyOverall += syy[i];
			nOverall += n[i];
			mean[i] = sy[i] / n[i];
			minMean = Math.min(minMean, mean[i]);
			maxMean = Math.max(maxMean, mean[i]);
			if (n[i] == 1) {
				sd[i] = Double.NaN;
				syOutlier += mean[i];
				nOutlier ++;
			}
			else {
				double ssWithinI = syy[i] - sy[i] * sy[i] / n[i];
				sd[i] = Math.sqrt(ssWithinI / (n[i] - 1));
				minSD = Math.min(minSD, sd[i]);
				maxSD = Math.max(maxSD, sd[i]);
				ssWithin += ssWithinI;
				dfWithin += n[i] - 1;
				sWithin += sy[i];
				nWithin += n[i];
			}
			for (int j=0 ; j<i ; j++)
				if (differentMeans(sd[i], mean[i], sd[j], mean[j]))
					meansDifferent = true;
		}
		
		withinGroupSD = Math.sqrt(ssWithin / dfWithin);
		meanWithoutSingletons = sWithin / nWithin;
		
		hasSingleton = nOutlier > 0;
		sdsDifferent = minSD / maxSD < kDifferentSdFactor;
		
		correctSD = new NumValue(Math.sqrt((syyOverall - syOverall * syOverall / nOverall)
																																	/ (nOverall - 1)), decimals);
		correctMean = new NumValue(syOverall / nOverall, decimals);
		
			double offset = 2 * Math.pow(10.0, -decimals);
			minMean -= offset;				//	to prevent all options for the mean being identical
			maxMean += offset;
		
		int optionIndex = 0;
		Random rand = new Random(exerciseApplet.nextSeed());
		boolean firstRandLow = (rand.nextDouble() < 0.5);
		boolean secondRandLow = (rand.nextDouble() < 0.66667) == firstRandLow;
		
		if (meanSdType == SD) {
			optionInfo[optionIndex++] = new CombinedSdOptionInfo(correctSD, true);
			double correct = correctSD.toDouble();
			
			if (hasSingleton) {
				if (meansDifferent) {
					double outlierMean = syOutlier / nOutlier;
					boolean extremeOutlier = Math.abs(outlierMean - meanWithoutSingletons) > 10 * withinGroupSD;
					double firstVal, secondVal;
					if (extremeOutlier) {		//	don't expect them to identify sd unless it is far too big
						firstVal = withinGroupSD;
						secondVal = firstRandLow ? 0.9 * withinGroupSD : Math.abs(outlierMean - meanWithoutSingletons);
					}
					else {
						firstVal = firstRandLow ? withinGroupSD : 1.5 * correct;
						secondVal = secondRandLow ? 0.9 * withinGroupSD : 2.5 * correct;
					}
					optionIndex = addTwoOptions(firstVal, secondVal, optionIndex);
				}
				else {
					if (!sdTooClose(withinGroupSD, optionIndex)) {
						optionInfo[optionIndex++] = new CombinedSdOptionInfo(new NumValue(withinGroupSD, decimals), false);
						if (firstRandLow)
							optionInfo[optionIndex++] = new CombinedSdOptionInfo(new NumValue(correct * 0.7, decimals), false);
						else
							optionInfo[optionIndex++] = new CombinedSdOptionInfo(new NumValue(correct * 1.3, decimals), false);
					}
					else
						optionIndex = addTwoOptions(correct * 0.7, correct * 1.3, optionIndex);
				}
			}
			else if (meansDifferent) {
				double firstVal = firstRandLow ? withinGroupSD : 2 * correct;
				double secondVal = secondRandLow ? minSD : 3 * correct;
				optionIndex = addTwoOptions(firstVal, secondVal, optionIndex);
			}
			else if (sdsDifferent) {
				double firstVal = firstRandLow ? minSD : maxSD;
				double secondVal = secondRandLow ? 0.7 * minSD : 1.5 * maxSD;
				optionIndex = addTwoOptions(firstVal, secondVal, optionIndex);
			}
			else {
				double firstVal = firstRandLow ? 0.7 * correct : 1.3 * correct;
				double secondVal = secondRandLow ? 0.5 * correct : 1.5 * correct;
				optionIndex = addTwoOptions(firstVal, secondVal, optionIndex);
			}
		}
		else {		//	meanSdType == MEAN
			optionInfo[optionIndex++] = new CombinedMeanOptionInfo(correctMean, true);
			double correct = correctMean.toDouble();
			
			if (hasSingleton) {
				double outlierMean = syOutlier / nOutlier;
				if (meansDifferent) {
					double firstVal = firstRandLow ? meanWithoutSingletons : (meanWithoutSingletons + outlierMean) / 2;
					double secondVal = secondRandLow ? 2 * meanWithoutSingletons - correct : (meanWithoutSingletons + 2 * outlierMean) / 3;
					optionIndex = addTwoOptions(firstVal, secondVal, optionIndex);
				}
				else {
					if (!meanTooClose(meanWithoutSingletons, optionIndex))
						optionInfo[optionIndex++] = new CombinedMeanOptionInfo(new NumValue(meanWithoutSingletons, decimals), false);
					double halfwayMean = (meanWithoutSingletons + outlierMean) / 2;
					if (!meanTooClose(halfwayMean, optionIndex))
						optionInfo[optionIndex++] = new CombinedMeanOptionInfo(new NumValue(halfwayMean, decimals), false);
					if (optionIndex < kOptions)
						optionInfo[optionIndex++] = new CombinedMeanOptionInfo(new NumValue(correct - withinGroupSD, decimals), false);
					if (optionIndex < kOptions)
						optionInfo[optionIndex++] = new CombinedMeanOptionInfo(new NumValue(correct + withinGroupSD, decimals), false);
				}
			}
			else {
				double firstVal = firstRandLow ? minMean : maxMean;
				double secondVal = secondRandLow ? minMean - (correct - minMean) / 2 : maxMean + (maxMean - correct) / 2;
				optionIndex = addTwoOptions(firstVal, secondVal, optionIndex);
			}
		}
		
		sortOptions();
		
		findCorrectOption();
	}
	
	private int addTwoOptions(double firstVal, double secondVal, int optionIndex) {
		if (meanSdType == MEAN) {
			optionInfo[optionIndex++] = new CombinedMeanOptionInfo(new NumValue(firstVal, decimals), false);
			optionInfo[optionIndex++] = new CombinedMeanOptionInfo(new NumValue(secondVal, decimals), false);
		}
		else  {
			optionInfo[optionIndex++] = new CombinedSdOptionInfo(new NumValue(firstVal, decimals), false);
			optionInfo[optionIndex++] = new CombinedSdOptionInfo(new NumValue(secondVal, decimals), false);
		}
		return optionIndex;
	}
	
	private boolean differentMeans(double sd0, double mean0, double sd1, double mean1) {
		if (Double.isNaN(sd0) && Double.isNaN(sd1))			//	both singletons
			return false;
		else {
			double diffMean = Math.abs(mean0 - mean1);
			if (Double.isNaN(sd0))
				return diffMean > 3.0 * sd1;
			else if (Double.isNaN(sd1))
				return diffMean > 3.0 * sd0;
			else
				return diffMean > 2.0 * Math.min(sd0, sd1);
		}
	}
	
	private boolean sdTooClose(double newSD, int nOld) {
		if (newSD > 0.65 * correctSD.toDouble() && newSD < 1.4 * correctSD.toDouble())
			return true;				//		No options within 40% of correct
		for (int i=1 ; i<nOld ; i++) {
			double oldSd = ((NumOptionInformation)optionInfo[i]).getOptionValue().toDouble();
			if (newSD > 0.85 * oldSd && newSD < 1.2 * oldSd)
				return true;				//		No options within 20% of any others
		}
		return false;
	}
	
	private boolean meanTooClose(double newMean, int nOld) {
		double mean = correctMean.toDouble();
		if (Math.abs(newMean - mean) < 0.1 * withinGroupSD)
			return true;				//		No options within 0.1 sd of correct
		for (int i=1 ; i<nOld ; i++) {
			double oldMean = ((NumOptionInformation)optionInfo[i]).getOptionValue().toDouble();
			if (Math.abs(newMean - oldMean) < 0.1 * withinGroupSD)
				return true;				//		No options within 0.05 sd of any others
		}
		return false;
	}
	
	protected Component createOptionPanel(int optionIndex, ExerciseApplet exerciseApplet) {
		return new OptionShortTextPanel(optionInfo, optionIndex, exerciseApplet);
	}
	
	public void changeOptions() {
		setupChoices();
		for (int i=0 ; i<option.length ; i++)
			((OptionShortTextPanel)option[i]).changeContent();
	}
	
//	public String getBiggestMessage(String nullHypoth) {
//		return "If " + nullHypoth + ", the probability of getting a p-value as low as 9.9999 is 9.9999. A p-value between 0.05 and 0.1 is fairly low, but not particularly unusual."; 
//	}
}