package normalProg;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;


public class JS_ZScoreProbApplet extends XApplet {
	static final private String LOW_LIMIT_PARAM = "lowZ";
	static final private String HIGH_LIMIT_PARAM = "highZ";
	
	static final private String kZDistn = "0 1";
	static final private String kZAxis = "-3 3 -3 1";
	
	static final private Color kHiliteColor = new Color(0xCCCCCC);
	static final private Color kDimColor = new Color(0x3366FF);
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new BorderLayout(0, 0));
		add("Center", displayPanel(data));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			NormalDistnVariable zDistn = new NormalDistnVariable("z");
			zDistn.setParams(kZDistn);
			
			String lowString = getParameter(LOW_LIMIT_PARAM);
			double lowZ = (lowString == null) ? Double.NEGATIVE_INFINITY : Double.parseDouble(lowString);
			zDistn.setMinSelection(lowZ);
			
			String highString = getParameter(HIGH_LIMIT_PARAM);
			double highZ = (highString == null) ? Double.POSITIVE_INFINITY : Double.parseDouble(highString);
			zDistn.setMaxSelection(highZ);
			
		data.addVariable("z", zDistn);
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			theHorizAxis.readNumLabels(kZAxis);
		thePanel.add("Bottom", theHorizAxis);
		
			SimpleDistnView zView = new SimpleDistnView(data, this, theHorizAxis, "z");
			zView.setDensityScaling(0.9);
			zView.setDensityColor(kDimColor);
			zView.setHighlightColor(kHiliteColor);
			zView.lockBackground(Color.white);
		thePanel.add("Center", zView);
		
		return thePanel;
	}
}