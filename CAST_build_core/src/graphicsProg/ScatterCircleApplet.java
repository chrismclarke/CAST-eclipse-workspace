package graphicsProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import valueList.*;
import coreGraphics.*;

import transform.LogAxis;
import cat.*;
import graphics.*;


public class ScatterCircleApplet extends ScatterApplet {
	static final private String N_SIZE_VARS_PARAM = "nSizeVars";
	static final private String SIZE_VAR_NAME_PARAM = "sizeVarName";
	static final private String SIZE_VALUES_PARAM = "sizeValues";
	static final private String GROUP_NAME_PARAM = "groupName";
	static final private String GROUP_LABELS_PARAM = "groupLabels";
	static final private String GROUP_VALUES_PARAM = "groupValues";
	
	
	private DataSet data;
	private String sizeKeys[];
	
	private ScatterCircleView theView;
	
	private XChoice displayTypeChoice, sizeVarChoice;
	private int currentDisplayType = 0, currentSizeVar = 0;
	
	protected XNoValueSlider radiusSlider;
	
	private XPanel circlePanel;
	private CardLayout circlePanelLayout;
	
	protected DataSet readData() {
		data = new DataSet();
		
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM));
		
		data.addLabelVariable("label", getParameter(LABEL_NAME_PARAM), getParameter(LABELS_PARAM));
		
		data.addCatVariable("group", getParameter(GROUP_NAME_PARAM), getParameter(GROUP_VALUES_PARAM),
																																getParameter(GROUP_LABELS_PARAM));
		
		int nSizeVars = Integer.parseInt(getParameter(N_SIZE_VARS_PARAM));
		sizeKeys = new String[nSizeVars];
		for (int i=0 ; i<nSizeVars ; i++) {
			sizeKeys[i] = "size" + i;
			data.addNumVariable(sizeKeys[i], getParameter(SIZE_VAR_NAME_PARAM + i),
																															getParameter(SIZE_VALUES_PARAM + i));
		}
		
		return data;
	}
	
	protected HorizAxis createHorizAxis(DataSet data) {
		String horizAxisString = getParameter(X_AXIS_INFO_PARAM);
		
		if (horizAxisString.substring(0, 4).equals("log ")) {
			LogAxis axis = new LogAxis(this);
			axis.readExtremes(horizAxisString.substring(4));
			axis.setTransValueDisplay(true);
			if (labelAxes)
				axis.setAxisName(getParameter(X_VAR_NAME_PARAM) + " (log scale)");
			return axis;
		}
		else
			return super.createHorizAxis(data);
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		theView = new ScatterCircleView(data, this, theHorizAxis, theVertAxis, "x", "y", sizeKeys[0], "group");
		theView.setRetainLastSelection(true);
		theView.setMaxRadius(ScatterCircleView.kMaxRadius / 2);
		return theView;
	}
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 30));
		
			CatKey3View theKey = new CatKey3View(data, this, "group");
			theKey.setCatColour(ScatterCircleView.kGroupColor);
		thePanel.add(theKey);
				
			XPanel labelPanel = new XPanel();
			labelPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
			
				XLabel labelLabel = new XLabel(data.getVariable("label").name, XLabel.LEFT, this);
				labelLabel.setFont(getStandardBoldFont());
			labelPanel.add(labelLabel);
				
				OneValueView labelView = new OneValueView(data, "label", this);
				labelView.setNameDraw(false);
			labelPanel.add(labelView);
			
		thePanel.add(labelPanel);
		
		return thePanel;
	}
	
	protected XPanel radiusPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
	
			int radiusSteps = ScatterCircleView.kMaxRadius;
			radiusSlider = new XNoValueSlider(translate("Small"), translate("Large"), translate("Circle size"), 0, radiusSteps,
																																				radiusSteps / 2, this);
		thePanel.add(radiusSlider);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		
			XPanel displayTypePanel = new XPanel();
			displayTypePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				
				XLabel typeLabel = new XLabel(translate("Display values as"), XLabel.LEFT, this);
				typeLabel.setFont(getStandardBoldFont());
			displayTypePanel.add(typeLabel);
			
				displayTypeChoice = new XChoice(this);
				displayTypeChoice.add(translate("Symbols"));
				displayTypeChoice.add(translate("Circles"));
			displayTypePanel.add(displayTypeChoice);
			
		thePanel.add("West", displayTypePanel);
			
			circlePanel = new XPanel();
			circlePanelLayout = new CardLayout();
			circlePanel.setLayout(circlePanelLayout);
			
			circlePanel.add("hide", new XPanel());
			
				XPanel rightPanel = new XPanel();
				rightPanel.setLayout(new BorderLayout(20, 0));
			
					XPanel sizeVarPanel = new XPanel();
					sizeVarPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
						
						XLabel sizeLabel = new XLabel("Circle size:", XLabel.LEFT, this);
						sizeLabel.setFont(getStandardBoldFont());
					sizeVarPanel.add(sizeLabel);
					
						sizeVarChoice = new XChoice(this);
						for (int i=0 ; i<sizeKeys.length ; i++)
							sizeVarChoice.add(data.getVariable(sizeKeys[i]).name);
					sizeVarPanel.add(sizeVarChoice);
				
				rightPanel.add("West", sizeVarPanel);
				
				rightPanel.add("Center", radiusPanel(data));
			
			circlePanel.add("show", rightPanel);
			
		thePanel.add("Center", circlePanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == displayTypeChoice) {
			int newChoice = displayTypeChoice.getSelectedIndex();
			if (newChoice != currentDisplayType) {
				currentDisplayType = newChoice;
				
				theView.setDrawType(newChoice);
				theView.repaint();
				
				circlePanelLayout.show(circlePanel, (newChoice == 0) ? "hide" : "show");
			}
			return true;
		}
		else if (target == sizeVarChoice) {
			int newChoice = sizeVarChoice.getSelectedIndex();
			if (newChoice != currentSizeVar) {
				currentSizeVar = newChoice;
				
				theView.setSizeKey(sizeKeys[newChoice]);
				theView.repaint();
			}
			return true;
		}
		else if (target == radiusSlider) {
			theView.setMaxRadius(radiusSlider.getValue());
			theView.repaint();
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}