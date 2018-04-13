package sampDesignProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;

import sampDesign.*;


public class TwoStageMeanApplet extends TwoStageSampleApplet {
	static final private String VERT_AXIS_PARAM = "vertAxis";
	static final private String HORIZ_AXIS_PARAM = "horizAxis";
	
	
	protected XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.667, 0, ProportionLayout.VERTICAL, ProportionLayout.REMAINDER));
		
		thePanel.add(ProportionLayout.TOP, super.controlPanel(data, summaryData));
		
			XPanel corrPanel = new XPanel();
			corrPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 15));
			corrPanel.add(correlationSliderPanel());
		
		thePanel.add(ProportionLayout.BOTTOM, corrPanel);
		return thePanel;
	}
	
	protected String getClusterName() {
		return translate("Primary unit");
	}
	
	protected XPanel displayPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel headerPanel = new XPanel();
			headerPanel.setLayout(new BorderLayout());
			
				CoreVariable y = data.getVariable("y");
				XLabel valueLabel = new XLabel(y.name, XLabel.LEFT, this);
				XLabel meanLabel = new XLabel(translate("Mean"), XLabel.RIGHT, this);
				meanLabel.setFont(getStandardBoldFont());
				meanLabel.setForeground(new Color(0x660000));
			headerPanel.add("Center", valueLabel);
			headerPanel.add("East", meanLabel);
		
		thePanel.add("North", headerPanel);
		
			XPanel graphPanel = new XPanel();
			graphPanel.setLayout(new AxisLayout());
			
				HorizAxis clusterAxis = new HorizAxis(this);
				clusterAxis.readNumLabels(getParameter(HORIZ_AXIS_PARAM));
				clusterAxis.setAxisName(getClusterName());
				
			graphPanel.add("Bottom", clusterAxis);
			
			
				VertAxis vertAxis = new VertAxis(this);
				String labelInfo = getParameter(VERT_AXIS_PARAM);
				vertAxis.readNumLabels(labelInfo);
			graphPanel.add("Left", vertAxis);
			
				TwoStageDotPlotView theView = new TwoStageDotPlotView(data, this, clusterAxis, vertAxis, "y");
				theView.lockBackground(Color.white);
			graphPanel.add("Center", theView);
			
				JitterPlusNormalView predDotPlot = new JitterPlusNormalView(summaryData, this, vertAxis, "normal", 1.0);
//				DotPlotView predDotPlot = new DotPlotView(summaryData, null, this, vertAxis,
//																														BufferedCanvas.BUFFERED, 1.0);
				predDotPlot.setActiveNumVariable("mean");
				predDotPlot.setMinDisplayWidth(40);
				predDotPlot.lockBackground(new Color(0xFFEEEE));
			graphPanel.add("RightMargin", predDotPlot);
		
		thePanel.add("Center", graphPanel);
		
		return thePanel;
	}
}