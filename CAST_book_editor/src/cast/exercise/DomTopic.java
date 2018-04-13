package cast.exercise;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import cast.utils.*;

public class DomTopic {
	
	private File xmlFile;
	
	private Document topicDomDocument = null;
	private String topicName, longTopicName;
	
	private Vector exerciseList = new Vector();
	
	public DomTopic(final String topicName, File exerciseXmlDir) {
		this.topicName = topicName;
		xmlFile = new File(exerciseXmlDir, topicName + ".xml");
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(true);
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setErrorHandler(new ErrorHandler() {
													public void warning(SAXParseException exception) throws SAXException {
														System.out.println("Warning during parse of " + topicName + ".xml.\n" + exception);
													}
													public void error(SAXParseException exception) throws SAXException {
														System.out.println("Error during parse of " + topicName + ".xml.\n" + exception);
													}
													public void fatalError(SAXParseException exception) throws SAXException {
														System.out.println("Fatal error during parse of " + topicName + ".xml.\n" + exception);
													}
												} );
			topicDomDocument = db.parse(xmlFile);
			Element topicElement = topicDomDocument.getDocumentElement();
			longTopicName = XmlHelper.getUniqueTagAsString(topicElement, "longName");
			
			NodeList exerciseNodes = topicDomDocument.getElementsByTagName("exercise");
			int nNodes = exerciseNodes.getLength();
			for (int i=0 ; i<nNodes ; i++)
				exerciseList.add(new DomExercise((Element)exerciseNodes.item(i), this));
		} catch(ParserConfigurationException e) {
			System.err.println("Parser error opening topic (" + topicName + ")\n" + e);
		} catch(SAXException e) {
			System.err.println("SAX error opening topic (" + topicName + ")\n" + e);
		} catch(IOException e) {
			System.err.println("IO error opening topic (" + topicName + ")\n" + e);
		}

	}
	
	public String getTopicName() {
		return topicName;
	}
	
	public String getLongName() {
		return longTopicName;
	}
	
	public Document getDocument() {
		return topicDomDocument;
	}
	
	public int noOfExercises() {
		return exerciseList.size();
	}
	
	public DomExercise getExercise(int index) {
		return (DomExercise)exerciseList.elementAt(index);
	}
	
	public DomExercise getExercise(String exerciseName) {
		int nExercises = exerciseList.size();
		for (int i=0 ; i<nExercises ; i++) {
			DomExercise exercise = (DomExercise)exerciseList.elementAt(i);
			if (exerciseName.equals(exercise.getName()))
				return exercise;
		}
		return null;
	}
	
	public boolean domHasChanged() {
		boolean domChanged = false;
		for (int i=0 ; i<noOfExercises() ; i++)
			domChanged = domChanged || getExercise(i).domHasChanged();
		return domChanged;
	}
	
	public void saveDom() {
//		System.out.println("Doing save of topic.");

		try {
				DOMSource domSource = new DOMSource(topicDomDocument);
				StreamResult streamResult = new StreamResult(xmlFile);
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer serializer = tf.newTransformer();
				serializer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
//				serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"../exercise_defns/exerciseXmlDefn.dtd");
				serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"exerciseXmlDefn.dtd");
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
