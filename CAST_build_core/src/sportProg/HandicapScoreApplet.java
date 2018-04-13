package sportProg;

import java.awt.*;

import dataView.*;
import utils.*;

import sport.*;


public class HandicapScoreApplet extends RawGolfScoreApplet {
	static final private String HANDICAP_PARAM = "handicap";
	static final private String HANDICAP_NAME_PARAM = "handicapName";
	
	protected DataSet getData() {
		DataSet data = super.getData();
		
		String distnName = getParameter(HANDICAP_NAME_PARAM);
		HandicapNormalVariable y = new HandicapNormalVariable(distnName,
										getParameter(SD_SCALING_PARAM), getParameter(HANDICAP_PARAM));
		y.setMean(initialMean);
		data.addVariable("handicap", y);
		
		return data;
	}
	
	protected String getDisplayDistnKey() {
		return "handicap";
	}
	
	protected void changeRawMean(NumValue newMean) {
		super.changeRawMean(newMean);
		HandicapNormalVariable y = (HandicapNormalVariable)data.getVariable("handicap");
		y.setMean(newMean);
		data.variableChanged("handicap");
	}
	
	protected XPanel bottomPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(50, 0));
			meanSlider = new ParameterSlider(minMean, maxMean, initialMean, translate("mean score") + " = ",
																	ParameterSlider.SHOW_MIN_MAX, this);
		thePanel.add("Center", meanSlider);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																		VerticalLayout.VERT_CENTER, 0));
				XLabel handicapLabel = new XLabel(translate("Handicap") + ":", XLabel.LEFT, this);
				handicapLabel.setFont(getSmallBoldFont());
			rightPanel.add(handicapLabel);
			rightPanel.add(new HandicapValueView(data, "handicap", this));
			
		thePanel.add("East", rightPanel);
		
		return thePanel;
	}
}