package cast.variableEditors;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import cast.exercise.*;
import cast.variationEditor.*;

public class ModelVariableEditor extends CoreVariableEditor {
	
	private class ModelValueEditor extends CoreValueEditor {
		private JTextField editors[];
		
		public ModelValueEditor(JPanel valuePanel, GridBagConstraints[] constraints) {
			editors = new JTextField[modelOrder+1];
			for (int i=0 ; i<modelOrder+1 ; i++) {
				editors[i] = variation.createMonitoredTextField("", 5);
				valuePanel.add(editors[i], constraints[i]);
			}
		}
		
		public void setValue(String valueString) {
			StringTokenizer st = new StringTokenizer(valueString);
			for (int i=0 ; i<modelOrder+1 ; i++)
				editors[i].setText(st.nextToken());
		}
		
		public String getValue() {
			String s = "";
			for (int i=0 ; i<editors.length ; i++) {
				if (i > 0)
					s += " ";
				s += editors[i].getText();
			}
			return s;
		}
		
		public void clearEditor(JPanel valuePanel) {
			for (int i=0 ; i<modelOrder+1 ; i++) {
				valuePanel.remove(editors[i]);
				editors[i] = null;
			}
		}
		
		public boolean isValidValue() {
			try {
				double param = 1.0;
				for (int i=0 ; i<modelOrder+1 ; i++)
					param = Double.parseDouble(editors[i].getText());
				return param > 0;			//		last param is sigma
			} catch (NumberFormatException e) {
				return false;
			}
		}
	}
	
//-----------------------------------------------------------------------------
	
	
	private int modelOrder = 2;
	private GridBagConstraints constraints[];
	
	public ModelVariableEditor(DomVariation variation, VariableType[] validParams, boolean hasIndex) {
		super(variation, validParams, hasIndex);
	}
	
	protected String getBaseType() {
		return "model";
	}
	
	public void setTypeDetails(String typeDetails) {
		modelOrder = Integer.parseInt(typeDetails);			//	number of fields is one more for sigma
	}
	
	public void initialiseValuePanel(JPanel valuePanel) {
		constraints = new GridBagConstraints[modelOrder + 1];
		for (int i=0 ; i<constraints.length ; i++)
			constraints[i] = new GridBagConstraints(i, 0,														//	gridx & gridy
																							1, 1,														//	gridwidth & gridheight
																							0, 0,														//	weightx & weighty,
																							GridBagConstraints.NORTH,				//	anchor
																							GridBagConstraints.NONE,				//	fill
																							new Insets(0, 0, 0, 0),					//	insets
																							5, 0);													//	ipadx & ipady

		valuePanel.setLayout(new GridBagLayout());
		
		for (int i=0 ; i<modelOrder ; i++) {
			JLabel paramLabel = new JLabel("b" + i, JLabel.CENTER);
			valuePanel.add(paramLabel, constraints[i]);
			constraints[i].gridy ++;
		}
		
		JLabel sigmaLabel = new JLabel("sigma", JLabel.CENTER);
		valuePanel.add(sigmaLabel, constraints[modelOrder]);
		constraints[modelOrder].gridy ++;
	}
	
	public CoreValueEditor addOneValueEditor(JPanel valuePanel) {
		CoreValueEditor valueEditor = new ModelValueEditor(valuePanel, constraints);
		for (int i=0 ; i<constraints.length ; i++)
			constraints[i].gridy ++;
		
		return valueEditor;
	}
	
	public void removeLastValueEditor(JPanel valuePanel, CoreValueEditor valueEditor) {
		valueEditor.clearEditor(valuePanel);
		for (int i=0 ; i<constraints.length ; i++)
			constraints[i].gridy --;
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
