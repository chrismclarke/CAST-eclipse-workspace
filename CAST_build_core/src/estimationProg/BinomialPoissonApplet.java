package estimationProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;

import estimation.*;


public class BinomialPoissonApplet extends XApplet {
	static final private String RATE_PARAM = "rate";
	static final private String COUNT_AXIS_PARAM = "countAxis";
	static final private String SECTIONS_PARAM = "sections";
	static final private String PROB_AXIS_PARAM = "probAxis";
	static final private String RANDOM_PARAM = "randomSeed";
	static final private String TIME_AXIS_PARAM = "timeAxis";
	
	static final private Color kDistnColor = new Color(0x999999);
	
	private int[] nSections;
	private NumValue rate;
	
	private DataSet data;
	
	private TimeSplitView timeView;
	
	private XChoice sectionsChoice;
	private int currentSectionsChoice = 0;
	private XButton generateEvents;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 0));
		
		XPanel mainPanel = new XPanel();
		mainPanel.setLayout(new ProportionLayout(0.35, 10, ProportionLayout.VERTICAL));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(0, 0));
			topPanel.add("Center", timePanel(data));
			topPanel.add("South", controlPanel());
		
		mainPanel.add(ProportionLayout.TOP, topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(0, 0));
			bottomPanel.add("North", new XLabel("p(x)", XLabel.LEFT, this));
			bottomPanel.add("Center", barchartPanel(data));
		mainPanel.add(ProportionLayout.BOTTOM, bottomPanel);
		
		add("Center", mainPanel);
	}
	
	private DataSet getData() {
		StringTokenizer st = new StringTokenizer(getParameter(SECTIONS_PARAM));
		nSections = new int[st.countTokens()];
		for (int i=0 ; i<nSections.length ; i++)
			nSections[i] = Integer.parseInt(st.nextToken());
		rate = new NumValue(getParameter(RATE_PARAM));
		
		DataSet data = new DataSet();
		
			BinomialDistnVariable binom = new BinomialDistnVariable("binom");
			binom.setCount(nSections[0]);
			binom.setProb(rate.toDouble() / nSections[0]);
		data.addVariable("binom", binom);
		
		binom.setMinSelection(-1);
		binom.setMaxSelection(-1);
		
		return data;
	}
	
	private XPanel barchartPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis nAxis = new HorizAxis(this);
			nAxis.readNumLabels(getParameter(COUNT_AXIS_PARAM));
			nAxis.setAxisName(translate("Number of events") + ", x");
		thePanel.add("Bottom", nAxis);
		
			VertAxis probAxis = new VertAxis(this);
			probAxis.readNumLabels(getParameter(PROB_AXIS_PARAM));
		thePanel.add("Left", probAxis);
		
			BinomialPoissonView barChart = new BinomialPoissonView(data, this, "binom", nAxis, rate,
																											 translate("Binomial"));
			barChart.setFixedMaxProb(probAxis.maxOnAxis);
			barChart.setDensityColor(kDistnColor);
			barChart.lockBackground(Color.white);
		thePanel.add("Center", barChart);
		
		return thePanel;
	}
	
	private XPanel timePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis timeAxis = new HorizAxis(this);
		timeAxis.readNumLabels(getParameter(TIME_AXIS_PARAM));
		timeAxis.setAxisName(translate("Time") + ", t");
		thePanel.add("Bottom", timeAxis);
		
		long randomSeed = Long.parseLong(getParameter(RANDOM_PARAM));
		timeView = new TimeSplitView(data, this, "binom", timeAxis, randomSeed);
//		timeView.lockBackground(Color.white);
		thePanel.add("Center", timeView);
		
		return thePanel;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new InsetPanel(0, 10, 0, 0);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
		StringTokenizer st = new StringTokenizer(translate("Split into*periods"), "*");
		sectionsChoice = new XChoice(st.nextToken(), st.nextToken(), XChoice.HORIZONTAL, this);
			for (int i=0 ; i<nSections.length ; i++)
				sectionsChoice.addItem("" + nSections[i]);
		thePanel.add(sectionsChoice);	
		
			generateEvents = new XButton(translate("Generate events"), this);
		thePanel.add(generateEvents);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == sectionsChoice) {
			if (sectionsChoice.getSelectedIndex() != currentSectionsChoice) {
				currentSectionsChoice = sectionsChoice.getSelectedIndex();
				BinomialDistnVariable binom = (BinomialDistnVariable)data.getVariable("binom");
				binom.setCount(nSections[currentSectionsChoice]);
				binom.setProb(rate.toDouble() / nSections[currentSectionsChoice]);
				data.variableChanged("binom");
			}
			return true;
		}
		else if (target == generateEvents) {
			timeView.takeSample();
			return true;
		}		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}