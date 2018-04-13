package exercise2;

import java.awt.*;

import dataView.*;
import images.*;


public class MarkCanvas extends XPanel implements ExerciseConstants {
	
	static final public int kAnswerSize = 36;
	static final private int kMaxWait = 30000;		//		30 seconds
	
	static public Image wrong, correct, close;
	static public boolean loadedCrossAndTick = false;
	
	private int answerMark = ANS_UNCHECKED;
	
	public MarkCanvas(XApplet applet) {
		MediaTracker tracker = new MediaTracker(applet);
		
		wrong = CoreImageReader.getImage("wrong.png");
		tracker.addImage(wrong, 0);
		correct = CoreImageReader.getImage("correct.png");
		tracker.addImage(correct, 0);
		close = CoreImageReader.getImage("close.png");
		tracker.addImage(close, 0);
		
		try {
			tracker.waitForAll(kMaxWait);
		} catch (InterruptedException e) {
		}
	}
	
	public void setIcon(int newAnswerMark) {
		if (newAnswerMark != answerMark) {
			answerMark = newAnswerMark;
			repaint();
		}
	}
	
	public int getDisplayedIcon() {
		return answerMark;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int answerHoriz = (getSize().width - kAnswerSize) / 2;
		int answerVert = (getSize().height - kAnswerSize) / 2;
		if (answerMark == ANS_UNCHECKED || answerMark == ANS_INVALID) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getSize().width, getSize().height);
		}
		else if (answerMark == ANS_CORRECT)
			g.drawImage(correct, answerHoriz, answerVert, this);
		else if (answerMark == ANS_CLOSE)
			g.drawImage(close, answerHoriz, answerVert, this);
		else				//		ANS_WRONG or higher
			g.drawImage(wrong, answerHoriz, answerVert, this);
	}
	
	public Dimension getMinimumSize() {
		return new Dimension(kAnswerSize, kAnswerSize);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}