package de.dlrg_rodenkirchen.sepa;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLCreator {

	public static void main(String[] args) {
		XMLCreator creator = new XMLCreator();
		creator.writeXML();
	}

	public void readExcel() {
		File inputWorkbook = new File("MITGLIED.xls");
		Workbook w;
		try {
			w = Workbook.getWorkbook(inputWorkbook);
			Sheet sheet = w.getSheet(0);
			for (int i = 1; i < sheet.getRows(); i++) {
				Cell cell = sheet.getCell(0, i);
				String nr = cell.getContents();
				if (nr.equals(""))
					break;
				cell = sheet.getCell(2, i);
				String name = cell.getContents();
				cell = sheet.getCell(3, i);
				String vorname = cell.getContents();
				cell = sheet.getCell(20, i);
				String iban = cell.getContents();
				cell = sheet.getCell(21, i);
				String bic = cell.getContents();
				cell = sheet.getCell(22, i);
				String inhaber = cell.getContents();
				cell = sheet.getCell(23, i);
				String betrag = cell.getContents();
				Mitglied tmp = new Mitglied(nr, name, vorname, iban, bic,
						inhaber, betrag);
				System.out.println(betrag);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeXML() {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();

			// Document
			Element root_Document = doc.createElement("Document");
			doc.appendChild(root_Document);
			// Namespaces
			root_Document.setAttribute("xmlns",
					"urn:iso:std:iso:20022:tech:xsd:pain.008.002.02");
			root_Document.setAttribute("xmlns:xsi",
					"http://www.w3.org/2001/XMLSchema-instance");
			root_Document
					.setAttribute("xsi:schemaLocation",
							"urn:iso:std:iso:20022:tech:xsd:pain.008.002.02 pain.008.002.02.xsd");

			// CstmrDrctDbtInitn
			Element root = doc.createElement("CstmrDrctDbtInitn");
			root_Document.appendChild(root);

			// GrpHdr
			Element header = createGrpHdr(doc);
			root.appendChild(header);

			// salary elements
			// Element salary = doc.createElement("salary");
			// salary.appendChild(doc.createTextNode("100000"));
			// staff.appendChild(salary);

			// XML schreiben
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("D:\\file.xml"));
			transformer.transform(source, result);
			System.out.println("File saved!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Element createGrpHdr(Document doc) throws Exception {
		Element header = doc.createElement("GrpHdr");
		Element tmp = doc.createElement("MsgId");
		tmp.appendChild(doc.createTextNode("MID201305273145044"));
		header.appendChild(tmp);

		tmp = doc.createElement("CreDtTm");
		tmp.appendChild(doc.createTextNode("2013-05-27T08:44:10Z"));
		header.appendChild(tmp);
		
		// TODO

		return header;
	}

	private class Mitglied {
		// TODO
		String mitgliedsNr;
		String name;
		String vorname;
		String iban;
		String bic;
		String kontoinhaber;
		String betrag;

		public Mitglied(String nr, String name, String vorname, String iban,
				String bic, String inhaber, String betrag) {
			this.mitgliedsNr = nr;
			this.name = name;
			this.vorname = vorname;
			this.iban = iban;
			this.bic = bic;
			this.kontoinhaber = inhaber;
			this.betrag = betrag;
		}
	}
}
