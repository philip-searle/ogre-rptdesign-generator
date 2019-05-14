package uk.me.philipsearle.ogre;

import java.io.InputStream;
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
					parentField);

			if (childElement.element("value") != null) {
				System.out.println("Descending into compound field: " + childField.getUniqueId());
				buildFieldTree(childField, childElement.element("value"));
			}

			if (childElement.elements("value").size() > 1) {
				System.out.println("Found multi-valued field: " + childField.getUniqueId());
				childField.setMultiValued(true);
			}
		}
	}
}
