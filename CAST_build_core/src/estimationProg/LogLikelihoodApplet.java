package estimationProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import formula.*;

import estimation.*;


public class LogLikelihoodApplet extends BinomialLikelihoodApplet {
	static final private String LOG_LIKELIHOOD_AXIS_PARAM = "loglikelihoodAxis";
		
	private XButton bestButton;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 0));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL));
			
				XPanel topPanel = new XPanel();
				topPanel.setLayout(new BorderLayout(0, 0));
				topPanel.add("North", new XLabel(translate("Likelihood") + MText.expandText(", L(#pi#)"), XLabel.LEFT, this));
				topPanel.add("Center", likelihoodPanel(data, true));
			
			mainPanel.add(ProportionLayout.TOP, topPanel);
			
				XPanel bottomPanel = new XPanel();
				bottomPanel.setLayout(new BorderLayout(0, 0));
				bottomPanel.add("North", new XLabel(translate("Log-likelihood") + MText.expandText(", #ell#(#pi#)"), XLabel.LEFT, this));
				bottomPanel.add("Center", logLikelihoodPanel(data, true));
			mainPanel.add(ProportionLayout.BOTTOM, bottomPanel);
		
		add("Center", mainPanel);
		
		add("South", sliderPanel());
	}
	
	protected XPanel logLikelihoodPanel(DataSet data, boolean showSlope) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis piAxis = new HorizAxis(this);
		piAxis.readNumLabels(kZeroOneAxisParams);
		piAxis.setAxisName(getParamAxisName());
		thePanel.add("Bottom", piAxis);
		
		VertAxis loglikelihoodAxis = new VertAxis(this);
		loglikelihoodAxis.readNumLabels(getParameter(LOG_LIKELIHOOD_AXIS_PARAM));
		thePanel.add("Left", loglikelihoodAxis);
		
		BinomialLikelihoodFinder likelihoodFinder = new BinomialLikelihoodFinder(data, "binom", n, x);
		LikelihoodView likelihood = new LogLikelihoodView(data, this, "binom", likelihoodFinder,
																															piAxis, loglikelihoodAxis);
		if (showSlope)
			likelihood.setDrawSlope(true);
		likelihood.lockBackground(Color.white);
		thePanel.add("Center", likelihood);
		
		return thePanel;
	}
	
	protected XPanel sliderPanel() {
		XPanel thePanel = new InsetPanel(10, 0);
		thePanel.setLayout(new BorderLayout(20, 0));
		
		thePanel.add("Center", super.sliderPanel());
		
			bestButton = new XButton(translate("Max likelihood"), this);
		thePanel.add("East", bestButton);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == bestButton) {
			BinomialDistnVariable distn = (BinomialDistnVariable)data.getVariable("binom");
			double bestProb = x / (double)n;
			distn.setProb(bestProb);
			data.variableChanged("binom");
			
			paramSlider.setParameter(bestProb);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}