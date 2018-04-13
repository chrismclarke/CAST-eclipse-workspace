package cast.exercise;

import java.awt.*;
import java.util.*;

import javax.swing.*;


abstract public class OneCoreVariation extends JPanel {
	protected DomVariation variation;
	protected ExerciseListFrame mainFrame;
	
	protected JButton editButton;
	protected JTextArea textArea;
	
	public OneCoreVariation(DomVariation variation, ExerciseListFrame mainFrame) {
		this.variation = variation;
		this.mainFrame = mainFrame;
		
		setLayout(new BorderLayout(0, 0));
		setBorder(BorderFactory.createLineBorder(Color.black));
			
			textArea = new JTextArea();
			textArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			textArea.setEditable(false);
			textArea.setEnabled(false);
			textArea.setDisabledTextColor(Color.black);
			textArea.setOpaque(false);
			
		add("Center", textArea);
	}
	
	abstract protected String getLongVariationName();
	abstract protected Color getLineColor();
	
	protected void initialise() {
		textArea.setText(getLongVariationName());
	}
	
	public Insets getInsets() {
		return new Insets(2, 10, 2, 10);
	}
	
	
	public void finishedEdit(DomVariation variation) {
		if (editButton != null)
			editButton.setEnabled(true);
		textArea.setText(getLongVariationName());
	}
	
	public Vector getCustomVariations() {
		DomCustomVariations customVariations = mainFrame.getCustomVariations();
		if (customVariations == null)
			return null;
		String topicName = variation.getExercise().getTopic().getTopicName();
		String exerciseName = variation.getExercise().getName();
		return customVariations.getVariations(topicName, exerciseName);
	}
}
