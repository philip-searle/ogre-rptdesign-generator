package uk.me.philipsearle.ogre;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.tree.TreeCellRenderer;

import org.dom4j.DocumentException;
import org.eclipse.birt.report.model.api.DesignFileException;
import org.eclipse.birt.report.model.api.activity.SemanticException;

class Application {
	private final OgreXmlDataParser ogreXmlDataParser;
	private final BirtDesignGenerator birtDesignGenerator;

	private URI previewXmlUrl;
	private OgreField rootField;

	// UI nonsense
	private JFrame mainWindow;
	private JButton loadButton;
	private JButton generateButton;
	private JTable fieldTable;

	public static void main(String[] args) throws SemanticException, IOException, DesignFileException, DocumentException {
		new Application();
	}

	public Application() {
		this.ogreXmlDataParser = new OgreXmlDataParser();
		this.birtDesignGenerator = new BirtDesignGenerator();

		this.mainWindow = new JFrame("BIRT .rptdesign Generator for Ogre");
		this.mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.mainWindow.setMinimumSize(new Dimension(320, 200));
		this.mainWindow.setPreferredSize(new Dimension(800, 600));

		Container pane = this.mainWindow.getContentPane();

		this.loadButton = new JButton();
		this.loadButton.setText("Load Ogre Data Source XML Preview");
		this.loadButton.addActionListener(this::selectPreviewXmlUrl);
		pane.add(this.loadButton, BorderLayout.PAGE_START);

		this.generateButton = new JButton();
		this.generateButton.setText("Generate BIRT .rptdesign");
		this.generateButton.addActionListener(this::generateReport);
		pane.add(this.generateButton, BorderLayout.PAGE_END);

		this.fieldTable = new JTable();
		this.fieldTable.setColumnModel(new DefaultTableColumnModel());
		pane.add(new JScrollPane(this.fieldTable), BorderLayout.CENTER);

		this.mainWindow.pack();
		this.mainWindow.setVisible(true);
	}

	private void selectPreviewXmlUrl(ActionEvent evt) {
		String response = JOptionPane.showInputDialog(mainWindow, "Enter the URL to the Ogre XML preview", "Ogre Data Source XML Preview", JOptionPane.OK_CANCEL_OPTION);
		try {
			previewXmlUrl = URI.create(response);
			try (InputStream is = previewXmlUrl.toURL().openStream()) {
				rootField = ogreXmlDataParser.extractFieldsFromOgreXml(is);
				fieldTable.setModel(new OgreFieldTableModel(rootField.flatten()));
			}
		} catch (RuntimeException | IOException | DocumentException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(mainWindow, e + ": " + e.getMessage());
		}
	}

	void generateReport(ActionEvent evt) {
		try {
			JFileChooser fileChooser = new JFileChooser();
			if (fileChooser.showSaveDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
				birtDesignGenerator.saveDesignToFile(previewXmlUrl.toString(), rootField, fileChooser.getSelectedFile().getAbsolutePath());
			}
		} catch (RuntimeException | SemanticException | IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(mainWindow, e + ": " + e.getMessage());
		}
	}
}

class CheckboxRenderer implements TreeCellRenderer {
	private JCheckBox checkbox = new JCheckBox();

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		if (value instanceof OgreField) {
			checkbox.setSelected(((OgreField) value).isMultiValued());
		}
		checkbox.setText(value.toString());
		return checkbox;
	}
	
}

class OgreFieldTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	private final List<OgreField> flatFields;

	public OgreFieldTableModel(List<OgreField> flatFields) {
		this.flatFields = flatFields;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return flatFields.size();
	}

	@Override
	public Object getValueAt(int row, int column) {
		switch (column) {
		case 0:
			return flatFields.get(row).getUniqueId();
		case 1:
			return flatFields.get(row).isMultiValued();
		default:
			throw new IllegalStateException("Bad column: " + column);
		}
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "Field";
		case 1:
			return "Is Multi-Valued?";
		default:
			throw new IllegalStateException("Bad column: " + column);
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return String.class;
		case 1:
			return Boolean.class;
		default:
			throw new IllegalStateException("Bad column: " + columnIndex);
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 1;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 1:
			flatFields.get(rowIndex).setMultiValued((Boolean) aValue);
			break;
		default:
			throw new IllegalStateException("Bad column: " + columnIndex);
		}
	}
}