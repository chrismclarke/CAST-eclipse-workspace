package estimationProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import formula.*;

import estimation.*;


public class RectangularLikelihoodApplet extends XApplet {
	static final private String RECTANGULAR_PARAM = "rectangular";
	static final private String DATA_AXIS_PARAM = "dataAxis";
	static final private String DENSITY_AXIS_PARAM = "densityAxis";
	static final private String PARAM_AXIS_PARAM = "paramAxis";
	static final private String LIKELIHOOD_AXIS_PARAM = "likelihoodAxis";
	
	private NumValue startBeta, minBeta, maxBeta;
	
	private DataSet data;
	
	private ParameterSlider paramSlider;
	private XButton bestButton;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 0));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL));
			
				XPanel topPanel = new XPanel();
				topPanel.setLayout(new BorderLayout(0, 0));
				topPanel.add("North", new XLabel(translate("Likelihood") + MText.expandText(", L(#beta#)"), XLabel.LEFT, this));
				topPanel.add("Center", likelihoodPanel(data, "rect", "y"));
			mainPanel.add(ProportionLayout.TOP, topPanel);
			
				XPanel bottomPanel = new XPanel();
				bottomPanel.setLayout(new BorderLayout(0, 0));
				bottomPanel.add("North", new XLabel(translate("Rectangular probability density"), XLabel.LEFT, this));
				bottomPanel.add("Center", densityPanel(data, "rect", "y"));
			mainPanel.add(ProportionLayout.BOTTOM, bottomPanel);
		
		add("Center", mainPanel);
		
		add("South", sliderPanel());
	}
	
	protected DataSet getData() {
		StringTokenizer st = new StringTokenizer(getParameter(RECTANGULAR_PARAM));
		minBeta = new NumValue(st.nextToken());
		maxBeta = new NumValue(st.nextToken());
		startBeta = new NumValue(st.nextToken());
		
		DataSet data = new DataSet();
		
			RectangularDistnVariable rectDistn = new RectangularDistnVariable("rect");
			rectDistn.setLimits(0, startBeta.toDouble());
			rectDistn.setDecimals(startBeta.decimals);
		data.addVariable("rect", rectDistn);
		
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		return data;
	}
	
	private XPanel densityPanel(DataSet data, String distnKey, String dataKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis valueAxis = new HorizAxis(this);
		valueAxis.readNumLabels(getParameter(DATA_AXIS_PARAM));
		valueAxis.setAxisName(translate("Data values") + ", X");
		thePanel.add("Bottom", valueAxis);
		
		VertAxis densityAxis = new VertAxis(this);
		densityAxis.readNumLabels(getParameter(DENSITY_AXIS_PARAM));
		thePanel.add("Left", densityAxis);
		
		DataRectangularView densityView = new DataRectangularView(data, this, distnKey, dataKey, valueAxis, densityAxis);
		densityView.lockBackground(Color.white);
		thePanel.add("Center", densityView);
		
		return thePanel;
	}
	
	protected XPanel likelihoodPanel(DataSet data, String distnKey, String yKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis paramAxis = new HorizAxis(this);
			paramAxis.readNumLabels(getParameter(PARAM_AXIS_PARAM));
			paramAxis.setAxisName(translate("Unknown parameter") + MText.expandText(", #mu#"));
		thePanel.add("Bottom", paramAxis);
		
			VertAxis likelihoodAxis = new VertAxis(this);
			likelihoodAxis.readNumLabels(getParameter(LIKELIHOOD_AXIS_PARAM));
		thePanel.add("Left", likelihoodAxis);
		
			NumVariable yVar = (NumVariable)data.getVariable("y");
			int n = yVar.noOfValues();	
			double yMax = 0.0;
			for (int i=0; i<n ; i++)
				yMax = Math.max(yMax, yVar.doubleValueAt(i));
			RectangularLikelihoodView likelihood = new RectangularLikelihoodView(data, this,
																						"rect", n, yMax, paramAxis, likelihoodAxis);
			likelihood.lockBackground(Color.white);
		thePanel.add("Center", likelihood);
		
		return thePanel;
	}
	
	protected XPanel sliderPanel() {
		XPanel thePanel = new InsetPanel(30, 0);
		thePanel.setLayout(new BorderLayout(0, 20));
		
			paramSlider = new ParameterSlider(minBeta, maxBeta, startBeta,
											translate("Rectangular distribution max") + MText.expandText(", #beta#"), this);
		thePanel.add("Center", paramSlider);
		
			bestButton = new XButton(translate("Max likelihood"), this);
		thePanel.add("East", bestButton);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == paramSlider) {
			RectangularDistnVariable distn = (RectangularDistnVariable)data.getVariable("rect");
			distn.setLimits(0, paramSlider.getParameter().toDouble());
			data.variableChanged("rect");
			return true;
		}
		else if (target == bestButton) {
			NumVariable y = (NumVariable)getData().getVariable("y");
			double maxY = 0.0;
			int n = y.noOfValues();
			for (int i=0 ; i<n ; i++)
				maxY = Math.max(maxY, y.doubleValueAt(i));
			
			RectangularDistnVariable distn = (RectangularDistnVariable)data.getVariable("rect");
			distn.setLimits(0, maxY);
			data.variableChanged("rect");
			
			paramSlider.setParameter(maxY);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}