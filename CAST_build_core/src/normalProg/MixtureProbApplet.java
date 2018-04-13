package normalProg;

import java.util.*;

import dataView.*;
import axis.*;
import distn.*;

import exerciseNormal.JdistnAreaLookup.*;


public class MixtureProbApplet extends XApplet {
	static final private String HORIZ_AXIS_PARAM = "horizAxis";
	static final private String NORMAL_MIXTURE_PARAM = "modelMixture";
	static final private String INITIAL_LIMITS_PARAM = "initialLimits";
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			horizAxis.readNumLabels(getParameter(HORIZ_AXIS_PARAM));
		add("Bottom", horizAxis);
		
			ContinDistnLookupView distnView = new ContinDistnLookupView(data, this,
																																horizAxis, "distn", true);
			distnView.setSingleDensity();
		add("Center", distnView);
	}
	
	protected DataSet getData() {
		StringTokenizer st = new StringTokenizer(getParameter(NORMAL_MIXTURE_PARAM));
		double mean1 = Double.parseDouble(st.nextToken());
		double sd1 = Double.parseDouble(st.nextToken());
		double mean2 = Double.parseDouble(st.nextToken());
		double sd2 = Double.parseDouble(st.nextToken());
		double p = Double.parseDouble(st.nextToken());
		
		DataSet data = new DataSet();
			NormalDistnVariable y1Distn = new NormalDistnVariable("Distn1");
			y1Distn.setMean(mean1);
			y1Distn.setSD(sd1);
		data.addVariable("y1", y1Distn);
		
			NormalDistnVariable y2Distn = new NormalDistnVariable("Distn1");
			y2Distn.setMean(mean2);
			y2Distn.setSD(sd2);
		data.addVariable("y2", y2Distn);
		
			MixtureDistnVariable mixtureDistn = new MixtureDistnVariable("Mixture", y1Distn, y2Distn);
			mixtureDistn.setPropn(p);
			st = new StringTokenizer(getParameter(INITIAL_LIMITS_PARAM));
			double lowLimit = Double.parseDouble(st.nextToken());
			double highLimit = Double.parseDouble(st.nextToken());
			mixtureDistn.setMinSelection(lowLimit);
			mixtureDistn.setMaxSelection(highLimit);
		data.addVariable("distn", mixtureDistn);
		
		return data;
	}
}