package cast.bookManager;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.event.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;

import org.w3c.dom.*;
import org.xml.sax.*;


public class CastSection {
	
	private String dir, filePrefix;
	private CastEbook castEbook;
	
	private Document sectionDomDocument = null;
	private Dom2Section sectionDom = null;
	private boolean domChanged;
	
	public CastSection(String dir, String filePrefix, CastEbook castEbook) {
		this.dir = dir;
		this.filePrefix = filePrefix;
		this.castEbook = castEbook;
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(true);
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setErrorHandler(new ErrorHandler() {
													public void warning(SAXParseException exception) throws SAXException {
													}
													public void error(SAXParseException exception) throws SAXException {
													}
													public void fatalError(SAXParseException exception) throws SAXException {
													}
												} );
			File sectionXmlFile = castEbook.getXmlFile(dir, filePrefix);
			sectionDomDocument = db.parse(sectionXmlFile);
			
			sectionDom = new Dom2Section(sectionDomDocument.getDocumentElement(), this);
			
			domChanged = false;
		} catch(Exception e) {
			System.err.println("Error opening section (" + dir + ", " + filePrefix + ")\n" + e);
		}
	}
	
	public String getDir() {
		return dir;
	}
	
	public String getFilePrefix() {
		return filePrefix;
	}
	
	public Document getDocument() {
		return sectionDomDocument;
	}
	
	public CastEbook getCastEbook() {
		return castEbook;
	}
	
	public Dom2Section getDomSection() {
		return sectionDom;
	}
	
	public boolean canEditSection() {
		return castEbook.canEditBook() && getDir().equals(castEbook.getHomeDirName());
	}
	
	public void setDomChanged() {
		domChanged = true;
	}
	
	public boolean domHasChanged() {
		return domChanged;
	}
	
	public void saveDom() {
		try {
				DOMSource domSource = new DOMSource(sectionDomDocument);
				File xmlFile = castEbook.getXmlFile(dir, filePrefix);
				StreamResult streamResult = new StreamResult(xmlFile);
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer serializer = tf.newTransformer();
				serializer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
				serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"../../../structure/sectionXmlDefn.dtd");
				serializer.setOutputProperty(OutputKeys.INDENT,"yes");
				serializer.transform(domSource, streamResult);
		}
		catch (TransformerFactoryConfigurationError factoryError) {
			System.err.println("Error creating TransformerFactory");
			factoryError.printStackTrace();
		} catch (TransformerException transformerError) {
			System.err.println("Error transforming document");
			transformerError.printStackTrace();
		}
	}
	
//-----------------------------------------------------------------

	public JTextField createMonitoredTextField(String text) {
		JTextField theText = new JTextField(text);
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
		theArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
			
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
	
	public JCheckBox createMonitoredCheckBox(String label, boolean initialState) {
		JCheckBox theCheck = new JCheckBox(label, initialState);
		theCheck.addItemListener(new ItemListener() {
													public void itemStateChanged(ItemEvent itemEvent) {
														setDomChanged();
													}
											});
		return theCheck;
	}
	
	public JComboBox createMonitoredMenu(String[] items, int initialState) {
		JComboBox theMenu = new JComboBox(items);
		theMenu.setSelectedIndex(initialState);
		theMenu.addActionListener(new ActionListener() {
													public void actionPerformed(ActionEvent e) {
														setDomChanged();
													}
											});
		return theMenu;
	}
	
}
