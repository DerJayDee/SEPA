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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
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
import de.dlrg_rodenkirchen.sepa.xml.XMLWriter;

@SuppressWarnings("serial")
public final class Xsl2Xml extends JFrame {

	private JButton button_open;
	private JButton button_save;
	private JButton button_next1;
	private JButton button_next2;
	private JButton button_prev;

	private JTextField tf_credName;
	private JTextField tf_credID;
	private JTextField tf_credIBAN;
	private JTextField tf_credBIC;
	private JTextField tf_execDate;

	private JComboBox<String> cb_excelSheetName;
	private JComboBox<String> cb_id;
	private JComboBox<String> cb_vname;
	private JComboBox<String> cb_nname;
	private JComboBox<String> cb_inhaber;
	private JComboBox<String> cb_signed;
	private JComboBox<String> cb_bic;
	private JComboBox<String> cb_iban;
	private JComboBox<String> cb_mandatsref;
	private JComboBox<String> cb_betrag;
	private JComboBox<String> cb_zweck;
	private JComboBox<String> cb_sequenztyp;

	private Properties props;

	private Properties zuordnung;

	private GridBagConstraints gbc;

	private int step;
	private static final int STEP_DEBITOR_INFO = 1;
	private static final int STEP_SHEET_SELECTION = 2;
	private static final int STEP_COLUMN_MATCHING = 3;
	private static final int STEP_ITEMS_READ = 4;

	private ExcelReader reader;
	private ArrayList<Person> firsts;
	private ArrayList<Person> recurrings;

	private ResourceBundle texte;

	public Xsl2Xml() {
		loadStrings();
		loadProps();
		loadZuordnung();
		this.setTitle(texte.getString("TITLE"));
		initButtons();
		createDebitorInfoGui();
	}

	final class OpenExcelListener implements ActionListener {
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
				reader = null;
				if (rVal == JFileChooser.APPROVE_OPTION) {
					if (reader == null) {
						try {
							reader = new ExcelReader(zuordnung);
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
						createSheetSelectionGui();
					} catch (Exception e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(c,
								texte.getString("D_ERROR_READ_FILE_TEXT"),
								texte.getString("D_ERROR"),
								JOptionPane.ERROR_MESSAGE);
					}

				}
			}
		}
	}

	final class PrevListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			switch (step) {
			case STEP_DEBITOR_INFO:
				break;
			case STEP_SHEET_SELECTION:
				createDebitorInfoGui();
				break;
			case STEP_COLUMN_MATCHING:
				createSheetSelectionGui();
				break;
			case STEP_ITEMS_READ:
				createColumnMatchingGui();
			default:
				break;
			}
		}
	}

	final class OpenSheetListener implements ActionListener {
		public final void actionPerformed(ActionEvent e) {
			saveProps();
			Container c = getContentPane();
			try {
				String sheetName = props
						.getProperty(StaticString.P_EXCEL_SHEET);
				reader.setSheet(sheetName);
				createColumnMatchingGui();
			} catch (Exception e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(c,
						texte.getString("D_ERROR_PARSING_TEXT"),
						texte.getString("D_ERROR"), JOptionPane.ERROR_MESSAGE);
			}
		}

	}

	final class ReadSheetListener implements ActionListener {
		public final void actionPerformed(ActionEvent e) {
			Container c = getContentPane();
			if (zuordnungNotCorrect()) {
				JOptionPane.showMessageDialog(c,
						texte.getString("D_ERROR_INVALID_MATCHING"),
						texte.getString("D_ERROR"), JOptionPane.ERROR_MESSAGE);
			} else {
				saveZuordnung();
				try {
					HashMap<String, ArrayList<Person>> personsMap = reader
							.read();
					firsts = personsMap.get(StaticString.PERSONS_FIRST);
					recurrings = personsMap.get(StaticString.PERSONS_RECURRING);

					if (firsts.size() > 0 || recurrings.size() > 0) {
						createItemsReadGui(firsts.size() + recurrings.size());
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

	final class SaveXMLListener implements ActionListener {
		public final void actionPerformed(ActionEvent e) {
			JFileChooser c = new JFileChooser();
			File f = new File(System.getProperty("java.class.path"));
			File dir = f.getAbsoluteFile().getParentFile();
			c.setCurrentDirectory(dir);
			if (inputNotCorrect(c)) {
				return;
			} else {
				int rVal = c.showSaveDialog(Xsl2Xml.this);
				if (rVal == JFileChooser.APPROVE_OPTION) {
					XMLWriter writer;
					try {
						writer = new XMLWriter(props);
						writer.write(c.getSelectedFile(), firsts, recurrings);
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

	final class MainWindowListener implements WindowListener {

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
			saveZuordnung();
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
		run(new Xsl2Xml());
	}

	public static final void run(JFrame frame) {
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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

		try {
			// trim execDate
			tf_execDate.setText(tf_execDate.getText().substring(0, 10));

			// set all props
			props.put(StaticString.P_CRED_NAME, tf_credName.getText());
			props.put(StaticString.P_CRED_ID, tf_credID.getText());
			props.put(StaticString.P_CRED_IBAN, tf_credIBAN.getText());
			props.put(StaticString.P_CRED_BIC, tf_credBIC.getText());
			props.put(StaticString.P_EXEC_DATE, tf_execDate.getText());
			if (cb_excelSheetName != null) {
				props.put(StaticString.P_EXCEL_SHEET, cb_excelSheetName
						.getSelectedItem().toString());
			}
			// save properties to project root folder
			props.store(new FileOutputStream(StaticString.PROPS_NAME), null);

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private final void loadZuordnung() {
		if (zuordnung == null) {
			zuordnung = new Properties();
		}
		try {
			InputStream in;
			File propsFile = new File(StaticString.ZUORDNUNGS_PROPS_NAME);
			if (propsFile.exists()) {
				in = new FileInputStream(propsFile);
				zuordnung.load(in);
				in.close();
			} else {
				in = ClassLoader.getSystemClassLoader().getResourceAsStream(
						StaticString.CONFIG_PATH
								+ StaticString.ZUORDNUNGS_PROPS_NAME);
				zuordnung.load(in);
				in.close();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private final void saveZuordnung() {
		if (zuordnung == null) {
			zuordnung = new Properties();
		}
		try {
			// set all props
			zuordnung.put(StaticString.Z_ID,
					Integer.toString(cb_id.getSelectedIndex()));
			zuordnung.put(StaticString.Z_VORNAME,
					Integer.toString(cb_vname.getSelectedIndex()));
			zuordnung.put(StaticString.Z_NACHNAME,
					Integer.toString(cb_nname.getSelectedIndex()));
			zuordnung.put(StaticString.Z_INHABER,
					Integer.toString(cb_inhaber.getSelectedIndex()));
			zuordnung.put(StaticString.Z_SIGNED,
					Integer.toString(cb_signed.getSelectedIndex()));
			zuordnung.put(StaticString.Z_BIC,
					Integer.toString(cb_bic.getSelectedIndex()));
			zuordnung.put(StaticString.Z_IBAN,
					Integer.toString(cb_iban.getSelectedIndex()));
			zuordnung.put(StaticString.Z_REFERENZ,
					Integer.toString(cb_mandatsref.getSelectedIndex()));
			zuordnung.put(StaticString.Z_BEITRAG,
					Integer.toString(cb_betrag.getSelectedIndex()));
			zuordnung.put(StaticString.Z_ZWECK,
					Integer.toString(cb_zweck.getSelectedIndex()));
			zuordnung.put(StaticString.Z_SEQUENZTYP,
					Integer.toString(cb_sequenztyp.getSelectedIndex()));
			// save properties to project root folder
			zuordnung.store(new FileOutputStream(
					StaticString.ZUORDNUNGS_PROPS_NAME), null);

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private final void initButtons() {
		button_open = new JButton(texte.getString("BUTTON_OPEN"));
		button_open.addActionListener(new OpenExcelListener());
		button_save = new JButton(texte.getString("BUTTON_SAVE"));
		button_save.addActionListener(new SaveXMLListener());
		button_next1 = new JButton(texte.getString("BUTTON_NEXT"));
		button_next1.addActionListener(new OpenSheetListener());
		button_next2 = new JButton(texte.getString("BUTTON_NEXT"));
		button_next2.addActionListener(new ReadSheetListener());
		button_prev = new JButton(texte.getString("BUTTON_PREV"));
		button_prev.addActionListener(new PrevListener());
	}

	private final void createDebitorInfoGui() {
		step = STEP_DEBITOR_INFO;

		this.addWindowListener(new MainWindowListener());
		JPanel mainP = new JPanel();
		mainP.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();

		// Text Pannel
		JPanel p1 = new JPanel();
		p1.setLayout(new GridBagLayout());

		// credName
		addContainer(new JLabel(texte.getString("TFL_CRED_NAME"),
				SwingConstants.LEFT), p1, 0, 0);
		addContainer(
				tf_credName = new JTextField(
						props.getProperty(StaticString.P_CRED_NAME), 20), p1,
				1, 0);

		// credID
		addContainer(new JLabel(texte.getString("TFL_CRED_ID"),
				SwingConstants.LEFT), p1, 0, 1);
		addContainer(
				tf_credID = new JTextField(
						props.getProperty(StaticString.P_CRED_ID), 20), p1, 1,
				1);

		// credIBAN
		addContainer(new JLabel(texte.getString("TFL_CRED_IBAN"),
				SwingConstants.LEFT), p1, 0, 2);
		addContainer(
				tf_credIBAN = new JTextField(
						props.getProperty(StaticString.P_CRED_IBAN), 20), p1,
				1, 2);

		// credBIC
		addContainer(new JLabel(texte.getString("TFL_CRED_BIC"),
				SwingConstants.LEFT), p1, 0, 3);
		addContainer(
				tf_credBIC = new JTextField(
						props.getProperty(StaticString.P_CRED_BIC), 20), p1, 1,
				3);

		// execDate
		addContainer(new JLabel(texte.getString("TFL_EXEC_DATE"),
				SwingConstants.LEFT), p1, 0, 4);
		addContainer(
				tf_execDate = new JTextField(
						props.getProperty(StaticString.P_EXEC_DATE), 20), p1,
				1, 4);

		// ButtonPannel
		JPanel p2 = new JPanel();
		p2.setLayout(new GridBagLayout());

		// Open-Button
		addContainer(button_open, p2, 0, 0);

		// Assemble
		addContainer(p1, mainP, 0, 0);
		addContainer(p2, mainP, 0, 1);

		setGui(mainP);
	}

	private final void createSheetSelectionGui() {
		step = STEP_SHEET_SELECTION;

		JPanel mainP = new JPanel();
		mainP.setLayout(new GridBagLayout());

		JPanel p1 = new JPanel();
		p1.setLayout(new GridBagLayout());

		addContainer(new JLabel(texte.getString("TFL_EXCEL_SHEET"),
				SwingConstants.LEFT), p1, 0, 0);
		cb_excelSheetName = new JComboBox<String>(reader.getSheetNames());
		if (Check.arrayContainsString(reader.getSheetNames(),
				props.getProperty(StaticString.P_EXCEL_SHEET))) {
			cb_excelSheetName.setSelectedItem(props
					.getProperty(StaticString.P_EXCEL_SHEET));
		}
		addContainer(cb_excelSheetName, p1, 1, 0);

		JPanel p2 = new JPanel();
		p2.setLayout(new GridBagLayout());
		addContainer(button_prev, p2, 0, 1);

		addContainer(button_next1, p2, 1, 1);

		// Assemble
		addContainer(p1, mainP, 0, 0);
		addContainer(p2, mainP, 0, 1);

		setGui(mainP);
	}

	private final void createColumnMatchingGui() {
		step = STEP_COLUMN_MATCHING;

		JPanel mainP = new JPanel();
		mainP.setLayout(new GridBagLayout());

		JPanel p1 = new JPanel();
		p1.setLayout(new GridBagLayout());

		addContainer(new JLabel(texte.getString("L_CELL_CONTENT"),
				SwingConstants.LEFT), p1, 0, 0);
		addContainer(new JLabel(texte.getString("L_CELL_LABELS"),
				SwingConstants.LEFT), p1, 1, 0);

		String[] labels = reader.getLabels();
		int zIndex = -1;

		// dbtID
		addContainer(
				new JLabel(texte.getString("CBL_ID"), SwingConstants.LEFT), p1,
				0, 1);
		addContainer(cb_id = new JComboBox<String>(labels), p1, 1, 1);
		zIndex = Integer.parseInt(zuordnung.getProperty(StaticString.Z_ID));
		setZuordnungsChoice(cb_id, zIndex, labels.length);

		// dbtNName
		addContainer(new JLabel(texte.getString("CBL_NNAME"),
				SwingConstants.LEFT), p1, 0, 2);
		addContainer(cb_nname = new JComboBox<String>(labels), p1, 1, 2);
		zIndex = Integer.parseInt(zuordnung
				.getProperty(StaticString.Z_NACHNAME));
		setZuordnungsChoice(cb_nname, zIndex, labels.length);

		// dbtVName
		addContainer(new JLabel(texte.getString("CBL_VNAME"),
				SwingConstants.LEFT), p1, 0, 3);
		addContainer(cb_vname = new JComboBox<String>(labels), p1, 1, 3);
		zIndex = Integer
				.parseInt(zuordnung.getProperty(StaticString.Z_VORNAME));
		setZuordnungsChoice(cb_vname, zIndex, labels.length);

		// dbtSigned
		addContainer(new JLabel(texte.getString("CBL_SIGNED"),
				SwingConstants.LEFT), p1, 0, 4);
		addContainer(cb_signed = new JComboBox<String>(labels), p1, 1, 4);
		zIndex = Integer.parseInt(zuordnung.getProperty(StaticString.Z_SIGNED));
		setZuordnungsChoice(cb_signed, zIndex, labels.length);

		// dbtIBAN
		addContainer(new JLabel(texte.getString("CBL_IBAN"),
				SwingConstants.LEFT), p1, 0, 5);
		addContainer(cb_iban = new JComboBox<String>(labels), p1, 1, 5);
		zIndex = Integer.parseInt(zuordnung.getProperty(StaticString.Z_IBAN));
		setZuordnungsChoice(cb_iban, zIndex, labels.length);

		// dbtBIC
		addContainer(
				new JLabel(texte.getString("CBL_BIC"), SwingConstants.LEFT),
				p1, 0, 6);
		addContainer(cb_bic = new JComboBox<String>(labels), p1, 1, 6);
		zIndex = Integer.parseInt(zuordnung.getProperty(StaticString.Z_BIC));
		setZuordnungsChoice(cb_bic, zIndex, labels.length);

		// dbtInhaber
		addContainer(new JLabel(texte.getString("CBL_INHABER"),
				SwingConstants.LEFT), p1, 0, 7);
		addContainer(cb_inhaber = new JComboBox<String>(labels), p1, 1, 7);
		zIndex = Integer
				.parseInt(zuordnung.getProperty(StaticString.Z_INHABER));
		setZuordnungsChoice(cb_inhaber, zIndex, labels.length);

		// dbtBetrag
		addContainer(new JLabel(texte.getString("CBL_BETRAG"),
				SwingConstants.LEFT), p1, 0, 8);
		addContainer(cb_betrag = new JComboBox<String>(labels), p1, 1, 8);
		zIndex = Integer
				.parseInt(zuordnung.getProperty(StaticString.Z_BEITRAG));
		setZuordnungsChoice(cb_betrag, zIndex, labels.length);

		// dbtMandatsreferenz
		addContainer(new JLabel(texte.getString("CBL_MANDATSREF"),
				SwingConstants.LEFT), p1, 0, 9);
		addContainer(cb_mandatsref = new JComboBox<String>(labels), p1, 1, 9);
		zIndex = Integer.parseInt(zuordnung
				.getProperty(StaticString.Z_REFERENZ));
		setZuordnungsChoice(cb_mandatsref, zIndex, labels.length);

		// dbtZweck
		addContainer(new JLabel(texte.getString("CBL_ZWECK"),
				SwingConstants.LEFT), p1, 0, 10);
		addContainer(cb_zweck = new JComboBox<String>(labels), p1, 1, 10);
		zIndex = Integer.parseInt(zuordnung.getProperty(StaticString.Z_ZWECK));
		setZuordnungsChoice(cb_zweck, zIndex, labels.length);

		// dbtSequenztyp
		addContainer(new JLabel(texte.getString("CBL_SEQUENZTYP"),
				SwingConstants.LEFT), p1, 0, 11);
		addContainer(cb_sequenztyp = new JComboBox<String>(labels), p1, 1, 11);
		zIndex = Integer.parseInt(zuordnung
				.getProperty(StaticString.Z_SEQUENZTYP));
		setZuordnungsChoice(cb_sequenztyp, zIndex, labels.length);

		JPanel p2 = new JPanel();
		p2.setLayout(new GridBagLayout());

		addContainer(button_prev, p2, 0, 1);

		addContainer(button_next2, p2, 1, 1);

		// Assemble
		addContainer(p1, mainP, 0, 0);
		addContainer(p2, mainP, 0, 1);

		setGui(mainP);
	}

	private final void createItemsReadGui(int read) {
		step = STEP_ITEMS_READ;

		JPanel mainP = new JPanel();
		mainP.setLayout(new GridBagLayout());

		JLabel p1 = new JLabel(texte.getString("L_ITEMS_READ") + read,
				SwingConstants.CENTER);

		JPanel p2 = new JPanel();
		p2.setLayout(new GridBagLayout());

		addContainer(button_prev, p2, 0, 1);

		addContainer(button_save, p2, 1, 1);

		// Assemble
		addContainer(p1, mainP, 0, 0);
		addContainer(p2, mainP, 0, 1);

		setGui(mainP);
	}

	private final void addContainer(Container tf, Container c, int gridx,
			int gridy) {
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		c.add(tf, gbc);
	}

	private final void setGui(JPanel panel) {
		Container c = getContentPane();
		c.removeAll();
		c.add(panel);
		this.revalidate();
		this.repaint();
		this.pack();
		this.setLocationRelativeTo(null);
	}

	private final boolean inputNotCorrect(JFileChooser c) {
		if (tf_credName.getText().equals("") || tf_credID.getText().equals("")
				|| tf_credIBAN.getText().equals("")
				|| tf_credBIC.getText().equals("")
				|| tf_execDate.getText().equals("")) {
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
		}
		return false;
	}

	private final boolean zuordnungNotCorrect() {
		int[] cb_values = new int[] { cb_id.getSelectedIndex(),
				cb_vname.getSelectedIndex(), cb_nname.getSelectedIndex(),
				cb_inhaber.getSelectedIndex(), cb_signed.getSelectedIndex(),
				cb_bic.getSelectedIndex(), cb_iban.getSelectedIndex(),
				cb_mandatsref.getSelectedIndex(), cb_betrag.getSelectedIndex(),
				cb_zweck.getSelectedIndex(), cb_sequenztyp.getSelectedIndex() };
		Set<Integer> values = new HashSet<Integer>();
		for (int i = 0; i < cb_values.length; i++) {
			if (!values.add(cb_values[i]))
				return true;
		}
		return false;
	}

	private final void setZuordnungsChoice(JComboBox<String> cb, int index,
			int length) {
		if (index >= 0 && index < length) {
			cb.setSelectedIndex(index);
		} else {
			cb.setSelectedIndex(-1);
		}
	}
}
