package exercise2;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import dataView.*;
import utils.*;
import images.*;


abstract public class TrueFalsePanel extends XPanel implements ActionListener, ExerciseConstants, StatusInterface {
	static final private String kIconDirectory = "trueFalse/";
	static final private String kIconSuffix = ".png";
	
	protected ExerciseApplet exerciseApplet;
	
	private boolean isCorrect, isBasicBox;
	private JCheckBox checkButton;
	
	private String falseIcon, falseRolloverIcon, trueIcon, trueRolloverIcon, trueFalseIcon;
	
	public TrueFalsePanel(ExerciseApplet exerciseApplet, boolean isCorrect, boolean isBasicBox) {
		this.exerciseApplet = exerciseApplet;
		this.isCorrect = isCorrect;
		this.isBasicBox = isBasicBox;
		setIcons("falseIcon", "falseRolloverIcon", "trueIcon", "trueRolloverIcon", "trueFalseIcon") ;
	}
	

	protected void setIcons(String falseIcon, String falseRolloverIcon, String trueIcon,
																									String trueRolloverIcon, String trueFalseIcon) {
		this.falseIcon = falseIcon;
		this.falseRolloverIcon = falseRolloverIcon;
		this.trueIcon = trueIcon;
		this.trueRolloverIcon = trueRolloverIcon;
		this.trueFalseIcon = trueFalseIcon;
	}
	
	public void showAnswer() {
		checkButton.setSelected(isCorrect);
	}
	
	public int checkCorrect() {
		if (checkButton.isSelected() == isCorrect)
			return ANS_CORRECT;
		else
			return ANS_WRONG;
	}
	
	public void setState(boolean selected) {
		checkButton.setSelected(selected);
	}
	
	public boolean isSelected() {
		return checkButton.isSelected();
	}
	
	public void setCorrect(boolean isCorrect) {
		this.isCorrect = isCorrect;
	}
	
	public String getStatus() {
		return isSelected() ? "true" : "false";
	}
	
	public void setStatus(String status) {
		setState(status.equals("true"));
	}
	
	protected void setupPanel() {
		setLayout(new BorderLayout(3, 0));
		
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
			
				checkButton = new JCheckBox();
				
				if (!isBasicBox) {
						Image falseImage = CoreImageReader.getImage(kIconDirectory + falseIcon + kIconSuffix);
					checkButton.setIcon(new ImageIcon(falseImage));
						Image falseRolloverImage = CoreImageReader.getImage(kIconDirectory + falseRolloverIcon + kIconSuffix);
					checkButton.setRolloverIcon(new ImageIcon(falseRolloverImage));

						Image trueImage = CoreImageReader.getImage(kIconDirectory + trueIcon + kIconSuffix);
					checkButton.setSelectedIcon(new ImageIcon(trueImage));
						Image trueRolloverImage = CoreImageReader.getImage(kIconDirectory + trueRolloverIcon + kIconSuffix);
					checkButton.setRolloverSelectedIcon(new ImageIcon(trueRolloverImage));

						Image trueFalseImage = CoreImageReader.getImage(kIconDirectory + trueFalseIcon + kIconSuffix);
					checkButton.setPressedIcon(new ImageIcon(trueFalseImage));
				}
				checkButton.setOpaque(false);
				checkButton.addActionListener(this);
			
			buttonPanel.add(checkButton);
		
		add("West", buttonPanel);
		add("Center", createStatement(exerciseApplet));
	}
	
	abstract protected Component createStatement(ExerciseApplet exerciseApplet);
	
	public void actionPerformed(ActionEvent e) {
		exerciseApplet.noteChangedWorking();
	}
	
}