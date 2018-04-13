package residProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import coreGraphics.*;
import models.*;

import resid.*;


public class DiagnosticsApplet extends XApplet {
	static final private String LEVERAGE_AXIS_PARAM = "leverageAxis";
	static final private String DFITS_AXIS_PARAM = "dfitsAxis";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	static final private String X_AXIS_INFO_PARAM = "xAxis";
	
	static final private int kAppletWidth = 550;
	static final private int kDataDescriptionWidth = 400;
	static final private int kDataSpacerWidth = kAppletWidth - kDataDescriptionWidth;
//	static final private int kHalfAppletWidth = kAppletWidth / 2;
	
	static final private String kResidAxisInfo = "-5 5 -4 2";
	
	static final private Color kDarkRed = new Color(0x990000);
	
	protected CoreModelDataSet data;
	
	private MultiHorizAxis leverageAxis, dfitsAxis;
	private MultiHorizAxis dataXAxis;
	private MultiVertAxis dataYAxis;
	private XLabel yAxisName;
	
	private XChoice dataSetChoice;
	private XCheckbox peekCheck;
	
	private XPanel scatterPanel;
	private CardLayout scatterPanelLayout;
	private ScatterView dataView;
	
	private XTextArea dataDescription, dataConclusion;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(0, 10));
		
		add("North", dataDescriptionPanel(data));
		
			XPanel displayPanel = new XPanel();
			displayPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																																	ProportionLayout.TOTAL));
			displayPanel.add(ProportionLayout.LEFT, diagnosticPanel(data));
			displayPanel.add(ProportionLayout.RIGHT, dataPeekPanel(data));
		
		add("Center", displayPanel);
		
		add("South", conclusionPanel(data));
	}
	
	protected CoreModelDataSet readData() {
		SimpleRegnDataSet data = new SimpleRegnDataSet(this);
		
			LinearModel deletedLS = new LinearModel("Deleted LS", data, "x");
		data.addVariable("deletedLS", deletedLS);
		
			DeletedSDVariable deletedSD = new DeletedSDVariable("Deleted sd", data, "y",
																																					"deletedLS");
		data.addVariable("deletedSD", deletedSD);
		
			ExtStudentResidVariable tResidVar = new ExtStudentResidVariable("Ext student resid", data,
																					"x", "y", "ls", "deletedLS", 9);
		data.addVariable("tResid", tResidVar);
		
			FitInfluenceVariable dfitsVar = new FitInfluenceVariable("DFITS", data, "x", "tResid",
																																										"ls", 9);
		data.addVariable("dfits", dfitsVar);
		
			LeverageValueVariable leverageVar = new LeverageValueVariable("Leverage", data, "x",
																									"ls", LeverageValueVariable.LEVERAGE, 9);
		data.addVariable("leverage", leverageVar);
		
		return data;
	}
	
	private XPanel dataDescriptionPanel(CoreModelDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_TOP, 0));
		
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
				XLabel dataLabel = new XLabel(translate("Data") + ":", XLabel.LEFT, this);
				dataLabel.setFont(getStandardBoldFont());
			choicePanel.add(dataLabel);
				
				dataSetChoice = data.dataSetChoice(this);
			choicePanel.add(dataSetChoice);
			
		thePanel.add(choicePanel);
			
			XPanel descriptionPanel = new XPanel();
			descriptionPanel.setLayout(new BorderLayout(0, 0));
			
				dataDescription = new XTextArea(data.getDescriptionStrings(), 0, kDataDescriptionWidth, this);
				dataDescription.lockBackground(Color.white);
				
			descriptionPanel.add("Center", dataDescription);
			
				XPanel spacingPanel = new XPanel();
				spacingPanel.setLayout(new FixedSizeLayout(kDataSpacerWidth, 10));
				spacingPanel.add(new XPanel());
				
			descriptionPanel.add("East", spacingPanel);
		
		thePanel.add(descriptionPanel);
		
		return thePanel;
	}
	
	protected int getNoOfParams() {
		return 2;
	}
	
	private XPanel diagnosticPanel(CoreModelDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.3333, 5, ProportionLayout.VERTICAL,
																																		ProportionLayout.TOTAL));
		
		thePanel.add(ProportionLayout.TOP, residPanel(data));
		
			XPanel lowerPanel = new XPanel();
			lowerPanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.VERTICAL,
																																		ProportionLayout.TOTAL));
			
			lowerPanel.add(ProportionLayout.TOP, leveragePanel(data));
			lowerPanel.add(ProportionLayout.BOTTOM, influencePanel(data));
		
		thePanel.add(ProportionLayout.BOTTOM, lowerPanel);
		
		return thePanel;
	}
	
	private XPanel residPanel(CoreModelDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			horizAxis.readNumLabels(kResidAxisInfo);
			horizAxis.setAxisName(translate("Externally studentised residuals"));
		thePanel.add("Bottom", horizAxis);
		
//			DotPlotView view = new DotPlotView(data, this, horizAxis, 0.5);
			DiagnoticDotPlotView view = new DiagnoticDotPlotView(data, this, horizAxis,
																											DiagnoticDotPlotView.ST_RESID, getNoOfParams());
			view.setActiveNumVariable("tResid");
			view.setRetainLastSelection(true);
			view.lockBackground(Color.white);
		
		thePanel.add("Center", view);
		
		return thePanel;
	}
	
	private XPanel leveragePanel(CoreModelDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			int nDataSets = data.getDescriptionStrings().length;
			leverageAxis = new MultiHorizAxis(this, nDataSets);
			leverageAxis.readNumLabels(getParameter(LEVERAGE_AXIS_PARAM));
			for (int i=2 ; i<=nDataSets ; i++)
				leverageAxis.readExtraNumLabels(getParameter(LEVERAGE_AXIS_PARAM + i));
			leverageAxis.setChangeMinMax(true);
			leverageAxis.setAxisName(translate("Leverage"));
		thePanel.add("Bottom", leverageAxis);
		
//			DotPlotView view = new DotPlotView(data, this, leverageAxis, 0.5);
			DiagnoticDotPlotView view = new DiagnoticDotPlotView(data, this, leverageAxis,
																										DiagnoticDotPlotView.LEVERAGE, getNoOfParams());
			view.setActiveNumVariable("leverage");
			view.setRetainLastSelection(true);
			view.lockBackground(Color.white);
		
		thePanel.add("Center", view);
		
		return thePanel;
	}
	
	private XPanel influencePanel(CoreModelDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			int nDataSets = data.getDescriptionStrings().length;
			dfitsAxis = new MultiHorizAxis(this, nDataSets);
			dfitsAxis.readNumLabels(getParameter(DFITS_AXIS_PARAM));
			for (int i=2 ; i<=nDataSets ; i++)
				dfitsAxis.readExtraNumLabels(getParameter(DFITS_AXIS_PARAM + i));
			dfitsAxis.setChangeMinMax(true);
			dfitsAxis.setAxisName(translate("DFITS"));
		thePanel.add("Bottom", dfitsAxis);
		
//			DotPlotView view = new DotPlotView(data, this, dfitsAxis, 0.5);
			DiagnoticDotPlotView view = new DiagnoticDotPlotView(data, this, dfitsAxis, DiagnoticDotPlotView.DFITS,
																																														getNoOfParams());
			view.setActiveNumVariable("dfits");
			view.setRetainLastSelection(true);
			view.lockBackground(Color.white);
		
		thePanel.add("Center", view);
		
		return thePanel;
	}
	
	
	private XPanel conclusionPanel(CoreModelDataSet data) {
//		XPanel thePanel = new XPanel();
//		thePanel.setLayout(new BorderLayout(0, 10));
			
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(0, 0));
			
				XLabel concLabel = new XLabel(translate("Conclusion") + ":", XLabel.LEFT, this);
				concLabel.setFont(getStandardBoldFont());
				concLabel.setForeground(kDarkRed);
			topPanel.add("North", concLabel);
			
				dataConclusion = new XTextArea(data.getQuestionStrings(), 0, kAppletWidth, this);
				dataConclusion.lockBackground(Color.white);
				dataConclusion.setForeground(kDarkRed);
			topPanel.add("Center", dataConclusion);
			
//		thePanel.add("North", topPanel);
//		
//		thePanel.add("Center", dataPeekPanel(data));
		
		return topPanel;
	}
	
	private XPanel dataPeekPanel(CoreModelDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 4));
			
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				peekCheck = new XCheckbox(translate("Peek at data"), this);
			topPanel.add(peekCheck);
			
		thePanel.add("North", peekCheck);
		
			scatterPanel = new XPanel();
			scatterPanelLayout = new CardLayout();
			scatterPanel.setLayout(scatterPanelLayout);
			
			scatterPanel.add("blank", new XPanel());
			scatterPanel.add("scatterplot", scatterContentsPanel(data));
			scatterPanelLayout.show(scatterPanel, "blank");
			
		thePanel.add("Center", scatterPanel);
		
		return thePanel;
	}
	
	protected XPanel scatterContentsPanel(CoreModelDataSet data) {
		SimpleRegnDataSet regnData = (SimpleRegnDataSet)data;
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			XPanel plotPanel = new XPanel();
			plotPanel.setLayout(new AxisLayout());
			
				int nDataSets = regnData.getDescriptionStrings().length;
				dataXAxis = new MultiHorizAxis(this, nDataSets);
				dataXAxis.readNumLabels(getParameter(X_AXIS_INFO_PARAM));
				for (int i=2 ; i<=nDataSets ; i++)
					dataXAxis.readExtraNumLabels(getParameter(X_AXIS_INFO_PARAM + i));
				dataXAxis.setChangeMinMax(true);
				dataXAxis.setAxisName(regnData.getXVarName());
			plotPanel.add("Bottom", dataXAxis);
			
				dataYAxis = new MultiVertAxis(this, nDataSets);
				dataYAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
				for (int i=2 ; i<=nDataSets ; i++)
					dataYAxis.readExtraNumLabels(getParameter(Y_AXIS_INFO_PARAM + i));
				dataYAxis.setChangeMinMax(true);
			plotPanel.add("Left", dataYAxis);
			
				dataView = new ScatterView(regnData, this, dataXAxis, dataYAxis, "x", "y");
				dataView.lockBackground(Color.white);
				dataView.setRetainLastSelection(true);
			plotPanel.add("Center", dataView);
		
		thePanel.add("Center", plotPanel);
		
			yAxisName = new XLabel(regnData.getYVarName(), XLabel.LEFT, this);
			yAxisName.setFont(dataYAxis.getFont());
		thePanel.add("North", yAxisName);
		
		return thePanel;
	}
	
	protected void changeDataPlot(int dataSetChoice) {
		dataYAxis.setAlternateLabels(dataSetChoice);
		dataXAxis.setAlternateLabels(dataSetChoice);
		dataXAxis.setAxisName(data.getXVarName());
		yAxisName.setText(data.getYVarName());
	}

	
	private boolean localAction(Object target) {
		if (target == dataSetChoice) {
			int newChoice = dataSetChoice.getSelectedIndex();
			if (data.changeDataSet(newChoice, dataDescription, dataConclusion)) {
				peekCheck.setState(false);
				scatterPanelLayout.show(scatterPanel, "blank");
				
				changeDataPlot(newChoice);
				
				leverageAxis.setAlternateLabels(newChoice);
				leverageAxis.repaint();
				
				dfitsAxis.setAlternateLabels(newChoice);
				dfitsAxis.repaint();
				
				data.variableChanged("x");	//	should cause all plots to be redrawn
			}
			return true;
		}
		else if (target == peekCheck) {
			scatterPanelLayout.show(scatterPanel, peekCheck.getState() ? "scatterplot" : "blank");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}