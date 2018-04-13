package qnUtils;

import java.awt.*;

import dataView.*;
import imageGroups.*;


public class QuestionPanel extends XPanel {
	static final private int kMaxQuestions = 10;
	
	private QuestionLayout theLayout;
	
	private int noOfQuestions = 0;
	private String questions[] = new String[kMaxQuestions];
	private String range[] = new String[kMaxQuestions];
	
	private int qnIndex = 0;
	
	public QuestionPanel(XApplet theApplet, String baseQnParam, String baseRangeParam) {
		readQuestions(theApplet, baseQnParam, baseRangeParam);
		theLayout = new QuestionLayout(questions[0]);
		setLayout(theLayout);
	}
	
	private void readQuestions(XApplet theApplet, String baseQnParam, String baseRangeParam) {
		if (readOneQuestion(theApplet, baseQnParam, baseRangeParam, ""))
			for (int i=2 ; i<kMaxQuestions ; i++)
				if (!readOneQuestion(theApplet, baseQnParam, baseRangeParam, String.valueOf(i)))
					break;
	}
	
	private boolean readOneQuestion(XApplet theApplet, String baseQnParam, String baseRangeParam, String suffix) {
		String qn = theApplet.getParameter(baseQnParam + suffix);
		String paramRange = theApplet.getParameter(baseRangeParam + suffix);
		if (qn != null) {
			questions[noOfQuestions] = qn;
			range[noOfQuestions] = paramRange;
			noOfQuestions ++;
			return true;
		}
		else
			return false;
	}

	public Insets insets() {
		return new Insets(2, 5, 2, 5);
	}
	
	private void drawTextAroundComponents(Graphics g) {
		Insets insets = insets();
		
		g.setColor(getBackground());
		g.fillRect(0, 0, getSize().width, getSize().height);
		Graphics2DActions.setAliasing(g, Graphics2DActions.ANTI_ALIASING_ON);
		g.setColor(getForeground());
		
		QuestionTokenizer wordTokenizer = theLayout.getTokenizer(this, g, insets);
		int baselineFromTop = theLayout.getBaselineFromTop(g);
		
		while (wordTokenizer.hasMoreWords()) {
			TextWord word = wordTokenizer.nextWord();
			if (word != null) {
				if (word.picture != null)
					g.drawImage(word.picture, insets.left + word.horizPos,
									word.lineTop + baselineFromTop - MeanSDImages.kParamAscent, this);
				g.drawString(word.word, insets.left + word.horizPos + word.pictureWidth
														+ word.editWidth, word.lineTop + baselineFromTop);
			}
		}
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawTextAroundComponents(g);
	}
	
	public String getValueString(String key) {
		return theLayout.getValueString(key);
	}
	
	public String[] getValueStrings() {
		return theLayout.getValueStrings();
	}
	
	public String getNextRange() {
		int nextIndex = qnIndex + 1;
		if (nextIndex >= noOfQuestions)
			nextIndex = 0;
		return range[nextIndex];
	}
	
	public void showNextQuestion(String[] paramValue) {
		qnIndex ++;
		if (qnIndex >= noOfQuestions)
			qnIndex = 0;
			
		theLayout.changeQuestionText(questions[qnIndex], paramValue);
		doLayout();
		Graphics g = getGraphics();
		g.setColor(getBackground());
		g.fillRect(0, 0, getSize().width, getSize().height);
		g.setColor(getForeground());
		drawTextAroundComponents(g);
	}
}