package histoProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import histo.*;


public class FreqPolyApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final protected String COUNTS_PARAM = "counts";
	
	private DataView histoPoly;
	private XNoValueSlider animationSlider;
	
	public void setupApplet() {
		DataSet data = new DataSet();		//		no variables
		
		setLayout(new BorderLayout());
		add("Center", createDataView(data));
		add("South", createControls(data));
	}
	
	protected int[] readCounts(String param) {
		String countString = getParameter(param);
		StringTokenizer st = new StringTokenizer(countString);
		int[] counts = new int[st.countTokens()];
		for (int i=0 ; i<counts.length ; i++)
			counts[i] = Integer.parseInt(st.nextToken());
		return counts;
	}
	
	protected DataView createHistoPoly(DataSet data) {
		int[] counts = readCounts(COUNTS_PARAM);
		
		FreqPolyView histoPoly = new FreqPolyView(data, this, counts);
		histoPoly.lockBackground(Color.white);
		return histoPoly;
	}
	
	private XPanel createDataView(DataSet data) {
		XPanel histoPanel = new XPanel();
		histoPanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			String varName = getParameter(VAR_NAME_PARAM);
			if (varName != null)
				theHorizAxis.setAxisName(varName);
		
		histoPanel.add("Bottom", theHorizAxis);
		
			histoPoly = createHistoPoly(data);
		histoPanel.add("Center", histoPoly);
		
		return histoPanel;
	}
	
	private XPanel createControls(DataSet data) {
		XPanel controlPanel = new InsetPanel(50, 0);
		controlPanel.setLayout(new BorderLayout());
		
		animationSlider = new XNoValueSlider(translate("Histogram"), translate("Frequency polygon"), null, 0, FreqPolyView.kMaxFrames, 0, this);
		controlPanel.add("Center", animationSlider);
		
		return controlPanel;
	}
	
	private boolean localAction(Object target) {
		if (target == animationSlider) {
			int value = animationSlider.getValue();
			histoPoly.setFrame(value);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}