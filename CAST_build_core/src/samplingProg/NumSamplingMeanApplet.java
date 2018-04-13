package samplingProg;

import java.awt.*;

import dataView.*;
import utils.*;
import survey.*;


public class NumSamplingMeanApplet extends NumSamplingApplet {
	static final private String MAX_MEAN_PARAM = "maxMean";
	
	static final private Color kDarkBlue = new Color(0x000099);
	
	private PopSampMeanView sampMean;
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
			String varName = data.getVariable("y").name;
			XLabel meanLabel = new XLabel(translate("Mean of") + " " + varName, XLabel.CENTER, this);
			meanLabel.setFont(getBigBoldFont());
			meanLabel.setForeground(kDarkBlue);
		thePanel.add(meanLabel);
			
			NumValue maxMean = new NumValue(getParameter(MAX_MEAN_PARAM));
			PopSampMeanView popMean = new PopSampMeanView(data, this, PopSampMeanView.POPN, maxMean, "y", "freq");
			popMean.setFont(getBigBoldFont());
		thePanel.add(popMean);
			sampMean = new PopSampMeanView(data, this, PopSampMeanView.SAMPLE, maxMean, "y", "freq");
			sampMean.setFont(getBigBoldFont());
			sampMean.setHighlight(true);
			sampMean.setEnabled(false);
		thePanel.add(sampMean);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		currentWithReplacement = false;
		
		XPanel spacerPanel = new XPanel();		//		to give 10 pixels above sample button
		spacerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 10));
		spacerPanel.add(sampleSizePanel());
		return spacerPanel;
	}
	
	protected void doTakeSample() {
		sampMean.setEnabled(true);
		super.doTakeSample();
	}
}