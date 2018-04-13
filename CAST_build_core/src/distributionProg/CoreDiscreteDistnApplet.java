package distributionProg;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import utils.*;

import distribution.*;


abstract public class CoreDiscreteDistnApplet extends XApplet {
	static final protected String X_AXIS_PARAM = "xAxis";
	static final protected String X_AXIS_NAME_PARAM = "xAxisName";
	static final protected String CUMULATIVE_PARAM = "cumulative";
	
	static final private String kProbAxis = "0 1 0 0.1";
	
	private boolean showCumulative;
	
	protected DataSet data;
	
	private XCheckbox probDisplayCheck;
	protected VertAxis probAxis;
	protected XLabel probLabel;
	protected DiscreteProbView barChart;
	
	public void setupApplet() {
		String cumulativeString = getParameter(CUMULATIVE_PARAM);
		showCumulative = cumulativeString !=  null && cumulativeString.equals("true");
		
		data = new DataSet();
		data.addVariable("distn", getDistn());
		data.setSelection("distn", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		
		setLayout(new BorderLayout(0, 0));
		
		if (!showCumulative)
			add("North", topPanel());
		
		add("South", sliderPanel());
		
		add("Center", showCumulative ? cdfPanel(data, this) : barchartPanel(data, this));
		
		initialiseParams();
	}
	
	abstract protected DiscreteDistnVariable getDistn();
	
	abstract protected void initialiseParams();
	
	private XPanel topPanel() {
		XPanel thePanel = new XPanel();
		GridBagLayout gbl = new GridBagLayout();
		thePanel.setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor=GridBagConstraints.SOUTHWEST; gbc.fill=GridBagConstraints.NONE; gbc.gridheight=1;
			gbc.gridwidth=1; gbc.gridx=GridBagConstraints.RELATIVE; gbc.gridy=GridBagConstraints.RELATIVE;
			gbc.insets=new Insets(0,0,0,0); gbc.ipadx=0; gbc.ipady=0;
			gbc.weightx=0.0; gbc.weighty=0.0;		//	ensures that the label is bottom-justified
		
			probLabel = new XLabel(translate("Probability") + ", p(x)", XLabel.LEFT, this);
		thePanel.add(probLabel);
		gbl.setConstraints(probLabel, gbc);
		
			gbc.anchor=GridBagConstraints.CENTER; gbc.weightx=1.0;
		
			probDisplayCheck = new XCheckbox("Show zero-one axis", this);
			probDisplayCheck.setState(true);
		thePanel.add(probDisplayCheck);
		gbl.setConstraints(probDisplayCheck, gbc);
		
		return thePanel;
	}
	
	private XPanel barchartPanel(DataSet data, XApplet applet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis xAxis = new HorizAxis(applet);
		xAxis.readNumLabels(getParameter(X_AXIS_PARAM));
		xAxis.setAxisName(getParameter(X_AXIS_NAME_PARAM));
		thePanel.add("Bottom", xAxis);
		
		probAxis = new VertAxis(applet);
		probAxis.readNumLabels(kProbAxis);
		thePanel.add("Left", probAxis);
		
		barChart = new DiscreteProbView(data, applet, "distn", null, xAxis, DiscreteProbView.NO_DRAG);
		barChart.lockBackground(Color.white);
		barChart.setForceZeroOneAxis(true);
		thePanel.add("Center", barChart);
		
		return thePanel;
	}
	
	private XPanel cdfPanel(DataSet data, XApplet applet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis xAxis = new HorizAxis(applet);
		xAxis.readNumLabels(getParameter(X_AXIS_PARAM));
		xAxis.setAxisName(getParameter(X_AXIS_NAME_PARAM));
		thePanel.add("Bottom", xAxis);
		
		probAxis = new CdfHalfAxis(applet);
		thePanel.add("Left", probAxis);
		
		DiscreteProbView cdf = new DiscreteCdfView(data, applet, "distn", null, xAxis, DiscreteProbView.DRAG_CUMULATIVE);
		cdf.setTitleString("F(x)", applet);
		cdf.lockBackground(Color.white);
		
		barChart = new DiscreteProbView(data, applet, "distn", null, null, xAxis, DiscreteProbView.DRAG_CUMULATIVE);
		barChart.setTitleString("p(x)", applet);
		barChart.lockBackground(Color.white);
		
		thePanel.add("Center", new MultipleDataView(data, applet, cdf, barChart));
		
		return thePanel;
	}
	
	abstract protected XPanel sliderPanel();
	
	private boolean localAction(Object target) {
		if (target == probDisplayCheck) {
			boolean showAxis = probDisplayCheck.getState();
			probAxis.show(showAxis);
			probLabel.show(showAxis);
			
			barChart.setForceZeroOneAxis(showAxis);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}