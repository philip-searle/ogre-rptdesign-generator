package uk.me.philipsearle.ogre;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.dom4j.DocumentException;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.model.api.DesignConfig;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.ElementFactory;
import org.eclipse.birt.report.model.api.IDesignEngine;
import org.eclipse.birt.report.model.api.IDesignEngineFactory;
import org.eclipse.birt.report.model.api.OdaDataSetHandle;
import org.eclipse.birt.report.model.api.OdaDataSourceHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.SessionHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;

public class Test1 {
	public static void main(String[] args) throws IOException, DocumentException, BirtException {
		DesignConfig designConfig = new DesignConfig();
		Platform.startup(designConfig);
		IDesignEngineFactory factory = (IDesignEngineFactory) Platform
				.createFactoryObject(IDesignEngineFactory.EXTENSION_DESIGN_ENGINE_FACTORY);
		IDesignEngine engine = factory.createDesignEngine(designConfig);
		OgreXmlDataParser parser = new OgreXmlDataParser();
		try (InputStream is = Files.newInputStream(Paths.get("D:\\Downloads\\data.xml"))) {
			OgreField rootField = parser.extractFieldsFromOgreXml(is);

			SessionHandle sessionhandle = engine.newSessionHandle(null);
			ReportDesignHandle reportDesignHandle = sessionhandle.createDesign();
			ElementFactory elementFactory = reportDesignHandle.getElementFactory();
			DesignElementHandle masterPage = elementFactory.newSimpleMasterPage("Master Page");
			reportDesignHandle.getMasterPages().add(masterPage);
			reportDesignHandle.saveAs("D:\\Downloads\\report.rptdesign");

			buildDataSource(reportDesignHandle, elementFactory);
			buildDataSet(reportDesignHandle, elementFactory);
			createDataSource(reportDesignHandle, rootField);

			reportDesignHandle.close();
		}
		Platform.shutdown();
	}

	static void buildDataSource(ReportDesignHandle reportDesignHandle, ElementFactory elementFactory)
			throws SemanticException {

		OdaDataSourceHandle dsHandle = elementFactory.newOdaDataSource("Data Source",
				"org.eclipse.birt.report.data.oda.jdbc");
		dsHandle.setProperty("odaDriverClass", "net.sourceforge.jtds.jdbc.Driver");
		dsHandle.setProperty("odaURL", "jdbc:jtds:sqlserver://spmdb:1433/gui");
		dsHandle.setProperty("odaUser", "sa");
		dsHandle.setProperty("odaPassword", "sa");

		reportDesignHandle.getDataSources().add(dsHandle);

	}

	static void buildDataSet(ReportDesignHandle reportDesignHandle, ElementFactory elementFactory) throws SemanticException {
		OdaDataSetHandle dsHandle = elementFactory.newOdaDataSet("ds",
				"org.eclipse.birt.report.data.oda.jdbc.JdbcSelectDataSet");
		dsHandle.setDataSource("Data Source");
		dsHandle.setQueryText("Select \"Transaction\".trans_id," + "\"Transaction\".trans_amt," + "Trans_desc.trans_ty,"
				+ "Account.account_nm," + "\"Transaction\".trand_dt," + "\"Transaction\".account_id"
				+ "FROM \"Transaction\" , Trans_desc , Account" + "Where\"Transaction\".account_id = Account.account_id"
				+ "and \"Transaction\".trans_cd = Trans_desc.trans_cd)");

		reportDesignHandle.getDataSets().add(dsHandle);

	}

	private static void createDataSource(ReportDesignHandle reportDesignHandle, OgreField rootField)
			throws BirtException {
		OdaDataSourceHandle dataSource = reportDesignHandle.getElementFactory().newOdaDataSource("record",
				"org.eclipse.datatools.enablement.oda.xml");
		dataSource.setStringProperty("FILELIST", "yyy");
		reportDesignHandle.getDataSources().add(dataSource);
	}
}
