package cast.variableEditors;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import cast.exercise.*;
import cast.variationEditor.*;

public class AxisVariableEditor extends CoreVariableEditor {
	
	private class AxisValueEditor extends CoreValueEditor {
		private JTextField minEditor, maxEditor, startEditor, stepEditor;
		
		public AxisValueEditor(JPanel valuePanel, GridBagConstraints minConstraint,
																GridBagConstraints maxConstraint, GridBagConstraints startConstraint,
																GridBagConstraints stepConstraint) {
			minEditor = variation.createMonitoredTextField("", 5);
			maxEditor = variation.createMonitoredTextField("", 5);
			startEditor = variation.createMonitoredTextField("", 5);
			stepEditor = variation.createMonitoredTextField("", 5);
			valuePanel.add(minEditor, minConstraint);
			valuePanel.add(maxEditor, maxConstraint);
			valuePanel.add(startEditor, startConstraint);
			valuePanel.add(stepEditor, stepConstraint);
		}
		
		public void setValue(String valueString) {
			StringTokenizer st = new StringTokenizer(valueString);
			minEditor.setText(st.nextToken());
			maxEditor.setText(st.nextToken());
			startEditor.setText(st.nextToken());
			stepEditor.setText(st.nextToken());
		}
		
		public String getValue() {
			return minEditor.getText() + " " + maxEditor.getText() + " " + startEditor.getText()
																							+ " " + stepEditor.getText();
		}
		
		public void clearEditor(JPanel valuePanel) {
			valuePanel.remove(minEditor);
			valuePanel.remove(maxEditor);
			valuePanel.remove(startEditor);
			valuePanel.remove(stepEditor);
			minEditor = null;
			maxEditor = null;
			startEditor = null;
			stepEditor = null;
		}
		
		public boolean isValidValue() {
			try {
				double minValue = Double.parseDouble(minEditor.getText());
				double maxValue = Double.parseDouble(maxEditor.getText());
				double startValue = Double.parseDouble(startEditor.getText());
				@SuppressWarnings("unused")
				double stepValue = Double.parseDouble(stepEditor.getText());
				return minValue < maxValue && minValue <= startValue && startValue <= maxValue;
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
	
	private GridBagConstraints maxConstraint = new GridBagConstraints(
																										1, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTH,				//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										5, 0);													//	ipadx & ipady
	
	private GridBagConstraints startConstraint = new GridBagConstraints(
																										2, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTH,				//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										5, 0);													//	ipadx & ipady
	
	private GridBagConstraints stepConstraint = new GridBagConstraints(
																										3, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTH,				//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										5, 0);													//	ipadx & ipady
	
	
	public AxisVariableEditor(DomVariation variation, VariableType[] validParams, boolean hasIndex) {
		super(variation, validParams, hasIndex);
	}
	
	protected String getBaseType() {
		return "axis";
	}
	
	public void initialiseValuePanel(JPanel valuePanel) {
		valuePanel.setLayout(new GridBagLayout());
		
		minConstraint.gridy = 0;
		JLabel minLabel = new JLabel("Min", JLabel.CENTER);
		valuePanel.add(minLabel, minConstraint);
		minConstraint.gridy ++;
		
		maxConstraint.gridy = 0;
		JLabel maxLabel = new JLabel("Max", JLabel.CENTER);
		valuePanel.add(maxLabel, maxConstraint);
		maxConstraint.gridy ++;
		
		startConstraint.gridy = 0;
		JLabel startLabel = new JLabel("1st label", JLabel.CENTER);
		valuePanel.add(startLabel, startConstraint);
		startConstraint.gridy ++;
		
		stepConstraint.gridy = 0;
		JLabel stepLabel = new JLabel("Step", JLabel.CENTER);
		valuePanel.add(stepLabel, stepConstraint);
		stepConstraint.gridy ++;
	}
	
	public CoreValueEditor addOneValueEditor(JPanel valuePanel) {
		CoreValueEditor valueEditor = new AxisValueEditor(valuePanel, minConstraint, maxConstraint,
																																	startConstraint, stepConstraint);
		minConstraint.gridy ++;
		maxConstraint.gridy ++;
		startConstraint.gridy ++;
		stepConstraint.gridy ++;
		
		return valueEditor;
	}
	
	public void removeLastValueEditor(JPanel valuePanel, CoreValueEditor valueEditor) {
		valueEditor.clearEditor(valuePanel);
		minConstraint.gridy --;
		maxConstraint.gridy --;
		startConstraint.gridy --;
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
