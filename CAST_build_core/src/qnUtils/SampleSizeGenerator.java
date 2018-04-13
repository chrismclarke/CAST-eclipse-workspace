package qnUtils;

import java.util.*;

import dataView.*;
import random.*;


public class SampleSizeGenerator {
	static final protected String SAMPLE_SIZE_PARAM = "sampleSize";
	
	private int sampleSize[];
	private RandomUniform sampleSizeGenerator;
	
	public SampleSizeGenerator(XApplet applet) {
		StringTokenizer sizes = new StringTokenizer(applet.getParameter(SAMPLE_SIZE_PARAM));
		sampleSize = new int[sizes.countTokens()];
		for (int i=0 ; i<sampleSize.length ; i++)
			sampleSize[i] = Integer.parseInt(sizes.nextToken());
		sampleSizeGenerator = new RandomUniform(1, 0, sampleSize.length - 1);
	}
	
	public int getNewSampleSize() {
		return sampleSize[sampleSizeGenerator.generateOne()];
	}
}