package de.dlrg_rodenkirchen.sepa;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import jxl.read.biff.BiffException;

public class Xsl2Xml extends JFrame {

	private static final long serialVersionUID = 737038282745995221L;

	private JButton open = new JButton("XLS-Datei öffnen...");
	private JButton save = new JButton("XML-Datei speichern unter...");

	private XMLCreator xmlc;

	public Xsl2Xml() {
		this.setTitle("XLS SEPA Converter");
		Container c = getContentPane();

		// Buttons (Unten)
		JPanel p1 = new JPanel();
		// Open-Button
		open.addActionListener(new OpenXLSListener());
		p1.add(open);
		// Save-Button
		save.addActionListener(new SaveXMLListener());
		save.setEnabled(false);
		p1.add(save);
		c.add(p1, BorderLayout.CENTER);
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
					xmlc = new XMLCreator();
				}
				try {
					int count = xmlc.readExcel(c.getSelectedFile());
					if (count > 0) {
						save.setEnabled(true);
						JOptionPane.showMessageDialog(c, "Anzahl eingelesene Einträge: " + count, "XLS eingelesen", JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(c, "Keine Einträge gefunden", "Fehler", JOptionPane.ERROR_MESSAGE);
					}
				} catch (BiffException | IOException e1) {
					// TODO Auto-generated catch block
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
			JFileChooser c = new JFileChooser();
			int rVal = c.showSaveDialog(Xsl2Xml.this);
			if (rVal == JFileChooser.APPROVE_OPTION) {
				try {
					xmlc.writeXML(c.getSelectedFile());
					JOptionPane.showMessageDialog(c, "XML-Datei erfolgreich geschrieben", "XML geschrieben", JOptionPane.INFORMATION_MESSAGE);
				} catch (ParserConfigurationException | TransformerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			if (rVal == JFileChooser.CANCEL_OPTION) {
				// TODO Vielleicht mal was mit machen
			}
		}
	}

	public static void main(String[] args) {
		run(new Xsl2Xml(), 250, 110);
	}

	public static void run(JFrame frame, int width, int height) {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(width, height);
		frame.setVisible(true);
	}
}
