package structureProg;

import java.awt.*;

import dataView.*;
import utils.*;

import structure.*;


public class AddNoiseApplet extends XApplet {
	static final private String kImageName = "castForNoise.gif";
	static final private int kSliderMax = 255;
	
	private NoiseImageCanvas picture;
	private XNoValueSlider noiseSlider;
	
	public void setupApplet() {
		setLayout(new BorderLayout(0, 0));
		
			picture = new NoiseImageCanvas(kImageName, this);
		add("Center", picture);
		
			noiseSlider = new XNoValueSlider(translate("Signal"), translate("Noise"), null, 0, kSliderMax, 0, this);
			noiseSlider.setFont(getStandardBoldFont());
		add("South", noiseSlider);
	}
	
	private boolean localAction(Object target) {
		if (target == noiseSlider) {
			double noisePropn = noiseSlider.getValue() / (double)kSliderMax;
			picture.setNoisePropn(noisePropn);
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}