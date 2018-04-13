package cast.exercise;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.w3c.dom.*;

import cast.utils.*;
import cast.variationEditor.*;


public class DomVariation {
	
	private Document domDocument;
	private DomExercise exercise;
	private Element domElement;
	private boolean domChanged, neverSaved;
	
//	private int nextAnonIndex = 1;
	
	private Vector endings = null;
	
	public DomVariation(Element domElement, DomExercise exercise, Document domDocument) {
		this.domElement = domElement;
		this.exercise = exercise;
		this.domDocument = domDocument;
		domChanged = false;
		neverSaved = false;
		
		addEndings();
	}
	
	public DomVariation(DomVariation original, Document domDocument) {
		this.domDocument = domDocument;
		exercise = original.exercise;
		domChanged = true;
		neverSaved = true;
		
		domElement = (Element)domDocument.importNode(original.domElement, true);
		
		addEndings();
	}
	
	private void addEndings() {
		NodeList nl = domElement.getElementsByTagName("ending");
		if (nl != null && nl.getLength() > 0) {
			endings = new Vector();
			for (int i=0 ; i<nl.getLength() ; i++) {
				Element endingElement = (Element)nl.item(i);
				endings.add(new DomEnding(endingElement, this, DomEnding.USE_ORIGINAL));
			}
		}
	}
	
	public void addNewEnding(DomEnding ending) {
		endings.add(ending);
		domElement.appendChild(ending.getDomElement());
	}
	
	public void deleteEnding(DomEnding ending) {
		endings.remove(ending);
		domElement.removeChild(ending.getDomElement());
	}
	
	public Element getDomElement() {
		return domElement;
	}
	
	public Document getDocument() {
		return domDocument;
	}
	
	public String getShortName() {
		return XmlHelper.getUniqueTagAsString(domElement, "shortName");
	}
	
	public String getLongName() {
		return XmlHelper.getUniqueTagAsString(domElement, "longName");
	}
	
	public int getHeight() {
		return XmlHelper.getUniqueTagAsInt(domElement, "height");
	}
	
	public String getRawQuestionText() {
		return XmlHelper.getUniqueTagAsString(domElement, "question");
	}
	
	public String getRawParamString() {
		return XmlHelper.getUniqueTagAsString(domElement, "qnParam");
	}
	
	public int getNoOfEndings() {
		if (endings == null)
			return 0;
		else
			return endings.size();
	}
	
	public DomEnding getEnding(int i) {
		return (DomEnding)endings.elementAt(i);
	}
	
	public String getRawEndingString(int i) {
		return getEnding(i).getRawEndingString();
	}
	
	public DomExercise getExercise() {
		return exercise;
	}
	
//-----------------------------------------------------------------
	
	public boolean isCoreVariation() {
		int nCore = exercise.noOfVariations();
		for (int i=0 ; i<nCore ; i++)
			if (exercise.getVariation(i) == this)
				return true;
		return false;
	}
	
	public void setDomChanged() {
		domChanged = true;
	}
	
	public void markAsNew() {
		domChanged = true;
		neverSaved = true;
	}
	
	public boolean neverSaved() {
		return neverSaved;
	}
	
	public void clearDomChanged() {
		domChanged = false;
	}
	
	public void clearNeverSaved() {
		neverSaved = false;
	}
	
	public boolean domHasChanged() {
		return domChanged;
	}
	
	public void updateDom(String longName, String shortName, int height, String processedQuestion, String paramString, String[] endingString) {
		Element longNameElement = XmlHelper.getUniqueTag(domElement, "longName");
		XmlHelper.setTagInterior(longNameElement, longName);
		
		Element shortNameElement = XmlHelper.getUniqueTag(domElement, "shortName");
		XmlHelper.setTagInterior(shortNameElement, shortName);
		
		Element heightElement = XmlHelper.getUniqueTag(domElement, "height");
		int defaultHeight = Integer.parseInt(getExercise().getAppletHeight());
		boolean isDefaultHeight = (height <= 0) || (height == defaultHeight);
		if (heightElement != null) {
			if (isDefaultHeight)
				heightElement.getParentNode().removeChild(heightElement);
			else
				XmlHelper.setTagInterior(heightElement, String.valueOf(height));
		}
		else if (!isDefaultHeight) {
			heightElement = domDocument.createElement("height");
			heightElement.appendChild(domDocument.createTextNode(String.valueOf(height)));
			domElement.appendChild(heightElement);
		}
		
		Element questionElement = XmlHelper.getUniqueTag(domElement, "question");
		XmlHelper.setTagInterior(questionElement, processedQuestion);
		
		Element paramElement = XmlHelper.getUniqueTag(domElement, "qnParam");
		XmlHelper.setTagInterior(paramElement, paramString);
		
		if (endingString != null)
			for (int i=0 ; i<endingString.length ; i++) {
				DomEnding ending = (DomEnding)endings.elementAt(i);
				ending.updateDom(endingString[i]);
			}
	}
	
//-----------------------------------------------------------------

	public JTextField createMonitoredTextField(String text, int chars) {
		JTextField theText = new JTextField(text, chars);
		theText.getDocument().addDocumentListener(new DocumentListener() {
													public void changedUpdate(DocumentEvent arg0) {
														setDomChanged();
													}
													
													public void insertUpdate(DocumentEvent arg0) {
														setDomChanged();
													}

													public void removeUpdate(DocumentEvent arg0) {
														setDomChanged();
													}
											});
		return theText;
	}
	
	public JTextArea createMonitoredTextArea(String text, int rows, int cols) {
		JTextArea theArea = new JTextArea(text, rows, cols);
		theArea.setLineWrap(true);
		theArea.setWrapStyleWord(true);
		theArea.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
			
		theArea.getDocument().addDocumentListener(new DocumentListener() {
													public void changedUpdate(DocumentEvent arg0) {
														setDomChanged();
													}
													
													public void insertUpdate(DocumentEvent arg0) {
														setDomChanged();
													}

													public void removeUpdate(DocumentEvent arg0) {
														setDomChanged();
													}
											});
		return theArea;
	}
	
	public QuestionPanel createMonitoredQuestionPanel(String text, VariableType[] validParams) {
		Vector validParamVector = new Vector();
		validParamVector.add("index");							//		highlights "index" even if one is not really allowed
		for (int i=0 ; i<validParams.length ; i++)
			validParamVector.add(validParams[i].getName());
		
		final QuestionPanel theArea = new QuestionPanel(validParamVector);
		theArea.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
			
		theArea.getStyledDocument().addDocumentListener(new DocumentListener() {
													public void changedUpdate(DocumentEvent arg0) {
														setDomChanged();
													}
													
													public void insertUpdate(DocumentEvent arg0) {
														setDomChanged();
													}

													public void removeUpdate(DocumentEvent arg0) {
														setDomChanged();
													}
											});
		theArea.setText(text);
		
		return theArea;
	}
	
	public JCheckBox createMonitoredCheckBox(String label, boolean initialState) {
		JCheckBox theCheck = new JCheckBox(label, initialState);
		theCheck.addItemListener(new ItemListener() {
													public void itemStateChanged(ItemEvent itemEvent) {
														setDomChanged();
													}
											});
		return theCheck;
	}
	
	public JComboBox createMonitoredMenu(String[] items) {
		JComboBox theMenu = new JComboBox();
		if (items != null)
			for (int i=0 ; i<items.length ; i++)
				if (items[i] != null)
					theMenu.addItem(items[i]);
		
//		JComboBox theMenu = (items == null) ? new JComboBox() : new JComboBox(items);
		theMenu.addActionListener(new ActionListener() {
													public void actionPerformed(ActionEvent e) {
														setDomChanged();
													}
											});
		return theMenu;
	}
}
