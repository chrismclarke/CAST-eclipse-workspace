package samplingProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import random.RandomMultinomial;
import imageGroups.*;

import sampling.*;


public class PopSampCStatsApplet extends XApplet {
	static final private String CAT_PROB_PARAM = "catProb";
	static final private String SAMPLING_PARAM = "sampling";
	static final protected String PROB_AXIS_PARAM = "probAxis";
	
	private RandomMultinomial generator;
	
	private DataSet data;
	private XButton takeSampleButton;
	
	public void setupApplet() {
		MeanSDImages.loadMeanSD(this);
		TickCrossImages.loadCrossAndTick(this);
		data = getData();
		
		setLayout(new BorderLayout());
		add("East", controlPanel(data));
		add("Center", displayPanel(data));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		CatDistnVariable dv = new CatDistnVariable(translate("Popn"));
		dv.readLabels(getParameter(CAT_LABELS_PARAM));
		dv.setParams(getParameter(CAT_PROB_PARAM));
		data.addVariable("popn", dv);
		
		StringTokenizer st = new StringTokenizer(getParameter(SAMPLING_PARAM));
		int sampleSize = Integer.parseInt(st.nextToken());
		long samplingSeed = Long.parseLong(st.nextToken());
		generator = new RandomMultinomial(sampleSize, dv.getProbs());
		generator.setSeed(samplingSeed);
		
		CatVariable sv = new CatVariable(getParameter(CAT_NAME_PARAM), Variable.USES_REPEATS);
		sv.readLabels(getParameter(CAT_LABELS_PARAM));
		data.addVariable("samp", sv);
		
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		CatVariable cv = (CatVariable)data.getVariable("samp");
		String cat0Name = cv.getLabel(0).toString();
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(12, 0));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new ProportionLayout(0.5, 20, ProportionLayout.VERTICAL, ProportionLayout.REMAINDER));
			leftPanel.add("Top", barChartPanel(data, "popn"));
			leftPanel.add("Bottom", barChartPanel(data, "samp"));
		thePanel.add("Center", leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new ProportionLayout(0.5, 20, ProportionLayout.VERTICAL, ProportionLayout.REMAINDER));
			rightPanel.add("Top", summaryPanel(data, "popn", translate("Popn") + " P(" + cat0Name + ")"));
			rightPanel.add("Bottom", summaryPanel(data, "samp", translate("Sample") + " P(" + cat0Name + ")"));
		thePanel.add("East", rightPanel);
		
		return thePanel;
	}
	
	private XPanel barChartPanel(DataSet data, String varKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		CatVariable cv = (CatVariable)data.getVariable("samp");
		
		HorizAxis horizAxis = new HorizAxis(this);
		horizAxis.setCatLabels(cv);
		thePanel.add("Bottom", horizAxis);
		
		VertAxis probAxis = new VertAxis(this);
		String labelInfo = getParameter(PROB_AXIS_PARAM);
		probAxis.readNumLabels(labelInfo);
		thePanel.add("Left", probAxis);
		
		PBarView theView = new PBarView(data, this, varKey, horizAxis, probAxis);
		
		theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	private XPanel summaryPanel(DataSet data, String varKey, String panelTitle) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 5));
		
			XLabel l = new XLabel(panelTitle, XLabel.LEFT, this);
			l.setFont(getBigBoldFont());
		thePanel.add(l);
		
			ProbView v = new ProbView(data, this, varKey);
			v.setFont(getBigFont());
		thePanel.add(v);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
		takeSampleButton = new XButton(translate("Take sample"), this);
		controlPanel.add(takeSampleButton);
		
		return controlPanel;
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			int count[] = generator.generate();
			CatVariable cv = (CatVariable)data.getVariable("samp");
			cv.setCounts(count);
			data.variableChanged("samp");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}