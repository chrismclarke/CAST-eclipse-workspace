package histoProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import histo.*;


public class TwoGroupHistoApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String CLASS_INFO_PARAM = "classInfo";
	static final private String COUNT_AXIS_INFO_PARAM = "countAxis";
	static final private String PROPN_AXIS_INFO_PARAM = "propnAxis";
	
	private String kFreqString, kRelFreqString;
	
	private Histo2GroupView theHisto;
	private MultiVertAxis freqAxis;
	private XChoice axisTypeChoice;
	private int axisType;
	private XLabel vertAxisNameLabel;
	
	public void setupApplet() {
		kFreqString = translate("Frequency");
		kRelFreqString = translate("Relative frequency");
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		data.addCatVariable("group", getParameter(CAT_NAME_PARAM),
									getParameter(CAT_VALUES_PARAM), getParameter(CAT_LABELS_PARAM));
		
		setLayout(new BorderLayout());
		vertAxisNameLabel = new XLabel("", XLabel.LEFT, this);
		vertAxisNameLabel.setFont(getStandardBoldFont());
		add("North", vertAxisNameLabel);
		add("Center", createHisto(data));
		add("South", createControls(data));
		vertAxisNameLabel.setText(kFreqString);
	}
	
	private XPanel createHisto(DataSet data) {
		XPanel histoPanel = new XPanel();
		histoPanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		theHorizAxis.setAxisName(getParameter(VAR_NAME_PARAM));
		histoPanel.add("Bottom", theHorizAxis);
		
		String classInfo = getParameter(CLASS_INFO_PARAM);
		StringTokenizer theParams = new StringTokenizer(classInfo);
		double class0Start = Double.parseDouble(theParams.nextToken());
		double classWidth = Double.parseDouble(theParams.nextToken());
//		double maxCount = Integer.parseInt(theParams.nextToken());
		
		freqAxis = new MultiVertAxis(this, 2);
		String countLabelInfo = getParameter(COUNT_AXIS_INFO_PARAM);
		freqAxis.readNumLabels(countLabelInfo);
		String propnLabelInfo = getParameter(PROPN_AXIS_INFO_PARAM);
		freqAxis.readExtraNumLabels(propnLabelInfo);
		freqAxis.setStartAlternate(0);
		histoPanel.add("Left", freqAxis);
		
		theHisto = new Histo2GroupView(data, this, theHorizAxis, freqAxis, class0Start, classWidth);
		histoPanel.add("Center", theHisto);
		theHisto.lockBackground(Color.white);
		
		return histoPanel;
	}
	
	private XPanel createControls(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new BorderLayout());
		
		controlPanel.add("West", new CatKey3View(data, this, "group"));
		
			XPanel menuPanel = new XPanel();
			menuPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 2));
				
				XLabel choiceLabel = new XLabel(translate("Vertical axis") + ":", XLabel.LEFT, this);
				choiceLabel.setFont(getStandardBoldFont());
			menuPanel.add(choiceLabel);
		
				axisTypeChoice = new XChoice(this);
				axisTypeChoice.addItem(translate("Frequency"));
				axisTypeChoice.addItem(translate("Relative frequency"));
				axisTypeChoice.select(0);
				axisType = 0;
			menuPanel.add(axisTypeChoice);
		
		controlPanel.add("Center", menuPanel);
		
		return controlPanel;
	}
	
	private boolean localAction(Object target) {
		if (target == axisTypeChoice)
			if (axisTypeChoice.getSelectedIndex() != axisType) {
				if (axisTypeChoice.getSelectedIndex() == 0) {
					vertAxisNameLabel.setText(kFreqString);
					freqAxis.setAlternateLabels(0);
					theHisto.doAnimation(Histo2GroupView.RELFREQ_TO_FREQ);
				}
				else {
					vertAxisNameLabel.setText(kRelFreqString);
					freqAxis.setAlternateLabels(1);
					theHisto.doAnimation(Histo2GroupView.FREQ_TO_RELFREQ);
				}
				axisType = axisTypeChoice.getSelectedIndex();
				return true;
			}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}