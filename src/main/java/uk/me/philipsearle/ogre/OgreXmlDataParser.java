package uk.me.philipsearle.ogre;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class OgreXmlDataParser {
	public OgreField extractFieldsFromOgreXml(InputStream is) throws DocumentException {
		SAXReader reader = new SAXReader();
        Document document = reader.read(is);
        
        Element recordElement = (Element) document.selectSingleNode("/dataExport/record");
        OgreField rootField = new OgreField(recordElement);
		buildFieldTree(rootField, recordElement);
		return rootField;
	}
	
	private static void buildFieldTree(OgreField parentField, Element element) {
		@SuppressWarnings("unchecked")
		List<Element> childElements = element.elements();
		for (Element childElement : childElements) {
			OgreField childField = new OgreField(
					childElement.element("value"),
					childElement.getName(),
					childElement.attributeValue("label"),
					parentField);
			if (childElement.element("value") != null) {
				buildFieldTree(childField, childElement.element("value"));
			} else if (childElement.elements("values").size() > 1) {
				childField.setMultiValued(true);
			}
		}
	}
}

class OgreField {
	private final Element sampleXmlElement;
	private final String name;
	private final String label;
	private final OgreField parent;
	private final List<OgreField> children = new ArrayList<>();
	private boolean multiValued;

	public OgreField(Element sampleXmlElement) {
		this.sampleXmlElement = sampleXmlElement;
		this.name = "<root>";
		this.label = "<root>";
		this.parent = this;
		this.multiValued = true;
	}

	OgreField(Element sampleXmlElement, String name, String label, OgreField parent) {
		this.sampleXmlElement = sampleXmlElement;
		this.name = name;
		this.label = label;
		this.parent = parent;
		parent.children.add(this);
	}
	
	void setMultiValued(boolean multiValued) {
		this.multiValued = multiValued;
	}
	
	String getUniqueId() {
		if (this == parent) {
			return "";
		}
		return parent.getUniqueId() + ":" + this.name;
	}

	public Iterable<OgreField> children() {
		return children;
	}

	public String getName() {
		return name;
	}

	public String getValueXPath() {
		return sampleXmlElement.getPath();
	}

	public String getValueXPathRelativeTo(OgreField ancestor) {
		return getValueXPath().substring(ancestor.getValueXPath().length());
	}
}
