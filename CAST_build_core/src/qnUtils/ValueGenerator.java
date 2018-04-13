package qnUtils;

import java.util.*;

import dataView.*;
import random.*;


public class ValueGenerator {
	private double value[];
	private RandomUniform valueGenerator;
	
	public ValueGenerator(XApplet applet, String parameterName) {
		StringTokenizer st = new StringTokenizer(applet.getParameter(parameterName));
		value = new double[st.countTokens()];
		for (int i=0 ; i<value.length ; i++)
			value[i] = Double.parseDouble(st.nextToken());
		valueGenerator = new RandomUniform(1, 0, value.length - 1);
	}
	
	public double getNewValue() {
		return value[valueGenerator.generateOne()];
	}
}