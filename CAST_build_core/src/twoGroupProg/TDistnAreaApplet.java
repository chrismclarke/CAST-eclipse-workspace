package twoGroupProg;

import java.awt.*;

import axis.*;
import dataView.*;
import distn.*;
import qnUtils.*;
import coreGraphics.*;


public class TDistnAreaApplet extends XApplet {		//	Only used to generate t distn pictures
	static final private String DF_PARAM = "df";
	static final private String T_AXIS_PARAM = "tAxis";
	static final private String ALTERNATIVE_PARAM = "alternative";
	static final private String VALUE_PARAM = "value";
	
//	static final private Color kDarkGreenColor = new Color(0x006600);
	
	private int testTail;
	private NumValue tValue;
	private int df;
	
	public void setupApplet() {
		readParameters();
		DataSet data = createData();
		
		setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			theHorizAxis.readNumLabels(getParameter(T_AXIS_PARAM));
			theHorizAxis.setAxisName("t distribution");
		add("Bottom", theHorizAxis);
		
			VertAxis theProbAxis = new VertAxis(this);
			theProbAxis.readNumLabels("0 0.5 7 0.1");
			theProbAxis.show(false);
		add("Left", theProbAxis);
		
			DistnDensityView tView = new DistnDensityView(data, this, theHorizAxis, theProbAxis, "tDistn");
			tView.setColors(new Color(0x99CCCC), new Color(0x990000));
			tView.lockBackground(Color.white);
		add("Center", tView);
		
		double pValue = TTable.cumulative(tValue.toDouble(), df);
			if (testTail == HypothesisTest.HA_NOT_EQUAL)
				pValue = 2.0 * Math.min(pValue, 1.0 - pValue);
			else if (testTail == HypothesisTest.HA_HIGH)
				pValue = 1.0 - pValue;
		System.out.println("p-value = " + pValue);
	}
	
	private DataSet createData() {
		DataSet theData = new DataSet();
			TDistnVariable tDistn = new TDistnVariable(translate("t distn"), df);
			if (testTail == HypothesisTest.HA_LOW) {
				tDistn.setMinSelection(tValue.toDouble());
				tDistn.setMaxSelection(Double.POSITIVE_INFINITY);
			}
			else if (testTail == HypothesisTest.HA_HIGH) {
				tDistn.setMaxSelection(tValue.toDouble());
				tDistn.setMinSelection(Double.NEGATIVE_INFINITY);
			}
			else {
				tDistn.setMinSelection(-Math.abs(tValue.toDouble()));
				tDistn.setMaxSelection(Math.abs(tValue.toDouble()));
			}
		theData.addVariable("tDistn", tDistn);
		
		return theData;
	}
	
	private void readParameters() {
			String tailString = getParameter(ALTERNATIVE_PARAM);
			if (tailString.equals("low"))
				testTail = HypothesisTest.HA_LOW;
			else if (tailString.equals("high"))
				testTail = HypothesisTest.HA_HIGH;
			else
				testTail = HypothesisTest.HA_NOT_EQUAL;
			
			df = Integer.parseInt(getParameter(DF_PARAM));
			tValue = new NumValue(getParameter(VALUE_PARAM));
	}
}