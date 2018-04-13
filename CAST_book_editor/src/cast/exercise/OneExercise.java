package cast.exercise;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


public class OneExercise extends JPanel {
	static final private Color kExerciseBorder = new Color(0xAAAAAA);
	static final private Color kExerciseHilite = new Color(0xFFCCCC);
	
	public OneExercise(final DomExercise exercise, final OneTopic topicPanel) {
		setLayout(new BorderLayout(0, 0));
		setBackground(Color.white);
		setBorder(BorderFactory.createLineBorder(kExerciseBorder));
			
			JTextArea textArea = new JTextArea(exercise.getDescription());
			textArea.setOpaque(false);
			textArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			textArea.setEditable(false);
			textArea.setEnabled(false);
			textArea.setDisabledTextColor(Color.black);
			textArea.addMouseListener(new MouseAdapter() {  
													public void mouseReleased(MouseEvent e) {
														topicPanel.highlightExercise(OneExercise.this);
													}  
												});
			
		add("Center", textArea);
	}
	
	public void setHilite(boolean doHilite) {
		setBackground(doHilite ? kExerciseHilite : Color.white);
		repaint();
	}
	
	public Insets getInsets() {
		return new Insets(2, 10, 2, 10);
	}
}
