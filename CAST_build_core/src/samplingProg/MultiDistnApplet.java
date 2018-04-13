package samplingProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.NormalDistnVariable;
import imageGroups.*;

import sampling.*;


public class MultiDistnApplet extends XApplet {
	static final private String HORIZ_AXIS_PARAM = "horizAxis";
	static final private String PROB_AXIS_PARAM = "probAxis";
	static final private String MEAN_PARAM = "mean";
	
	private NormalDistnVariable y;
	private String meanString;
	private NormExpRectView theView;
	
	private XChoice distnTypeChoice;
	private int currentDistnIndex = 0;
	
	public void setupApplet() {
		MeanSDImages.loadMeanSD(this);
		
		DataSet data = getData();
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		add("East", controlPanel(data));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		meanString = getParameter(MEAN_PARAM);
		
		y = new NormalDistnVariable("distn");
		y.setParams(meanString + " " + meanString);
		data.addVariable("distn", y);
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(HORIZ_AXIS_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		
		VertAxis theProbAxis = new VertAxis(this);
		labelInfo = getParameter(PROB_AXIS_PARAM);
		theProbAxis.readNumLabels(labelInfo);
		thePanel.add("Left", theProbAxis);
		
		theView = new NormExpRectView(data, this, theHorizAxis, theProbAxis, y.getMean().toDouble(),
																																			NormExpRectView.NORMAL);
		
		theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 2));
		
		thePanel.add(new SummaryView(data, this, "distn", null, SummaryView.MEAN, y.getMean().decimals,
																																						SummaryView.POPULATION));
		thePanel.add(new SummaryView(data, this, "distn", null, SummaryView.SD, y.getMean().decimals,
																																						SummaryView.POPULATION));
		
		distnTypeChoice = new XChoice(this);
		distnTypeChoice.addItem(translate("Normal"));
		distnTypeChoice.addItem("Exponential");
		distnTypeChoice.addItem("Rectangular");
		distnTypeChoice.addItem("Two values");
		thePanel.add(distnTypeChoice);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == distnTypeChoice) {
			int newDistnIndex = distnTypeChoice.getSelectedIndex();
			if (newDistnIndex != currentDistnIndex) {
				theView.setDistnType(newDistnIndex);
				currentDistnIndex = newDistnIndex;
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}