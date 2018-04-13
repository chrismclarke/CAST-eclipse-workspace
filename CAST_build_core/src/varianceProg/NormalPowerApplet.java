package varianceProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;

import variance.*;


public class NormalPowerApplet extends XApplet {
	static final private String kStandardAxisInfo = "0 4 0 1";
	
	static final private NumValue kPower1 = new NumValue(1.0, 2);
	static final private NumValue kPower2 = new NumValue(2.0, 2);
	
	private DataSet data;
	
	private ParameterSlider transformSlider;
	
	private HorizAxis standardAxis;
	private SquaredHorizAxis sqrAxis;
	private HalfNormPowerView theView;
	
	public void setupApplet() {
		data = new DataSet();
		
		setLayout(new BorderLayout());
		add("Center", createView(data));
		add("South", sliderPanel());
	}
	
	private XPanel sliderPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.25, 0, ProportionLayout.HORIZONTAL,
																							ProportionLayout.TOTAL));
		thePanel.add(ProportionLayout.LEFT, new XPanel());	
			
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new ProportionLayout(0.6667, 0, ProportionLayout.HORIZONTAL,
																							ProportionLayout.TOTAL));
		
				transformSlider = new ParameterSlider(kPower1, kPower2, kPower1, translate("Power"), this);
				transformSlider.setFont(getStandardBoldFont());
			innerPanel.add(ProportionLayout.LEFT, transformSlider);
			innerPanel.add(ProportionLayout.RIGHT, new XPanel());
		
		thePanel.add(ProportionLayout.RIGHT, innerPanel);
		
		return thePanel;
	}
	
	private XPanel createView(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		standardAxis = new HorizAxis(this);
		standardAxis.readNumLabels(kStandardAxisInfo);
		standardAxis.setAxisName("z (" + translate("absolute value of normal") + ")");
		thePanel.add("Bottom", standardAxis);
		
		sqrAxis = new SquaredHorizAxis(this);
		sqrAxis.setSqrLabels();
		sqrAxis.setAxisName(translate("z-squared"));
		thePanel.add("Bottom", sqrAxis);
		
		theView = new HalfNormPowerView(data, this, standardAxis);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == transformSlider) {
			double newPower = transformSlider.getParameter().toDouble();
			int newTransformIndex = 300 + (int)Math.round((newPower - 1.0) * 100.0);
			standardAxis.setPowerIndex(newTransformIndex);
			sqrAxis.setPowerIndex(newTransformIndex);
			
			theView.repaint();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}