package stdErrorProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;

import corr.*;
import stdError.*;


public class TwoSeBoundsApplet extends ErrorBoundsApplet {
	
	protected double topProportion() {
		return 0.5;
	}
	
	protected Stacked2SdBoundsView getStackedBoundsView(SummaryDataSet summaryData,
																														HorizAxis theHorizAxis) {
		Stacked2SdBoundsView theView = new Stacked2SdBoundsView(summaryData, this, theHorizAxis);
		theView.setFont(getStandardBoldFont());
		return theView;
	}
	
	protected XPanel boundsValuePanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
		
			NumValue maxBiasSe = new NumValue(getParameter(MAX_BIAS_SE_PARAM));
		
			MeanView biasValueView = new MeanView(summaryData, "error", MeanView.GENERIC_TEXT_FORMULA, 0, this);
			biasValueView.setLabel(translate("Bias"));
			String unitsString = getParameter(UNITS_PARAM);
			if (unitsString != null)
				biasValueView.setUnitsString(unitsString);
			biasValueView.setMaxValue(maxBiasSe);
		thePanel.add(biasValueView);
		
			StDevnView seValueView = new StDevnView(summaryData, "error", MeanView.GENERIC_TEXT_FORMULA, 0, this);
			seValueView.setLabel(translate("Standard error"));
			seValueView.setUnitsString(unitsString);
			seValueView.setMaxValue(maxBiasSe);
		thePanel.add(seValueView);
		
			XPanel boundsPanel = new XPanel();
			boundsPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
			
				TwoSdBoundsView boundsView = new TwoSdBoundsView(summaryData, this,
													errorBoundsView, new NumValue(getParameter(MAX_ERROR_BOUND_PARAM)));
			
				boundsView.setFont(getBigBoldFont());
				boundsView.setForeground(Color.blue);
			boundsPanel.add(boundsView);
			
			boundsPanel.lockBackground(Color.white);
		thePanel.add(boundsPanel);
		
		return thePanel;
	}
}