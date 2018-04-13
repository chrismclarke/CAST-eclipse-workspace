package loess;

import java.util.*;

import dataView.*;


public class SlugGenerator extends Random {
	static final private String X_GENERATOR_PARAM = "xGenerator";
	static final private String Y_GENERATOR_PARAM = "yGenerator";
	static final private String RANDOM_SEED_PARAM = "randomSeed";
	
	private int noOfSegments;
	private int segmentCount[];
	private double segmentMinX[], segmentMaxX[], errorSD[];
	private double slope, intercept;
	
	private double xValues[], yValues[];
	
	public SlugGenerator(XApplet applet) {
		String randomSeedString = applet.getParameter(RANDOM_SEED_PARAM);
		if (randomSeedString != null)
			setSeed(Long.parseLong(randomSeedString));
		
		StringTokenizer st = new StringTokenizer(applet.getParameter(X_GENERATOR_PARAM));
		noOfSegments = Integer.parseInt(st.nextToken());
		segmentCount = new int[noOfSegments];
		segmentMinX = new double[noOfSegments];
		segmentMaxX = new double[noOfSegments];
		errorSD = new double[noOfSegments];
		int totalCount = 0;
		for (int i=0 ; i<noOfSegments ; i++) {
			segmentCount[i] = Integer.parseInt(st.nextToken());
			totalCount += segmentCount[i];
			segmentMinX[i] = Double.parseDouble(st.nextToken());
			segmentMaxX[i] = Double.parseDouble(st.nextToken());
		}
		
		st = new StringTokenizer(applet.getParameter(Y_GENERATOR_PARAM));
		intercept = Double.parseDouble(st.nextToken());
		slope = Double.parseDouble(st.nextToken());
		for (int i=0 ; i<noOfSegments ; i++)
			errorSD[i] = Double.parseDouble(st.nextToken());
		
		xValues = new double[totalCount];
		yValues = new double[totalCount];
	}
	
	public void generateNextSample() {
		int dataIndex = 0;
		for (int i=0 ; i<noOfSegments ; i++) {
			double min = segmentMinX[i];
			double range = segmentMaxX[i] - min;
			int n = segmentCount[i];
			double sd = errorSD[i];
			for (int j=0 ; j<n ; j++) {
				double x = min + nextDouble() * range;
				double y = intercept + slope * x + nextGaussian() * sd;
				xValues[dataIndex] = Math.exp(x);
				yValues[dataIndex] = Math.exp(y);
				dataIndex ++;
			}
		}
	}
	
	public double[] getXValues() {
		return xValues;
	}
	
	public double[] getYValues() {
		return yValues;
	}
}