package exerciseNormal;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import exercise2.*;


public class BinomDistnChoicePanel extends MultichoicePanel {
	static final private int kOptions = 4;
	
	private DataSet data;
	private String[] distnKey;
	private String xAxisInfo[];
	
	private String successesName, failuresName, trialsName;
	
	private HorizAxis xAxis[] = new HorizAxis[kOptions];
	private SimpleBarView barView[] = new SimpleBarView[kOptions];
	
//================================================
	
	private class BarChartOptionInfo extends OptionInformation {
		private int qnIndex;
		
		BarChartOptionInfo(int qnIndex) {
			super(qnIndex == 0);
			this.qnIndex = qnIndex;
		}
		
		public String getDistnKey() {
			return distnKey[qnIndex];
		}
		
		public String getAxisInfo() {
			return xAxisInfo[qnIndex];
		}
		
		public boolean equals(OptionInformation a) {
			BarChartOptionInfo oa = (BarChartOptionInfo)a;
			return (qnIndex == oa.qnIndex);
		}
		
		public boolean lessThan(OptionInformation a) {
			return true;
		}
		
		public String getOptionString() {
			return null;
		}
		
		public String getMessageString() {
			BinomialDistnVariable thisDistn = (BinomialDistnVariable)data.getVariable(distnKey[qnIndex]);
			BinomialDistnVariable correctDistn = (BinomialDistnVariable)data.getVariable(distnKey[0]);
			switch (qnIndex) {
				case 0:
					return "This is the correct bar chart.";
				case 1:
					return "This is the bar chart for the number of " + failuresName + ", not the number of " + successesName + ".";
				case 2:
				case 3:
					NumValue thisP = new NumValue(thisDistn.getProb(), 2);
					NumValue correctP = new NumValue(correctDistn.getProb(), 2);
					return "The probability of " + successesName + " in the selected distribution is " + thisP + " but it should be " + correctP + ".";
				default:		//	4 or 5
					return "The total number of " + trialsName + " is more than " + thisDistn.getCount() + ".";
			}
		}
	}
	
//================================================
	
	public BinomDistnChoicePanel(ExerciseApplet exerciseApplet, DataSet data, String[] distnKey, String[] xAxisInfo) {
		super(exerciseApplet, kOptions, 2);			//	two columns
		
		this.data = data;
		this.distnKey = distnKey;
		this.xAxisInfo = xAxisInfo;
		
		optionInfo = new BarChartOptionInfo[distnKey.length];
		for (int i=0 ; i<distnKey.length ; i++)
			optionInfo[i] = new BarChartOptionInfo(i);
		
		randomiseOptions(kOptions);
		findCorrectOption();
		
		setupPanel();
	}
	
	protected Component createOptionPanel(int optionIndex, ExerciseApplet exerciseApplet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			xAxis[optionIndex] = new HorizAxis(exerciseApplet);
			xAxis[optionIndex].setFont(exerciseApplet.getSmallFont());
		thePanel.add("Bottom", xAxis[optionIndex]);
		
			barView[optionIndex] = new SimpleBarView(data, exerciseApplet, distnKey[optionIndex], xAxis[optionIndex]);
			barView[optionIndex].lockBackground(Color.white);
		thePanel.add("Center", barView[optionIndex]);
		
		return thePanel;
	}
	
	public void changeOptions(String[] xAxisInfo, String successesName, String failuresName, String trialsName) {
		this.xAxisInfo = xAxisInfo;
		this.successesName = successesName;
		this.failuresName = failuresName;
		this.trialsName = trialsName;
		
		randomiseOptions(kOptions);
		findCorrectOption();
		
		for (int i=0 ; i<kOptions ; i++) {
			BarChartOptionInfo barOptionInfo = (BarChartOptionInfo)optionInfo[i];
			String distnKey = barOptionInfo.getDistnKey();
			
			xAxis[i].readNumLabels(barOptionInfo.getAxisInfo());
			xAxis[i].setAxisName(data.getVariable(distnKey).name);
			xAxis[i].invalidate();
			
			barView[i].changeDistnKey(distnKey);
			barView[i].repaint();
		}
	}
	
}