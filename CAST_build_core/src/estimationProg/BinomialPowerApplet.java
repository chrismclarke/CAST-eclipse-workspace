package estimationProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import formula.*;

import estimation.*;


public class BinomialPowerApplet extends XApplet {
	static final private String P_SUCCESS_PARAM = "pSuccess";
	static final private String PARAM_AXIS_PARAM = "paramAxis";
	static final private String POWER_AXIS_PARAM = "powerAxis";
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	static final private String CUTOFF_PARAM = "cutoff";
	static final private String NULL_PROB_PARAM = "nullProb";
	static final private String PI_NAME_PARAM = "piName";
	
	private DataSet data;
	
	private BinomialPowerFinder powerFinder;
	private PowerView powerView;
	
	private ParameterSlider paramSlider = null;
	private ParameterSlider sampleSizeSlider = null;
	private ParameterSlider cutoffSlider = null;
	
	public void setupApplet() {
		data = new DataSet();
		
		setLayout(new BorderLayout(0, 0));
		
		add("North", new XLabel(translate("Power function"), XLabel.LEFT, this));
		add("South", sliderPanel());
		
		add("Center", powerPanel());
	}
	
	private XPanel powerPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis piAxis = new HorizAxis(this);
		piAxis.readNumLabels(getParameter(PARAM_AXIS_PARAM));
		piAxis.setAxisName(getParamAxisName());
		thePanel.add("Bottom", piAxis);
		
		VertAxis powerAxis = new VertAxis(this);
		powerAxis.readNumLabels(getParameter(POWER_AXIS_PARAM));
		thePanel.add("Left", powerAxis);
		
		int sampleSize = (sampleSizeSlider == null) ? Integer.parseInt(getParameter(SAMPLE_SIZE_PARAM))
																			: (int)Math.round(sampleSizeSlider.getParameter().toDouble());
		int cutoff = (cutoffSlider == null) ? Integer.parseInt(getParameter(CUTOFF_PARAM))
																			: (int)Math.round(cutoffSlider.getParameter().toDouble());
		powerFinder = new BinomialPowerFinder(cutoff, CorePowerFinder.LOW_TAIL, sampleSize);
		
		powerView = new PowerView(data, this, powerFinder, piAxis, powerAxis);
		
		NumValue startParam = (paramSlider == null) ? new NumValue(getParameter(P_SUCCESS_PARAM))
																					: paramSlider.getParameter();
		powerView.setArrowValue(startParam);
		
		String nullString = getParameter(NULL_PROB_PARAM);
		if (nullString != null) {
			powerView.setNullProb(new NumValue(nullString));
		}
		
		powerView.lockBackground(Color.white);
		thePanel.add("Center", powerView);
		
		return thePanel;
	}
	
	protected String getParamAxisName() {
		return getParameter(PI_NAME_PARAM) + MText.expandText(", #pi#");
	}
	
	private XPanel sliderPanel() {
		StringTokenizer cutoffSt = new StringTokenizer(getParameter(CUTOFF_PARAM));
		StringTokenizer sampleSizeSt = new StringTokenizer(getParameter(SAMPLE_SIZE_PARAM));
		StringTokenizer pSuccessSt = new StringTokenizer(getParameter(P_SUCCESS_PARAM));
		if (cutoffSt.countTokens() == 1 && sampleSizeSt.countTokens() == 1)
			return probPanel(pSuccessSt);
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
		
		XPanel topPanel;
		if (cutoffSt.countTokens() == 1) {	//	only adjust sample size
			topPanel = new InsetPanel(70, 0);
			topPanel.setLayout(new BorderLayout(0, 0));
			topPanel.add(createSampleSizeSlider(sampleSizeSt));
		}
		else if (sampleSizeSt.countTokens() == 1) {	//	only adjust cutoff
			topPanel = new InsetPanel(100, 0);
			topPanel.setLayout(new BorderLayout(0, 0));
			topPanel.add(createCutoffSlider(cutoffSt));
		}
		else {
			topPanel = new XPanel();
			topPanel.setLayout(new ProportionLayout(0.6, 10));
			topPanel.add(ProportionLayout.LEFT, createSampleSizeSlider(sampleSizeSt));
			topPanel.add(ProportionLayout.RIGHT, createCutoffSlider(cutoffSt));
		}

		thePanel.add(topPanel);
		
		if (pSuccessSt.countTokens() > 1)
			thePanel.add(probPanel(pSuccessSt));
		
		return thePanel;
	}
	
	private ParameterSlider createSampleSizeSlider(StringTokenizer st) {
		sampleSizeSlider = new ParameterSlider(new NumValue(st.nextToken()),
										new NumValue(st.nextToken()), new NumValue(st.nextToken()),
										translate("Sample size") + ", n", this);
		return sampleSizeSlider;
	}
	
	private ParameterSlider createCutoffSlider(StringTokenizer st) {
		cutoffSlider = new ParameterSlider(new NumValue(st.nextToken()), new NumValue(st.nextToken()),
														new NumValue(st.nextToken()), "", this);
		cutoffSlider.setAddEquals(false);		//	title must be set AFTER equals sign is removed
		cutoffSlider.setTitle(translate("Reject") + MText.expandText(" H#sub0# ") + translate("when")
																					+ MText.expandText(" x #le#"), this);
		return cutoffSlider;
	}
	
	protected XPanel probPanel(StringTokenizer st) {
		XPanel thePanel = new InsetPanel(70, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
		paramSlider = new ParameterSlider(new NumValue(st.nextToken()), new NumValue(st.nextToken()),
																			new NumValue(st.nextToken()), getParameter(PI_NAME_PARAM)
																			+ MText.expandText(", #pi#"), this);
		paramSlider.setForeground(Color.red);
		thePanel.add("Center", paramSlider);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == paramSlider) {
			powerView.setArrowValue(paramSlider.getParameter());
			return true;
		}
		else if (target == cutoffSlider) {
			powerFinder.setCutoff((int)Math.round(cutoffSlider.getParameter().toDouble()));
			powerView.repaint();
			return true;
		}
		else if (target == sampleSizeSlider) {
			powerFinder.setSampleSize((int)Math.round(sampleSizeSlider.getParameter().toDouble()));
			powerView.repaint();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}