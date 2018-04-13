package loessProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;
import valueList.*;

import regnView.*;
import randomisation.*;
import loess.*;


public class DurbinWatsonApplet extends CoreDiagnosticApplet {
	static final private String RESID_AXIS_INFO_PARAM = "residAxis";
	static final private String DW_AXIS_INFO_PARAM = "durbinWatsonAxis";
	
	static final private int kBigInt = 999;
	static final private int kDotplotHeight = 100;
	static final private double kBigValue = 999.0;
	
	private XCheckbox accumulateCheck;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		addSimulationVariables(data, "x", "y");
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = super.getSummaryData(sourceData);
		
		summaryData.addVariable("dw", new DurbinWatsonValueVariable(translate("Durbin-Watson"), "resid"));
		
		return summaryData;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.HORIZONTAL,
																						ProportionLayout.TOTAL));
			
			mainPanel.add("Left", createPlotPanel(data, false, "x", "response", null,
					getParameter(X_AXIS_INFO_PARAM), getParameter(Y_AXIS_INFO_PARAM), 0));
			mainPanel.add("Right", createPlotPanel(data, false, "x", "resid", null,
						getParameter(X_AXIS_INFO_PARAM), getParameter(RESID_AXIS_INFO_PARAM), 1));
		
		thePanel.add("Center", mainPanel);
			
			XPanel dwValuePanel = new InsetPanel(0, 0, 0, 10);
			dwValuePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
			dwValuePanel.add(new DurbWatsonValueView(data, this, "resid"));
//				Separator sep = new Separator(1.0, 4);
//				sep.setMinWidth(200);
//			dwValuePanel.add(sep);
		
		thePanel.add("South", dwValuePanel);
		
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
																									int plotIndex) {
		switch (plotIndex) {
			case 0:
				return new LSScatterView(data, this, theHorizAxis, theVertAxis, "x", "response", "lsLine");
			default:
				return new ScatterView(data, this, theHorizAxis, theVertAxis, "x", "resid");
		}
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 8));
		
			XPanel topPanel = super.controlPanel(data);
			
				accumulateCheck = new XCheckbox(translate("Accumulate"), this);
				accumulateCheck.disable();
			topPanel.add(accumulateCheck);
			
				ValueCountView theCount = new ValueCountView(summaryData, this);
			topPanel.add(theCount);
		
		thePanel.add("North", topPanel);
		
			XPanel fixedSizePanel = new XPanel();
			fixedSizePanel.setLayout(new FixedSizeLayout(kBigInt, kDotplotHeight));
			
				XPanel dotPlotPanel = new XPanel();
				dotPlotPanel.setLayout(new AxisLayout());
				
					HorizAxis dwAxis = new HorizAxis(this);
					String labelInfo = getParameter(DW_AXIS_INFO_PARAM);
					dwAxis.readNumLabels(labelInfo);
					CoreVariable v = summaryData.getVariable("dw");
					dwAxis.setAxisName(v.name);
				dotPlotPanel.add("Bottom", dwAxis);
				
					NumVariable resid = (NumVariable)data.getVariable("resid");
					double actualDW = DurbinWatsonValueVariable.durbWatsonFromResid(resid).toDouble();
					
					RandomDiffDotView dwView = new RandomDiffDotView(summaryData, this, dwAxis, actualDW, kBigValue);
					dwView.lockBackground(Color.white);
				dotPlotPanel.add("Center", dwView);
				
			fixedSizePanel.add(dotPlotPanel);
		
		thePanel.add("Center", fixedSizePanel);
		
			XPanel pValuePanel = new XPanel();
			pValuePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				ExtremePropnView pExtreme = new ExtremePropnView(summaryData, this, "dw", actualDW, kBigValue);
				pExtreme.setFont(getBigFont());
				pExtreme.setHighlight(true);
			pValuePanel.add(pExtreme);
		
		thePanel.add("South", pValuePanel);
		
		return thePanel;
	}
	
	protected XButton getSampleButton() {
		return new RepeatingButton(translate("Simulate"), this);
	}
	
	protected void changeDataSample(boolean toData) {
		super.changeDataSample(toData);
		
		if (toData == SwitchResponseVariable.SHOW_RANDOM) {
			NumVariable dwSummary = (NumVariable)summaryData.getVariable("dw");
			int nValues = dwSummary.noOfValues();
			if (nValues == 0)
				summaryData.takeSample();
		}
		
		if (toData == SwitchResponseVariable.SHOW_RANDOM)
			accumulateCheck.enable();
		else
			accumulateCheck.disable();
	}
	
	private boolean localAction(Object target) {
		if (target == accumulateCheck) {
			summaryData.setAccumulate(accumulateCheck.getState());
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