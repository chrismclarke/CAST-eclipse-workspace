package cast.variationEditor;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import cast.exercise.*;

public class ParameterListPanel extends JPanel {
	static final private Color kVariableBackground = new Color(0xFFFFEE);
	static final private Color kLineColor = new Color(0x999999);
	
	private GridBagConstraints nameConstraint = new GridBagConstraints(
																										0, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTHWEST,		//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 5, 0, 10),				//	insets
																										0, 0);													//	ipadx & ipady
	private GridBagConstraints valueConstraint = new GridBagConstraints(
																										1, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										1, 0,														//	weightx & weighty,
																										GridBagConstraints.CENTER,			//	anchor
																										GridBagConstraints.HORIZONTAL,	//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										0, 0);													//	ipadx & ipady												//	ipadx & ipady
	private GridBagConstraints lineConstraint = new GridBagConstraints(
																										0, 1,														//	gridx & gridy
																										2, 1,														//	gridwidth & gridheight
																										1, 0,														//	weightx & weighty,
																										GridBagConstraints.CENTER,			//	anchor
																										GridBagConstraints.HORIZONTAL,	//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										0, 0);													//	ipadx & ipady
	private GridBagConstraints fillerConstraint = new GridBagConstraints(
																										0, 0,														//	gridx & gridy
																										2, 1,														//	gridwidth & gridheight
																										1, 1,														//	weightx & weighty,
																										GridBagConstraints.CENTER,			//	anchor
																										GridBagConstraints.HORIZONTAL,	//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										0, 0);													//	ipadx & ipady
																										
	private Hashtable variableEditors = new Hashtable();
	
	public ParameterListPanel(Hashtable params, VariableType[] validParams, DomVariation variation) {
		boolean hasIndexVariable = params.containsKey("index");
		
		setLayout(new GridBagLayout());
			nameConstraint.gridy = 0;
			valueConstraint.gridy = 0;
			lineConstraint.gridy = 1;
			lineConstraint.gridwidth = 2;
		
		setBackground(kVariableBackground);
			Border insetBorder = BorderFactory.createMatteBorder(4, 0, 0, 0, kVariableBackground);
		setBorder(insetBorder);
		
		int nVariables = validParams.length;
		if (hasIndexVariable)
			addVariableToPanel("index", "index", null, params, validParams, variation, hasIndexVariable,
																													nVariables > 0);				//	index is always first
		
		for (int i=0 ; i< nVariables; i++) {
			String name = validParams[i].getName();
			String type = validParams[i].getType();
			String comment = validParams[i].getComment();
			boolean notLastVariable = i < (nVariables - 1);
			addVariableToPanel(name, type, comment, params, validParams, variation, hasIndexVariable, notLastVariable);
		}
		
			if (hasIndexVariable)
				nVariables ++;
			JPanel fillerPanel = new JPanel();
			fillerPanel.setOpaque(false);
			fillerConstraint.gridy = 2 * nVariables  - 1;
		add(fillerPanel, fillerConstraint);
	}
	
	private void addVariableToPanel(String name, String type, String comment, Hashtable paramMap,
																										VariableType[] validParams, DomVariation variation,
																										boolean hasIndexVariable, boolean notLastVariable) {
			JPanel namePanel = CoreVariableEditor.createNamePanel(name, type, comment);
			
			CoreVariableEditor editor = CoreVariableEditor.createEditor(name, type, variation, validParams, hasIndexVariable);
			String initialValue = (String)paramMap.get(name);		//	does not include type
			if (initialValue == null)
				editor.setDefaultValueType();
			else
				editor.setInitialValue(initialValue);
		
		add(namePanel, nameConstraint);
		add(editor, valueConstraint);
		if (notLastVariable)
			add(new LinePanel(), lineConstraint);
			
		nameConstraint.gridy += 2;
		valueConstraint.gridy += 2;
		lineConstraint.gridy += 2;
		
		variableEditors.put(name, editor);
	}
	
	public void updateParams(Hashtable params) throws ParamValueException {
						//		entries in variableEditors and params should match
		Enumeration ve = params.keys();
		while (ve.hasMoreElements()) {
			String varName = (String)ve.nextElement();
			CoreVariableEditor varEditor = (CoreVariableEditor)variableEditors.get(varName);
			
			String varValue = varEditor.getTextValue();
			params.put(varName, varValue);
		}
	}
	
	
//-------------------------------------------------------
	
	private class LinePanel extends JPanel {
		final private Dimension kPrefSize = new Dimension(0, 5);
		
		public LinePanel() {
			setOpaque(false);
		}
		
		public Dimension getPreferredSize() {
			return kPrefSize;
		}
		
		public void paintComponent(Graphics g) {
			g.setColor(kLineColor);
			g.drawLine(0, getSize().height / 2, getSize().width, getSize().height / 2);
		}
	}
}