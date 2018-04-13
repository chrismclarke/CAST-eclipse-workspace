package ebookStructure;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;


public class DomExercise {
	private String appletName, appletWidth, appletHeight;
	private Hashtable coreParams = new Hashtable();
	
	private String options;
	private Vector variationList = new Vector();
	
	public DomExercise(CastEbook castEbook, String topicName, String appletName,
																													String variations, String options) {
		this.options = options;
		Element topicElement = readTopicDom(castEbook, topicName);
		
		NodeList exerciseNodes = topicElement.getElementsByTagName("exercise");
		for (int i=0; i<exerciseNodes.getLength(); i++) {
			Element exerciseDom = (Element)exerciseNodes.item(i);
			String eApplet = XmlHelper.getUniqueTagAsString(exerciseDom, "applet");
			if (appletName.equals(eApplet)) {
				parseExercise(exerciseDom, variations);
				break;
			}
		}
	}
	
	private Element readTopicDom(CastEbook castEbook, String topicName) {
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
			File coreDir = castEbook.getCoreDir();
			File exercisesDir = new File(new File(coreDir, "exercises"), "xml");
			File topicFile = new File(exercisesDir, topicName + ".xml");
			
			Document exerciseDomDocument = db.parse(topicFile);
			
			return exerciseDomDocument.getDocumentElement();
		} catch(Exception e) {
			System.err.println("Error opening exercises for topic (" + topicName + ")\n" + e);
		}
		return null;
	}
	
	private void parseExercise(Element exerciseDom, String variationsString) {
		String variationsUsed[] = null;
		if (variationsString != null) {
			StringTokenizer st = new StringTokenizer(variationsString, ", ");
			int nVarUsed = st.countTokens();
			variationsUsed = new String[nVarUsed];
			for (int i=0 ; i<nVarUsed ; i++)
				variationsUsed[i] = st.nextToken();
		}
		
		appletName = XmlHelper.getUniqueTagAsString(exerciseDom, "applet");
		appletWidth = XmlHelper.getUniqueTagAsString(exerciseDom, "width");
		appletHeight = XmlHelper.getUniqueTagAsString(exerciseDom, "height");

		NodeList variationNodes = exerciseDom.getElementsByTagName("variation");
		int nVariations = variationNodes.getLength();
		for (int i=0 ; i<nVariations ; i++) {
			DomVariation v = new DomVariation((Element)variationNodes.item(i));
			if (variationsUsed == null)
				variationList.add(v);
			else
				for (int j=0 ; j<variationsUsed.length ; j++)
					if (v.getShortName().equals(variationsUsed[j])) {
						variationList.add(v);
						break;
					}
		}
		
		NodeList coreParamNodes = exerciseDom.getElementsByTagName("coreParam");
		int nCoreParams = coreParamNodes.getLength();
		for (int i=0 ; i<nCoreParams ; i++) {
			Element param = (Element)coreParamNodes.item(i);
			String coreParamName = param.getAttribute("name");
			String coreParamValue = param.getFirstChild().getNodeValue();
			coreParams.put(coreParamName, coreParamValue);
		}
	}
	
	public String getAppletString(String testParams) {
		String appletParams = "<param name=\"appletName\" value=\"" + appletName + "\">\n";
		appletParams += "<param name=\"width\" value=\"" + appletWidth + "\">\n";
		appletParams += "<param name=\"height\" value=\"" + appletHeight + "\">\n";
		if (testParams != null)
			appletParams += testParams + "\n";
		
		if (options != null && options.length() > 0)
			appletParams += "<param name=\"options\" value=\"" + options + "\">\n";
		
		Enumeration coreEnum = coreParams.keys();
		while (coreEnum.hasMoreElements()) {
			String paramName = (String)coreEnum.nextElement();
			String paramValue = (String)coreParams.get(paramName);
			appletParams += "<param name=\"" + paramName + "\" value=\"" + paramValue + "\">\n";
		}
		
		int nVariations = variationList.size();
		appletParams += "<param name=\"nQuestions\" value=\"" + nVariations + "\">\n";
		
		for (int i=0 ; i<nVariations ; i++) {
			DomVariation v = (DomVariation)variationList.elementAt(i);
			String qn = v.getRawQuestionText();
			String params = v.getRawParamString();
			String endings = v.getEndings();
			appletParams += "<param name=\"question" + i + "\" value=\"" + qn + "\">\n";
			appletParams += "<param name=\"qnParam" + i + "\" value=\"" + params + "\">\n";
			if (endings != null)
				appletParams += "<param name=\"questionExtra" + i + "\" value=\"" + endings + "\">\n";
		}
		
		return "<div class=\"applet\">\n" + appletParams + "\n</div>";
	}
	
	
	public String getAppletString() {
		return getAppletString(null);
	}
}
