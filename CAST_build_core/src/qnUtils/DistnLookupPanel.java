package qnUtils;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import distn.*;


public class DistnLookupPanel extends XPanel {
	static final public int Z_DISTN = 0;
	static final public int T_DISTN = 1;
	
	static final private int kStartDF = 20;
	
	private DFSlider dfSlider;
	private DataSet data;
	private DistnDensityView theView;
	private int distnType;
	
	
	public DistnLookupPanel(int distnType, XApplet theApplet, Color bgColor) {
		this.distnType = distnType;
		data = getData(distnType);
		
		setLayout(new BorderLayout());
		
		if (distnType == T_DISTN) {
			dfSlider = new DFSlider(kStartDF, theApplet);
			add("North", dfSlider);
		}
		
		add("Center", displayPanel(data, distnType, theApplet));
		add("South", probPanel(data, theApplet));
		
		lockBackground(bgColor);
	}
	
	private DataSet getData(int distnType) {
		DataSet data = new DataSet();
		
		if (distnType == Z_DISTN) {
			NormalDistnVariable y = new NormalDistnVariable("Z");
			y.setParams("0 1");
			data.addVariable("distn", y);
		}
		else {
			TDistnVariable y = new TDistnVariable("T", kStartDF);
			data.addVariable("distn", y);
		}
		
		data.setSelection("distn", Double.NEGATIVE_INFINITY, 0.0);
		
		return data;
	}
	
	private XPanel probPanel(DataSet data, XApplet theApplet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 2));
		
		ZProbView pView = new ZProbView(data, "distn", theApplet, ZProbView.BELOW);
		thePanel.add(pView);
		
		return thePanel;
	}
	
	private XPanel displayPanel(DataSet data, int distnType, XApplet theApplet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(theApplet);
		thePanel.add("Bottom", theHorizAxis);
		
		VertAxis theProbAxis = new VertAxis(theApplet);
		thePanel.add("Left", theProbAxis);
		theProbAxis.show(false);
		
		if (distnType == Z_DISTN) {
			theHorizAxis.readNumLabels("-3.5 3.5 -3 1");
			theProbAxis.readNumLabels("0 0.5 7 0.1");
		}
		else {
			theHorizAxis.readNumLabels("-5 5 -4 2");
			theProbAxis.readNumLabels("0 0.5 7 0.1");
		}
		
		theView = new DistnDensityView(data, theApplet, theHorizAxis, theProbAxis,
										"distn", DistnDensityView.NO_SHOW_MEANSD, DistnDensityView.MAX_DRAG);
		
		theView.lockBackground(Color.white);
		theView.setViewBorder(new Insets(0,0,0,0));
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	public Insets insets() {
		return new Insets(3, 3, 3, 3);
	}

	
	private boolean localAction(Object target) {
		if (target == dfSlider) {
			if (distnType == T_DISTN) {
				TDistnVariable tDistn = (TDistnVariable)data.getVariable("distn");
				tDistn.setDF(dfSlider.getDF());
			}
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