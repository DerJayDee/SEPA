package de.dlrg_rodenkirchen.sepa;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import de.dlrg_rodenkirchen.sepa.excel.ExcelFilter;
import de.dlrg_rodenkirchen.sepa.excel.ExcelReader;
import de.dlrg_rodenkirchen.sepa.helper.Check;
import de.dlrg_rodenkirchen.sepa.helper.Person;
import de.dlrg_rodenkirchen.sepa.helper.StaticString;
import de.dlrg_rodenkirchen.sepa.interfaces.IReader;
import de.dlrg_rodenkirchen.sepa.interfaces.IWriter;
import de.dlrg_rodenkirchen.sepa.xml.XMLWriter;

@SuppressWarnings("serial")
public final class Xsl2Xml extends JFrame {

	private JButton button_open;
	private JButton button_save;

	private JTextField tf_credName;
	private JTextField tf_credID;
	private JTextField tf_credIBAN;
	private JTextField tf_credBIC;
	private JTextField tf_execDate;
	private JTextField tf_excelSheet;

	private Properties props;

	private GridBagConstraints gbc;

	private ArrayList<Person> persons;

	private ResourceBundle texte;

	public Xsl2Xml() {
		loadStrings();
		loadProps();
		createGui();
	}

	class OpenExcelListener implements ActionListener {
		public final void actionPerformed(ActionEvent e) {
			saveProps();
			JFileChooser c = new JFileChooser();
			File f = new File(System.getProperty("java.class.path"));
			File dir = f.getAbsoluteFile().getParentFile();
			c.setCurrentDirectory(dir);
			if (inputNotCorrect(c)) {
				return;
			} else {
				FileFilter ff = new ExcelFilter();
				c.addChoosableFileFilter(ff);
				c.setFileFilter(ff);
				int rVal = c.showOpenDialog(Xsl2Xml.this);
				IReader reader = null;
				if (rVal == JFileChooser.APPROVE_OPTION) {
					if (reader == null) {
						try {
							reader = new ExcelReader();
						} catch (Exception e1) {
							e1.printStackTrace();
							JOptionPane.showMessageDialog(c,
									texte.getString("D_ERROR_MATCHING_TEXT"),
									texte.getString("D_ERROR"),
									JOptionPane.ERROR_MESSAGE);
						}
					}
					try {
						reader.setFile(c.getSelectedFile());
					} catch (Exception e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(c,
								texte.getString("D_ERROR_READ_FILE_TEXT"),
								texte.getString("D_ERROR"),
								JOptionPane.ERROR_MESSAGE);
					}
					try {
						int excelSheetInt = Integer.parseInt(props
								.getProperty(StaticString.P_EXCEL_SHEET));
						reader.setSheet(excelSheetInt);
						persons = reader.read();
						if (persons.size() > 0) {
							button_save.setEnabled(true);
							JOptionPane.showMessageDialog(c,
									texte.getString("D_FILE_READ_TEXT")
											+ persons.size(),
									texte.getString("D_FILE_READ"),
									JOptionPane.INFORMATION_MESSAGE);
						} else {
							JOptionPane.showMessageDialog(c,
									texte.getString("D_ERROR_NO_RECORDS_TEXT"),
									texte.getString("D_ERROR"),
									JOptionPane.ERROR_MESSAGE);
						}
					} catch (Exception e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(c,
								texte.getString("D_ERROR_PARSING_TEXT"),
								texte.getString("D_ERROR"),
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
	}

	class SaveXMLListener implements ActionListener {
		public final void actionPerformed(ActionEvent e) {
			saveProps();
			JFileChooser c = new JFileChooser();
			File f = new File(System.getProperty("java.class.path"));
			File dir = f.getAbsoluteFile().getParentFile();
			c.setCurrentDirectory(dir);
			if (inputNotCorrect(c)) {
				return;
			} else {
				int rVal = c.showSaveDialog(Xsl2Xml.this);
				if (rVal == JFileChooser.APPROVE_OPTION) {
					IWriter writer;
					try {
						writer = new XMLWriter(props);
						writer.write(c.getSelectedFile(), persons);
						JOptionPane.showMessageDialog(c,
								texte.getString("D_XML_WRITTEN_TEXT"),
								texte.getString("D_XML_WRITTEN"),
								JOptionPane.INFORMATION_MESSAGE);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(c,
								texte.getString("D_ERROR_WRITING_XML_TEXT"),
								texte.getString("D_ERROR"),
								JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					}
				}
			}

		}
	}

	class MainWindowListener implements WindowListener {

		@Override
		public final void windowActivated(WindowEvent arg0) {
			// Do nothing
		}

		@Override
		public final void windowClosed(WindowEvent arg0) {
			// Do nothing
		}

		@Override
		public final void windowClosing(WindowEvent arg0) {
			saveProps();
			System.exit(0);
		}

		@Override
		public final void windowDeactivated(WindowEvent arg0) {
			// Do nothing
		}

		@Override
		public final void windowDeiconified(WindowEvent arg0) {
			// Do nothing
		}

		@Override
		public final void windowIconified(WindowEvent arg0) {
			// Do nothing
		}

		@Override
		public final void windowOpened(WindowEvent arg0) {
			// Do nothing
		}

	}

	public static final void main(String[] args) {
		run(new Xsl2Xml(), 400, 220);
	}

	public static final void run(JFrame frame, int width, int height) {
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setSize(width, height);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private final void loadStrings() {
		if (texte == null) {
			texte = ResourceBundle.getBundle(StaticString.STRINGS_BUNDLE);
		}
	}

	private final void loadProps() {
		if (props == null) {
			props = new Properties();
		}
		InputStream in;
		try {
			File propsFile = new File(StaticString.PROPS_NAME);
			if (propsFile.exists()) {
				in = new FileInputStream(propsFile);
			} else {
				in = ClassLoader.getSystemClassLoader().getResourceAsStream(
						StaticString.CONFIG_PATH + StaticString.PROPS_NAME);
			}
			if (in != null) {
				props.load(in);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private final void saveProps() {
		if (props == null) {
			props = new Properties();
		}

		// remove whitespaces
		tf_credID.setText(tf_credID.getText().replaceAll("\\s", ""));
		tf_credIBAN.setText(tf_credIBAN.getText().replaceAll("\\s", ""));
		tf_credBIC.setText(tf_credBIC.getText().replaceAll("\\s", ""));
		tf_execDate.setText(tf_execDate.getText().replaceAll("\\s", ""));
		tf_excelSheet.setText(tf_excelSheet.getText().replaceAll("\\s", ""));

		try {
			// trim execDate
			tf_execDate.setText(tf_execDate.getText().substring(0, 10));

			// set all props
			props.put(StaticString.P_CRED_NAME, tf_credName.getText());
			props.put(StaticString.P_CRED_ID, tf_credID.getText());
			props.put(StaticString.P_CRED_IBAN, tf_credIBAN.getText());
			props.put(StaticString.P_CRED_BIC, tf_credBIC.getText());
			props.put(StaticString.P_EXEC_DATE, tf_execDate.getText());
			props.put(StaticString.P_EXCEL_SHEET, tf_excelSheet.getText());
			// save properties to project root folder
			props.store(new FileOutputStream(StaticString.PROPS_NAME), null);

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private final void createGui() {
		button_open = new JButton(texte.getString("BUTTON_OPEN"));
		button_save = new JButton(texte.getString("BUTTON_SAVE"));

		this.setTitle(texte.getString("TITLE"));

		this.addWindowListener(new MainWindowListener());
		Container c = getContentPane();
		JPanel mainP = new JPanel();
		mainP.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();

		// Text Pannel
		JPanel p1 = new JPanel();
		p1.setLayout(new GridBagLayout());

		// credName
		addLabel(texte.getString("TFL_CRED_NAME"), p1, 0, 0);
		addTextField(
				tf_credName = new JTextField(
						props.getProperty(StaticString.P_CRED_NAME), 20), p1,
				1, 0);

		// credID
		addLabel(texte.getString("TFL_CRED_ID"), p1, 0, 1);
		addTextField(
				tf_credID = new JTextField(
						props.getProperty(StaticString.P_CRED_ID), 20), p1, 1,
				1);

		// credIBAN
		addLabel(texte.getString("TFL_CRED_IBAN"), p1, 0, 2);
		addTextField(
				tf_credIBAN = new JTextField(
						props.getProperty(StaticString.P_CRED_IBAN), 20), p1,
				1, 2);

		// credBIC
		addLabel(texte.getString("TFL_CRED_BIC"), p1, 0, 3);
		addTextField(
				tf_credBIC = new JTextField(
						props.getProperty(StaticString.P_CRED_BIC), 20), p1, 1,
				3);

		// execDate
		addLabel(texte.getString("TFL_EXEC_DATE"), p1, 0, 4);
		addTextField(
				tf_execDate = new JTextField(
						props.getProperty(StaticString.P_EXEC_DATE), 20), p1,
				1, 4);

		// excelSheet
		addLabel(texte.getString("TFL_EXCEL_SHEET"), p1, 0, 5);
		addTextField(
				tf_excelSheet = new JTextField(
						props.getProperty(StaticString.P_EXCEL_SHEET)), p1, 1,
				5);

		// ButtonPannel
		// Buttons
		JPanel p2 = new JPanel();
		p2.setLayout(new GridBagLayout());
		// Open-Button
		button_open.addActionListener(new OpenExcelListener());
		gbc.gridx = 0;
		gbc.gridy = 0;
		p2.add(button_open, gbc);
		// Save-Button
		button_save.addActionListener(new SaveXMLListener());
		button_save.setEnabled(false);
		gbc.gridy = 1;
		p2.add(button_save, gbc);

		// Assemble
		gbc.gridx = 0;
		gbc.gridy = 0;
		mainP.add(p1, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		mainP.add(p2, gbc);

		c.add(mainP);
	}

	private final void addLabel(String name, Container c, int gridx, int gridy) {
		JLabel label = new JLabel(name, SwingConstants.LEFT);
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		c.add(label, gbc);
	}

	private final void addTextField(JTextField tf, Container c, int gridx,
			int gridy) {
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		c.add(tf, gbc);
	}

	private final boolean inputNotCorrect(JFileChooser c) {
		if (tf_credName.getText().equals("") || tf_credID.getText().equals("")
				|| tf_credIBAN.getText().equals("")
				|| tf_credBIC.getText().equals("")
				|| tf_execDate.getText().equals("")
				|| tf_excelSheet.getText().equals("")) {
			JOptionPane.showMessageDialog(c,
					texte.getString("D_MISSING_VALUES_TEXT"),
					texte.getString("D_MISSING_VALUES"),
					JOptionPane.ERROR_MESSAGE);
			return true;
		} else if (Check.wrongDate(props.getProperty(StaticString.P_EXEC_DATE))) {
			JOptionPane.showMessageDialog(c,
					texte.getString("D_ILLEGAL_DATE_TEXT"),
					texte.getString("D_ILLEGAL_DATE"),
					JOptionPane.ERROR_MESSAGE);
			return true;
		} else if (Check.notInt(props.getProperty(StaticString.P_EXCEL_SHEET))) {
			JOptionPane.showMessageDialog(c,
					texte.getString("D_INVALID_SHEET_TEXT"),
					texte.getString("D_INVALID_SHEET"),
					JOptionPane.ERROR_MESSAGE);
			return true;
		}
		return false;
	}
}
