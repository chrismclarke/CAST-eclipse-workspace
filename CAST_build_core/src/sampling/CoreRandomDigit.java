package sampling;

import java.awt.*;

import dataView.*;
import random.*;


abstract public class CoreRandomDigit extends BufferedCanvas implements Runnable {
	private RandDigActionInterface digitAction;
	protected int maxDigit;
	
	protected RandomInteger generator;
	protected int oldDigit, currentDigit;
	
	protected int finalFrame, currentFrame;
	
	private int millisecPerFrame;
	private Thread runner = null;
	
	public CoreRandomDigit(int maxDigit, XApplet applet, RandDigActionInterface digitAction, long seed) {
		super(applet);
		this.digitAction = digitAction;
		this.maxDigit = maxDigit;
		generator = new RandomInteger(0, maxDigit, 1, seed);
		oldDigit = currentDigit = 0;
		
		repaint();
	}
	
	public CoreRandomDigit(XApplet applet, RandDigActionInterface digitAction, long seed) {
		this(9, applet, digitAction, seed);
	}
	
	public int getDigit() {
		return currentDigit;
	}
	
//---------------------------------------------------------------------
	
	public void animateNextDigit(int millisecPerFrame) {
		pause();
		
		digitAction.noteClearedDigit(this);
		
		setupNewDigit();
		
		currentFrame = 0;
		this.millisecPerFrame = millisecPerFrame;
		restart();
	}
	
	abstract protected void setupNewDigit();
	
	public void instantNextDigit() {
		pause();
		
		setupNewDigit();
		
		currentFrame = finalFrame;
		repaint();
	}
	
	public void restart() {
		if (runner == null && currentFrame != finalFrame) {
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
			for ( ; currentFrame <= finalFrame ; currentFrame++) {
					repaint();
					Thread.sleep(getNextDelay());
				}
			repaint();
		} catch (InterruptedException e) {
		}
		runner = null;
		digitAction.noteNewDigit(this);
	}
	
	protected int getNextDelay() {
		return millisecPerFrame;
	}
	
//---------------------------------------------------------------------

	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		return false;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}