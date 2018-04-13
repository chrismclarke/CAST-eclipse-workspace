package varianceProg;

import java.awt.*;

import axis.*;
import dataView.*;
import distn.*;

import variance.*;


public class Chi2QuantilesApplet extends XApplet {
															//		Only to draw Chi-squared with 2.5% and 97.5% points
															//		to be captured for image
	static final private String AXIS_PARAM = "horizAxis";
	static final private String AREA_PROPN_PARAM = "areaProportion";
	static final private String DF_PARAM = "df";
	
	private DataSet data;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(0, 10));
		
		add("Center", displayPanel(data));
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
		int df = Integer.parseInt(getParameter(DF_PARAM));
		Chi2DistnVariable chi2Distn = new Chi2DistnVariable("Chi2 distn");
		chi2Distn.setDF(df);
		
		double lowQuantile = chi2Distn.getQuantile(0.025);
		double highQuantile = chi2Distn.getQuantile(0.975);
		chi2Distn.setMinSelection(lowQuantile);
		chi2Distn.setMaxSelection(highQuantile);
		System.out.println("df = " + df + ", lowQ = " + lowQuantile + ", highQ = " + highQuantile);
		
		data.addVariable("chi2", chi2Distn);
		
		data.addVariable("dummy", new NumVariable("dummy"));
		
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			theHorizAxis.readNumLabels(getParameter(AXIS_PARAM));
		thePanel.add("Bottom", theHorizAxis);
		
			Chi2TailView theView = new Chi2TailView(data, this, theHorizAxis, "chi2");
			double areaPropn = Double.parseDouble(getParameter(AREA_PROPN_PARAM));
			theView.setAreaProportion(areaPropn);
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
}