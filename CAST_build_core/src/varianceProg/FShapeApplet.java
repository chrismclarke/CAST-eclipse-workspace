package varianceProg;

import java.awt.*;

import axis.*;
import dataView.*;
import distn.*;
import utils.*;

import variance.*;


public class FShapeApplet extends Chi2ShapeApplet {
	static final private String F_NAME_PARAM = "fName";
	
	private ParameterSlider df1Slider, df2Slider;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		FDistnVariable fDistn = new FDistnVariable("F distn", startDF, startDF);
		data.addVariable("f", fDistn);
		
		return data;
	}
	
	protected XPanel sliderPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
		
			df1Slider = new ParameterSlider(new NumValue(lowDF, 0), new NumValue(highDF, 0),
																				new NumValue(startDF, 0), translate("Numerator d.f."), this);
			df1Slider.setFont(getStandardBoldFont());
		
		thePanel.add(df1Slider);
		
			df2Slider = new ParameterSlider(new NumValue(lowDF, 0), new NumValue(highDF, 0),
																				new NumValue(startDF, 0), translate("Denominator d.f."), this);
			df2Slider.setFont(getStandardBoldFont());
		
		thePanel.add(df2Slider);
		
		return thePanel;
	}
	
	protected Chi2View createView(DataSet data, HorizAxis horizAxis) {
		FView theView = new FView(data, this, horizAxis, "f");
		theView.setDistnLabel(new LabelValue(getParameter(F_NAME_PARAM)), kGray);
		return theView;
	}
	
	private boolean localAction(Object target) {
		if (target == df1Slider) {
			int newDF1 = (int)Math.round(df1Slider.getParameter().toDouble());
			
			FDistnVariable fDistn = (FDistnVariable)data.getVariable("f");
			fDistn.setDF(newDF1, fDistn.getDF2());
			data.variableChanged("f");
				
			return true;
		}
		else if (target == df2Slider) {
			int newDF2 = (int)Math.round(df2Slider.getParameter().toDouble());
			
			FDistnVariable fDistn = (FDistnVariable)data.getVariable("f");
			fDistn.setDF(fDistn.getDF1(), newDF2);
			data.variableChanged("f");
				
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}