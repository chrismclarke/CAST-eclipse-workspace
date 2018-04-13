package varianceProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import distn.*;
import utils.*;

import variance.*;


public class Chi2ShapeApplet extends XApplet {
	static final private String AXIS_PARAM = "horizAxis";
	static final private String CHI2_NAME_PARAM = "chi2Name";
	static final private String DF_PARAM = "df";
	static final private String AREA_USED_PARAM = "areaUsed";
	
	static final protected Color kGray = new Color(0x555555);
	
	protected DataSet data;
	protected int lowDF, highDF, startDF;
	
	private ParameterSlider dfSlider;
	
	public void setupApplet() {
		readDFs();
		data = readData();
		
		setLayout(new BorderLayout(0, 10));
		
		add("Center", displayPanel(data));
		add("South", insetPanel(sliderPanel()));
	}
	
	private void readDFs() {
		StringTokenizer st = new StringTokenizer(getParameter(DF_PARAM));
		lowDF = Integer.parseInt(st.nextToken());
		highDF = Integer.parseInt(st.nextToken());
		startDF = Integer.parseInt(st.nextToken());
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		Chi2DistnVariable chi2Distn = new Chi2DistnVariable("Chi2 distn");
		chi2Distn.setDF(startDF);
		data.addVariable("chi2", chi2Distn);
		
		return data;
	}
	
	private XPanel insetPanel(XPanel mainPanel) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.25, 0, ProportionLayout.HORIZONTAL,
																							ProportionLayout.TOTAL));
		thePanel.add(ProportionLayout.LEFT, new XPanel());	
			
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new ProportionLayout(0.6667, 0, ProportionLayout.HORIZONTAL,
																							ProportionLayout.TOTAL));
		
			innerPanel.add(ProportionLayout.LEFT, mainPanel);
			innerPanel.add(ProportionLayout.RIGHT, new XPanel());
		
		thePanel.add(ProportionLayout.RIGHT, innerPanel);
		
		return thePanel;
	}
	
	protected XPanel sliderPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
				dfSlider = new ParameterSlider(new NumValue(lowDF, 0), new NumValue(highDF, 0),
																					new NumValue(startDF, 0), translate("Degrees of freedom"), this);
				dfSlider.setFont(getStandardBoldFont());
		
		thePanel.add("Center", dfSlider);
		
		return thePanel;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
			theHorizAxis.readNumLabels(getParameter(AXIS_PARAM));
			thePanel.add("Bottom", theHorizAxis);
		
			Chi2View theView = createView(data, theHorizAxis);
			
			String areaString = getParameter(AREA_USED_PARAM);
			theView.setAreaProportion(Double.parseDouble(areaString));
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected Chi2View createView(DataSet data, HorizAxis horizAxis) {
		Chi2View theView = new Chi2View(data, this, horizAxis, "chi2");
		theView.setDistnLabel(new LabelValue(getParameter(CHI2_NAME_PARAM)), kGray);
		return theView;
	}

	
	private boolean localAction(Object target) {
		if (target == dfSlider) {
			int newDF = (int)Math.round(dfSlider.getParameter().toDouble());
			
			Chi2DistnVariable chi2Distn = (Chi2DistnVariable)data.getVariable("chi2");
			chi2Distn.setDF(newDF);
			data.variableChanged("chi2");
				
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}