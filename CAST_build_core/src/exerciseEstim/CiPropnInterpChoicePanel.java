package exerciseEstim;

import exercise2.*;


public class CiPropnInterpChoicePanel extends CoreCiInterpChoicePanel {
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
//			String notPercent = "5";
			switch (qnIndex) {
				case 0:
					return "We are " + percent + "% confident that between " + lowLimit + " and " + highLimit + " of " + popnValues + " are " + success + ".";
				case 1:
					return "We are " + percent + "% confident that between " + lowLimit + " and " + highLimit + " of " + sampleValues + " are " + success + ".";
				case 2:
					return "We are " + percent + "% confident that between " + lowLimit + " and " + highLimit + " of " + newSample + " will be " + success + ".";
				case 3:
					return "We are between " + lowLimit + " and " + highLimit + " confident that " + percent + "% of " + popnValues + " are " + success + ".";
				case 4:
					return "We are confident that " + percent + "% of " + samples + " will have between " + lowLimit + " and " + highLimit + " that are " + success + ".";
//				case 5:
//					return "We are confident that " + notPercent + "% of new similar samples will have a proportion of " + success + " that is less than " + lowLimit + " or greater than " + highLimit + ".";
				default:
					return null;		//	should never happen
			}
		}
		
		public String getMessageString() {
			switch (qnIndex) {
				case 0:
					return "The confidence interval expresses the information from the sample about the likely value of the probability (i.e. population proportion), #pi#.";
				case 1:
					return "The confidence interval is p #plusMinus# some value so the sample proportion, p, is always within the confidence interval.";
				case 3:
					return "The level of confidence is 95%.";
				case 2:
				case 4:
					return "The difference between this sample proportion and the sample proportion in a new sample will be more variable than the difference between this sample proportion and the population proportion (a fixed target).\nFewer than 95% of proportions in new samples are therefore expected to be within the interval.";
//				case 5:
//					return "The difference between this sample proportion and the sample proportion in a new sample will be more variable than the difference between this sample proportion and the population proportion (a fixed target). More than 95% of proportions in new samples are therefore expected to be outside the interval.";
			}
			return "";
		}
	}
	
//================================================
	
	public CiPropnInterpChoicePanel(ExerciseApplet exerciseApplet) {
		super(exerciseApplet, kDisplayOptions, 4);
	}
	
	protected OptionInformation createOptionInfo(int optionIndex) {
		return new CiInterpOptionInfo(optionIndex);
	}
}