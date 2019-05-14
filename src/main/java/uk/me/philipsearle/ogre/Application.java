package uk.me.philipsearle.ogre;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.dom4j.DocumentException;
import org.eclipse.birt.report.model.api.DesignConfig;
import org.eclipse.birt.report.model.api.DesignEngine;
import org.eclipse.birt.report.model.api.DesignFileException;
import org.eclipse.birt.report.model.api.ElementFactory;
import org.eclipse.birt.report.model.api.OdaDataSetHandle;
import org.eclipse.birt.report.model.api.OdaDataSourceHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.SessionHandle;
import org.eclipse.birt.report.model.api.SimpleMasterPageHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;

import com.ibm.icu.util.ULocale;

class Application {

	/** FIXME: There are constants for all of these this somewhere in the BIRT IOda* interfaces... */
	private static final String XML_ODA_DATA_SOURCE_ID = "org.eclipse.datatools.enablement.oda.xml";
	private static final String XML_ODA_DATA_SET_ID = "org.eclipse.datatools.enablement.oda.xml.dataSet";

	private static final String DATA_SOURCE_NAME = "Data Source";
	private static final String DATA_SET_NAME = "Data Set";

	private ReportDesignHandle designHandle = null;
	private ElementFactory designFactory = null;

	public static void main(String[] args) throws SemanticException, IOException, DesignFileException, DocumentException {
		new Application().buildReport();
	}

	void buildReport() throws SemanticException, IOException, DesignFileException, DocumentException {
		String xmlUrl = "D:\\Downloads\\data.xml";
		try (InputStream is = new FileInputStream(xmlUrl)) {
			OgreXmlDataParser parser = new OgreXmlDataParser();
			OgreField rootField = parser.extractFieldsFromOgreXml(is);

			// Deprecated method, I know... 
			//SessionHandle session = DesignEngine.newSession(ULocale.ENGLISH);
			SessionHandle session = new DesignEngine(new DesignConfig()).newSessionHandle(ULocale.ENGLISH);
			designHandle = session.createDesign();
			designFactory = designHandle.getElementFactory();

			buildMasterPages();
			buildDataSource(xmlUrl);
			buildDataSet("Data Set", rootField);

			designHandle.saveAs("D:\\Downloads\\Sample2.rptdesign");
		}
	}

	void buildDataSource(String xmlUrl) throws SemanticException {
		OdaDataSourceHandle dsHandle = designFactory.newOdaDataSource(DATA_SOURCE_NAME, XML_ODA_DATA_SOURCE_ID);
		dsHandle.setProperty("FILELIST", xmlUrl);

		designHandle.getDataSources().add(dsHandle);
	}

	void buildDataSet(String dataSetName, OgreField dataSetField) throws SemanticException {
		OdaDataSetHandle dsHandle = designFactory.newOdaDataSet(DATA_SET_NAME + dataSetField.getUniqueId(),
				XML_ODA_DATA_SET_ID);
		dsHandle.setDataSource(DATA_SOURCE_NAME);
		
		StringBuilder queryText = new StringBuilder();
		queryText.append("table0#-TNAME-#table0#:#[");
		queryText.append(dataSetField.getValueXPath());
		queryText.append("]#:#");
		for (OgreField childField : dataSetField.children()) {
			queryText.append('{');
			queryText.append(childField.getName());
			queryText.append(";STRING;");
			queryText.append(childField.getValueXPathRelativeTo(dataSetField));
			queryText.append("},");
		}
		queryText.deleteCharAt(queryText.length() - 1);
		queryText.append('}');
		dsHandle.setQueryText(queryText.toString());

		designHandle.getDataSets().add(dsHandle);

	}

	private void buildMasterPages() throws SemanticException {
		SimpleMasterPageHandle masterpage = (SimpleMasterPageHandle) designFactory.newSimpleMasterPage("Simple MasterPage");
		designHandle.getMasterPages().add(masterpage);
	}
}
