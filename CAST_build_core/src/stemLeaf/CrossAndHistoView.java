package stemLeaf;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import images.*;


public class CrossAndHistoView extends DataView {
	
	static final public int kBoxedFrame = 40;
	static final public int kHistoIndex = 80;
	
	static final private int kMaxWait = 30000;		//		30 seconds
	
	static final private Color kCrossColor = new Color(0x0000BB);
	static final private Color kHistoFillColor = new Color(0xC8CDEB);
	
	static final public int DATA_ONLY = 0;
	static final public int DATA_AND_DENSITY = 1;
	static final public int DENSITY_ONLY = 2;
	
	private HorizAxis axis;
	private Image backgroundImage;
	private int displayType;
	
	private double class0Start, classWidth;
	
	static Color getShade(int val, int min, int max, Color lowC, Color highC) {
		int red = ((val - min) * highC.getRed() + (max - val) * lowC.getRed()) / (max - min);
		int blue = ((val - min) * highC.getBlue() + (max - val) * lowC.getBlue()) / (max - min);
		int green = ((val - min) * highC.getGreen() + (max - val) * lowC.getGreen()) / (max - min);
		return new Color(red, green, blue);
	}
	
	public CrossAndHistoView(DataSet theData, XApplet applet, HorizAxis axis, String groupingInfo,
																														String backgroundGif, int displayType) {
		super(theData, applet, new Insets(0,0,-1,0));
		this.axis = axis;
		this.displayType = displayType;
		
		StringTokenizer st = new StringTokenizer(groupingInfo);
		class0Start = Double.parseDouble(st.nextToken());
		classWidth = Double.parseDouble(st.nextToken());
		
		if (backgroundGif != null) {
			MediaTracker tracker = new MediaTracker(this);
			backgroundImage = CoreImageReader.getImage(backgroundGif);
			tracker.addImage(backgroundImage, 0);
			try {
				tracker.waitForAll(kMaxWait);
			} catch (InterruptedException e) {
			}
		}
	}
	
	public void setDisplayType(int displayType) {
		this.displayType = displayType;
		repaint();
	}
	
	public void paintView(Graphics g) {
		if (displayType != DATA_ONLY) {
			g.drawImage(backgroundImage, 0, 0, getSize().width, getSize().height, this);
		}
		
		if (displayType != DENSITY_ONLY) {
			NumVariable xVar = getNumVariable();
			NumValue sortedVal[] = xVar.getSortedData();
			
			int classPix = axis.numValToRawPosition(class0Start + classWidth)
																										- axis.numValToRawPosition(class0Start);
			int crossSize = (classPix * 2) / 3;
			int halfCrossSize = crossSize / 2;
			Point p = null;
			Point p0 = null;
			Point p1 = null;
			
			int frame = getCurrentFrame();
			Color crossColor = (frame >= kBoxedFrame) ? null
												: getShade(frame, 0, kBoxedFrame, kCrossColor, Color.white);
			Color horizLineColor = (frame == 0) ? null
												: (frame >= kBoxedFrame) ? getShade(frame, kBoxedFrame, kHistoIndex, Color.black, kHistoFillColor)
												: getShade(frame, 0, kBoxedFrame, Color.white, Color.black);
			Color histoFillColor = (frame < kBoxedFrame) ? null
												: getShade(frame, kBoxedFrame, kHistoIndex, Color.white, kHistoFillColor);
			Color outlineColor = (frame == 0) ? null
												: (frame >= kBoxedFrame) ? Color.black
												: getShade(frame, 0, kBoxedFrame, Color.white, Color.black);
			
			double classBottom = class0Start;
			double classTop = class0Start + classWidth;
			int xBottom = axis.numValToRawPosition(classBottom);
			int xTop = axis.numValToRawPosition(classTop);
			int xCenter = axis.numValToRawPosition(classTop - classWidth / 2);
//			int yCenter = classPix / 2;
			
			int i = 0;
			while (true) {
				if (i >= sortedVal.length)
					break;
				if (sortedVal[i].toDouble() >= classTop) {
					while (sortedVal[i].toDouble() >= classTop) {
						classBottom = classTop;
						classTop += classWidth;
						xBottom = xTop;
						xTop = axis.numValToRawPosition(classTop);
					}
					xCenter = axis.numValToRawPosition(classTop - classWidth / 2);
				}
				
				int noInClass = 0;
				while (i < sortedVal.length && sortedVal[i].toDouble() < classTop) {
					noInClass ++;
					i ++;
				}
				
				int classY = noInClass * classPix;
				p0 = translateToScreen(xBottom, classY, p0);
				p1 = translateToScreen(xTop, 0, p1);
				
				if (histoFillColor != null) {
					g.setColor(histoFillColor);
					g.fillRect(p0.x, p0.y, (p1.x - p0.x), (p1.y - p0.y));
				}
				
				if (crossColor != null) {
					g.setColor(crossColor);
					for (int j=0 ; j<noInClass ; j++) {
						p = translateToScreen(xCenter, classPix / 2 + j * classPix, p);
						g.drawLine(p.x - halfCrossSize, p.y - halfCrossSize, p.x + halfCrossSize,
																																						p.y + halfCrossSize);
						g.drawLine(p.x - halfCrossSize, p.y + halfCrossSize, p.x + halfCrossSize,
																																						p.y - halfCrossSize);
					}
				}
				
				if (horizLineColor != null) {
					g.setColor(horizLineColor);
					for (int j=1 ; j<noInClass ; j++) {
						p = translateToScreen(xBottom, j * classPix, p);
						g.drawLine(p.x, p.y, p1.x, p.y);
					}
				}
				
				if (outlineColor != null) {
					g.setColor(outlineColor);
					g.drawRect(p0.x, p0.y, (p1.x - p0.x), (p1.y - p0.y));
				}
			}
		}
	}
	
//-----------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
}