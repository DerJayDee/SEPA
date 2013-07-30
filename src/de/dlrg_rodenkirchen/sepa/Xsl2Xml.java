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
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
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

public class Xsl2Xml extends JFrame {

	private static final long serialVersionUID = 737038282745995221L;

	private static final String propsName = "SEPA_config.cfg";
	private static final String p_credName = "credName";
	private static final String p_credID = "credId";
	private static final String p_credIBAN = "credIBAN";
	private static final String p_credBIC = "credBIC";
	private static final String p_execDate = "execDate";

	private static final String tfl_credName = "Gl�ubiger:";
	private static final String tfl_credID = "Gl�ubiger-ID:";
	private static final String tfl_credIBAN = "Gl�ubiger-IBAN:";
	private static final String tfl_credBIC = "Gl�ubiger-BIC:";
	private static final String tfl_execDate = "Datum der Ausf�hrung:";

	private JButton open = new JButton("XLS-Datei �ffnen...");
	private JButton save = new JButton("XML-Datei speichern unter...");

	private JTextField tf_credName;
	private JTextField tf_credID;
	private JTextField tf_credIBAN;
	private JTextField tf_credBIC;
	private JTextField tf_execDate;

	private Properties props;

	private GridBagConstraints gbc;

	private XMLCreator xmlc;

	public Xsl2Xml() {
		loadProps();
		createGui();
	}

	class OpenXLSListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JFileChooser c = new JFileChooser();
			FileFilter xlsff = new XLSFilter();
			c.addChoosableFileFilter(xlsff);
			c.setFileFilter(xlsff);
			int rVal = c.showOpenDialog(Xsl2Xml.this);
			if (rVal == JFileChooser.APPROVE_OPTION) {
				if (xmlc == null) {
					xmlc = new XMLCreator(props);
				}
				try {
					int count = xmlc.readExcel(c.getSelectedFile());
					if (count > 0) {
						save.setEnabled(true);
						JOptionPane.showMessageDialog(c,
								"Anzahl eingelesene Eintr�ge: " + count,
								"XLS eingelesen",
								JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(c,
								"Keine Eintr�ge gefunden", "Fehler",
								JOptionPane.ERROR_MESSAGE);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			if (rVal == JFileChooser.CANCEL_OPTION) {
				// TODO Vielleicht mal was mit machen
			}
		}
	}

	class SaveXMLListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			saveProps();
			JFileChooser c = new JFileChooser();
			if (tf_credName.getText().equals("")
					|| tf_credID.getText().equals("")
					|| tf_credIBAN.getText().equals("")
					|| tf_credBIC.getText().equals("")
					|| tf_execDate.getText().equals("")) {
				JOptionPane.showMessageDialog(c,
						"Sie m�ssen alle Werte eingeben", "Fehlende Werte",
						JOptionPane.ERROR_MESSAGE);
			} else if (wrongDate()) {
				JOptionPane
						.showMessageDialog(
								c,
								"Das Datum muss im Format 'yyyy-mm-dd' eingegeben werden und in der Zukunft liegen.",
								"Ung�ltiges Datum", JOptionPane.ERROR_MESSAGE);
			} else {
				int rVal = c.showSaveDialog(Xsl2Xml.this);
				if (rVal == JFileChooser.APPROVE_OPTION) {
					xmlc.setProps(props);
					try {
						xmlc.writeXML(c.getSelectedFile());
						JOptionPane.showMessageDialog(c,
								"XML-Datei erfolgreich geschrieben",
								"XML geschrieben",
								JOptionPane.INFORMATION_MESSAGE);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				if (rVal == JFileChooser.CANCEL_OPTION) {
					// TODO Vielleicht mal was mit machen
				}
			}

		}
	}

	class MainWindowListener implements WindowListener {

		@Override
		public void windowActivated(WindowEvent arg0) {
			// Do nothing
		}

		@Override
		public void windowClosed(WindowEvent arg0) {
			// Do nothing
		}

		@Override
		public void windowClosing(WindowEvent arg0) {
			saveProps();
			System.exit(0);
		}

		@Override
		public void windowDeactivated(WindowEvent arg0) {
			// Do nothing
		}

		@Override
		public void windowDeiconified(WindowEvent arg0) {
			// Do nothing
		}

		@Override
		public void windowIconified(WindowEvent arg0) {
			// Do nothing
		}

		@Override
		public void windowOpened(WindowEvent arg0) {
			// Do nothing
		}

	}

	public static void main(String[] args) {
		run(new Xsl2Xml(), 400, 200);
	}

	public static void run(JFrame frame, int width, int height) {
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setSize(width, height);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private void loadProps() {
		if (props == null) {
			props = new Properties();
		}
		InputStream in;
		try {
			File propsFile = new File(propsName);
			if (propsFile.exists()) {
				in = new FileInputStream(propsFile);
			} else {
				in = Xsl2Xml.class.getClassLoader().getResourceAsStream(
						propsName);
			}
			if (in != null) {
				props.load(in);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void saveProps() {
		if (props == null) {
			props = new Properties();
		}
		try {
			// remove whitespaces
			tf_credID.setText(tf_credID.getText().replaceAll("\\s",""));
			tf_credIBAN.setText(tf_credIBAN.getText().replaceAll("\\s",""));
			tf_credBIC.setText(tf_credBIC.getText().replaceAll("\\s",""));
			tf_execDate.setText(tf_execDate.getText().replaceAll("\\s",""));
			
			// trim execDate
			tf_execDate.setText(tf_execDate.getText().substring(0, 10));
			
			// set all props
			props.put(p_credName, tf_credName.getText());
			props.put(p_credID, tf_credID.getText());
			props.put(p_credIBAN, tf_credIBAN.getText());
			props.put(p_credBIC, tf_credBIC.getText());
			props.put(p_execDate, tf_execDate.getText());
			// save properties to project root folder
			props.store(new FileOutputStream(propsName), null);

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void createGui() {
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
		addLabel(tfl_credName, p1, 0, 0);
		addTextField(tf_credName = new JTextField(
				props.getProperty(p_credName), 20), p1, 1, 0);

		// credID
		addLabel(tfl_credID, p1, 0, 1);
		addTextField(
				tf_credID = new JTextField(props.getProperty(p_credID), 20),
				p1, 1, 1);

		// credIBAN
		addLabel(tfl_credIBAN, p1, 0, 2);
		addTextField(tf_credIBAN = new JTextField(
				props.getProperty(p_credIBAN), 20), p1, 1, 2);

		// credBIC
		addLabel(tfl_credBIC, p1, 0, 3);
		addTextField(tf_credBIC = new JTextField(props.getProperty(p_credBIC),
				20), p1, 1, 3);

		// execDate
		addLabel(tfl_execDate, p1, 0, 4);
		addTextField(tf_execDate = new JTextField(
				props.getProperty(p_execDate), 20), p1, 1, 4);

		// ButtonPannel
		// Buttons
		JPanel p2 = new JPanel();
		p2.setLayout(new GridBagLayout());
		// Open-Button
		open.addActionListener(new OpenXLSListener());
		gbc.gridx = 0;
		gbc.gridy = 0;
		p2.add(open, gbc);
		// Save-Button
		save.addActionListener(new SaveXMLListener());
		save.setEnabled(false);
		gbc.gridy = 1;
		p2.add(save, gbc);

		// Assemble
		gbc.gridx = 0;
		gbc.gridy = 0;
		mainP.add(p1, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		mainP.add(p2, gbc);

		c.add(mainP);
	}

	private void addLabel(String name, Container c, int gridx, int gridy) {
		JLabel label = new JLabel(name, SwingConstants.LEFT);
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		c.add(label, gbc);
	}

	private void addTextField(JTextField tf, Container c, int gridx, int gridy) {
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		c.add(tf, gbc);
	}

	private boolean wrongDate() {
		String datum = props.getProperty(p_execDate);
		SimpleDateFormat sdfToDate = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date execDate = sdfToDate.parse(datum);
			Date now = new Date();
			if(now.after(execDate)){
				return true;
			}
		} catch (Exception e1) {
			return true;
		}
		return false;
	}
}
