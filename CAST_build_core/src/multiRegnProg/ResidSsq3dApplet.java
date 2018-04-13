package multiRegnProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import multiRegn.*;


public class ResidSsq3dApplet extends CoreRegnPlaneApplet {
	static final private String BIGGEST_RSS_PARAM = "biggestRss";
	
	
	static final private NumValue kZero = new NumValue(0.0, 0);
	
	protected MultiAnovaTableView theTable = null;
	
	private XButton minRssButton, zeroButton;
	
	private XChoice[] paramTypeChoice;
	protected int currentDragParam = 0;
	
	private XPanel sliderPanel;
	private CardLayout sliderCardLayout;
	private ParameterSlider param3Slider;
	
	
	
	protected Rotate3DView getRotatingView(DataSet data, D3Axis yAxis, D3Axis xAxis, D3Axis zAxis) {
		return new Model3DragView(data, this, xAxis, yAxis, zAxis, "model", explanKey, "y");
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.HORIZONTAL,
																					ProportionLayout.TOTAL));
		thePanel.add(ProportionLayout.LEFT, super.controlPanel(data));
		
			sliderPanel = new XPanel();
			sliderCardLayout = new CardLayout();
			sliderPanel.setLayout(sliderCardLayout);
					XPanel emptyPanel = new XPanel();
				sliderPanel.add("blank", emptyPanel);
				if (explanKey.length > 2) {
					XPanel actualPanel = new XPanel();
					actualPanel.setLayout(new BorderLayout(0, 0));
						param3Slider = new ParameterSlider(minParam[3], maxParam[3], kZero,
									"Coeff of " + explanName[2], ParameterSlider.NO_SHOW_MIN_MAX, this);
					actualPanel.add("Center", param3Slider);
					sliderPanel.add("slider", actualPanel);
				}
		thePanel.add(ProportionLayout.RIGHT, sliderPanel);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 40));
		
		thePanel.add(parameterPanel(data));
		
			NumValue maxSsq = new NumValue(getParameter(BIGGEST_RSS_PARAM));
		
		thePanel.add(ssqPanel(data, maxSsq));
		
		return thePanel;
	}
	
	protected XPanel ssqPanel(DataSet data, NumValue maxSsq) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		MultiResidSsqView rssView = new MultiResidSsqView(data, "y", "model", maxSsq, this);
		
		thePanel.add(rssView);
		
		return thePanel;
	}
	
	private XChoice addParamRow(String paramName, int row, XPanel thePanel,
													GridBagConstraints c, GridBagLayout gbl) {
		c.gridx = 0;
		c.gridy = row;
		XLabel paramLabel = new XLabel(paramName, XLabel.LEFT, this);
		gbl.setConstraints(paramLabel, c);
		thePanel.add(paramLabel);
		
		XChoice theChoice = new XChoice(this);
		if (row != 1)
			theChoice.addItem(translate("Zero"));
		theChoice.addItem(translate("Drag"));
		boolean lastRow = (row == explanKey.length + 1);
		if (!lastRow)
			theChoice.addItem(translate("Fix at best"));
		c.gridx = 1;
		gbl.setConstraints(theChoice, c);
		thePanel.add(theChoice);
		if (row != 1)
			theChoice.disable();
		return theChoice;
	}
	
	private XPanel parameterPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			XPanel topPanel = new XPanel();
			GridBagLayout gbl = new GridBagLayout();
			topPanel.setLayout(gbl);
				GridBagConstraints c = new GridBagConstraints();
				c.gridx = 0;
				c.gridy = 0;
				c.ipady = 1;
				c.anchor = GridBagConstraints.WEST;
				c.weightx = 1.0;
				
				c.gridwidth = 2;
				XLabel title = new XLabel(translate("Parameter"), XLabel.LEFT, this);
				title.setFont(getStandardBoldFont());
			gbl.setConstraints(title, c);
			topPanel.add(title);
				c.gridwidth = 1;
			
			paramTypeChoice = new XChoice[explanKey.length + 1];
			paramTypeChoice[0] = addParamRow(translate("Constant"), 1, topPanel, c, gbl);
			for (int i=0 ; i<explanKey.length ; i++)
				paramTypeChoice[i+1] = addParamRow(explanName[i], i+2, topPanel, c, gbl);
			
		thePanel.add("Center", topPanel);
		
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
				
				minRssButton = new XButton(translate("Min resid ssq"), this);
				buttonPanel.add(minRssButton);
				
				zeroButton = new XButton(translate("Zero"), this);
				zeroButton.disable();
				buttonPanel.add(zeroButton);
		
		thePanel.add("South", buttonPanel);
		return thePanel;
	}
	
	private void setBestUpTo(int paramIndex) {
		double[] fixedB = new double[explanKey.length + 1];
		for (int i=0 ; i<=paramIndex ; i++)
			fixedB[i] = Double.NaN;
		for (int i=paramIndex+1 ; i<fixedB.length ; i++)
			fixedB[i] = 0.0;
		MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
		model.updateLSParams("y", fixedB);
	}
	
	private void setBestUpTo(int paramIndex, double paramValue) {
		double[] fixedB = new double[explanKey.length + 1];
		for (int i=0 ; i<=paramIndex-1 ; i++)
			fixedB[i] = Double.NaN;
		fixedB[paramIndex] = paramValue;
		for (int i=paramIndex+1 ; i<fixedB.length ; i++)
			fixedB[i] = 0.0;
		MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
		model.updateLSParams("y", fixedB);
	}
	
	protected void zeroParameter(int paramIndex) {
		setBestUpTo(currentDragParam - 1);
		paramTypeChoice[paramIndex].disable();
		paramTypeChoice[paramIndex - 1].select((paramIndex == 1) ? 0 : 1);
		paramTypeChoice[paramIndex - 1].enable();
		currentDragParam = paramIndex - 1;
		((Model3DragView)theView).setDragIndex(paramIndex - 1);
		theEqn.setLastDrawParameter(paramIndex - 1);
		if (theTable != null)
			theTable.setDragIndex(paramIndex - 1);
		if (paramIndex == 1)
			zeroButton.disable();
		data.variableChanged("model");
		if (paramIndex == 3) {
			sliderCardLayout.show(sliderPanel, "blank");
			param3Slider.setParameter(0.0);
		}
	}
	
	private boolean localAction(Object target) {
		for (int i=0 ; i<paramTypeChoice.length ; i++)
			if (target == paramTypeChoice[i]) {
				int selection = paramTypeChoice[i].getSelectedIndex();
				if (i == 0)
					selection ++;
				
				switch (selection) {
					case ZERO:
						zeroParameter(i);
						break;
					case BEST:
						setBestUpTo(currentDragParam);
						paramTypeChoice[i].disable();
						paramTypeChoice[i+1].enable();
						paramTypeChoice[i+1].select(1);
						currentDragParam = i + 1;
						((Model3DragView)theView).setDragIndex(i + 1);
						theEqn.setLastDrawParameter(i + 1);
						if (theTable != null)
							theTable.setDragIndex(i + 1);
						if (i == 0)
							zeroButton.enable();
						data.variableChanged("model");
						if (i == 2) {
							sliderCardLayout.show(sliderPanel, "slider");
//							System.out.println("Showing slider panel");
						}
						break;
					case DRAG:		//		drag must be old selection
				}
				return true;
			}
		
		if (target == zeroButton) {
			setBestUpTo(currentDragParam - 1);
			data.variableChanged("model");
			if (currentDragParam > 2) {
				param3Slider.setParameter(0.0);
			}	
			return true;
		}
		else if (target == minRssButton) {
			setBestUpTo(currentDragParam);
			data.variableChanged("model");
			if (currentDragParam > 2) {
				MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
				param3Slider.setParameter(model.getParameter(currentDragParam).toDouble());
			}	
			return true;
		}
		else if (target == param3Slider) {
			setBestUpTo(currentDragParam, param3Slider.getParameter().toDouble());
			data.variableChanged("model");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (localAction(evt.target))
			return true;
		else
			return super.action(evt, what);
	}
}