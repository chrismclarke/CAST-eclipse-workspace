package cast.exercise;

import java.io.*;
import java.util.*;

import javax.swing.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import cast.utils.*;

public class DomCustomVariations {
	private File xmlFile;
	private ExerciseListFrame exerciseFrame;
	
	private Document customDomDocument;
	
	private Hashtable topics = new Hashtable();
	
	public DomCustomVariations(File xmlFile, ExerciseListFrame exerciseFrame) {
		this.xmlFile = xmlFile;
		this.exerciseFrame = exerciseFrame;
		
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
			customDomDocument = db.parse(xmlFile);
			Element customElement = customDomDocument.getDocumentElement();
			
			NodeList exerciseNodes = customElement.getElementsByTagName("exercise");
			int nNodes = exerciseNodes.getLength();
			for (int i=0 ; i<nNodes ; i++) {
				Element exerciseElement = (Element)exerciseNodes.item(i);
				String tName = exerciseElement.getAttribute("topic");
				String eName = exerciseElement.getAttribute("name");
				
				NodeList variationNodes = exerciseElement.getElementsByTagName("variation");
				Element variationElement = (Element)variationNodes.item(0);			//		there should only be one
				String vName = XmlHelper.getUniqueTagAsString(variationElement, "shortName");
				
				DomExercise exercise = exerciseFrame.findExercise(tName, eName);
				if (exercise == null) {
					JOptionPane.showMessageDialog(exerciseFrame, "Error! Custom variations file contains a topic or exercise that does not exist.\n"
																					+ "(" + tName + "," + eName + ")", "Error!", JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (exercise.variationExists(vName, null, getVariations(tName, eName))) {
					JOptionPane.showMessageDialog(exerciseFrame, "Error! A variation with name (" + tName + "," + eName
																			+ "," + vName + ") already exists.", "Error!", JOptionPane.ERROR_MESSAGE);
					break;
				}
				rememberVariation(tName, eName, variationElement, false);
			}
		} catch(SAXException e) {
			System.err.println("Invalid XML file for the custom variations.\n" + e);
		} catch(ParserConfigurationException e) {
			System.err.println("ParserConfigurationException when reading XML file.\n" + e);
		} catch(IOException e) {
			System.err.println("IOException when reading XML file.\n" + e);
		}
	}
	
	public File getFile() {
		return xmlFile;
	}
	
	public Hashtable getExercises(String topicName) {
		return (Hashtable)topics.get(topicName);
	}
	
	private void rememberVariation(String topicName, String exerciseName, Element variationElement, boolean markAsNew) {
		DomExercise coreExercise = exerciseFrame.findExercise(topicName, exerciseName);
		DomVariation v = new DomVariation(variationElement, coreExercise, customDomDocument);
		
		Hashtable topic = (Hashtable)topics.get(topicName);
		if (topic == null) {
			topic = new Hashtable();
			topics.put(topicName, topic);
		}
		
		Vector exercise = (Vector)topic.get(exerciseName);
		if (exercise == null) {
			exercise = new Vector();
			topic.put(exerciseName, exercise);
		}
		if (markAsNew)
			v.markAsNew();
		
		exercise.add(v);
	}
	
	public Document getDocument() {
		return customDomDocument;
	}
	
	public Vector getVariations(String topicName, String exerciseName) {
		Hashtable exercises = (Hashtable)topics.get(topicName);
		if (exercises == null)
			return null;
		
		return (Vector)exercises.get(exerciseName);
	}
	
	public void addVariation(DomExercise coreExercise) {
		String exerciseName = coreExercise.getName();
		String topicName = coreExercise.getTopic().getTopicName();
		
		Element customExerciseElement = customDomDocument.createElement("exercise");
		customExerciseElement.setAttribute("topic", topicName);
		customExerciseElement.setAttribute("name", exerciseName);
		
		Element newVariationElement = customDomDocument.createElement("variation");
			Element longName = customDomDocument.createElement("longName");
			Node longNameText = customDomDocument.createTextNode("Name of new variation");
			longName.appendChild(longNameText);
		newVariationElement.appendChild(longName);
			Element shortName = customDomDocument.createElement("shortName");
			Node shortNameText = customDomDocument.createTextNode("newVariation");
			shortName.appendChild(shortNameText);
		newVariationElement.appendChild(shortName);
		
		Element coreExerciseElement = coreExercise.getDomElement();
		Element template = XmlHelper.getUniqueTag(coreExerciseElement, "template");
		NodeList templateNodes = template.getChildNodes();
		int nChildren = templateNodes.getLength();
		for (int i=0 ; i<nChildren ; i++) {
			Node n = customDomDocument.importNode(templateNodes.item(i), true);
			newVariationElement.appendChild(n);
		}
		
		customExerciseElement.appendChild(newVariationElement);
		customDomDocument.getDocumentElement().appendChild(customExerciseElement);
		
		rememberVariation(topicName, exerciseName, newVariationElement, true);
	}
	
	public void deleteVariation(DomVariation variation, DomExercise exercise) {
		String exerciseName = exercise.getName();
		String topicName = exercise.getTopic().getTopicName();
		
		Element customElement = customDomDocument.getDocumentElement();
		
		NodeList exerciseNodes = customElement.getElementsByTagName("exercise");
		int nNodes = exerciseNodes.getLength();
		for (int i=0 ; i<nNodes ; i++) {
			Element exerciseElement = (Element)exerciseNodes.item(i);
			
			NodeList variationNodes = exerciseElement.getElementsByTagName("variation");
			Element variationElement = (Element)variationNodes.item(0);			//		there should only be one
			if (variationElement == variation.getDomElement()) {
				customElement.removeChild(exerciseElement);
				break;
			}
		}
				
		Hashtable topicContents = (Hashtable)topics.get(topicName);
		Vector exerciseContents = (Vector)topicContents.get(exerciseName);
		exerciseContents.remove(variation);
	}
	
	public void duplicateVariation(DomVariation variation) {
		DomVariation newVariation = new DomVariation(variation, customDomDocument);
		Element newVariationElement = newVariation.getDomElement();
		
		String topicName = variation.getExercise().getTopic().getTopicName();
		String exerciseName = variation.getExercise().getName();
		
		Element exerciseElement = customDomDocument.createElement("exercise");
		exerciseElement.setAttribute("topic", topicName);
		exerciseElement.setAttribute("name", exerciseName);
		
		exerciseElement.appendChild(newVariationElement);
		customDomDocument.getDocumentElement().appendChild(exerciseElement);
		
		rememberVariation(topicName, exerciseName, newVariationElement, true);
	}
	
	public boolean domHasChanged() {
		Enumeration topicKeys = topics.keys();
		
		while (topicKeys.hasMoreElements()) {
			String tName = (String)topicKeys.nextElement();
			Hashtable exercises = (Hashtable)topics.get(tName);
			
			Enumeration exerciseKeys = exercises.keys();
			while (exerciseKeys.hasMoreElements()) {
				String eName = (String)exerciseKeys.nextElement();
				Vector variations = (Vector)exercises.get(eName);
				
				Enumeration ve = variations.elements();
				while (ve.hasMoreElements()) {
					DomVariation variation = (DomVariation)ve.nextElement();
					if (variation.domHasChanged())
						return true;
				}
			}
		}
		return false;
	}
	
	public void saveDom() {
//		System.out.println("Doing save of topic.");

		try {
				DOMSource domSource = new DOMSource(customDomDocument);
				StreamResult streamResult = new StreamResult(xmlFile);
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer serializer = tf.newTransformer();
				serializer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
//				serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"../exercise_defns/customXmlDefn.dtd");
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
	
}
