package experProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import coreGraphics.*;
import axis.*;
import utils.*;
import models.*;

import exper.*;


public class CovariateUnbiasedApplet extends SelectTreatmentsApplet {
	static final private String EFFECT_AXIS_INFO_PARAM = "effectAxis";
	
//	static final private String[] kCovarExplanKeys = {"lurking", "treat"};
	
	private XCheckbox animateCheck;
	private XButton quickSampleButton;
	
	private CardLayout buttonLayout, plotLayout;
	private XPanel buttonPanel, plotPanel;

	protected DataSet getData() {
		data = super.getData();
		
			String[] kTreatKey = {"treat", "lurking"};
			MultipleRegnModel lsCovar = new MultipleRegnModel("LS with covars", data, kTreatKey,
																																createCloneArray(maxDiff, 3));
		data.addVariable("lsCovar", lsCovar);
		
		return data;
	}
	
	
	protected String[] getLsKeys() {
		String[] keys = {"ls", "lsCovar"};
		return keys;
	}
	
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = super.getSummaryData(sourceData);
		
			String treatEstName = "Estimate using covar";
		summaryData.addVariable("treatEstCovar", new MultiRegnParamVariable(treatEstName, "lsCovar", 1));
		
		return summaryData;
	}
	
	protected XPanel controlPanel() {
		XPanel thePanel = new InsetPanel(14, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 40));
		
			animateCheck = new XCheckbox(translate("Animate"), this);
			animateCheck.setState(true);
		thePanel.add(animateCheck);
		
			buttonPanel = new XPanel();
			buttonLayout = new CardLayout();
			buttonPanel.setLayout(buttonLayout);
			
			buttonPanel.add("animate", super.controlPanel());
			
				XPanel quickPanel = new XPanel();
				quickPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				
					StringTokenizer st = new StringTokenizer(translate("Run*Experiment"), "*");
					quickSampleButton = new XButton(st.nextToken() + "\n" + st.nextToken(), this);
				quickPanel.add(quickSampleButton);
			
			buttonPanel.add("noAnimate", quickPanel);
		
		thePanel.add(buttonPanel);
		
		return thePanel;
	}
	
	protected XPanel responsePanel(DataSet data) {
			plotPanel = new XPanel();
			plotLayout = new CardLayout();
			plotPanel.setLayout(plotLayout);
			
			plotPanel.add("animate", super.responsePanel(data));
			plotPanel.add("noAnimate", cumEffectPanel(data));
		
		return plotPanel;
	}
	
	private XPanel cumEffectPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			VertAxis yAxis = new VertAxis(this);
			yAxis.readNumLabels(getParameter(EFFECT_AXIS_INFO_PARAM));
		thePanel.add("Left", yAxis);
		
			HorizAxis analysisAxis = new HorizAxis(this);
			CatVariable tempCat = new CatVariable("");
			tempCat.readLabels(translate("Std Covar"));
			analysisAxis.setCatLabels(tempCat);
			analysisAxis.setAxisName(translate("Estimation method"));
			
		thePanel.add("Bottom", analysisAxis);
		
			StackedDotPlotView stdEstView = new StackedDotPlotView(summaryData, this, yAxis);
			stdEstView.setActiveNumVariable("treatEst");
			stdEstView.setCanDragCrosses(false);
			stdEstView.lockBackground(Color.white);
		
			StackedDotPlotView covarEstView = new StackedDotPlotView(summaryData, this, yAxis);
			covarEstView.setActiveNumVariable("treatEstCovar");
			covarEstView.setCanDragCrosses(false);
			covarEstView.lockBackground(Color.white);
			
			MultipleDataView estView = new MultipleDataView(summaryData, this, stdEstView,
																									covarEstView, MultipleDataView.HORIZONTAL);
			
		thePanel.add("Center", estView);
		
		return thePanel;
	}
	
	protected XPanel diffMeanPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == animateCheck) {
			if (animateCheck.getState()) {
				allocateTreatsButton.enable();
				takeSampleButton.disable();
				buttonLayout.show(buttonPanel, "animate");
				plotLayout.show(plotPanel, "animate");
				summaryData.setAccumulate(false);
			}
			else {
				buttonLayout.show(buttonPanel, "noAnimate");
				plotLayout.show(plotPanel, "noAnimate");
				summaryData.setAccumulate(true);
			}
			return true;
		}
		else if (target == quickSampleButton) {
			NumSampleVariable lurkingError = (NumSampleVariable)data.getVariable("selectError");
			lurkingError.generateNextSample();
			data.variableChanged("selectError");
			summaryData.takeSample();
			return true;
		}
		else if (target == effectChoice) {
			summaryData.setAccumulate(false);			//	to clear all summaries except current one
			summaryData.setAccumulate(true);
			
			return false;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (localAction(evt.target))
			return true;
		else
			return super.action(evt, what);
	}
}