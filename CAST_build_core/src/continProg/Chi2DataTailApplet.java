package continProg;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;


public class Chi2DataTailApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String VALUE_PARAM = "value";
	static final private String DF_PARAM = "df";
	
	static final private Color kFillColor = new Color(0x99CCCC);
	static final private Color kHiliteColor = new Color(0x990000);
	
	private int df;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(0, 0));
		add("Center", distnPanel(data));
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
			Chi2DistnVariable chi2Distn = new Chi2DistnVariable("chi2 distn");
			chi2Distn.setParams("4");
			df = Integer.parseInt(getParameter(DF_PARAM));
			chi2Distn.setDF(df);
		data.addVariable("chi2Distn", chi2Distn);
		
			NumVariable chi2 = new NumVariable("Chi2 value");
			chi2.readValues(getParameter(VALUE_PARAM));
		data.addVariable("chi2", chi2);
		
		return data;
	}
	
	private XPanel distnPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		
			AccurateTailAreaView theView = new AccurateTailAreaView(data, this, theHorizAxis, "chi2Distn");
			theView.setDistnLabel(new LabelValue(translate("Chi-squared") + " (" + df + " df)"), Color.lightGray);
			theView.setValueLabel(null);
			theView.lockBackground(Color.white);
			theView.setActiveNumVariable("chi2");
			theView.setDensityScaling(0.92);
			theView.setDistnColors(kFillColor, kHiliteColor);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
}