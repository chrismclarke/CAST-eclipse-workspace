package graphics;

import java.util.*;

import dataView.*;
import axis.*;


public class DynamicVertAxis extends VertAxis implements Runnable {
	static final private int kNoOfFrames = 40;
	static final private int kFramesPerSec = 14;
	
	private DataView theView;
	private double startMin, endMin, startMax, endMax;
	
	private int endFrame, currentFrame;
	private Thread runner = null;
	
	public DynamicVertAxis(XApplet applet, double startMin, double endMin,
											double startMax, double endMax) {
		super(applet);
		this.startMin = startMin;
		this.endMin = endMin;
		this.startMax = startMax;
		this.endMax = endMax;
	}
	
	public void setLinkedView(DataView theView) {
		this.theView = theView;
	}
	
	public void animateFrames(boolean startToEnd) {
		pause();
		currentFrame = startToEnd ? 0 : kNoOfFrames;
		endFrame = kNoOfFrames - currentFrame;
		restart();
	}
	
	protected void setupAxis() {
		minOnAxis = (currentFrame * endMin + (kNoOfFrames - currentFrame) * startMin) / kNoOfFrames;
		maxOnAxis = (currentFrame * endMax + (kNoOfFrames - currentFrame) * startMax) / kNoOfFrames;
		minPower = minOnAxis;
		maxPower = maxOnAxis;
		powerRange = (maxPower - minPower);
		
		Enumeration e = labels.elements();
		while (e.hasMoreElements()) {
			AxisLabel nextLabel = (AxisLabel)e.nextElement();
			double labelValue = ((NumValue)nextLabel.label).toDouble();
			nextLabel.position = (labelValue - minPower) / powerRange;
		}
		
//		labels.removeAllElements();
		
		repaint();
		theView.repaint();
	}
	
	public void restart() {
		if (runner == null && currentFrame != endFrame) {
			runner = new Thread(this);
			runner.start();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void pause() {
		if (runner != null) {
			runner.stop();
			runner = null;
		}
	}
	
	public void run() {
		try {
			for ( ; currentFrame != endFrame
						; currentFrame = ((currentFrame<endFrame) ? (currentFrame+1) : (currentFrame-1))) {
					setupAxis();
					Thread.sleep(1000 / kFramesPerSec);
				}
			setupAxis();
		} catch (InterruptedException e) {
			System.out.println("Animation interrupted: " + e);
		}
		runner = null;
	}

//-----------------------------------------------------------------------------------
}