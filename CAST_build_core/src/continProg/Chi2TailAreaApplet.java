package continProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;
import distn.*;

import contin.*;


public class Chi2TailAreaApplet extends ObsExpApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	
	
	public void setupApplet() {
		checkDataSets();
		
		data = readData();
		
		setLayout(new BorderLayout(0, 30));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout());
			topPanel.add("West", dataPanel(data));
			
		summaryData = createSummaryData(data);
		summaryData.takeSample();
				
				XPanel statPanel = new XPanel();
				statPanel.setLayout(new VerticalLayout(VerticalLayout.RIGHT, VerticalLayout.VERT_SPACED, 6));
				statPanel.add("North", chi2Panel(data));
					Chi2PValueView pValue = new Chi2PValueView(data, this, oeView);
					pValue.setForeground(Color.blue);
				statPanel.add(pValue);
			topPanel.add("Center", statPanel);
			
		add("North", topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(20, 0));
			
				XPanel controlPanel = new XPanel();
				controlPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																												VerticalLayout.VERT_CENTER, 10));
				controlPanel.add(sampleSizePanel());
				controlPanel.add(takeSampleButton(false));
			bottomPanel.add("West", controlPanel);
		
			bottomPanel.add("Center", distnPanel(summaryData));
		
		add("Center", bottomPanel);
	}
	
	protected SummaryDataSet createSummaryData(DataSet data) {
		SummaryDataSet summaryData = super.createSummaryData(data);
		
			NumValue maxChi2 = new NumValue(getParameter(MAX_CHI2_PARAM));
			Chi2Variable chi2 = new Chi2Variable(null, oeView, maxChi2.decimals);
		summaryData.addVariable("chi2", chi2);
		
			Chi2DistnVariable chi2Distn = new Chi2DistnVariable("chi2 distn");
			chi2Distn.setParams("4");
			chi2Distn.setDF(oeView.getDF());
		summaryData.addVariable("chi2Distn", chi2Distn);
		
		return summaryData;
	}
	
	protected XPanel distnPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		
			AccurateTailAreaView theView = new AccurateTailAreaView(summaryData, this, theHorizAxis, "chi2Distn");
			theView.setDistnLabel(new LabelValue(translate("Chi-squared") + " (" + oeView.getDF() + " df)"),
																																				Color.lightGray);
			theView.setValueLabel(null);
			theView.lockBackground(Color.white);
			theView.setActiveNumVariable("chi2");
		thePanel.add("Center", theView);
		
		return thePanel;
	}
}