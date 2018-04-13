package sampDesignProg;

import dataView.*;
import utils.*;


public class ClusterAndSimpleApplet extends TwoStageMeanApplet {
	
	protected XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.667, 0, ProportionLayout.VERTICAL, ProportionLayout.REMAINDER));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL, ProportionLayout.REMAINDER));
				
				XPanel topTopPanel = new XPanel();
				topTopPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 15));
				topTopPanel.add(samplingPanel(false));
			
			topPanel.add(ProportionLayout.TOP, topTopPanel);
			
				XPanel middlePanel = new XPanel();
				middlePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 15));
				middlePanel.add(samplingTypePanel());
			
			topPanel.add(ProportionLayout.BOTTOM, middlePanel);
			
		thePanel.add(ProportionLayout.TOP, topPanel);
			
				XPanel corrPanel = new XPanel();
				corrPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 15));
				corrPanel.add(correlationSliderPanel());
		
		thePanel.add(ProportionLayout.BOTTOM, corrPanel);
		return thePanel;
	}
	
	protected String correlationSliderName() {
		return translate("Variation within clusters") + ":";
	}
	
	protected String getClusterName() {
		return translate("Clusters");
	}
}