package testProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import distn.*;
import valueList.*;
import coreGraphics.*;


public class DecisionsApplet extends XApplet {
	static final protected String NULL_HYPOTH_PARAM = "nullModel";
	static final private String ALT_MEAN_SLIDER_PARAM = "altMeanSlider";
	static final protected String CRITICAL_SLIDER_PARAM = "criticalSlider";
	static final private String AXIS_INFO_PARAM = "axisInfo";
	static final protected String SAMPLE_SIZE_PARAM = "sampleSize";
	
	static final private Color kNullBackground = new Color(0xD6D8FF);
	static final private Color kAltBackground = new Color(0xE4D1CC);
	static final private Color kCorrectColor = new Color(0x006600);
	static final private Color kWrongColor = new Color(0xCC0000);
	static final private Color kNullForeground = new Color(0x000066);
	static final private Color kProbBackground = new Color(0xFFFF99);
	static final protected Color kDecisionColor = new Color(0x990000);
	
	static final public String MU = "\u03BC";
	static final public char BAR = '\u0305';
	
	protected NumValue nullMean, popnSd, meanSd;
	private NumValue meanLow, meanHigh, meanStart;
	protected NumValue criticalLow, criticalHigh, criticalStart;
	
	protected DataSet data;
	
	private ParameterSlider criticalSlider, altMeanSlider;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 12));
		
		add("North", criticalValuePanel());
		add("Center", displayPanel(data));
		add("West", hypothesesPanel(data));
	}
	
	protected void readNullDistn() {
		StringTokenizer st = new StringTokenizer(getParameter(NULL_HYPOTH_PARAM));
		nullMean = new NumValue(st.nextToken());
		popnSd = new NumValue(st.nextToken());
		
		int n = Integer.parseInt(getParameter(SAMPLE_SIZE_PARAM));
		meanSd = new NumValue(popnSd.toDouble() / Math.sqrt(n), 9);
	}
	
	protected void readCriticalValues() {
		StringTokenizer st = new StringTokenizer(getParameter(CRITICAL_SLIDER_PARAM));
		criticalLow = new NumValue(st.nextToken());
		criticalHigh = new NumValue(st.nextToken());
		criticalStart = new NumValue(st.nextToken());
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
			
			readNullDistn();
			readCriticalValues();
			
			NormalDistnVariable nullUpper = new NormalDistnVariable("nullUpper");
			nullUpper.setParams(getParameter(NULL_HYPOTH_PARAM));
			nullUpper.setSD(meanSd.toDouble());
			nullUpper.setMinSelection(criticalStart.toDouble());
			nullUpper.setMaxSelection(Double.POSITIVE_INFINITY);
		data.addVariable("nullUpper", nullUpper);
		
			NormalDistnVariable nullLower = new NormalDistnVariable(nullUpper);
			nullLower.name = "nullLower";
			nullLower.setMinSelection(Double.NEGATIVE_INFINITY);
			nullLower.setMaxSelection(criticalStart.toDouble());
		data.addVariable("nullLower", nullLower);
		
		StringTokenizer st = new StringTokenizer(getParameter(ALT_MEAN_SLIDER_PARAM));
		meanLow = new NumValue(st.nextToken());
		meanHigh = new NumValue(st.nextToken());
		meanStart = new NumValue(st.nextToken());
		
		String altParamString = meanStart.toString() + " " + meanSd.toString();
		
			NormalDistnVariable altUpper = new NormalDistnVariable("altUpper");
			altUpper.setParams(altParamString);
			altUpper.setMinSelection(criticalStart.toDouble());
			altUpper.setMaxSelection(Double.POSITIVE_INFINITY);
		data.addVariable("altUpper", altUpper);
		
			NormalDistnVariable altLower = new NormalDistnVariable(altUpper);
			altLower.name = "altLower";
			altLower.setMinSelection(Double.NEGATIVE_INFINITY);
			altLower.setMaxSelection(criticalStart.toDouble());
		data.addVariable("altLower", altLower);
		
		return data;
	}
	
	protected ParameterSlider createDecisionSlider() {
		criticalSlider = new ParameterSlider(criticalLow, criticalHigh, criticalStart, "", this);
		criticalSlider.setAddEquals(false);
		criticalSlider.setTitle(translate("Reject H0 if sample mean") + " > ", this);
		criticalSlider.setFont(getStandardBoldFont());
		criticalSlider.setForeground(kDecisionColor);
		return criticalSlider;
	}
	
	protected int getRightSliderGap() {
		return 100;
	}
	
	protected XPanel criticalValuePanel() {
		int rightSliderGap = getRightSliderGap();
		XPanel thePanel = new InsetPanel(20, 0, rightSliderGap, 0);
		thePanel.setLayout(new BorderLayout(30, 0));
			
		thePanel.add("Center", createDecisionSlider());
		
			XPanel headingPanel = new XPanel();
			headingPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				XLabel heading = new XLabel(translate("Decision rule") + ":", XLabel.LEFT, this);
				heading.setFont(getBigBoldFont());
				heading.setForeground(kDecisionColor);
			headingPanel.add(heading);
			
		thePanel.add("West", headingPanel);
		
		return thePanel;
	}
	
	private XPanel hypothesesPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL,
																																		ProportionLayout.TOTAL));
			
			XPanel nullPanel = new InsetPanel(5, 0, 0, 0);
			nullPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				XPanel nullInteriorPanel = new XPanel();
				nullInteriorPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
					XLabel nullLabel = new XLabel(translate("If H0") + ":  " + MU + " = " + nullMean.toString() + "...",
																																					XLabel.LEFT, this);
					nullLabel.setForeground(kNullForeground);
					nullLabel.setFont(getBigBoldFont());
				nullInteriorPanel.add(nullLabel);
		
					XPanel spacerPanel = new XPanel();
					spacerPanel.setLayout(new FixedSizeLayout(30, 10));
					spacerPanel.add(new XPanel());
				nullInteriorPanel.add(spacerPanel);
				
			nullPanel.add(nullInteriorPanel);
		
			nullPanel.lockBackground(kNullBackground);
		thePanel.add(ProportionLayout.TOP, nullPanel);
			
			XPanel altPanel = new InsetPanel(5, 0, 10, 0);
			altPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 14));
			
				XLabel rejectLabel = new XLabel(translate("If HA") + ":  " + MU + " > " + nullMean.toString() + "...",
																																					XLabel.LEFT, this);
				rejectLabel.setFont(getBigBoldFont());
			altPanel.add(rejectLabel);
			
				altMeanSlider = new ParameterSlider(meanLow, meanHigh, meanStart, MU, this);
				altMeanSlider.setFont(getStandardBoldFont());
			altPanel.add(altMeanSlider);
		
			altPanel.lockBackground(kAltBackground);
		thePanel.add(ProportionLayout.BOTTOM, altPanel);
			
		return thePanel;
	}
	
	protected boolean showDistns() {
		return true;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		
		if (showDistns()) {
			thePanel.setLayout(new ProportionLayout(0.55, 0, ProportionLayout.HORIZONTAL,
																																		ProportionLayout.TOTAL));
			
			thePanel.add(ProportionLayout.LEFT, distnPanel(data));
		
			thePanel.add(ProportionLayout.RIGHT, probPanel(data));
		}
		else {
			thePanel.setLayout(new BorderLayout(0, 0));
		
			thePanel.add("Center", probPanel(data));
		}
		
		return thePanel;
	}
	
	private XPanel distnPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL,
																																	ProportionLayout.TOTAL));
		thePanel.add(ProportionLayout.TOP, normalPanel(data, "nullUpper", kNullForeground,
																																				kNullBackground));
		thePanel.add(ProportionLayout.BOTTOM, normalPanel(data, "altLower", null,
																																					kAltBackground));
		return thePanel;
	}
	
	private XPanel probPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL,
																																ProportionLayout.TOTAL));
		thePanel.add(ProportionLayout.TOP, probRowPanel(data, "nullUpper", "nullLower", true, kNullBackground));
		thePanel.add(ProportionLayout.BOTTOM, probRowPanel(data, "altUpper", "altLower", false, kAltBackground));
			
		return thePanel;
	}
	
	private XPanel normalPanel(DataSet data, String normalKey, Color foregroundColor,
																														Color backgroundColor) {
		XPanel thePanel = new InsetPanel(0, 5, 0, 0);
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			theHorizAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
			theHorizAxis.setAxisName(translate("distn of sample mean"));
			if (foregroundColor != null)
				theHorizAxis.setForeground(foregroundColor);
		thePanel.add("Bottom", theHorizAxis);
		
			SimpleDistnView theView = new SimpleDistnView(data, this, theHorizAxis, normalKey);
			theView.lockBackground(Color.white);
			theView.setDensityColor(Color.red);
			theView.setHighlightColor(Color.green);
			
		thePanel.add("Center", theView);
		thePanel.lockBackground(backgroundColor);
		return thePanel;
	}
	
	private XPanel probRowPanel(DataSet data, String upperNormKey, String lowerNormKey,
																				boolean nullNotAlternative, Color backgroundColor) {
		XPanel thePanel = new InsetPanel(10, (nullNotAlternative ? 5 : 2), 10, (nullNotAlternative ? 2 : 5));
		thePanel.setLayout(new BorderLayout(0, 3));
		
		thePanel.add("Center", probValuePanel(data, upperNormKey, lowerNormKey,
																								nullNotAlternative, backgroundColor));
		
			XPanel headingPanel = new XPanel();
			headingPanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.HORIZONTAL,
																																		ProportionLayout.TOTAL));
				XLabel acceptLabel = new XLabel(translate("accept H0"), XLabel.CENTER, this);
				acceptLabel.setFont(getBigBoldFont());
				if (nullNotAlternative)
					acceptLabel.setForeground(kNullForeground);
			headingPanel.add(ProportionLayout.LEFT, acceptLabel);
			
				XLabel rejectLabel = new XLabel(translate("reject H0"), XLabel.CENTER, this);
				rejectLabel.setFont(getBigBoldFont());
				if (nullNotAlternative)
					rejectLabel.setForeground(kNullForeground);
			headingPanel.add(ProportionLayout.RIGHT, rejectLabel);
		
		thePanel.add(nullNotAlternative ? "North" : "South", headingPanel);
		
		thePanel.lockBackground(backgroundColor);
		return thePanel;
	}
	
	private XPanel probValuePanel(DataSet data, String upperNormKey, String lowerNormKey,
																				boolean nullNotAlternative, Color backgroundColor) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.HORIZONTAL,
																																		ProportionLayout.TOTAL));
//		thePanel.lockBackground(backgroundColor);
		
			XPanel lowPanel = new XPanel();
			lowPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				Color lowColor = nullNotAlternative ? kCorrectColor : kWrongColor;
				
				XLabel probLabel = new XLabel(translate("prob") + " =", XLabel.CENTER, this);
				probLabel.setFont(getBigBoldFont());
				probLabel.setForeground(lowColor);
			lowPanel.add(probLabel);
				
				ProportionView lowPropn = new ProportionView(data, lowerNormKey, this);
				lowPropn.unboxValue();
				lowPropn.setLabel("");
				lowPropn.setForeground(lowColor);
				lowPropn.setFont(getBigBoldFont());
			lowPanel.add(lowPropn);
			
			lowPanel.lockBackground(kProbBackground);
		thePanel.add(ProportionLayout.LEFT, lowPanel);
			
				Color highColor = nullNotAlternative ? kWrongColor : kCorrectColor;
		
			XPanel highPanel = new XPanel();
			highPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				probLabel = new XLabel(translate("prob") + " =", XLabel.CENTER, this);
				probLabel.setFont(getBigBoldFont());
				probLabel.setForeground(highColor);
			highPanel.add(probLabel);
			
				ProportionView highPropn = new ProportionView(data, upperNormKey, this);
				highPropn.unboxValue();
				highPropn.setLabel("");
				highPropn.setForeground(highColor);
				highPropn.setFont(getBigBoldFont());
			highPanel.add(highPropn);
			
			highPanel.lockBackground(kProbBackground);
		thePanel.add(ProportionLayout.RIGHT, highPanel);
		
		return thePanel;
	}
	
	protected void setCriticalMeanValue(double newCriticalMean) {
		NormalDistnVariable nullUpper = (NormalDistnVariable)data.getVariable("nullUpper");
		nullUpper.setMinSelection(newCriticalMean);
		data.variableChanged("nullUpper");
		
		NormalDistnVariable nullLower = (NormalDistnVariable)data.getVariable("nullLower");
		nullLower.setMaxSelection(newCriticalMean);
		data.variableChanged("nullLower");
		
		NormalDistnVariable altUpper = (NormalDistnVariable)data.getVariable("altUpper");
		altUpper.setMinSelection(newCriticalMean);
		data.variableChanged("altUpper");
		
		NormalDistnVariable altLower = (NormalDistnVariable)data.getVariable("altLower");
		altLower.setMaxSelection(newCriticalMean);
		data.variableChanged("altLower");
	}
	
	protected double getCriticalMean() {
		return criticalSlider.getParameter().toDouble();
	}

	
	private boolean localAction(Object target) {
		if (target == criticalSlider) {
			NumValue newCritical = criticalSlider.getParameter();
			
			setCriticalMeanValue(newCritical.toDouble());
			
			return true;
		}
		else if (target == altMeanSlider) {
			NumValue newMean = altMeanSlider.getParameter();
			double critical = getCriticalMean();
			
			NormalDistnVariable altUpper = (NormalDistnVariable)data.getVariable("altUpper");
			altUpper.setMean(newMean.toDouble());
			altUpper.setMinSelection(critical);
			altUpper.setMaxSelection(Double.POSITIVE_INFINITY);
			data.variableChanged("altUpper");
			
			NormalDistnVariable altLower = (NormalDistnVariable)data.getVariable("altLower");
			altLower.setMean(newMean.toDouble());
			altLower.setMinSelection(Double.NEGATIVE_INFINITY);
			altLower.setMaxSelection(critical);
			data.variableChanged("altLower");
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}