package regnProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import coreGraphics.*;

import regn.*;
import regnView.*;


public class TwoWayResidApplet extends ScatterApplet {
	static final protected String X_RSS_AXIS_PARAM = "xRssAxis";
	static final protected String Y_RSS_AXIS_PARAM = "yRssAxis";
	static final protected String X_RSS_DECIMALS_PARAM = "xRssDecimals";
	static final protected String Y_RSS_DECIMALS_PARAM = "yRssDecimals";
	static final protected String SLIDER_HEIGHT_PARAM = "sliderHeight";
	
	private XButton minYRssButton, minXRssButton;
	
	private TwoWayResidView theView;
	
	private XPanel yResidPanel, xResidPanel;
	private CardLayout yResidCardLayout, xResidCardLayout;
	
	private XCheckbox showXResidCheck, showYResidCheck;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		TwoWayModel model = new TwoWayModel("Model");
		model.initialise(data, "x", "y");
		
		data.addVariable("model", model);
		
		return data;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		theView = new TwoWayResidView(data, this, theHorizAxis, theVertAxis, "x", "y", "model");
		return theView;
	}
	
	private XPanel rssPanel(DataSet data, boolean straightNotInv) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		Color residColor = straightNotInv ? TwoWayResidView.yResidColor
											: TwoWayResidView.xResidColor;
		
		String responseKey = straightNotInv ? "y" : "x";
		XLabel titleLabel = new XLabel(translate("Response") + ": "
									+ data.getVariable(responseKey).name, XLabel.LEFT, this);
		titleLabel.setForeground(residColor);
		thePanel.add("North", titleLabel);
		
		XLabel rssLabel = new XLabel(translate("RSS") + ": ", XLabel.LEFT, this);
		rssLabel.setForeground(residColor);
		thePanel.add("West", rssLabel);
		
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				XButton minimiseButton = new XButton(translate("Minimise"), this);
				if (straightNotInv)
					minYRssButton = minimiseButton;
				else
					minXRssButton = minimiseButton;
			buttonPanel.add(minimiseButton);
		thePanel.add("East", buttonPanel);
		
			XPanel sliderPanel = new XPanel();
			sliderPanel.setLayout(new AxisLayout());
			
			HorizAxis axis = new HorizAxis(this);
			axis.readNumLabels(getParameter(straightNotInv ? Y_RSS_AXIS_PARAM
																	: X_RSS_AXIS_PARAM));
			sliderPanel.add("Bottom", axis);
				int decimals = Integer.parseInt(getParameter(straightNotInv
											? Y_RSS_DECIMALS_PARAM : X_RSS_DECIMALS_PARAM));
			RssSliderView slider = new RssSliderView(data, this, "x", "y", "model", straightNotInv, axis, decimals);
			slider.setForeground(residColor);
			sliderPanel.add("Center", slider);
			
		thePanel.add("Center", sliderPanel);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
			XPanel checkPanel = new XPanel();
			checkPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
			
			XLabel checkLabel = new XLabel(translate("Resids for") + ":",
																		XLabel.LEFT, this);
			checkLabel.setFont(getStandardBoldFont());
			checkPanel.add(checkLabel);
				
				showYResidCheck = new XCheckbox(data.getVariable("y").name, this);
				showYResidCheck.setForeground(TwoWayResidView.yResidColor);
			checkPanel.add(showYResidCheck);
				
				showXResidCheck = new XCheckbox(data.getVariable("x").name, this);
				showXResidCheck.setForeground(TwoWayResidView.xResidColor);
			checkPanel.add(showXResidCheck);
			
		thePanel.add("North", checkPanel);
			
			XPanel superPanel = new XPanel();
				int sliderHeight = Integer.parseInt(getParameter(SLIDER_HEIGHT_PARAM));
			superPanel.setLayout(new FixedSizeLayout(50, sliderHeight * 2 + 5));
			
				XPanel meterPanel = new XPanel();
				meterPanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.VERTICAL));
					
					yResidPanel = new XPanel();
						yResidCardLayout = new CardLayout();
					yResidPanel.setLayout(yResidCardLayout);
					yResidPanel.add("Off", new XPanel());
					yResidPanel.add("On", rssPanel(data, true));
						yResidCardLayout.show(yResidPanel, "Off");
					
				meterPanel.add(ProportionLayout.TOP, yResidPanel);
				
					xResidPanel = new XPanel();
						xResidCardLayout = new CardLayout();
					xResidPanel.setLayout(xResidCardLayout);
					xResidPanel.add("Off", new XPanel());
					xResidPanel.add("On", rssPanel(data, false));
						xResidCardLayout.show(xResidPanel, "Off");
					
				meterPanel.add(ProportionLayout.BOTTOM, xResidPanel);
			
			superPanel.add(meterPanel);
		
		thePanel.add("Center", superPanel);
		
		return thePanel;
	}

//--------------------------------------------------------------

	
	private boolean localAction(Object target) {
		if (target == minYRssButton) {
			TwoWayModel model = (TwoWayModel)data.getVariable("model");
			model.setToBest(data, "x", "y", true);
			theView.setAnchorsFromLine();
			data.variableChanged("model");
			return true;
		}
		else if (target == minXRssButton) {
			TwoWayModel model = (TwoWayModel)data.getVariable("model");
			model.setToBest(data, "x", "y", false);
			theView.setAnchorsFromLine();
			data.variableChanged("model");
			return true;
		}
		else if (target == showXResidCheck) {
			String showResid = showXResidCheck.getState() ? "On" : "Off";
			xResidCardLayout.show(xResidPanel, showResid);
			theView.setDrawResiduals(showXResidCheck.getState(), showYResidCheck.getState());
			return true;
		}
		else if (target == showYResidCheck) {
			String showResid = showYResidCheck.getState() ? "On" : "Off";
			yResidCardLayout.show(yResidPanel, showResid);
			theView.setDrawResiduals(showXResidCheck.getState(), showYResidCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}