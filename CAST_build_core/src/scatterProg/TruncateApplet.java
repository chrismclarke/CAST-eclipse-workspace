package scatterProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import coreVariables.*;
import coreGraphics.*;

import scatter.*;


public class TruncateApplet extends ScatterApplet {
	static final private String SCALE_PARAM = "scale";
	static final private String TRUNCATION_PARAM = "truncation";
	static final private String PROPN_PARAM = "propnText";
	
	private ScatterTruncView theView;
	private Propn2View thePropn;
	
	private XNoValueSlider strengthSlider;
	private XCheckbox truncateCheck;
	
	private double truncation;
	private double propnCutOff;
	private String propnString;
	
	public void setupApplet() {
		String cutOffString = getParameter(PROPN_PARAM);
		int spaceIndex = cutOffString.indexOf(' ');
		propnCutOff = Double.parseDouble(cutOffString.substring(0, spaceIndex));
		propnString = cutOffString.substring(spaceIndex + 1);
		
		super.setupApplet();
	}
	
	protected DataSet readData() {
		DataSet data = readCoreData();
		
		String paramString = getParameter(SCALE_PARAM);
		NumVariable xVariable = (NumVariable)data.getVariable("x");
		ScaledVariable y2Variable = new ScaledVariable(getParameter(Y_VAR_NAME_PARAM),
																	xVariable, "x", paramString);
		data.addVariable("y2", y2Variable);
		return data;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		theView = new ScatterTruncView(data, this, theHorizAxis, theVertAxis, "x", "y", "x", "y2", propnCutOff);
		return theView;
	}
	
	protected XPanel controlPanel(DataSet data) {
		return null;
	}
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 0));
			Font boldFont = getStandardBoldFont();
		
			int startFrame = ScatterMoveView.kMaxFrame / 2;
			theView.setFrame(startFrame);
			strengthSlider = new XNoValueSlider("Weak", "Strong", "Strength of Relationship", 0,
																		ScatterMoveView.kMaxFrame, startFrame, this);
			strengthSlider.setFont(boldFont);
		
		thePanel.add(strengthSlider);
		
			XPanel resultsPanel = new XPanel();
			resultsPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 10));
			
				thePropn = new Propn2View(data, this, theView, propnString);
				thePropn.setFont(boldFont);
			resultsPanel.add(thePropn);
			
				String truncationString = getParameter(TRUNCATION_PARAM);
				int spaceIndex = truncationString.indexOf(' ');
				truncation = Double.parseDouble(truncationString.substring(0, spaceIndex));
				String checkLabel = truncationString.substring(spaceIndex + 1);
				
				truncateCheck = new XCheckbox(checkLabel, this);
			resultsPanel.add(truncateCheck);
		
		thePanel.add(resultsPanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == truncateCheck) {
			theView.setTruncation(truncateCheck.getState() ? truncation : Double.NEGATIVE_INFINITY);
			thePropn.repaint();
			return true;
		}
		else if (target == strengthSlider) {
			int frameNo = strengthSlider.getValue();
			theView.setFrame(frameNo);
			thePropn.repaint();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}