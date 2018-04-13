package simulationProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;

import simulation.*;
import sampling.*;
import samplingProg.*;


public class RandomCat3Applet extends RandomCatApplet implements RandomDigitProgInterface {
	static final private String RANDOM_SEED_PARAM = "random";
	
	static final private int[] kMillisecPerFrame = {25, 23, 27, 24, 26};
	
	private RandomDigitsPanel theDigits;
	private XButton generateButton, reset2Button;
	
	public void setupApplet() {
		DigitImages.loadDigits(this);
		
		data = getData();
		
		setLayout(new BorderLayout(20, 20));
		add("North", topPanel(data));
		add("East", catValuePanel(data));
		add("Center", horizDotPlotPanel(data));
		add("South", sliderControlPanel(data));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		NumVariable rand = new NumVariable("Random");
		data.addVariable("random", rand);
		
		data.addVariable("randomCat", createCatVariable(data));
		
//		data.setSelection(0);
		
		return data;
	}
	
	private XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
			generateButton = new XButton(translate("Generate value"), this);
		thePanel.add(generateButton);
		
			theDigits = new RandomDigitsPanel(this, this, RandomDigitsPanel.DECIMALS,
																											getParameter(RANDOM_SEED_PARAM));
		thePanel.add(theDigits);
		
		return thePanel;
	}
	
	private XPanel horizDotPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			horizAxis.readNumLabels("0 1 0.0 0.1");
		thePanel.add("Bottom", horizAxis);
		
			valueView = new RandomCatGeneratorView(data, this, horizAxis);
			valueView.setActiveNumVariable("random");
//			valueView.lockBackground(Color.white);
		thePanel.add("Center", valueView);
		return thePanel;
	}
	
	private XPanel sliderControlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel slider1Panel = new XPanel();
			slider1Panel.setLayout(new ProportionLayout(0.25, 0));
			
				XPanel slider2Panel = new XPanel();
				slider2Panel.setLayout(new ProportionLayout(0.6667, 0));
				
				slider2Panel.add(ProportionLayout.LEFT, controlPanel(data));
			slider1Panel.add(ProportionLayout.RIGHT, slider2Panel);
			
		thePanel.add("Center", slider1Panel);
			
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				reset2Button = new XButton(translate("Reset"), this);
			buttonPanel.add(reset2Button);
			
		thePanel.add("East", buttonPanel);
		return thePanel;
	}

//----------------------------------------------------------------
	
	public void noteNewValue(RandomDigitsPanel valuePanel) {
		NumVariable rand = (NumVariable)data.getVariable("random");
		rand.addValue(new NumValue(theDigits.getDecimalValue(), 6));
		data.valuesAdded(rand.noOfValues());
		
		generateButton.enable();
	}
	
	public void noteClearedValue() {
		generateButton.disable();
	}

//----------------------------------------------------------------
	
	private boolean localAction(Object target) {
		if (target == generateButton) {
			theDigits.animateNextDigits(kMillisecPerFrame);
			
			return true;
		}
		else if (target == reset2Button) {
			NumVariable rand = (NumVariable)data.getVariable("random");
			rand.clearData();
			data.variableChanged("random");
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}