package exerciseEstim;

import exercise2.*;


public class CiMeanInterpChoicePanel extends CoreCiInterpChoicePanel {
	static final private int kDisplayOptions = 4;
	
//================================================
	
	private class CiInterpOptionInfo extends OptionInformation {
		private int qnIndex;
		
		CiInterpOptionInfo(int qnIndex) {
			super(qnIndex == 0);
			this.qnIndex = qnIndex;
		}
		
		public boolean equals(OptionInformation a) {
			CiInterpOptionInfo oa = (CiInterpOptionInfo)a;
			return (qnIndex == oa.qnIndex);
		}
		
		public boolean lessThan(OptionInformation a) {
			return true;
		}
		
		public String getOptionString() {
			String percent = "95";
			String notPercent = "5";
			switch (qnIndex) {
				case 0:
					return "We are " + percent + "% confident that " + popnParam + " is between " + lowLimit + " and " + highLimit + " " + units + ".";
				case 1:
					return "We are " + percent + "% confident that " + sampleStat + " is between " + lowLimit + " and " + highLimit + " " + units + ".";
				case 2:
					return "We are " + percent + "% confident that " + newValue + " will be between " + lowLimit + " and " + highLimit + " " + units + ".";
				case 3:
					return "We are " + percent + "% confident that " + newSample + " will all be between " + lowLimit + " and " + highLimit + " " + units + ".";
				case 4:
					return "We are confident that " + percent + "% of " + sampleValues + " are between " + lowLimit + " and " + highLimit + " " + units + ".";
				case 5:
					return "We are confident that " + notPercent + "% of " + sampleValues + " are outside the interval " + lowLimit + " to " + highLimit + " " + units + ".";
				case 6:
					return "We are confident that " + percent + "% of " + popnValues + " are between " + lowLimit + " and " + highLimit + " " + units + ".";
				case 7:
					return "We are confident that " + notPercent + "% of " + popnValues + " are outside the interval " + lowLimit + " to " + highLimit + " " + units + ".";
				default:
					return null;		//	should never happen
			}
		}
		
		public String getMessageString() {
			switch (qnIndex) {
				case 0:
					return "The confidence interval expresses the information from the sample about the likely value of the population mean, #mu#.";
				case 1:
					return "The confidence interval is x#bar# #plusMinus# some value so the sample mean, x#bar#, is always within the confidence interval.";
				case 2:
					return "This confidence interval is not intended for prediction of a single value. Individual values are more variable than the sample mean, so there is much less than 95% probability that a new value will be inside the confidence interval.";
				case 3:
					return "The confidence interval reflects the variability of the sample mean, not of individual values. Far fewer than 95% of values in a new sample are likely to be within the interval.";
				case 4:
				case 6:
					return "The confidence interval reflects the variability of the sample mean, not of individual values. Far fewer than 95% of individual sample values will be within the interval.\n(If the sample size was increased, the CI would become narrower but the individual values would be equally variable but an interval including 95% of the sample should be approximately the same.)";
				case 5:
				case 7:
					return "The confidence interval reflects the variability of the sample mean, not of individual values. Far more than 5% of individual population values will be outside the interval.\n(If the sample size was increased, the CI would become narrower but the population variability is unchanged so an interval including 95% of population values should not be any narrower.)";
			}
			return "";
		}
	}
	
//================================================
	
	public CiMeanInterpChoicePanel(ExerciseApplet exerciseApplet) {
		super(exerciseApplet, kDisplayOptions, 7);
	}
	
	protected OptionInformation createOptionInfo(int optionIndex) {
		return new CiInterpOptionInfo(optionIndex);
	}
}