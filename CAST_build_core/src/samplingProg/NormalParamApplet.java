package samplingProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import formula.*;

import sampling.*;

public class NormalParamApplet extends DensityAreaApplet {
	static final private String MEAN_PARAM = "mean";
	static final private String SD_PARAM = "sd";
	static final private String BEST_LABEL_PARAM = "bestLabel";
	
	protected ParameterSlider meanSlider, sdSlider;
	private XButton bestFitButton; 
	
/*
	public void setupApplet() {
		MeanSDImages.loadMeanSD(this);
		
		super.setupApplet();
	}
*/
	
	protected DataView getDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theProbAxis,
								String yKey, String distnKey, double class0Start, double classWidth) {
		boolean fillOrOutline = (yKey == null) ? HistoAndNormalView.FILL_DENSITY
															: HistoAndNormalView.OUTLINE_DENSITY;
		return new HistoAndNormalView(data, this, theHorizAxis, theProbAxis, distnKey, yKey, class0Start, classWidth,
																				HistoAndNormalView.SHOW_MEANSD, fillOrOutline);
	}
	
	protected XPanel probPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		
		XPanel sliderPanel = new XPanel();
		sliderPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 10));
		
		NormalDistnVariable y = (NormalDistnVariable)data.getVariable("distn");
		
		StringTokenizer st = new StringTokenizer(getParameter(MEAN_PARAM));
		NumValue min = new NumValue(st.nextToken());
		NumValue max = new NumValue(st.nextToken());
		int steps = Integer.parseInt(st.nextToken());
		String mu = MText.expandText("#mu#");
		meanSlider = new ParameterSlider(min, max, y.getMean(), steps, mu, this);
		meanSlider.setForeground(Color.blue);
		sliderPanel.add(meanSlider);
		
		String sdSliderValues = getParameter(SD_PARAM);
		if (sdSliderValues != null) {														//	when sigma is a known constant
			st = new StringTokenizer(sdSliderValues);
			min = new NumValue(st.nextToken());
			max = new NumValue(st.nextToken());
			steps = Integer.parseInt(st.nextToken());
			String sigma = MText.expandText("#sigma#");
			sdSlider = new ParameterSlider(min, max, y.getSD(), steps, sigma, this);
			sdSlider.setForeground(Color.red);
			sliderPanel.add(sdSlider);
		}
		
		thePanel.add("Center", sliderPanel);
		
		if (yKey != null) {
			XPanel bestPanel = new XPanel();
			bestPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
			
			String bestLabel = getParameter(BEST_LABEL_PARAM);
			if (bestLabel == null)
				bestLabel = translate("Best fit");
			else
				bestLabel = MText.expandText(bestLabel);

			bestFitButton = new XButton(bestLabel, this);
			bestPanel.add(bestFitButton);
			
			thePanel.add("East", bestPanel);
		}
		
		return thePanel;
	}
	
	public void setBestParams(NormalDistnVariable distn) {
		NumVariable y = (NumVariable)data.getVariable(yKey);
		
		double sy = 0.0;
		double syy = 0.0;
		int n = 0;
		ValueEnumeration e = y.values();
		while (e.hasMoreValues()) {
			double nextVal = e.nextDouble();
			sy += nextVal;
			syy += nextVal * nextVal;
			n ++;
		}
		double mean = sy / n;
		distn.setMean(mean);
		
		if (meanSlider != null)
			meanSlider.setParameter(mean);
		
		if (sdSlider != null) {
			double sd = Math.sqrt((syy - mean * sy) / (n - 1));
			distn.setSD(sd);
			
			sdSlider.setParameter(sd);
		}
	}

	
	private boolean localAction(Object target) {
		NormalDistnVariable distn = (NormalDistnVariable)data.getVariable("distn");
		if (target == meanSlider) {
			distn.setMean(meanSlider.getParameter().toDouble());
			data.variableChanged("distn");
			return true;
		}
		else if (target == sdSlider) {
			distn.setSD(sdSlider.getParameter().toDouble());
			data.variableChanged("distn");
			return true;
		}
		else if (target == bestFitButton) {
			setBestParams(distn);
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