package linModProg;

import java.awt.*;

import axis.*;
import dataView.*;
import regn.*;
import utils.*;


public class ZoomLSApplet extends SampleLSApplet {
	static final protected String X_AXIS_SCALE_PARAM = "horiz2Scale";
	static final protected String Y_AXIS_SCALE_PARAM = "vert2Scale";
	
	static final protected Color kAnswerBackground = new Color(0xEEEEDD);
	
	private ZoomSlider zoomer;
	
	public void setupApplet() {
		zoomer = new ZoomSlider(this, 100);
		super.setupApplet();
	}
	
	protected XPanel samplingControlPanel(DataSet summaryData, int topInset, int bottomInset) {
		XPanel thePanel = new InsetPanel(0, 10, 0, 0);
		thePanel.setLayout(new BorderLayout());
		
			XPanel samplingPanel = new XPanel();
			samplingPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				XPanel innerPanel = new InsetPanel(5, 0);
				innerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				innerPanel.add(super.samplingControlPanel(summaryData, 10, 10));
				
				innerPanel.lockBackground(kAnswerBackground);
			samplingPanel.add(innerPanel);
			
		thePanel.add("West", samplingPanel);
		
		thePanel.add("Center", zoomer);
		
		return thePanel;
	}
	
	protected XPanel bivarDisplayPanel(DataSet data, int viewType) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		XPanel plotPanel = new XPanel();
		plotPanel.setLayout(new AxisLayout());
		
			HorizAxis xAxis = zoomer.createHorizAxis(data, getParameter(X_AXIS_PARAM),
																		getParameter(X_AXIS_SCALE_PARAM), this);
			Variable x = (Variable)this.data.getVariable("x");		//		the parameter 'data' may be summaryData
			xAxis.setAxisName(x.name);
			plotPanel.add("Bottom", xAxis);
			
			VertAxis yAxis = zoomer.createVertAxis(data, getParameter(Y_AXIS_PARAM),
																		getParameter(Y_AXIS_SCALE_PARAM), this);
			plotPanel.add("Left", yAxis);
			
			DataView dataView = (viewType == SAMPLE) ? getSampleView(data, xAxis, yAxis)
																: getSummaryView(data, xAxis, yAxis);
			dataView.lockBackground(Color.white);
			zoomer.setControlledView(dataView);
			dataView.setCrossSize(DataView.LARGE_CROSS);
			
			plotPanel.add("Center", dataView);
		
		thePanel.add("Center", plotPanel);
		thePanel.add("North", yNamePanel(data, yAxis));
		
		return thePanel;
	}
}