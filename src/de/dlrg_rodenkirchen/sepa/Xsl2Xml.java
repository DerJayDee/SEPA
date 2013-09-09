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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import de.dlrg_rodenkirchen.sepa.helper.Person;
import de.dlrg_rodenkirchen.sepa.helper.Strings;
import de.dlrg_rodenkirchen.sepa.interfaces.Reader;
import de.dlrg_rodenkirchen.sepa.interfaces.Writer;
import de.dlrg_rodenkirchen.sepa.xls.XLSFilter;
import de.dlrg_rodenkirchen.sepa.xls.XLSReader;
import de.dlrg_rodenkirchen.sepa.xlsx.XLSXFilter;
import de.dlrg_rodenkirchen.sepa.xlsx.XLSXReader;
import de.dlrg_rodenkirchen.sepa.xml.XMLWriter;

public class Xsl2Xml extends JFrame {

	private static final long serialVersionUID = 737038282745995221L;

	private JButton button_open = new JButton("XLS-Datei öffnen...");
	private JButton button_save = new JButton("XML-Datei speichern unter...");

	private JTextField tf_credName;
	private JTextField tf_credID;
	private JTextField tf_credIBAN;
	private JTextField tf_credBIC;
	private JTextField tf_execDate;
	private JTextField tf_excelSheet;

	private Properties props;

	private GridBagConstraints gbc;

	private ArrayList<Person> persons;

	public Xsl2Xml() {
		loadProps();
		createGui();
	}

	class OpenXLSListener implements ActionListener {
		public final void actionPerformed(ActionEvent e) {
			saveProps();
			JFileChooser c = new JFileChooser();
			File f = new File(System.getProperty("java.class.path"));
			File dir = f.getAbsoluteFile().getParentFile();
			c.setCurrentDirectory(dir);
			if (tf_credName.getText().equals("")
					|| tf_credID.getText().equals("")
					|| tf_credIBAN.getText().equals("")
					|| tf_credBIC.getText().equals("")
					|| tf_execDate.getText().equals("")
					|| tf_excelSheet.getText().equals("")) {
				JOptionPane.showMessageDialog(c,
						"Sie müssen alle Werte eingeben", "Fehlende Werte",
						JOptionPane.ERROR_MESSAGE);
			} else if (wrongDate()) {
				JOptionPane
						.showMessageDialog(
								c,
								"Das Datum muss im Format 'yyyy-mm-dd' eingegeben werden und in der Zukunft liegen.",
								"Ungültiges Datum", JOptionPane.ERROR_MESSAGE);
			} else if (notInt()) {
				JOptionPane.showMessageDialog(c,
						"Die Nummer des Excel Sheets muss eine Zahl sein.",
						"Ungültiges Excel Sheet", JOptionPane.ERROR_MESSAGE);
			} else {
				FileFilter ff = new XLSXFilter();
				c.addChoosableFileFilter(ff);
				c.setFileFilter(ff);
				ff = new XLSFilter();
				c.addChoosableFileFilter(ff);
				int rVal = c.showOpenDialog(Xsl2Xml.this);
				Reader reader = null;
				if (rVal == JFileChooser.APPROVE_OPTION) {
					if (reader == null) {
						try {
							String filename = c.getSelectedFile().getName();
							if (filename.endsWith("xls")) {
								reader = new XLSReader();
							} else if (filename.endsWith("xlsx")) {
								reader = new XLSXReader();
							}

						} catch (Exception e1) {
							e1.printStackTrace();
							JOptionPane
									.showMessageDialog(
											c,
											"Ein Fehler beim Einlesen der Zuordnungskonfiguration ist aufgetreten.",
											"Fehler", JOptionPane.ERROR_MESSAGE);
						}
					}
					try {
						reader.setFile(c.getSelectedFile());
					} catch (Exception e1) {
						e1.printStackTrace();
						JOptionPane
								.showMessageDialog(
										c,
										"Ein Fehler ist beim Einlesen der Excel-Date ist aufgetreten.",
										"Fehler", JOptionPane.ERROR_MESSAGE);
					}
					try {
						int excelSheetInt = Integer.parseInt(props
								.getProperty(Strings.P_EXCEL_SHEET.toString()));
						reader.setSheet(excelSheetInt);
						persons = reader.read();
						if (persons.size() > 0) {
							button_save.setEnabled(true);
							JOptionPane.showMessageDialog(
									c,
									"Anzahl eingelesene Einträge: "
											+ persons.size(), "XLS eingelesen",
									JOptionPane.INFORMATION_MESSAGE);
						} else {
							JOptionPane.showMessageDialog(c,
									"Keine Einträge gefunden", "Fehler",
									JOptionPane.ERROR_MESSAGE);
						}
					} catch (Exception e1) {
						e1.printStackTrace();
						JOptionPane
								.showMessageDialog(
										c,
										"Ein Fehler ist beim Parsen der Excel-Date ist aufgetreten.",
										"Fehler", JOptionPane.ERROR_MESSAGE);
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
			if (tf_credName.getText().equals("")
					|| tf_credID.getText().equals("")
					|| tf_credIBAN.getText().equals("")
					|| tf_credBIC.getText().equals("")
					|| tf_execDate.getText().equals("")
					|| tf_excelSheet.getText().equals("")) {
				JOptionPane.showMessageDialog(c,
						"Sie müssen alle Werte eingeben", "Fehlende Werte",
						JOptionPane.ERROR_MESSAGE);
			} else if (wrongDate()) {
				JOptionPane
						.showMessageDialog(
								c,
								"Das Datum muss im Format 'yyyy-mm-dd' eingegeben werden und in der Zukunft liegen.",
								"Ungültiges Datum", JOptionPane.ERROR_MESSAGE);
			} else if (notInt()) {
				JOptionPane.showMessageDialog(c,
						"Die Nummer des Excel Sheets muss eine Zahl sein.",
						"Ungültiges Excel Sheet", JOptionPane.ERROR_MESSAGE);
			} else {
				int rVal = c.showSaveDialog(Xsl2Xml.this);
				if (rVal == JFileChooser.APPROVE_OPTION) {
					Writer writer;
					try {
						writer = new XMLWriter(props);
						writer.write(c.getSelectedFile(), persons);
						JOptionPane.showMessageDialog(c,
								"XML-Datei erfolgreich geschrieben",
								"XML geschrieben",
								JOptionPane.INFORMATION_MESSAGE);
					} catch (Exception e1) {
						JOptionPane
								.showMessageDialog(
										c,
										"Es ist ein Fehler beim Schreiben der XML-Datei aufgetreten.",
										"Fehler", JOptionPane.ERROR_MESSAGE);
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

	private final void loadProps() {
		if (props == null) {
			props = new Properties();
		}
		InputStream in;
		try {
			File propsFile = new File(Strings.PROPS_NAME.toString());
			if (propsFile.exists()) {
				in = new FileInputStream(propsFile);
			} else {
				in = Xsl2Xml.class.getClassLoader().getResourceAsStream(
						Strings.PROPS_NAME.toString());
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
			props.put(Strings.P_CRED_NAME.toString(), tf_credName.getText());
			props.put(Strings.P_CRED_ID.toString(), tf_credID.getText());
			props.put(Strings.P_CRED_IBAN.toString(), tf_credIBAN.getText());
			props.put(Strings.P_CRED_BIC.toString(), tf_credBIC.getText());
			props.put(Strings.P_EXEC_DATE.toString(), tf_execDate.getText());
			props.put(Strings.P_EXCEL_SHEET.toString(), tf_excelSheet.getText());
			// save properties to project root folder
			props.store(new FileOutputStream(Strings.PROPS_NAME.toString()),
					null);

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private final void createGui() {
		this.setTitle("XLS SEPA Converter");

		this.addWindowListener(new MainWindowListener());
		Container c = getContentPane();
		JPanel mainP = new JPanel();
		mainP.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();

		// Text Pannel
		JPanel p1 = new JPanel();
		p1.setLayout(new GridBagLayout());

		// credName
		addLabel(Strings.TFL_CRED_NAME.toString(), p1, 0, 0);
		addTextField(
				tf_credName = new JTextField(
						props.getProperty(Strings.P_CRED_NAME.toString()), 20),
				p1, 1, 0);

		// credID
		addLabel(Strings.TFL_CRED_ID.toString(), p1, 0, 1);
		addTextField(
				tf_credID = new JTextField(props.getProperty(Strings.P_CRED_ID
						.toString()), 20), p1, 1, 1);

		// credIBAN
		addLabel(Strings.TFL_CRED_IBAN.toString(), p1, 0, 2);
		addTextField(
				tf_credIBAN = new JTextField(
						props.getProperty(Strings.P_CRED_IBAN.toString()), 20),
				p1, 1, 2);

		// credBIC
		addLabel(Strings.TFL_CRED_BIC.toString(), p1, 0, 3);
		addTextField(
				tf_credBIC = new JTextField(
						props.getProperty(Strings.P_CRED_BIC.toString()), 20),
				p1, 1, 3);

		// execDate
		addLabel(Strings.TFL_EXEC_DATE.toString(), p1, 0, 4);
		addTextField(
				tf_execDate = new JTextField(
						props.getProperty(Strings.P_EXEC_DATE.toString()), 20),
				p1, 1, 4);

		// excelSheet
		addLabel(Strings.TFL_EXCEL_SHEET.toString(), p1, 0, 5);
		addTextField(
				tf_excelSheet = new JTextField(
						props.getProperty(Strings.P_EXCEL_SHEET.toString())),
				p1, 1, 5);

		// ButtonPannel
		// Buttons
		JPanel p2 = new JPanel();
		p2.setLayout(new GridBagLayout());
		// Open-Button
		button_open.addActionListener(new OpenXLSListener());
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

	private final boolean wrongDate() {
		String datum = props.getProperty(Strings.P_EXEC_DATE.toString());
		SimpleDateFormat sdfToDate = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date execDate = sdfToDate.parse(datum);
			Date now = new Date();
			if (now.after(execDate)) {
				return true;
			}
		} catch (Exception e1) {
			return true;
		}
		return false;
	}

	private final boolean notInt() {
		String excelSheet = props.getProperty(Strings.P_EXCEL_SHEET.toString());
		return !excelSheet.matches("[0-9]+");
	}
}
