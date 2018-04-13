package exerciseBivar;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import random.*;
import exercise2.*;


public class ResidPlotChoicePanel extends MultichoicePanel {
	static final private int kOptions = 4;
	static final private Color kScatterColor = new Color(0x990000);
	
	private DataSet data;
	private String xKey;
	@SuppressWarnings("unused")
	private String residAxisInfo, xAxisInfo;
	private String[] residKey;
	
	private HorizAxis xAxis[] = new HorizAxis[kOptions];;
	private VertAxis residAxis[] = new VertAxis[kOptions];
	private ScatterAndLineView scatterView[] = new ScatterAndLineView[kOptions];
	
//================================================
	
	private class ResidPlotOptionInfo extends OptionInformation {
		private int qnIndex;
		
		ResidPlotOptionInfo(int qnIndex) {
			super(qnIndex == 1);
			this.qnIndex = qnIndex;
			changeResidKey();
		}
		
		public boolean equals(OptionInformation a) {
			ResidPlotOptionInfo oa = (ResidPlotOptionInfo)a;
			return (qnIndex == oa.qnIndex);
		}
		
		public boolean lessThan(OptionInformation a) {
			return true;
		}
		
		public String getOptionString() {
			return null;
		}
		
		public String getResidKey() {
			return residKey[qnIndex];
		}
		
		public void changeResidKey() {
			if (qnIndex >= 3) {
				RandomInteger rand = new RandomInteger(3, 5, 1, exerciseApplet.nextSeed());
				qnIndex = rand.generateOne();
			}
		}
		
		public String getMessageString() {
			switch (qnIndex) {
				case 0:
					return "There should be no overall upward or downward linear trend in the residual plot.";
				case 1:
					return "This is the correct residual plot.";
				case 2:
					return "The curvature in the data should also be present in the residual plot.";
				default:
					return "The residuals are distances above the least squares line, not below it.";
			}
		}
	}
	
//================================================
	
	public ResidPlotChoicePanel(ExerciseApplet exerciseApplet, DataSet data, String xKey,
																String[] residKey, String residAxisInfo, String xAxisInfo) {
		super(exerciseApplet, kOptions, 2);			//	two columns
		
		this.data = data;
		this.xKey = xKey;
		this.residAxisInfo = residAxisInfo;
		this.xAxisInfo = xAxisInfo;
		this.residKey = residKey;
		
		optionInfo = new ResidPlotOptionInfo[kOptions];
		for (int i=0 ; i<kOptions ; i++)
			optionInfo[i] = new ResidPlotOptionInfo(i);
		
		randomiseOptions();
		findCorrectOption();
		
		setupPanel();
	}
	
	protected Component createOptionPanel(int optionIndex, ExerciseApplet exerciseApplet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XLabel residLabel = new XLabel(exerciseApplet.translate("Residual"), XLabel.LEFT, exerciseApplet);
			residLabel.setFont(exerciseApplet.getSmallFont());
			residLabel.setForeground(kScatterColor);
		thePanel.add("North", residLabel);
			
			XPanel displayPanel = new XPanel();
			displayPanel.setLayout(new AxisLayout());
			
				xAxis[optionIndex] = new HorizAxis(exerciseApplet);
				xAxis[optionIndex].setForeground(kScatterColor);
				xAxis[optionIndex].setFont(exerciseApplet.getSmallFont());
			displayPanel.add("Bottom", xAxis[optionIndex]);
			
				residAxis[optionIndex] = new VertAxis(exerciseApplet);
				residAxis[optionIndex].setForeground(kScatterColor);
				residAxis[optionIndex].setFont(exerciseApplet.getSmallFont());
			displayPanel.add("Left", residAxis[optionIndex]);
			
				scatterView[optionIndex] = new ScatterAndLineView(data, exerciseApplet,
															xAxis[optionIndex], residAxis[optionIndex], xKey,
															((ResidPlotOptionInfo)optionInfo[optionIndex]).getResidKey(), null);
				scatterView[optionIndex].setForeground(kScatterColor);
				scatterView[optionIndex].lockBackground(Color.white);
			displayPanel.add("Center", scatterView[optionIndex]);
			
		thePanel.add("Center", displayPanel);
		
		return thePanel;
	}
	
	public void changeOptions(String residAxisInfo, String xAxisInfo) {
		randomiseOptions();
		findCorrectOption();
		
		for (int i=0 ; i<option.length ; i++) {
			xAxis[i].readNumLabels(xAxisInfo);
			xAxis[i].setAxisName(data.getVariable(xKey).name);
			residAxis[i].readNumLabels(residAxisInfo);
			xAxis[i].invalidate();
			residAxis[i].invalidate();
			
			((ResidPlotOptionInfo)optionInfo[i]).changeResidKey();		//	randomises negative option key
			scatterView[i].changeVariables(((ResidPlotOptionInfo)optionInfo[i]).getResidKey(), xKey);
		}
	}
	
	public void setShowHints(boolean showHints) {
		for (int i=0 ; i<option.length ; i++) {
			scatterView[i].setShowHints(showHints);
			scatterView[i].repaint();
		}
	}
	
}