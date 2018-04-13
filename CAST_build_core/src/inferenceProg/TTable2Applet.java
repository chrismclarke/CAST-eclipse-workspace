package inferenceProg;

import java.awt.*;

import axis.*;
import dataView.*;
import valueList.OneValueView;
import utils.*;

import scatter.*;
import inference.*;


class DFDataSlider extends XSlider {
	private DataSet data;
	private String dfKey;
	
	DFDataSlider(DataSet data, String dfKey, int startDF, XApplet applet) {
		super(null, null, applet.translate("Degrees of freedom") + " = ", 0,
							((NumVariable)data.getVariable(dfKey)).noOfValues() - 1, startDF - 1, applet);
		
		this.data = data;
		this.dfKey = dfKey;
	}
	
	protected Value translateValue(int val) {
		NumVariable dfVar = (NumVariable)data.getVariable(dfKey);
		return dfVar.valueAt(val);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return translateValue(getMaxValue()).stringWidth(g);
	}
}


public class TTable2Applet extends XApplet {
	static final private String X_AXIS_INFO_PARAM = "horizAxis";
	static final private String Y_AXIS_INFO_PARAM = "vertAxis";
	
	static final private Color kDFColor = Color.blue;
	static final private Color kTColor = new Color(0x009900);
	static final private int kStartDF = 30;
	
	private TTable2DataSet tData;
	private DFDataSlider dfSlider;
	
	public void setupApplet() {
		tData = new TTable2DataSet(0.975, kStartDF);
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(tData));
		add("South", dfPanel(tData));
		add("North", tValuePanel(tData));
	}
	
	protected XPanel tValuePanel(TTable2DataSet tData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		OneValueView tValue = new OneValueView(tData, "t", this);
		tValue.setLabel(translate("t-value") + " =");
		tValue.setForeground(kTColor);
		thePanel.add(tValue);
		
		return thePanel;
	}
	
	protected XPanel displayPanel(TTable2DataSet tData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(X_AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		theHorizAxis.setForeground(kDFColor);
		thePanel.add("Bottom", theHorizAxis);
		
		VertAxis theVertAxis = new VertAxis(this);
		labelInfo = getParameter(Y_AXIS_INFO_PARAM);
		theVertAxis.readNumLabels(labelInfo);
		theVertAxis.setForeground(kTColor);
		thePanel.add("Left", theVertAxis);
		
		DataView theView = new ScatterArrowView(tData, this, theHorizAxis, theVertAxis, "df", "t");
								
		theView.setCrossSize(DataView.SMALL_CROSS);
		theView.setRetainLastSelection(true);
//		theView.setStickyDrag(true);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected XPanel dfPanel(TTable2DataSet tData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			dfSlider = new DFDataSlider(tData, "df", kStartDF, this);
			dfSlider.setForeground(kDFColor);
			dfSlider.setFont(getStandardBoldFont());
			tData.setDFSlider(dfSlider);
		thePanel.add("Center", dfSlider);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == dfSlider) {
			tData.setSelection(dfSlider.getValue());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}