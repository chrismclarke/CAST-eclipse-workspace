package cast.variableEditors;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import cast.exercise.*;
import cast.variationEditor.*;

public class AnswerVariableEditor extends CoreVariableEditor {
	
	private class AnswerValueEditor extends CoreValueEditor {
		private JTextField minEditor, bestEditor, maxEditor;
		
		public AnswerValueEditor(JPanel valuePanel, GridBagConstraints minConstraint, GridBagConstraints bestConstraint,
																GridBagConstraints maxConstraint) {
			minEditor = variation.createMonitoredTextField("", 5);
			bestEditor = variation.createMonitoredTextField("", 5);
			maxEditor = variation.createMonitoredTextField("", 5);
			valuePanel.add(minEditor, minConstraint);
			valuePanel.add(bestEditor, bestConstraint);
			valuePanel.add(maxEditor, maxConstraint);
		}
		
		public void setValue(String valueString) {
			StringTokenizer st = new StringTokenizer(valueString);
			minEditor.setText(st.nextToken());
			bestEditor.setText(st.nextToken());
			maxEditor.setText(st.nextToken());
		}
		
		public String getValue() {
			return minEditor.getText() + " " + bestEditor.getText() + " " + maxEditor.getText();
		}
		
		public void clearEditor(JPanel valuePanel) {
			valuePanel.remove(minEditor);
			valuePanel.remove(bestEditor);
			valuePanel.remove(maxEditor);
			minEditor = null;
			bestEditor = null;
			maxEditor = null;
		}
		
		public boolean isValidValue() {
			try {
				double minValue = Double.parseDouble(minEditor.getText());
				double bestValue = Double.parseDouble(bestEditor.getText());
				double maxValue = Double.parseDouble(maxEditor.getText());
				return minValue > 0 && minValue < bestValue && bestValue < maxValue;
			} catch (NumberFormatException e) {
				return false;
			}
		}
	}
	
//-----------------------------------------------------------------------------
	
	private GridBagConstraints minConstraint = new GridBagConstraints(
																										0, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTH,				//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										5, 0);													//	ipadx & ipady
	
	private GridBagConstraints bestConstraint = new GridBagConstraints(
																										1, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTH,				//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										5, 0);													//	ipadx & ipady
	
	private GridBagConstraints maxConstraint = new GridBagConstraints(
																										2, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTH,				//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										5, 0);													//	ipadx & ipady
	
			
	public AnswerVariableEditor(DomVariation variation, VariableType[] validParams, boolean hasIndex) {
		super(variation, validParams, hasIndex);
	}
	
	protected String getBaseType() {
		return "answer";
	}
	
	public void initialiseValuePanel(JPanel valuePanel) {
		valuePanel.setLayout(new GridBagLayout());
		
		minConstraint.gridy = 0;
		JLabel minLabel = new JLabel("Min acceptable", JLabel.CENTER);
		valuePanel.add(minLabel, minConstraint);
		minConstraint.gridy ++;
		
		bestConstraint.gridy = 0;
		JLabel bestLabel = new JLabel("Best value", JLabel.CENTER);
		valuePanel.add(bestLabel, bestConstraint);
		bestConstraint.gridy ++;
		
		maxConstraint.gridy = 0;
		JLabel maxLabel = new JLabel("Max acceptable", JLabel.CENTER);
		valuePanel.add(maxLabel, maxConstraint);
		maxConstraint.gridy ++;
	}
	
	public CoreValueEditor addOneValueEditor(JPanel valuePanel) {
		CoreValueEditor valueEditor = new AnswerValueEditor(valuePanel, minConstraint, bestConstraint, maxConstraint);
		minConstraint.gridy ++;
		bestConstraint.gridy ++;
		maxConstraint.gridy ++;
		
		return valueEditor;
	}
	
	public void removeLastValueEditor(JPanel valuePanel, CoreValueEditor valueEditor) {
		valueEditor.clearEditor(valuePanel);
		minConstraint.gridy --;
		bestConstraint.gridy --;
		maxConstraint.gridy --;
	}
	
	public void createRandomValuePanel(JPanel valuePanel) {
	}
	
	public boolean hasRandomValue() {
		return false;
	}
	
	public void initialiseRandomValue(String initialValue) {		//	no random axes are allowed
	}
	
	public String getRandomValue() {
		return null;
	}
	
	protected boolean isValidRandomValue() {
		return false;
	}
	
	public char arrayDelimiter() {
		return '*';
	}
}
