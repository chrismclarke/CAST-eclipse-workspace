package distributionProg;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import utils.*;

import distribution.*;


public class PoissonUniformApplet extends XApplet {
	static final protected String X_AXIS_PARAM = "xAxis";
	static final protected String CDF_AXIS_PARAM = "cdfAxis";
	static final protected String START_CUM_PARAM = "startCum";
	static final protected String LAMBDA_PARAM = "lambda";
	
	static final private Color kDensityBackground = new Color(0xEEEEF6);
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new BorderLayout(0, 0));
		
		add("North", topPanel());
		add("Center", cdfPanel(data, this));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		data.addVariable("rect", new RectangularDistnVariable("rect"));
		data.setSelection("rect", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		
		PoissonDistnVariable xVar = new PoissonDistnVariable("poisson");
		NumValue lambda = new NumValue(getParameter(LAMBDA_PARAM));
		xVar.setLambda(lambda);
		data.addVariable("poisson", xVar);
		data.setSelection("poisson", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		return data;
	}
	
	private XPanel topPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			XLabel densityLabel = new XLabel("Y = F(x)", XLabel.LEFT, this);
		thePanel.add(densityLabel);
		return thePanel;
	}
	
	protected XPanel cdfPanel(DataSet data, XApplet applet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		NumCatAxis xAxis = new HorizAxis(applet);
		xAxis.readNumLabels(getParameter(X_AXIS_PARAM));
		xAxis.setAxisName("X = F\u207B\u00B9(Y)");
		thePanel.add("Bottom", xAxis);
		
		NumCatAxis cdfAxis = new VertAxis(applet);
		cdfAxis.readNumLabels(getParameter(CDF_AXIS_PARAM));
		thePanel.add("Left", cdfAxis);
		
		double startCum = Double.parseDouble(getParameter(START_CUM_PARAM));
		DiscreteCdfView cdfView = new DiscreteCdfDragView(data, applet, "poisson", xAxis,
																																		cdfAxis, startCum);
		cdfView.lockBackground(Color.white);
		thePanel.add("Center", cdfView);
		
		
		DiscreteProbView poissonProbPlot = new DiscreteProbView(data, this, "poisson", null,
																															xAxis, DiscreteProbView.NO_DRAG);
		poissonProbPlot.lockBackground(kDensityBackground);
		poissonProbPlot.setMinDisplayWidth(70);
		thePanel.add("BottomMargin", poissonProbPlot);
		
		ContinuousProbView rectDensityPlot = new ContinuousProbView(data, this,
																													"rect", cdfAxis, null);
		rectDensityPlot.lockBackground(kDensityBackground);
		rectDensityPlot.setSupport(0.0, 1.0);
		rectDensityPlot.setMinDisplayWidth(70);
		thePanel.add("LeftMargin", rectDensityPlot);
		
		return thePanel;
	}
}