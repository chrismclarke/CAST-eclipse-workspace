package stdErrorProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import distn.*;
import coreGraphics.*;
import coreVariables.*;
import coreSummaries.*;

import corr.*;
import stdError.*;


public class MeanBootstrapApplet extends ErrorBootstrapApplet {
	
	static final private Color kPink = new Color(0xFFE0E0);
	
	private StackedPlusNormalView errorView;
	
	private XCheckbox showTheoryCheck;
	
	private XPanel theoryPanel;
	private CardLayout theoryPanelLayout;
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
			int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
			MeanVariable meanVar = new MeanVariable(translate("Mean"), "y", decimals);
		summaryData.addVariable("est", meanVar);
			
			NumVariable popn = (NumVariable)sourceData.getVariable("popn");
			ValueEnumeration pe = popn.values();
			int n = 0;
			double sy = 0.0;
			double syy = 0.0;
			while (pe.hasMoreValues()) {
				double y = pe.nextDouble();
				sy += y;
				syy += y * y;
				n ++;
			}
			target = new NumValue(sy / n, decimals);
			
			ScaledVariable error = new ScaledVariable(getParameter(ERROR_NAME_PARAM), meanVar,
																									"est", -target.toDouble(), 1.0, decimals);
		
		summaryData.addVariable("error", error);
		
			NormalDistnVariable errorDistn = new NormalDistnVariable("Error distn");
			NumValue maxBiasSe = new NumValue(getParameter(MAX_BIAS_SE_PARAM));
			errorDistn.setDecimals(maxBiasSe.decimals);
			errorDistn.setMean(0.0);
			errorDistn.setSD(Math.sqrt((syy - sy * sy / n) / (n - 1) / n));
			
		summaryData.addVariable("errorDistn", errorDistn);
		
		return summaryData;
	}
	
	protected StackedPlusNormalView getDataView(DataSet data, SummaryDataSet summaryData,
														HorizAxis horizAxis, String paramName, String statisticName) {
		MeanBootstrapErrorView theView = new MeanBootstrapErrorView(data, this, horizAxis, null, "y",
																									summaryData, "est", "error", target);
		theView.setShowDensity(ParamAndStatView.NO_DISTN);
		theView.setActiveNumVariable("popn");
		theView.setCrossSize(DataView.LARGE_CROSS);
		return theView;
	}
	
	protected XPanel summaryPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = getAxis(summaryData, "error", ERROR_AXIS_INFO_PARAM);
			theHorizAxis.setForeground(Color.red);
		thePanel.add("Bottom", theHorizAxis);
		
			errorView = new StackedPlusNormalView(summaryData, this, theHorizAxis, "errorDistn");
			errorView.setActiveNumVariable("error");
			errorView.lockBackground(Color.white);
			errorView.setForeground(Color.red);
			errorView.setDensityColor(kPink);
			errorView.setShowDensity(StackedPlusNormalView.NO_DISTN);
		thePanel.add("Center", errorView);
		
		return thePanel;
	}
	
	protected XPanel errorSummaryPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 20));
		
			StDevnView seValueView = new StDevnView(summaryData, "error", MeanView.GENERIC_TEXT_FORMULA, 0, this);
			seValueView.setLabel("Bootstrap standard error");
			String unitsString = getParameter(UNITS_PARAM);
			NumValue maxBiasSe = new NumValue(getParameter(MAX_BIAS_SE_PARAM));
			seValueView.setUnitsString(unitsString);
			seValueView.setMaxValue(maxBiasSe);
			seValueView.setForeground(Color.red);
		thePanel.add(seValueView);
		
			showTheoryCheck = new XCheckbox("Show Theory", this);
			showTheoryCheck.setForeground(Color.red);
		thePanel.add(showTheoryCheck);
				
			theoryPanel = new XPanel();
			theoryPanelLayout = new CardLayout();
			theoryPanel.setLayout(theoryPanelLayout);
			
			theoryPanel.add("blank", new XPanel());
			
				XPanel theoryInnerPanel = new XPanel();
				theoryInnerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					StdErrorValueView seView = new StdErrorValueView(summaryData, this,
																												"errorDistn", true, maxBiasSe);
					seView.setUnitsString(unitsString);
				theoryInnerPanel.add(seView);
			
			theoryPanel.add("show", theoryInnerPanel);
			
		thePanel.add(theoryPanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == showTheoryCheck) {
			theoryPanelLayout.show(theoryPanel, showTheoryCheck.getState() ? "show" : "blank");
			
			errorView.setShowDensity(showTheoryCheck.getState() ?
										StackedPlusNormalView.CONTIN_DISTN : StackedPlusNormalView.NO_DISTN);
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}