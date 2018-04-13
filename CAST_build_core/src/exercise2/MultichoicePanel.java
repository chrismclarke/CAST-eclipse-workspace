package exercise2;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import dataView.*;
import utils.*;
import random.*;


abstract public class MultichoicePanel extends XPanel implements ActionListener, ExerciseConstants, StatusInterface {
	public ExerciseApplet exerciseApplet;		//	must be public to allow ResidPlotChoicePanel's inner class to access it
	
	private int nItems, nRows;
	protected int correctChoice;
	
	protected OptionInformation optionInfo[];
	
	private JRadioButton button[];
	private JRadioButton clearButton;		//	not displayed, but selecting it clears other buttons
	protected Component option[];
	
	private String unselectedString = "You must select an option by clicking on a radio button.";
	
	public MultichoicePanel(ExerciseApplet exerciseApplet, int nItems, int nColumns) {
		this.exerciseApplet = exerciseApplet;
		this.nItems = nItems;
		nRows = (nItems + nColumns - 1) / nColumns;
	}
	
	public MultichoicePanel(ExerciseApplet exerciseApplet, int nItems) {
		this(exerciseApplet, nItems, 1);
	}
	
	public void setUnselectedString(String unselectedString) {
		this.unselectedString = unselectedString;
	}
	
	public void setFont(Font f) {
		super.setFont(f);
		if (option != null)
			for (int i=0 ; i<option.length ; i++)
				option[i].setFont(f);
	}
	
	public String getStatus() {
		return String.valueOf(getSelectedOption());
	}
	
	public void setStatus(String status) {
		int selection = Integer.parseInt(status);
		if (selection >= 0)
			button[selection].setSelected(true);
		else
			clearRadioButtons();
	}
	
	protected void setupPanel() {
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		
    ButtonGroup group = new ButtonGroup();
    button = new JRadioButton[nItems];
    option = new Component[nItems];
		
		GridBagConstraints rc = new GridBagConstraints();
		rc.anchor = GridBagConstraints.CENTER;
		rc.fill = GridBagConstraints.NONE;
		rc.gridheight = rc.gridwidth = 1;
		rc.gridx = 0;
		rc.insets = new Insets(0,0,0,5);
		rc.ipadx = rc.ipady = 0;
		rc.weightx = rc.weighty = 0.0;
		
		GridBagConstraints vc = new GridBagConstraints();
		vc.anchor = GridBagConstraints.CENTER;
		vc.fill = GridBagConstraints.BOTH;
		vc.gridheight = vc.gridwidth = 1;
		vc.gridx = 1;
		vc.insets = new Insets(3,0,3,5);
		vc.ipadx = vc.ipady = 0;
		vc.weightx = vc.weighty = 1.0;
		
		rc.gridy = 0;
		vc.gridy = 0;
		for (int i=0 ; i<nItems ; i++) {
			button[i] = new JRadioButton();
			button[i].setOpaque(false);
			button[i].addActionListener(this);
			button[i].setActionCommand(String.valueOf(i));
			
			add(button[i]);
			gbl.setConstraints(button[i], rc);
			group.add(button[i]);
			
			option[i] = createOptionPanel(i, exerciseApplet);
			add(option[i]);
			gbl.setConstraints(option[i], vc);
			
			updateConstraints(rc, vc);
		}
		
		clearButton = new JRadioButton();
		clearButton.setActionCommand("-1");
		group.add(clearButton);
	}
	
	private void updateConstraints(GridBagConstraints rc, GridBagConstraints vc) {
		rc.gridy ++;
		vc.gridy ++;
		if (rc.gridy >= nRows) {
			rc.gridx += 2;
			vc.gridx += 2;
			rc.gridy = vc.gridy = 0;
		}
	}
	
	public void showAnswer() {
		button[correctChoice].setSelected(true);
	}
	
	public int checkCorrect() {
		if (getSelectedOption() < 0)
			return ANS_INCOMPLETE;
		else if (button[correctChoice].isSelected())
			return ANS_CORRECT;
		else
			return ANS_WRONG;
	}
	
	public void clearRadioButtons() {
		clearButton.setSelected(true);
	}
	
	public int getSelectedOption() {
		for (int i=0 ; i<button.length ; i++)
			if (button[i].isSelected())
				return i;
		return -1;
	}
	
	protected void randomiseOptions() {
		randomiseOptions(optionInfo.length);
	}
	
	protected void randomiseOptions(int nDisplayed) {
		RandomInteger rand = new RandomInteger(0, optionInfo.length - 1, optionInfo.length);
		rand.setSeed(exerciseApplet.nextSeed());
		int[] swap = rand.generate();
		for (int i=0 ; i<optionInfo.length ; i++)
			if (i != swap[i]) {
				OptionInformation temp = optionInfo[i];
				optionInfo[i] = optionInfo[swap[i]];
				optionInfo[swap[i]] = temp;
			}
		
		if (nDisplayed < optionInfo.length)
			for (int i=nDisplayed ; i<optionInfo.length ; i++)
				if (optionInfo[i].isCorrect()) {
					rand.setMinMax(0, nDisplayed - 1);
					int j = rand.generateOne();
					OptionInformation temp = optionInfo[i];
					optionInfo[i] = optionInfo[j];
					optionInfo[j] = temp;
				}
	}
	
	protected void sortOptions() {
		for (int i=0 ; i<optionInfo.length ; i++)
			for (int j=i ; j>0 ; j--)
				if (optionInfo[j].lessThan(optionInfo[j-1])) {
					OptionInformation temp = optionInfo[j];
					optionInfo[j] = optionInfo[j-1];
					optionInfo[j-1] = temp;
				}
	}
	
	protected void findCorrectOption() {
		for (int i=0 ; i<optionInfo.length ; i++)
			if (optionInfo[i].isCorrect())
				correctChoice = i;
	}
	
	abstract protected Component createOptionPanel(int optionIndex, ExerciseApplet exerciseApplet);
	
	public String getSelectedOptionMessage() {
		int selectedIndex = getSelectedOption();
		if (selectedIndex < 0)
			return unselectedString;
		else
			return optionInfo[selectedIndex].getMessageString();
	}
	
	public String getCorrectOptionMessage() {
		return optionInfo[correctChoice].getMessageString();
	}
	
	public void actionPerformed(ActionEvent e) {
		exerciseApplet.noteChangedWorking();
	}
	
}