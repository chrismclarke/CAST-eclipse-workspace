package cast.exercise;

import java.io.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;


public class ExerciseXmlHelper {
	
	static public String quoteString(String s) {
		if (s ==  null)
			return "null";
		else
			return "\"" + s + "\"";
	}
	
	static public String[] getTopics(File xmlDir) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(true);
		try {
			File topicsFile = new File(xmlDir, "topics.xml");
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(topicsFile);
			
			Element docElement = dom.getDocumentElement();
			
			NodeList nl = docElement.getElementsByTagName("topic");
			String topicName[] = new String[nl.getLength()];
			for (int i=0 ; i<nl.getLength() ; i++) {
				Element topicElement = (Element)nl.item(i);
				topicName[i] = topicElement.getAttribute("name");
			}
			
			return topicName;
		} catch(Exception e) {
			System.err.println("Error: " + e);
		}
		return null;
	}
}
