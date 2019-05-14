package uk.me.philipsearle.ogre;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.dom4j.DocumentException;
import org.eclipse.birt.report.model.api.CellHandle;
import org.eclipse.birt.report.model.api.ColumnHandle;
import org.eclipse.birt.report.model.api.DataItemHandle;
import org.eclipse.birt.report.model.api.DesignEngine;
import org.eclipse.birt.report.model.api.DesignFileException;
import org.eclipse.birt.report.model.api.ElementFactory;
import org.eclipse.birt.report.model.api.GridHandle;
import org.eclipse.birt.report.model.api.LabelHandle;
import org.eclipse.birt.report.model.api.LibraryHandle;
import org.eclipse.birt.report.model.api.OdaDataSetHandle;
import org.eclipse.birt.report.model.api.OdaDataSourceHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.RowHandle;
import org.eclipse.birt.report.model.api.ScalarParameterHandle;
import org.eclipse.birt.report.model.api.SessionHandle;
import org.eclipse.birt.report.model.api.SharedStyleHandle;
import org.eclipse.birt.report.model.api.SimpleMasterPageHandle;
import org.eclipse.birt.report.model.api.StructureFactory;
import org.eclipse.birt.report.model.api.TableHandle;
import org.eclipse.birt.report.model.api.TextItemHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;

import com.ibm.icu.util.ULocale;

class Application {

	/** FIXME: There are constants for all of these this somewhere in the BIRT IOda* interfaces... */
	private static final String XML_ODA_DATA_SOURCE_ID = "org.eclipse.datatools.enablement.oda.xml";
	private static final String XML_ODA_DATA_SET_ID = "org.eclipse.datatools.enablement.oda.xml.dataSet";

	private static final String DATA_SOURCE_NAME = "Data Source";
	private static final String DATA_SET_NAME = "Data Set";

	private ReportDesignHandle designHandle = null;
	private LibraryHandle libraryHandle = null;
	private ElementFactory designFactory = null;
	private ElementFactory libraryFactory = null;
	private StructureFactory structFactory = null;

	public static void main(String[] args) throws SemanticException, IOException, DesignFileException, DocumentException {
		new Application().buildReport();
	}

	void buildReport() throws SemanticException, IOException, DesignFileException, DocumentException {
		String xmlUrl = "D:\\Downloads\\data.xml";
		try (InputStream is = new FileInputStream(xmlUrl)) {
			OgreXmlDataParser parser = new OgreXmlDataParser();
			OgreField rootField = parser.extractFieldsFromOgreXml(is);

			SessionHandle session = DesignEngine.newSession(ULocale.ENGLISH);
			designHandle = session.createDesign();
			libraryHandle = session.createLibrary();
			designFactory = designHandle.getElementFactory();
			libraryFactory = libraryHandle.getElementFactory();
			structFactory = new StructureFactory();

			buildMasterPages();
			buildDataSource(xmlUrl);
			buildDataSet("Data Set", rootField);
			// buildParameter();
			// buildBody();

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

	private void buildMasterPages() throws SemanticException, IOException {

		SimpleMasterPageHandle masterpage = (SimpleMasterPageHandle) designFactory
				.newSimpleMasterPage("Simple MasterPage");
		// Grid in master page header
		GridHandle grid = designFactory.newGridItem(null, 2, 1);
		grid.setProperty("marginBottom", "1cm");
		grid.setProperty("textAlign", "right");
		grid.setProperty("verticalAlign", "baseline");
		grid.setProperty("width", "100%");
		ColumnHandle column0 = (ColumnHandle) grid.getColumns().get(0);
		column0.setProperty("width", "4.26in");
		ColumnHandle column1 = (ColumnHandle) grid.getColumns().get(1);
		column1.setProperty("width", "1.625in");

		CellHandle cell0 = grid.getCell(0, 0);
		TextItemHandle text = designFactory.newTextItem("masterpage text");
		text.setProperty("fontSize", "14pt");
		text.setProperty("fontWeight", "bold");
		text.setProperty("display", "block");
		text.setProperty("contentType", "plain");
		text.setContent("Created by: Actuate");
		cell0.getContent().add(text);

		// Text in master page footer
		TextItemHandle text1 = designFactory.newTextItem("pageheader text");
		text1.setContentType(DesignChoiceConstants.TEXT_CONTENT_TYPE_HTML);
		text1.setContent("<value-of>new Date()</value-of>");

		masterpage.getPageHeader().add(grid);
		masterpage.getPageFooter().add(text1);

		designHandle.getMasterPages().add(masterpage);

	}

}