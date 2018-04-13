package scatterProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import coreGraphics.*;
import coreVariables.*;

import scatter.*;


public class Strength3Applet extends ScatterApplet {
	static final private String SLICE_WIDTH_PARAM = "sliceWidth";
	static final private String POS_NEG_NAMES_PARAM = "posNegNames";
	static final private String OTHER_HORIZ_AXIS_PARAM = "horizAxis2";
	
	static final private int kSliderSteps = 200;
	
	static final private Color kStrengthBackground = new Color(0xDDDDEE);
	static final private Color kDarkBlue = new Color(0x000099);
	
	private CorrelatedVariable y2Variable;
	
	private XNoValueSlider strengthSlider;
	private XChoice posNegCorrChoice;
	private boolean isPosCorr = true;
	
	private XLabel posNegRelnLabel;
	
	protected DataSet readData() {
		DataSet data = readCoreData();
		
		NumVariable yVariable = (NumVariable)data.getVariable("y");
		
		y2Variable = new CorrelatedVariable(getParameter(Y_VAR_NAME_PARAM), data, "x", "y",
																					yVariable.getMaxDecimals());
		data.addVariable("y2", y2Variable);

		return data;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		double selectRange = Double.parseDouble(getParameter(SLICE_WIDTH_PARAM));
		SliceScatterView theView = new SliceScatterView(data, this, theHorizAxis, theVertAxis,
																																							"x", "y2", selectRange);
		return theView;
	}
	
	protected HorizAxis createHorizAxis(DataSet data) {
		String axisInfo = getParameter(X_AXIS_INFO_PARAM);
		String otherAxisInfo = getParameter(OTHER_HORIZ_AXIS_PARAM);
		
		if (otherAxisInfo == null) {
			HorizAxis axis = new HorizAxis(this);
			axis.readNumLabels(axisInfo);
			return axis;
		}
		else {
			MultiHorizAxis axis = new MultiHorizAxis(this, 2);
			axis.readNumLabels(axisInfo);
			axis.readExtraNumLabels(otherAxisInfo);
			return axis;
		}
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 25));
		
			XPanel posNegCorrPanel = new XPanel();
			posNegCorrPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
			
				posNegCorrChoice = new XChoice(this);
				StringTokenizer st = new StringTokenizer(getParameter(POS_NEG_NAMES_PARAM), "#");
				posNegCorrChoice.addItem(st.nextToken());
				posNegCorrChoice.addItem(st.nextToken());
				
			posNegCorrPanel.add(posNegCorrChoice);
		
		thePanel.add("North", posNegCorrPanel);
		
			XPanel controlPanel = new InsetPanel(10, 4);
			controlPanel.setLayout(new ProportionLayout(0.3, 25));
			
				String text[] = getSliderText();
				int initSliderVal = kSliderSteps / 2;
				strengthSlider = new XNoValueSlider(text[0], text[1], text[2], 0, kSliderSteps, initSliderVal, this);
			controlPanel.add(ProportionLayout.RIGHT, strengthSlider);
			
				st = new StringTokenizer(translate("Positive*relationship"), "*");
				String positiveString = st.nextToken();
				String relationshipString = st.nextToken();
			
				XPanel posNegPanel = new XPanel();
				posNegPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
					posNegRelnLabel = new XLabel(positiveString, XLabel.CENTER, this);
					posNegRelnLabel.setFont(getBigBoldFont());
					posNegRelnLabel.setForeground(kDarkBlue);
				posNegPanel.add(posNegRelnLabel);
				
					XLabel relnLabel = new XLabel(relationshipString, XLabel.CENTER, this);
					relnLabel.setFont(getBigBoldFont());
					relnLabel.setForeground(kDarkBlue);
				posNegPanel.add(relnLabel);
				
			controlPanel.add(ProportionLayout.LEFT, posNegPanel);
			
			controlPanel.lockBackground(kStrengthBackground);
		thePanel.add("Center", controlPanel);
		
		setCorrelation();
		
		return thePanel;
	}
	
	protected String[] getSliderText() {
		String text[] = {translate("Weak"), translate("Strong"), translate("Strength of relationship")};
		return text;
	}
	
	private void setCorrelation() {
		double sliderPropn = strengthSlider.getValue() / (double)kSliderSteps;
		double newCorr = Math.sqrt(sliderPropn);
		if (!isPosCorr)
			newCorr = -newCorr;
		y2Variable.setCorrelation(newCorr);
		data.variableChanged("y2");
	}

	
	private boolean localAction(Object target) {
		if (target == posNegCorrChoice) {
			boolean newPosNotNeg = posNegCorrChoice.getSelectedIndex() == 0;
			if (newPosNotNeg != isPosCorr) {
				isPosCorr = newPosNotNeg;
				setCorrelation();
				posNegRelnLabel.setText(isPosCorr ? "Positive" : "Negative");
				
				if (theHorizAxis instanceof MultiHorizAxis) {
					((MultiHorizAxis)theHorizAxis).setAlternateLabels(isPosCorr ? 0 : 1);
					theHorizAxis.repaint();
				}
			}
			return true;
		}
		else if (target == strengthSlider) {
			setCorrelation();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}