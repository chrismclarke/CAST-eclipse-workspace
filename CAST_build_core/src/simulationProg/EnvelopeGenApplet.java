package simulationProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import valueList.*;

import simulation.*;


public class EnvelopeGenApplet extends XApplet {
	static final private String MAX_RANDOM_PARAM = "maxRandom";
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String Z_VAR_NAME_PARAM = "zVarName";
	static final private String NO_OF_SD_PARAM = "noOfSDs";
	
	static final private String k01AxisString = "0 1 0 0.2";
	
	static final private Color kPaleRed = new Color(0xFF6666);
	static final private Color kPaleGreen = new Color(0x33FF33);
	
	private XButton resetButton;
	private RepeatingButton generateButton;
	private SimpleTextArea message;
	
	private DataSet data;
	private NumValue maxRandom;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(20, 0));
		
		add("Center", displayPanel(data));
		add("East", controlPanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		maxRandom = new NumValue(getParameter(MAX_RANDOM_PARAM));
		
		data.addVariable("z", new NumVariable(getParameter(Z_VAR_NAME_PARAM)));
		
			double noOfSDs = Double.parseDouble(getParameter(NO_OF_SD_PARAM));
		data.addVariable("x", new EnvelopeVariable(getParameter(X_VAR_NAME_PARAM), data,
																															"z", noOfSDs));
		
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis xAxis = new HorizAxis(this);
			xAxis.readNumLabels(k01AxisString);
		thePanel.add("Bottom", xAxis);
		
			VertAxis probAxis = new VertAxis(this);
			probAxis.readNumLabels(k01AxisString);
		thePanel.add("Left", probAxis);
			
			EnvelopeGeneratorView theView = new EnvelopeGeneratorView(data, this, xAxis, probAxis, "x", "z");
		thePanel.add("Center", theView);
		
		return thePanel;
	}

	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
		
			XPanel resultPanel = new XPanel();
			resultPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 4));
			resultPanel.add(new OneValueView(data, "x", this, maxRandom));
			resultPanel.add(new OneValueView(data, "z", this, maxRandom));
				message = new SimpleTextArea(1);
				message.lockBackground(Color.white);
				message.setFont(getBigBoldFont());
			resultPanel.add(message);
		
		thePanel.add(resultPanel);
		
			generateButton = new RepeatingButton(translate("Generate value"), this);
		thePanel.add(generateButton);
		
			resetButton = new XButton(translate("Reset"), this);
		thePanel.add(resetButton);
			
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == generateButton) {
			EnvelopeVariable x = (EnvelopeVariable)data.getVariable("x");
			x.generateNewValue(maxRandom.decimals);
			data.variableChanged("x");
			data.variableChanged("z");
			data.setSelection(x.noOfValues() - 1);
			boolean failure = x.lastValueRejected();
			if (failure) {
				message.lockBackground(kPaleRed);
				message.setText("Value rejected");
			}
			else {
				message.lockBackground(kPaleGreen);
				message.setText("Value accepted");
			}
			return true;
		}
		else if (target == resetButton) {
			EnvelopeVariable x = (EnvelopeVariable)data.getVariable("x");
			NumVariable z = (NumVariable)data.getVariable("z");
			x.clearData();
			z.clearData();
			data.variableChanged("x");
			data.variableChanged("z");
			message.lockBackground(getBackground());
			message.setText("");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}