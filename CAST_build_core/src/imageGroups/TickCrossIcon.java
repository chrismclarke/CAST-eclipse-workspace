package imageGroups;

import java.awt.*;

import dataView.*;


public class TickCrossIcon extends XPanel {
	static public final int NO_ANSWER = 0;
	static public final int CORRECT_ANSWER = 1;
	static public final int WRONG_ANSWER = 2;
	static public final int UNKNOWN_ANSWER = 3;
	
	private int answerMark = NO_ANSWER;
	
	public static final int kAnswerSize = 36;
	
	public TickCrossIcon() {
	}
	
	public void setAnswerMark(int newAnswerMark) {
		if (newAnswerMark != answerMark) {
			answerMark = newAnswerMark;
			repaint();
		}
	}
	
	public int getAnswerMark() {
		return answerMark;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		TickCrossImages.loadCrossAndTick(this);
		
		int answerHoriz = (getSize().width - kAnswerSize) / 2;
		int answerVert = (getSize().height - kAnswerSize) / 2;
		if (answerMark == CORRECT_ANSWER)
			g.drawImage(TickCrossImages.tick, answerHoriz, answerVert, this);
		else if (answerMark == WRONG_ANSWER)
			g.drawImage(TickCrossImages.cross, answerHoriz, answerVert, this);
		else if (answerMark == UNKNOWN_ANSWER)
			g.drawImage(TickCrossImages.question, answerHoriz, answerVert, this);
		else {
			g.setColor(getBackground());
			g.fillRect(0, 0, getSize().width, getSize().height);
		}
	}
	
	public Dimension getMinimumSize() {
		return new Dimension(kAnswerSize, kAnswerSize);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}