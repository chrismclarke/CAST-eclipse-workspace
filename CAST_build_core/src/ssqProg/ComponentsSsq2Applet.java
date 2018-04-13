package ssqProg;

import java.awt.*;

import dataView.*;
import models.*;
import formula.*;


import ssq.*;

public class ComponentsSsq2Applet extends Components2Applet {
	static final private String MAX_SSQ_PARAM = "maxSsq";
	
	static final private int kR2Decimals = 3;
	
	private NumValue maxSsq;
	
	protected SummaryDataSet getSummaryData(DataSet data) {
		maxSsq = new NumValue(getParameter(MAX_SSQ_PARAM));
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "error", BasicComponentVariable.kComponentKey,
												maxSsq.decimals, kR2Decimals);
		
		summaryData.setSingleSummaryFromData();
		return summaryData;
	}
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = super.rightPanel(data);
		
		componentPlot.getView().setShowSD(true);
				
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout( 0, 0));
		
			XPanel equationPanel = new XPanel();
			equationPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				Image[] compImages;
				int imageWidth;
				if (xNumNotCat) {
					AnovaImages.loadRegnImages(this);
					compImages = AnovaImages.basicRegnSsqs;
					imageWidth = AnovaImages.kSsqWidth;
				}
				else  {
					AnovaImages.loadGroupImages(this);
					compImages = AnovaImages.basicGroupSsqs;
					imageWidth = AnovaImages.kSsq2Width;
				}
				
				FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
				theEquation = new ComponentEqnPanel(summaryData, BasicComponentVariable.kComponentKey, 
									maxSsq, compImages, BasicComponentVariable.kComponentColor, imageWidth,
									AnovaImages.kSsqHeight, stdContext);
				
			equationPanel.add(theEquation);
		thePanel.add("Center", equationPanel);
		
		thePanel.add("East", componentChoicePanel());
		
		return thePanel;
	}
	
}