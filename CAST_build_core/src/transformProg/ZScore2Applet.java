package transformProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.*;
import formula.*;

import transform.*;


public class ZScore2Applet extends ZScoreApplet {
	static final private String MAX_MEAN_SD_PARAM = "maxMeanSd";
	static final private String MESSAGE_PARAM = "message";
	
	static final private Color kZColor = new Color(0x990000);
	static final private Color kInterpBackground = new Color(0xDDDDEE);
	
	private NumValue maxMeanSd;
	
	private NumValue meanValue, sdValue;
	private Const meanConst, sdConst;
	private OneValueView xTemplateView, zTemplateView;
	private FixedValueView meanView, sdView;
	private ZInterpretationView zInterpView;
	
	public void setupApplet() {
		data = getData();
		maxMeanSd = new NumValue(getParameter(MAX_MEAN_SD_PARAM));
		
		setLayout(new BorderLayout(0, 10));
		add("North", controlPanel(data));
		add("Center", dataPanel(data));
		add("South", valuePanel(data));
		
		changeVariable(0);
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel leftPanel = new InsetPanel(0, 0, 0, 20);
			leftPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));
			
				meanView = new FixedValueView(MText.expandText("x#bar#"), maxMeanSd, 0.0, this);
				meanView.addEqualsSign();
//				meanView.setForeground(Color.blue);
//				meanView.setFont(getBigFont());
			leftPanel.add(meanView);
			
				sdView = new FixedValueView("s", maxMeanSd, 0.0, this);
				sdView.addEqualsSign();
//				sdView.setForeground(Color.blue);
//				sdView.setFont(getBigFont());
			leftPanel.add(sdView);
		
		thePanel.add("Center", leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.RIGHT, VerticalLayout.VERT_BOTTOM, 0));
			
			rightPanel.add(super.controlPanel(data));
		thePanel.add("East", rightPanel);
		
		return thePanel;
	}
	
	protected XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis xAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		xAxis.readNumLabels(labelInfo);
		thePanel.add("Top", xAxis);
		
		zAxis = createZAxis(data, xAxis);
		zAxis.setForeground(kZColor);
		thePanel.add("Bottom", zAxis);
		
		theView = coreView(data, xAxis);
		theView.setActiveNumVariable("y0");
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	protected XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 12));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
			
			topPanel.add(rawValuePanel(data));
				
			topPanel.add(zCalcPanel(data));
		
		thePanel.add("Center", topPanel);
		
			XPanel bottomPanel = new InsetPanel(0, 10);
			bottomPanel.setLayout(new BorderLayout(0, 0));
			
				zInterpView = new ZInterpretationView(data, this, "label", "z0",
													getParameter(MEASUREMENT_PARAM), kZColor, getParameter(MESSAGE_PARAM));
				zInterpView.setFont(getBigFont());
			bottomPanel.add("Center", zInterpView);
			bottomPanel.lockBackground(kInterpBackground);
		
		thePanel.add("South", bottomPanel);

		return thePanel;
	}
	
	private XPanel rawValuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
			
			OneValueView labelView = new OneValueView(data, "label", this);
			labelView.setNameDraw(false);
		thePanel.add(labelView);
		
			yView = new OneValueView(data, "y0", this);
			yView.setLabel(translate("has") + " " + getParameter(MEASUREMENT_PARAM));
		thePanel.add(yView);
		
		return thePanel;
	}
	
	private XPanel zCalcPanel(DataSet data) {
		FormulaContext context = new FormulaContext(kZColor, getStandardFont(), this);
		
		xTemplateView = new OneValueView(data, "y0", this);
		xTemplateView.setCenterValue(true);
		xTemplateView.setNameDraw(false);
		SummaryValue xValue = new SummaryValue(xTemplateView, context);
		
		meanValue = new NumValue(0.0, maxMeanSd.decimals);
		meanConst = new Const(meanValue, context);
		
		Binary numer = new Binary(Binary.MINUS, xValue, meanConst, context);
		
		sdValue = new NumValue(0.0, maxMeanSd.decimals);
		sdConst = new Const(sdValue, context);
		
		Ratio middlePart = new Ratio(numer, sdConst, context);
			
		int zDecimals = ((ZScoreVariable)data.getVariable("z0")).getMaxDecimals();
		NumValue maxZ = new NumValue(-1.0, zDecimals);
		zTemplateView = new OneValueView(data, "z0", this, maxZ);
			zTemplateView.setCenterValue(true);
			zTemplateView.setNameDraw(false);
		SummaryValue zValue = new SummaryValue(zTemplateView, context);
		
		Binary rightPart = new Binary(Binary.EQUALS, middlePart, zValue, context);
		
		TextLabel zEquals = new TextLabel(translate("z-score"), context);
		
		return new Binary(Binary.EQUALS, zEquals, rightPart, context);
	}
	
	protected void changeVariable(int i) {
		String newY = "y" + i;
		xTemplateView.setVariableKey(newY);
		String newZ = "z" + i;
		zTemplateView.setVariableKey(newZ);
		
		ValueEnumeration ye = ((NumVariable)data.getVariable(newY)).values();
		int n = 0;
		double sy = 0;
		double syy = 0;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			sy += y;
			syy += y * y;
			n ++;
		}
		double mean = sy / n;
		double sd = Math.sqrt((syy - sy * mean) / (n - 1));
		
		meanValue.setValue(mean);
		sdValue.setValue(sd);
		meanConst.repaint();
		sdConst.repaint();
		
		meanView.setValue(mean);
		sdView.setValue(sd);
		
		zInterpView.setZKey(newZ);
		
		super.changeVariable(i);
	}
}