package scatterProg;

import java.awt.*;

import scatter.*;
import dataView.*;
import axis.*;
import utils.*;
import random.*;
import coreGraphics.*;


public class RandomScatter2Applet extends ScatterApplet {
	static final private String RANDOM_BINORMAL_PARAM = "random";
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	
	private XButton sampleButton;
	private XCheckbox popnCheck;
	private RandomBiNormal generator;
	private RandomRectangular propnGenerator;
	private int sampleSize;
	
	private SampleScatterView theView;
	
	public void setupApplet() {
		super.setupApplet();
		
		theView.setFrame(SampleScatterView.POPN_FRAME);
		
		propnGenerator = new RandomRectangular(1, 0.0, 1.0);
		String sampleInfo = getParameter(SAMPLE_SIZE_PARAM);
		sampleSize = Integer.parseInt(sampleInfo);
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		String randomInfo = getParameter(RANDOM_BINORMAL_PARAM);
		generator = new RandomBiNormal(randomInfo);
		double vals[][] = generator.generate();
		data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), vals[0]);
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), vals[1]);
		return data;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		theView = new SampleScatterView(data, this, theHorizAxis, theVertAxis, "x", "y");
		return theView;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		sampleButton = new XButton(translate("Take sample"), this);
		thePanel.add(sampleButton);
		
		popnCheck = new XCheckbox(translate("Show population"), this);
		popnCheck.setState(true);
		thePanel.add(popnCheck);
		return thePanel;
	}
	
	protected void frameChanged(DataView theView) {
		if (theView.getCurrentFrame() >= SampleScatterView.SAMP_FRAME)
			popnCheck.enable();
	}

	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			popnCheck.setState(false);
			popnCheck.disable();
			theView.rememberOldSelection();
			NumVariable xVariable = (NumVariable)data.getVariable("x");
			int noOfValues = xVariable.noOfValues();
			boolean selection[] = new boolean[noOfValues];
			int sampleLeft = sampleSize;
			for (int popnLeft=noOfValues ; popnLeft>0 ; popnLeft--) {
				selection[popnLeft - 1] = propnGenerator.generateOne() <= ((double)sampleLeft) / popnLeft;
				if (selection[popnLeft - 1])
					sampleLeft --;
			}
			synchronized(data) {
				theView.doSamplingAnimation(selection);
			}
			return true;
		}
		else if (target == popnCheck) {
			int newFrame = popnCheck.getState() ? SampleScatterView.POPN_FRAME
															: SampleScatterView.SAMP_FRAME;
			theView.setFrame(newFrame);
			repaint();
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}