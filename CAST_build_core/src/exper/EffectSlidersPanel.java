package exper;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import graphics3D.*;


public class EffectSlidersPanel extends XPanel {
	
	public EffectSlidersPanel(DataSet data, String factorKey, String modelKey, int factorIndex,
								String effectAxisString, boolean useGrayColors, boolean keepZeroMeanEffect,
								XApplet applet) {
		this(data, factorKey, modelKey, factorIndex, effectAxisString, useGrayColors,
																								keepZeroMeanEffect, null, null, 0.0, applet);
	}
	
	public EffectSlidersPanel(DataSet data, String factorKey, String modelKey, int factorIndex,
								String effectAxisString, boolean useGrayColors, boolean keepZeroMeanEffect,
								String numAxisInfo, double catToNum[], double xMean, XApplet applet) {
		setLayout(new BorderLayout());
		
			Color axisColor = getFactorColor(factorIndex);
		
			XPanel barPanel = new XPanel();
			barPanel.setLayout(new AxisLayout());
			
				VertAxis effectAxis = new VertAxis(applet);
				effectAxis.readNumLabels(effectAxisString);
				effectAxis.setForeground(axisColor);
			barPanel.add("Left", effectAxis);
			
				HorizAxis factorAxis = new HorizAxis(applet);
				if (numAxisInfo ==  null)
					factorAxis.setCatLabels((CatVariable)data.getVariable(factorKey));
				else
					factorAxis.readNumLabels(numAxisInfo);
				factorAxis.setForeground(axisColor);
			
			barPanel.add("Bottom", factorAxis);
			
				TreatEffectSliderView effectView = new TreatEffectSliderView(data, applet, effectAxis, factorAxis, factorKey,
											modelKey, factorIndex, useGrayColors, catToNum, xMean);
				effectView.setKeepZeroMeanEffect(keepZeroMeanEffect);
				effectView.lockBackground(Color.white);
												
			barPanel.add("Center", effectView);
		
		add("Center", barPanel);
		
			CatVariable factorVar = (CatVariable)data.getVariable(factorKey);
			XLabel effectName = new XLabel("Effect of " + factorVar.name, XLabel.LEFT, applet);
			effectName.setForeground(axisColor);
			effectName.setFont(applet.getStandardBoldFont());
		add("North", effectName);
	}
	
	protected Color getFactorColor(int factorIndex) {
		return (factorIndex == 0) ? D3Axis.axisColor[D3Axis.X_AXIS][D3Axis.FOREGROUND]
									: (factorIndex == 1) ? D3Axis.axisColor[D3Axis.Z_AXIS][D3Axis.FOREGROUND]
									: Color.black;
	}
}