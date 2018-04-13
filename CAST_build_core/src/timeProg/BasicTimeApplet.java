package timeProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import time.*;
import utils.*;
import valueList.OneValueView;


abstract public class BasicTimeApplet extends XApplet {
	static final protected String AXIS_INFO_PARAM = "vertAxis";
	static final private String TIME_INFO_PARAM = "timeAxis";
	static final protected String SEASON_PARAM = "seasonName";
	static final private String SEASON_INFO_PARAM = "seasonInfo";
	static final private String TRANSFORM_PARAM = "transform";
	static final protected String TIME_NAME_PARAM = "timeAxisName";
	static final private String LABEL_AXES_PARAM = "labelAxes";
	static final protected String TIME_SEQUENCE_PARAM = "timeSequence";
	static final private String RAW_NAME_PARAM = "rawName";		//		to label value at top
	
	static final public Color kSmoothedColor = new Color(0xCC0000);		//		dark red
	static final public Color kActualColor = Color.black;
//	static final public Color kSmoothedColor = new Color(0x006600);	//		dark green
	
	private DataSet data;
	private TimeView theView;
	private XCheckbox sourceShadingCheck = null;
	
	private boolean labelAxes = false;
	protected VertAxis theVertAxis;
	protected XLabel yVariateName;
	
	public void setupApplet() {
		data = readData();
		
		String labelAxesParam = getParameter(LABEL_AXES_PARAM);
		labelAxes = labelAxesParam != null && labelAxesParam.equals("true");
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		
		XPanel controlP = controlPanel(data);
		if (controlP != null)
			add("South", controlP);
		
		XPanel topP = topPanel(data);
		if (topP != null)
			add("North", topP);
	}
	
	abstract protected DataSet readData();
	
	abstract protected String getCrossKey();
	abstract protected String[] getLineKeys();
	
	protected DataSet getData() {
		return data;
	}
	
	protected TimeView getView() {
		return theView;
	}
	
	protected TimeView createTimeView(TimeAxis theHorizAxis, VertAxis theVertAxis) {
		return new TimeView(data, this, theHorizAxis, theVertAxis);
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
			XPanel valPanel = valuePanel(data);
		if (valPanel != null)
			thePanel.add("North", valPanel);
		
		if (labelAxes) {
			XPanel labelPanel = new XPanel();
			labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
				yVariateName = new XLabel(getParameter(VAR_NAME_PARAM), XLabel.LEFT, this);
				yVariateName.setFont(theVertAxis.getFont());
			labelPanel.add(yVariateName);
				
			thePanel.add("Center", labelPanel);
		}
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		theVertAxis = vertAxis(data);
		thePanel.add("Left", theVertAxis);
		
		TimeAxis theHorizAxis = horizAxis(data);
		thePanel.add("Bottom", theHorizAxis);
		
		theView = createTimeView(theHorizAxis, theVertAxis);
		String crossKey = getCrossKey();
		theView.setActiveNumVariable(crossKey);
		if (theVertAxis instanceof TransformVertAxis)
			theVertAxis.setLinkedData(data, true);
		
		String lineKeys[] = getLineKeys();
		if (lineKeys.length > 0) {
			theView.setSmoothedVariable(lineKeys[0]);
			for (int i=1 ; i<lineKeys.length ; i++)
				theView.addSmoothedVariable(lineKeys[i]);
		}
		
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected VertAxis vertAxis(DataSet data) {
		String transformString = getParameter(TRANSFORM_PARAM);
		boolean initialLogTrans = false;
		VertAxis localAxis;
		if (transformString == null)
			localAxis = new VertAxis(this);
		else {
			StringTokenizer transform = new StringTokenizer(transformString);
			if (!transform.hasMoreTokens() || !transform.nextToken().equals("true"))
				localAxis = new VertAxis(this);
			else {
				localAxis = new TransformVertAxis(this);
				initialLogTrans = transform.hasMoreTokens() && transform.nextToken().equals("log");
			}
		}
		localAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
		if (initialLogTrans)
			localAxis.setPower(0.0);
		return localAxis;
	}
	
	protected TimeAxis horizAxis(DataSet data) {
		TimeAxis valueAxis;
		String timeParam = getParameter(TIME_INFO_PARAM);
		if (timeParam != null) {
			IndexTimeAxis theHorizAxis = new IndexTimeAxis(this, data.getNumVariable().noOfValues());
			theHorizAxis.setTimeScale(getParameter(TIME_INFO_PARAM));
			valueAxis = theHorizAxis;
		}
		else {
			SeasonTimeAxis theHorizAxis = new SeasonTimeAxis(this, data.getNumVariable().noOfValues());
			theHorizAxis.setTimeScale(getParameter(SEASON_PARAM), getParameter(SEASON_INFO_PARAM));
			valueAxis = theHorizAxis;
		}
		if (labelAxes)
			valueAxis.setAxisName(getParameter(TIME_NAME_PARAM));
		return valueAxis;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		return thePanel;
	}
	
	abstract protected boolean showDataValue();
	abstract protected boolean showSmoothedValue();
	
	private String getRawString(String rawKey) {
		return getParameter(RAW_NAME_PARAM);
	}
	
	protected XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		if (showDataValue() && getCrossKey() != null) {
			OneValueView actual = new OneValueView(data, getCrossKey(), this);
			actual.setForeground(kActualColor);
			String rawString = getRawString(getCrossKey());
			if (rawString != null)
				actual.setLabel(rawString);
			
			thePanel.add(actual);
		}
		if (showSmoothedValue())
			for (int i=0 ; i<getLineKeys().length ; i++) {
				OneValueView smooth = new OneValueView(data, getLineKeys()[i], this);
				smooth.setForeground(kSmoothedColor);
				thePanel.add(smooth);
		}
		return thePanel;
	}
	
	protected XCheckbox createShadingCheck() {
		sourceShadingCheck = new XCheckbox(translate("Show Contributors"), this);
		sourceShadingCheck.setState(theView.getSourceShading());
		return sourceShadingCheck;
	}

	
	private boolean localAction(Object target) {
		if (target == sourceShadingCheck) {
			boolean newState = sourceShadingCheck.getState();
			if (newState != theView.getSourceShading())
				theView.setSourceShading(newState);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}