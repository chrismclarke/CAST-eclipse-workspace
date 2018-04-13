package cast.exercise;

import java.util.*;

import org.w3c.dom.*;

import cast.utils.*;


public class DomExercise {
	static final public int MAIN_PARAMS_ONLY = 0;
	static final public int ENDING_PARAMS_ONLY = 1;
	static final public int ALL_PARAMS = 2;
	
	private DomTopic topic;
	private Element domElement;
	private String description, name;
	private VariableType[] variableTypes;
	
	private String appletName, appletWidth, appletHeight;
	private Hashtable coreParams = new Hashtable();
	
	private Vector variationList = new Vector();
	private boolean hasTemplate;
	
	public DomExercise(Element domElement, DomTopic topic) {
		this.domElement = domElement;
		this.topic = topic;
		
		description = XmlHelper.getUniqueTagAsString(domElement, "description");
		name = domElement.getAttribute("name");
		
		NodeList variableNodes = domElement.getElementsByTagName("variable");
		int nVars = variableNodes.getLength();
		variableTypes = new VariableType[nVars];
		for (int i=0 ; i<nVars ; i++)
			variableTypes[i] = new VariableType((Element)variableNodes.item(i));
		
		NodeList variationNodes = domElement.getElementsByTagName("variation");
		int nVariations = variationNodes.getLength();
		for (int i=0 ; i<nVariations ; i++)
			variationList.add(new DomVariation((Element)variationNodes.item(i), this, topic.getDocument()));
		
		NodeList templateNodes = domElement.getElementsByTagName("template");
		hasTemplate = (templateNodes.getLength() == 1);
		
		
		appletName = XmlHelper.getUniqueTagAsString(domElement, "applet");
		appletWidth = XmlHelper.getUniqueTagAsString(domElement, "width");
		appletHeight = XmlHelper.getUniqueTagAsString(domElement, "height");
		
		NodeList coreParamNodes = domElement.getElementsByTagName("coreParam");
		int nCoreParams = coreParamNodes.getLength();
		for (int i=0 ; i<nCoreParams ; i++) {
			Element param = (Element)coreParamNodes.item(i);
			String coreParamName = param.getAttribute("name");
			String coreParamValue = param.getFirstChild().getNodeValue();
			coreParams.put(coreParamName, coreParamValue);
		}
	}
	
	public Element getDomElement() {
		return domElement;
	}
	
	public DomTopic getTopic() {
		return topic;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getName() {
		return name;
	}
	
	public String getAppletName() {
		return appletName;
	}
	
	public String getAppletWidth() {
		return appletWidth;
	}
	
	public String getAppletHeight() {
		return appletHeight;
	}
	
	public Hashtable getCoreParams() {
		return coreParams;
	}
	
//------------------------------------------------------------------
	
	public int noOfVariations() {
		return variationList.size();
	}
	
	public DomVariation getVariation(int i) {
		return (DomVariation)variationList.elementAt(i);
	}
	
	public boolean variationExists(String variationName, DomVariation excludeFromCheck, Vector customVariations) {
		int nVariations = variationList.size();
		for (int i=0 ; i<nVariations ; i++) {
			DomVariation variation = (DomVariation)variationList.elementAt(i);
			if (variation != excludeFromCheck && variationName.equals(variation.getShortName()))
				return true;
		}
		if (customVariations != null) {
			nVariations = customVariations.size();
			for (int i=0 ; i<nVariations ; i++) {
				DomVariation variation = (DomVariation)customVariations.elementAt(i);
				if (variation != excludeFromCheck && variationName.equals(variation.getShortName()))
					return true;
			}
		}
		return false;
	}
	
	public void addVariation(DomVariation newVariation) {
		domElement.appendChild(newVariation.getDomElement());
		variationList.add(newVariation);
	}
	
	public void deleteVariation(DomVariation variation) {
		Element variationElement = variation.getDomElement();
		domElement.removeChild(variationElement);
		
		variationList.remove(variation);
	}
	
	public VariableType[] getVariableTypes(int paramType) {
		if (paramType == ALL_PARAMS)
			return variableTypes;
		else {
			int n = 0;
			for (int i=0 ; i<variableTypes.length ; i++)
				if (variableTypes[i].isExtra() == (paramType == ENDING_PARAMS_ONLY))
					n ++;
			VariableType types[] = new VariableType[n];
			n = 0;
			for (int i=0 ; i<variableTypes.length ; i++)
				if (variableTypes[i].isExtra() == (paramType == ENDING_PARAMS_ONLY))
					types[n++] = variableTypes[i];
			return types;
		}
	}
	
	public boolean hasTemplate() {
		return hasTemplate;
	}
	
//-----------------------------------------------------------------
	
	public boolean domHasChanged() {
		boolean domChanged = false;
		for (int i=0 ; i<noOfVariations() ; i++)
			domChanged = domChanged || getVariation(i).domHasChanged();
		return domChanged;
	}
}
