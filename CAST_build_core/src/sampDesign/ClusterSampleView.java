package sampDesign;

import java.awt.*;

import dataView.*;
import axis.*;


public class ClusterSampleView extends DataView {
//	static public final String CLUSTER_SAMPLE_PLOT = "clusterSamplePlot";
	
//	static final private int kHalfHiliteWidth = 4;
	static final private Color kCrossColor[] = {Color.black, new Color(0xCC0000),
											Color.blue, new Color(0xCC33FF), new Color(0x006600),
											new Color(0xFF6600), new Color(0x990099)};
	
	private TimeAxis timeAxis;
	private VertAxis numAxis;
	
	private String yKey;
	private SummaryDataSet summaryData;
	
	public ClusterSampleView(DataSet theData, XApplet applet, TimeAxis timeAxis, VertAxis numAxis,
																														String yKey, SummaryDataSet summaryData) {
		super(theData, applet, new Insets(5, 5, 5, 5));
																//		5 pixels round for crosses to overlap into
		this.timeAxis = timeAxis;
		this.numAxis = numAxis;
		this.yKey = yKey;
		this.summaryData = summaryData;
	}
	
	protected Point getScreenPoint(int clusterIndex, double theVal, int offset, Point thePoint) {
		if (Double.isNaN(theVal))
			return null;
		else
			try {
				int vertPos = numAxis.numValToPosition(theVal);
				int horizPos = timeAxis.timePosition(clusterIndex);
				return translateToScreen(horizPos + offset, vertPos, thePoint);
			} catch (AxisException ex) {
				return null;
			}
	}
	
	public void paintView(Graphics g) {
		ClusterSampleVariable variable = (ClusterSampleVariable)getVariable(yKey);
		int sampleSize = variable.getSampleSize();
		int highlightSample = variable.getHighlightSample();
		
		int separation = 0;
		int clusterPerSamp = variable.getClustersPerSample();
		
		Point thePoint = null;
		
		if (highlightSample >= 0)
			try {
				int lowHorizPos = timeAxis.timePositionBefore(highlightSample);
				int highHorizPos = timeAxis.timePositionBefore(highlightSample + 1);
				separation = highHorizPos - lowHorizPos;
				thePoint = translateToScreen(lowHorizPos, 0, thePoint);
				g.setColor(Color.yellow);
				g.fillRect(thePoint.x, 0, highHorizPos - lowHorizPos, getSize().height);
			} catch (AxisException e) {
			}
		
		int noOfSamples = variable.getNoOfClusters() / variable.getClustersPerSample();
		setCrossSize(noOfSamples < 25 ? MEDIUM_CROSS
									: noOfSamples < 50 ? SMALL_CROSS
									: DOT_CROSS);
		
		int scale = Math.min(getCrossSize() * 2 - 1, separation / (clusterPerSamp + 3));
		int offset[] = new int[clusterPerSamp];
		for (int i=0 ; i<clusterPerSamp ; i++)
			offset[i] = scale * i - (scale * (clusterPerSamp - 1)) / 2;
		
		ValueEnumeration e = variable.values();
		int index = 0;
		int colorIndex = -1;
		int clusterSize = variable.getClusterSize();
		while (e.hasMoreValues()) {
			if (index % clusterSize == 0) {
				colorIndex = (colorIndex + 1) % clusterPerSamp;
				g.setColor(kCrossColor[colorIndex]);
			}
			double y = e.nextDouble();
			thePoint = getScreenPoint(index / sampleSize, y, offset[colorIndex], thePoint);
			if (thePoint != null)
				drawCross(g, thePoint);
			index ++;
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		try {
			Point hitPos = translateFromScreen(x, y, null);
			int clusterIndex = timeAxis.positionToIndex(hitPos.x);
			
			ClusterSampleVariable variable = (ClusterSampleVariable)getVariable(yKey);
		
			int noOfSamples = variable.getNoOfClusters() / variable.getClustersPerSample();
			if (clusterIndex >= noOfSamples)
				return null;
			return new IndexPosInfo(clusterIndex);
		} catch (AxisException e) {
			return null;
		}
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		if (startPos != null) {
			IndexPosInfo posInfo = (IndexPosInfo)startPos;
			summaryData.setSelection(posInfo.itemIndex);
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		startDrag(endPos);
	}
}
	
