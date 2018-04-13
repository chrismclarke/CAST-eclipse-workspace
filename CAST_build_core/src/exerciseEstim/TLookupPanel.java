package exerciseEstim;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import distn.*;
import utils.*;
import exercise2.*;


public class TLookupPanel extends XPanel implements StatusInterface {
	static final public int QUANTILE_LOOKUP = 0;
	static final public int PROB_LOOKUP = 1;
	
	static final public int T_AND_NORMAL = 0;
	static final public int NORMAL_ONLY = 1;
	static final public int T_ONLY = 2;
	
	static final private String kTAxisInfo = "-4 4 -4 1";
	static final private int kStartDF = 10;
	
	private XApplet applet;
	private int lookupType, distnType;
	
	private DataSet tData;
	private DFRealSlider dfSlider;
	private XCheckbox normalCheck;
	private XLabel normalLabel;
	
	private HorizAxis axis;
	private CoreLookupView lookupView;
	private boolean finiteDf = true;
	
	private boolean maskChanges = false;
	
	public TLookupPanel(XApplet applet, int lookupType, int distnType) {
		this.applet = applet;
		this.lookupType = lookupType;
		this.distnType = distnType;
		
		tData = getTData();
		
		setLayout(new BorderLayout(0, 0));
		
		if (distnType == NORMAL_ONLY)
			add("North", normalHeadingPanel(applet));
		else
			add("North", controlPanel(applet));
		
		add("Center", densityPanel(tData, applet));
	}
	
	public TLookupPanel(XApplet applet, int lookupType) {
		this(applet, lookupType, T_AND_NORMAL);
	}
	
	public void reset() {
		lookupView.setEditValue(new NumValue((lookupType == QUANTILE_LOOKUP) ? 0.8 : 0.0, 1));
		if (distnType == T_AND_NORMAL)
			setTDistnDf(10);
	}
	
	public String getStatus() {
		String distnString;
		double df = dfSlider.getDF();
		if (normalCheck.getState())
			distnString = "normal";
		else {
			String dfString = Double.isInfinite(df) ? "infinite" : String.valueOf(Math.round(df));
			distnString = "t " + dfString;
		}
		
		String editValue = lookupView.getEditValue().toString();
		
		return distnString + " " + editValue;
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		
		String distn = st.nextToken();
		if (distn.equals("normal"))
			setNormalDistn();
		else {
			String dfString = st.nextToken();
			double df;
			if (dfString.equals("infinite"))
				df = Double.POSITIVE_INFINITY;
			else
				df = Double.parseDouble(dfString);
			setTDistnDf(df);
		}
		
		NumValue editVal = new NumValue(st.nextToken());
		lookupView.setEditValue(editVal);
	}
	
	public void setTDistnDf(double df) {
		maskChanges = true;		//		so ExerciseApplet.noteChangedWorking() is not called
		if (distnType == NORMAL_ONLY)
			throw new RuntimeException("Error: You cannot change the df when the distn must be normal.");
		if (normalCheck != null && normalCheck.getState()) {
			normalCheck.setState(false);
			lookupView.setDistnKey("t");
			dfSlider.setEnabled(true);
		}
		dfSlider.setDf(df);
		maskChanges = false;
	}
	
	public void setNormalDistn() {
		if (distnType == NORMAL_ONLY)
			return;
		if (!normalCheck.getState()) {
			maskChanges = true;		//		so ExerciseApplet.noteChangedWorking() is not called
			normalCheck.setState(true);
			lookupView.setDistnKey("z");
			dfSlider.setDf(Double.POSITIVE_INFINITY);
			dfSlider.setEnabled(false);
			maskChanges = false;
		}
	}
	
	public void setConfidenceLevel(NumValue ciLevel) {
		if (lookupType == QUANTILE_LOOKUP)
			lookupView.setEditValue(ciLevel);
		else
			throw new RuntimeException("Error: You cannot call setConfidenceLevel() when not QUANTILE_LOOKUP type");
	}
	
	public void setTValue(NumValue tValue) {
		if (lookupType == PROB_LOOKUP)
			lookupView.setEditValue(tValue);
		else
			throw new RuntimeException("Error: You cannot call setConfidenceLevel() when not QUANTILE_LOOKUP type");
	}
	
	public boolean isCorrectDf(double df) {
		if (distnType == NORMAL_ONLY)
			return Double.isInfinite(df);
		else
			return dfSlider.isCorrect(df);
	}
	
	public boolean isCorrectConfidenceLevel(NumValue ciLevel) {
		if (lookupType == QUANTILE_LOOKUP) {
			NumValue levelSet = lookupView.getEditValue();
			double error = Math.abs(ciLevel.toDouble() - levelSet.toDouble());
			int decimals = Math.max(ciLevel.decimals, levelSet.decimals);
			for (int i=0 ; i<decimals ; i++)
				error *= 10;
			return Math.round(error) == 0;
		}
		else
			throw new RuntimeException("Error: You cannot call setConfidenceLevel() when not QUANTILE_LOOKUP type");
	}
	
	private DataSet getTData() {
		DataSet data = new DataSet();
		
		if (distnType != NORMAL_ONLY) {
			TDistnVariable tDistn = new TDistnVariable("T", kStartDF);
			data.addVariable("t", tDistn);
		}
		
		NormalDistnVariable zDistn = new NormalDistnVariable("Z");
		data.addVariable("z", zDistn);
		
		return data;
	}
	
	private XPanel controlPanel(XApplet applet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(4, 0));
		
			dfSlider = new DFRealSlider(kStartDF, applet);
		thePanel.add("Center", dfSlider);
		
		if (distnType == T_AND_NORMAL) {
			XPanel normalPanel = new XPanel();
			normalPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				normalCheck = new XCheckbox(applet.translate("Normal"), applet);
			normalPanel.add(normalCheck);
			
			thePanel.add("East", normalPanel);
		}
		
		return thePanel;
	}
	
	private XPanel normalHeadingPanel(XApplet applet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			normalLabel = new XLabel("z distribution (standard normal)", XLabel.CENTER, applet);
			normalLabel.setFont(applet.getStandardBoldFont());
		thePanel.add(normalLabel);
		
		return thePanel;
	}
	
	private XPanel densityPanel(DataSet data, XApplet applet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		axis = new HorizAxis(applet);
			axis.readNumLabels(kTAxisInfo);
		thePanel.add("Bottom", axis);
		
			String startDistnKey = (distnType == NORMAL_ONLY) ? "z" : "t";
			
			if (lookupType == QUANTILE_LOOKUP)
				lookupView = new TQuantileLookupView(data, applet, axis, startDistnKey);
			else
				lookupView = new TProbLookupView(data, applet, axis, startDistnKey);
		thePanel.add("Center", lookupView);
			lookupView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	public Dimension getPreferredSize() {
		int sliderHt = (dfSlider == null) ? normalLabel.getPreferredSize().height
																			: dfSlider.getPreferredSize().height;
		int axisHt = axis.getPreferredSize().height;
		return new Dimension(200, sliderHt + axisHt + 100);
	}
	
	protected void noteChange() {
		if (!maskChanges && applet instanceof ExerciseApplet) {
//			System.out.println("Trying to call noteChangedWorking()");
			((ExerciseApplet)applet).noteChangedWorking();
		}
	}
	
	private boolean localAction(Object target) {
		if (target == dfSlider) {
			double df = dfSlider.getDF();
			if (Double.isInfinite(df)) {
				lookupView.setDistnKey("z");
				finiteDf = false;
			}
			else {
				TDistnVariable tDistn = (TDistnVariable)tData.getVariable("t");
				tDistn.setDF((int)Math.round(df));
				if (!finiteDf) {
					lookupView.setDistnKey("t");
					finiteDf = true;
				}
				else
					tData.variableChanged("t");
			}
			noteChange();
			
			return true;
		}
		else if (target == normalCheck) {
			if (normalCheck.getState()) {
				lookupView.setDistnKey("z");
				dfSlider.setDf(Double.POSITIVE_INFINITY);
				dfSlider.setEnabled(false);
			}
			else
				dfSlider.setEnabled(true);
			noteChange();
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}