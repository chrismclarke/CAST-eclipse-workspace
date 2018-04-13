package cast.exercise;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import cast.utils.*;


public class OneTopic extends JPanel {
	static final private Color kTopicBorder = new Color(0xAAAAAA);
	static final private Color kTopicHilite = Color.yellow;
	
	private DomTopic topic;
	private ExerciseListFrame listFrame;
	
	private JLabel nameLabel;
	private boolean showingExercises = false;
	
	private JPanel exerciseList;
	private DomExercise[] exercises = null;
	private OneExercise[] exercisePanels = null;
	private int selectedExercise = -1;
	
	public OneTopic(final DomTopic topic, final ExerciseListFrame listFrame) {
		this.listFrame = listFrame;
		this.topic = topic;
		
		setLayout(new BorderLayout(0, 0));
		setOpaque(false);
//		setBackground(Color.white);
//		setBorder(BorderFactory.createLineBorder(kTopicBorder));
		
			nameLabel = new JLabel(topic.getLongName(), JLabel.LEFT);
				Border blackline = BorderFactory.createLineBorder(kTopicBorder);
				Border spacingBorder = BorderFactory.createEmptyBorder(2, 5, 2, 5);
				nameLabel.setBorder(BorderFactory.createCompoundBorder(blackline, spacingBorder));
				nameLabel.setOpaque(true);
				nameLabel.setBackground(Color.white);
				nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
				nameLabel.addMouseListener(new MouseAdapter() {  
														public void mouseReleased(MouseEvent e) {
															listFrame.highlightTopic(OneTopic.this);
														}  
													}); 
			
		add("North", nameLabel);
		
			exerciseList = new JPanel();
			exerciseList.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 6));
			exerciseList.setBorder(BorderFactory.createEmptyBorder(6, 20, 0, 0));
			exerciseList.setVisible(false);
			
		add("Center", exerciseList);
	}
	
	public DomExercise getSelectedExercise() {
		if (selectedExercise >= 0 && exercises != null)
			return exercises[selectedExercise];
		else
			return null;
	}
	
	private void updateExerciseList() {
		exerciseList.removeAll();
		exerciseList.setVisible(showingExercises);
		exercises = null;
		exercisePanels = null;
		
		if (showingExercises) {
			int nExercises = topic.noOfExercises();
			
			exercises = new DomExercise[nExercises];
			exercisePanels = new OneExercise[nExercises];
			
			for (int i=0 ; i<nExercises ; i++) {
				exercises[i] = topic.getExercise(i);
				exercisePanels[i] = new OneExercise(exercises[i], this);
				exerciseList.add(exercisePanels[i]);
			}
		}
		
		exerciseList.invalidate();
		validate();
		repaint();
	}
	
	public void highlightExercise(OneExercise exercise) {
		int exerciseIndex = -1;
		for (int i=0 ; i<exercisePanels.length ; i++)
			if (exercisePanels[i] == exercise)
				exerciseIndex = i;
		if (exerciseIndex != selectedExercise) {
			if (selectedExercise >= 0)
				exercisePanels[selectedExercise].setHilite(false);
			if (exerciseIndex >= 0)
				exercisePanels[exerciseIndex].setHilite(true);
			selectedExercise = exerciseIndex;
			listFrame.updateVariationList();
		}
	}
	
	public void setHilite(boolean showExercises) {
		nameLabel.setBackground(showExercises ? kTopicHilite : Color.white);
		selectedExercise = -1;
		showingExercises = showExercises;
		updateExerciseList();
	}
	
	public Insets getInsets() {
		return new Insets(2, 10, 2, 10);
	}
}
