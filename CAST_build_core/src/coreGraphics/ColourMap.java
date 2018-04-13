package coreGraphics;

import java.awt.*;

import dataView.*;

public class ColourMap {
	
	private Color keyColor[];
	private double keyValue[];

	public ColourMap(Color[] keyColor, double[] keyValue) {
		this.keyColor = keyColor;
		this.keyValue = keyValue;
	}
	
	public Color getColour(double fit) {
		fit = Math.max(keyValue[0], Math.min(keyValue[keyValue.length - 1], fit));
		
		for (int i=0 ; i<keyValue.length-1 ;i++)
			if (fit <= keyValue[i + 1]) {
				double p = (fit - keyValue[i]) / (keyValue[i + 1] - keyValue[i]);
				return DataView.mixColors(keyColor[i + 1], keyColor[i], p);
			}
		return Color.lightGray;
	}
}