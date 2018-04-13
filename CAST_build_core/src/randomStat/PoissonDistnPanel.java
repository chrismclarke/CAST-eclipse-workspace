package randomStat;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import distn.*;
import utils.*;
import formula.*;

import distribution.*;


public class PoissonDistnPanel extends XPanel {
	private ParameterSlider lambdaSlider;
	
	private DataSet data;
	private HorizAxis countAxis;
	private NumValue minLambda, maxLambda, startLambda;
	
	public PoissonDistnPanel(XApplet applet, String lambdaLimits, String axisInfo, double baseLambda,
																																							int dragType) {
			StringTokenizer st = new StringTokenizer(lambdaLimits);
			minLambda = new NumValue(st.nextToken());
			maxLambda = new NumValue(st.nextToken());
			startLambda = new NumValue(st.nextToken());
		
		data = getData();
		
		setLayout(new BorderLayout(0, 10));
		
		add("North", sliderPanel(applet));
		
		add("Center", barchartPanel(data, applet, axisInfo, baseLambda, dragType));
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				int maxCount = (int)Math.round(Math.floor(countAxis.maxOnAxis));
				DiscreteProbValueView probView = new DiscreteProbValueView(data, "distn", applet, maxCount);
				probView.setFont(applet.getBigFont());
			bottomPanel.add(probView);
		add("South", bottomPanel);
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
			PoissonDistnVariable y = new PoissonDistnVariable("x");
			y.setLambda(startLambda);
		data.addVariable("distn", y);
		data.setSelection("distn", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		
		return data;
	}
	
	private XPanel sliderPanel(XApplet applet) {
		XPanel thePanel = new InsetPanel(50, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
			lambdaSlider = new ParameterSlider(minLambda, maxLambda, startLambda, MText.translateUnicode("lambda"), applet);
		thePanel.add("Center", lambdaSlider);
		return thePanel;
	}
	
	private XPanel barchartPanel(DataSet data, XApplet applet, String axisInfo, double baseLambda,
																																								int dragType) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		countAxis = new HorizAxis(applet);
		countAxis.readNumLabels(axisInfo);
		countAxis.setAxisName("number of events");
		thePanel.add("Bottom", countAxis);
		
		DiscreteProbView barChart = new DiscreteProbView(data, applet, "distn", null, null, countAxis, dragType);
		barChart.setBaseLambda(baseLambda);
		barChart.lockBackground(Color.white);
		thePanel.add("Center", barChart);
		
		return thePanel;
	}
	
	public Insets insets() {
		return new Insets(3, 3, 3, 3);
	}

	
	private boolean localAction(Object target) {
		PoissonDistnVariable y = (PoissonDistnVariable)data.getVariable("distn");
		if (target == lambdaSlider) {
			NumValue newLambda = lambdaSlider.getParameter();
			y.setLambda(newLambda);
			data.variableChanged("distn");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}