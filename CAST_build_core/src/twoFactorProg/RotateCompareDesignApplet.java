package twoFactorProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;
import valueList.*;
import graphics3D.*;

import corr.*;
import twoFactor.*;


public class RotateCompareDesignApplet extends XApplet {
	static final private String MAX_EFFECT_PARAM = "maxEffect";
	static final private String EFFECT_AXIS_PARAM = "effectAxis";
	
	static final private Color kLightGrey = new Color(0xDDDDDD);
	
	private TwoFactorDataSet data;
	private SummaryDataSet summaryOneData, summaryCrossedData;
	
	private RotateEstimatesView dataView;
	private StackedDotPlotView estimateOneView, estimateCrossedView;
	private OneValueView estimateValueView;
	
	private StDevnView seOneView, seCrossedView;
	
	private XChoice designChoice, effectChoice;
	private int currentDesignChoice = 0;
	private int currentEffectChoice = 0;
	
	private RepeatingButton sampleButton;
	
	public void setupApplet() {
		data = new TwoFactorDataSet(this);
		
		NumValue maxEffect = new NumValue(getParameter(MAX_EFFECT_PARAM));
		summaryOneData = getSummaryData(data, maxEffect);
		summaryCrossedData = getSummaryData(data, maxEffect);
		summaryOneData.takeSample();
		
		setLayout(new ProportionLayout(0.6, 0, ProportionLayout.VERTICAL));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(5, 0));
			topPanel.add("Center", displayPanel(data));
			topPanel.add("East", controlPanel(data, summaryOneData, maxEffect));
			
		add(ProportionLayout.TOP, topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL));
			bottomPanel.add(ProportionLayout.TOP, estimatePanel("One at a time", summaryOneData, true, maxEffect));
			bottomPanel.add(ProportionLayout.BOTTOM, estimatePanel("Factorial design", summaryCrossedData, false, maxEffect));
		
		add(ProportionLayout.BOTTOM, bottomPanel);
	}
	
	private SummaryDataSet getSummaryData(DataSet sourceData, NumValue maxEffect) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "error");
		
		int decimals = maxEffect.decimals;
		summaryData.addVariable("xEffect", new FactorEffectVariable("Estimate", "y", "ls",
																									FactorEffectVariable.X_EFFECT, decimals));
		summaryData.addVariable("zEffect", new FactorEffectVariable("Estimate", "y", "ls",
																									FactorEffectVariable.Z_EFFECT, decimals));
		
		summaryData.setAccumulate(true);
		return summaryData;
	}
	
	private XPanel displayPanel(TwoFactorDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		D3Axis xAxis = new D3Axis(data.getXVarName(), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		CatVariable xVar = (CatVariable)data.getVariable("x");
		xAxis.setCatScale(xVar);
		
		D3Axis yAxis = new D3Axis(data.getYVarName(), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
		yAxis.setNumScale(data.getYAxisInfo());
		
		D3Axis zAxis = new D3Axis(data.getZVarName(), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		CatVariable zVar = (CatVariable)data.getVariable("z");
		zAxis.setCatScale(zVar);
		
		dataView = new RotateEstimatesView(data, this, xAxis, yAxis, zAxis, "x", "y", "z", "ls",
														RotateEstimatesView.X_EFFECT);
		dataView.setCrossSize(DataView.LARGE_CROSS);
		dataView.lockBackground(Color.white);
			
		thePanel.add("Center", dataView);
		return thePanel;
	}
	
	private XPanel estimatePanel(String title, SummaryDataSet summaryData, boolean oneAtATime, NumValue maxSe) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			XLabel titleLabel = new XLabel(title, XLabel.LEFT, this);
			titleLabel.setFont(getStandardBoldFont());
			
		thePanel.add("North", titleLabel);
			
			XPanel scatterPanel = new XPanel();
			scatterPanel.setLayout(new AxisLayout());
			
					HorizAxis axis = new HorizAxis(this);
					axis.readNumLabels(getParameter(EFFECT_AXIS_PARAM));
					axis.setAxisName("Estimate of effect");
				scatterPanel.add("Bottom", axis);
					
					StackedDotPlotView theView = new StackedDotPlotView(summaryData, this, axis);
					theView.setActiveNumVariable("xEffect");
					if (oneAtATime) {
						estimateOneView = theView;
						theView.lockBackground(Color.white);
					}
					else {
						estimateCrossedView = theView;
						estimateCrossedView.setCanDragCrosses(false);
						theView.lockBackground(kLightGrey);
					}
				
				scatterPanel.add("Center", theView);
				
		thePanel.add("Center", scatterPanel);
		
			XPanel sePanel = new XPanel();
			sePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 7));
				
				StDevnView seView = new StDevnView(summaryData, "xEffect", MeanView.GENERIC_TEXT_FORMULA, 0, this);
				seView.setLabel("SE");
				seView.setMaxValue(maxSe);
				if (oneAtATime)
					seOneView = seView;
				else
					seCrossedView = seView;
				
			sePanel.add(seView);
			
				ValueCountView theCount = new ValueCountView(summaryData, this);
				theCount.setLabel("n =");
			sePanel.add(theCount);
				
		thePanel.add("East", sePanel);
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data, SummaryDataSet summaryOneData, NumValue maxEffect) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_SPACED, 20));
		
			XPanel designPanel = new XPanel();
			designPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
				
				XLabel designLabel = new XLabel(translate("Design"), XLabel.LEFT, this);
				designLabel.setFont(getStandardBoldFont());
			designPanel.add(designLabel);
				
				designChoice = new XChoice(this);
				designChoice.addItem("One at a time");
				designChoice.addItem("Factorial");
			designPanel.add(designChoice);
		
		thePanel.add(designPanel);
		
			XPanel effectPanel = new XPanel();
			effectPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
				
				XLabel effectLabel = new XLabel("Effect of changing...", XLabel.LEFT, this);
				effectLabel.setFont(getStandardBoldFont());
			effectPanel.add(effectLabel);
				
				effectChoice = new XChoice(this);
				effectChoice.addItem(data.getVariable("x").name);
				effectChoice.addItem(data.getVariable("z").name);
			effectPanel.add(effectChoice);
		
		thePanel.add(effectPanel);
		
			estimateValueView = new OneValueView(summaryOneData, "xEffect", this, maxEffect);
		thePanel.add(estimateValueView);
		
			sampleButton = new RepeatingButton(translate("Repeat experiment"), this);
		thePanel.add(sampleButton);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			SummaryDataSet summaryData = (currentDesignChoice == 0) ? summaryOneData
																															: summaryCrossedData;
			summaryData.takeSample();
			return true;
		}
		else if (target == effectChoice) {
			int newChoice = effectChoice.getSelectedIndex();
			if (newChoice != currentEffectChoice) {
				currentEffectChoice = newChoice;
				String effectKey = (newChoice == 0) ? "xEffect" : "zEffect";
				
				estimateOneView.setActiveNumVariable(effectKey);
				estimateOneView.repaint();
				
				estimateCrossedView.setActiveNumVariable(effectKey);
				estimateCrossedView.repaint();
				
				seOneView.setVariableKey(effectKey);
				seOneView.repaint();
				
				seCrossedView.setVariableKey(effectKey);
				seCrossedView.repaint();
				
				estimateValueView.setVariableKey(effectKey);
				
				dataView.setDisplayEffect((newChoice == 0) ? RotateEstimatesView.X_EFFECT
																										: RotateEstimatesView.Z_EFFECT);
				dataView.repaint();
			}
			return true;
		}
		else if (target == designChoice) {
			int newChoice = designChoice.getSelectedIndex();
			if (newChoice != currentDesignChoice) {
				currentDesignChoice = newChoice;
				
				data.changeDataSet(newChoice);
				
				estimateOneView.lockBackground(newChoice == 0 ? Color.white : kLightGrey);
				estimateOneView.setCanDragCrosses(newChoice == 0);
				estimateOneView.repaint();
				estimateCrossedView.lockBackground(newChoice == 1 ? Color.white : kLightGrey);
				estimateCrossedView.setCanDragCrosses(newChoice == 1);
				estimateCrossedView.repaint();
				
				SummaryDataSet summaryData = (currentDesignChoice == 0) ? summaryOneData
																															: summaryCrossedData;
				int nEstimates = summaryData.getSelection().getNoOfFlags();
				if (nEstimates == 0)
					summaryData.takeSample();
				else {
					summaryData.setSelection(nEstimates - 1);
					data.variableChanged("y");
				}
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}