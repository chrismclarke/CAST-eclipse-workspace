package cast.variableEditors;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import cast.exercise.*;
import cast.variationEditor.*;

public class HistoVariableEditor extends CoreVariableEditor {
	
	private class HistoValueEditor extends CoreValueEditor {
		private JTextField minEditor, stepEditor;
		
		public HistoValueEditor(JPanel valuePanel, GridBagConstraints minConstraint, GridBagConstraints stepConstraint) {
			minEditor = variation.createMonitoredTextField("", 5);
			stepEditor = variation.createMonitoredTextField("", 5);
			valuePanel.add(minEditor, minConstraint);
			valuePanel.add(stepEditor, stepConstraint);
		}
		
		public void setValue(String valueString) {
			if (valueString.length() == 0) {
				minEditor.setText("");
				stepEditor.setText("");
			}
			else {
				StringTokenizer st = new StringTokenizer(valueString);
				minEditor.setText(st.nextToken());
				stepEditor.setText(st.nextToken());
			}
		}
		
		public String getValue() {
			String min = minEditor.getText();
			String step = stepEditor.getText();
			if (min.length() == 0 && step.length() == 0)
				return "";
			else
				return min + " " + step;
		}
		
		public void clearEditor(JPanel valuePanel) {
			valuePanel.remove(minEditor);
			valuePanel.remove(stepEditor);
			minEditor = null;
			stepEditor = null;
		}
		
		public boolean isValidValue() {
			if (getValue().length() == 0 && optionalVariable)
				return true;
			try {
				@SuppressWarnings("unused")
				double minValue = Double.parseDouble(minEditor.getText());
				double stepValue = Double.parseDouble(stepEditor.getText());
				return stepValue > 0;
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
	
	private GridBagConstraints stepConstraint = new GridBagConstraints(
																										1, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTH,				//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										5, 0);													//	ipadx & ipady
	
	
	public HistoVariableEditor(DomVariation variation, VariableType[] validParams, boolean hasIndex) {
		super(variation, validParams, hasIndex);
	}
	
	protected String getBaseType() {
		return "histo";
	}
	
	public void initialiseValuePanel(JPanel valuePanel) {
		valuePanel.setLayout(new GridBagLayout());
		
		minConstraint.gridy = 0;
		JLabel minLabel = new JLabel("Class 1 start", JLabel.CENTER);
		valuePanel.add(minLabel, minConstraint);
		minConstraint.gridy ++;
		
		stepConstraint.gridy = 0;
		JLabel stepLabel = new JLabel("Step", JLabel.CENTER);
		valuePanel.add(stepLabel, stepConstraint);
		stepConstraint.gridy ++;
	}
	
	public CoreValueEditor addOneValueEditor(JPanel valuePanel) {
		CoreValueEditor valueEditor = new HistoValueEditor(valuePanel, minConstraint, stepConstraint);
		minConstraint.gridy ++;
		stepConstraint.gridy ++;
		
		return valueEditor;
	}
	
	public void removeLastValueEditor(JPanel valuePanel, CoreValueEditor valueEditor) {
		valueEditor.clearEditor(valuePanel);
		minConstraint.gridy --;
		stepConstraint.gridy --;
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
