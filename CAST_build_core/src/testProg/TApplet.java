package testProg;

import java.awt.*;

import dataView.*;
import qnUtils.*;
import axis.*;
import test.*;
import distn.*;


public class TApplet extends XApplet {
	static final private String kZAxisScale = "-4 4 -4 1";
	static final private int kStartDF = 4;
	
	private DataSet data;
	private DFSlider dfSlider;
	
	public void setupApplet() {
		getData();
		
		setLayout(new BorderLayout());
		dfSlider = new DFSlider(kStartDF, this);
		add("South", dfSlider);
		
		add("Center", displayPanel(data));
	}
	
	protected void getData() {
		data = new DataSet();
		
		TDistnVariable tDistn = new TDistnVariable(translate("t distn"), kStartDF);
		data.addVariable("tDistn", tDistn);
		
		NormalDistnVariable zDistn = new NormalDistnVariable("z distn");
		data.addVariable("zDistn", zDistn);
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		theHorizAxis.readNumLabels(kZAxisScale);
		thePanel.add("Bottom", theHorizAxis);
		CoreVariable v = data.getVariable("tDistn");
		theHorizAxis.setAxisName(v.name);
		
		TNormalView tView = new TNormalView(data, this, theHorizAxis, "zDistn", "tDistn");
		thePanel.add("Center", tView);
		
		tView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == dfSlider) {
			TDistnVariable tDistn = (TDistnVariable)data.getVariable("tDistn");
			tDistn.setDF(dfSlider.getDF());
			data.variableChanged("tDistn");
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}