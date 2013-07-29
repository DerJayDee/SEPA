package de.dlrg_rodenkirchen.sepa;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

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
import jxl.read.biff.BiffException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLCreator {

	private ArrayList<Mitglied> mitglieder;

	private static final String p_credName = "credName";
	private static final String p_credID = "credId";
	private static final String p_credIBAN = "credIBAN";
	private static final String p_credBIC = "credBIC";
	private static final String p_execDate = "execDate";

	private Properties props;

	public XMLCreator(Properties props) {
		this.props = props;
	}

	public int readExcel(File xlsFile) throws BiffException, IOException {
		mitglieder = new ArrayList<Mitglied>();
		File inputWorkbook = xlsFile;
		Workbook w = Workbook.getWorkbook(inputWorkbook);
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
			Mitglied tmp = new Mitglied(nr, name, vorname, iban, bic, inhaber,
					betrag);
			mitglieder.add(tmp);
		}
		return mitglieder.size();
	}

	public void writeXML(File xmlFile) throws ParserConfigurationException,
			TransformerException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		Document doc = docBuilder.newDocument();

		// Document
		Element document = createDocumentElem(doc);
		doc.appendChild(document);

		// CstmrDrctDbtInitn
		Element root = doc.createElement("CstmrDrctDbtInitn");
		document.appendChild(root);

		// GrpHdr
		Element header = createGrpHdrElem(doc);
		root.appendChild(header);

		// PmtInf
		Element pmtInf = doc.createElement("PmtInf");
		appendPmtInfHdr(pmtInf, doc);
		appendCdtr(pmtInf, doc);

		Element dbtr;
		for(Mitglied m : mitglieder){
			dbtr = createDbtr(m, doc);
			pmtInf.appendChild(dbtr);
		}

		root.appendChild(pmtInf);

		// XML schreiben
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(xmlFile);
		transformer.transform(source, result);
	}
	
	public void setProps(Properties props){
		this.props = props;
	}

	private Element createDocumentElem(Document doc) {
		Element root = doc.createElement("Document");
		// Namespaces
		root.setAttribute("xmlns",
				"urn:iso:std:iso:20022:tech:xsd:pain.008.002.02");
		root.setAttribute("xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		root.setAttribute("xsi:schemaLocation",
				"urn:iso:std:iso:20022:tech:xsd:pain.008.002.02 pain.008.002.02.xsd");

		return root;
	}

	private Element createGrpHdrElem(Document doc) {
		Element header = doc.createElement("GrpHdr");
		
		SimpleDateFormat sdf = new SimpleDateFormat();

		// MsgID
		Element msgId = doc.createElement("MsgId");
		sdf.applyPattern("yyyyMMddHHmmssSS");
		msgId.appendChild(doc
				.createTextNode("MID" + sdf.format(new Date())));
		header.appendChild(msgId);

		// CreDtTm
		Element creDtTm = doc.createElement("CreDtTm");
		sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
		creDtTm.appendChild(doc.createTextNode(sdf.format(new Date())));
		header.appendChild(creDtTm);

		// NbOfTxs
		Element nbOfTxs = doc.createElement("NbOfTxs");
		nbOfTxs.appendChild(doc.createTextNode("" + mitglieder.size()));
		header.appendChild(nbOfTxs);

		// CtrlSum
		Element ctrlSum = doc.createElement("CtrlSum");
		ctrlSum.appendChild(doc.createTextNode(getTotalSum()));
		header.appendChild(ctrlSum);

		// InitgPty
		Element initgPty = doc.createElement("InitgPty");
		Element nm = doc.createElement("Nm");
		nm.appendChild(doc.createTextNode(props.getProperty(p_credName)));
		initgPty.appendChild(nm);
		header.appendChild(initgPty);

		return header;
	}

	private String getTotalSum() {
		double sum = 0.0;
		for (Mitglied mitglied : mitglieder) {
			sum += Double.parseDouble(mitglied.betrag.replace(',', '.'));
		}
		String sumStr = ""+sum;
		if(sumStr.substring(sumStr.indexOf('.')).length() <= 3){
			sumStr += "0";
		}
		return sumStr;
	}

	private void appendPmtInfHdr(Element pmtInf, Document doc) {
		SimpleDateFormat sdf = new SimpleDateFormat();
		
		// PmtInfId
		Element pmtInfId = doc.createElement("PmtInfId");
		sdf.applyPattern("yyyyMMddHHmmssSS");
		pmtInfId.appendChild(doc.createTextNode("PID" + sdf.format(new Date())));
		pmtInf.appendChild(pmtInfId);

		// PmtMtd
		Element pmtMtd = doc.createElement("PmtMtd");
		pmtMtd.appendChild(doc.createTextNode("DD"));
		pmtInf.appendChild(pmtMtd);

		// Btchbookg
		Element btchBookg = doc.createElement("BtchBookg");
		btchBookg.appendChild(doc.createTextNode("true"));
		pmtInf.appendChild(btchBookg);

		// NbOfTxs
		Element nbOfTxs = doc.createElement("NbOfTxs");
		nbOfTxs.appendChild(doc.createTextNode("" + mitglieder.size()));
		pmtInf.appendChild(nbOfTxs);

		// CtrlSum
		Element ctrlSum = doc.createElement("CtrlSum");
		ctrlSum.appendChild(doc.createTextNode(getTotalSum()));
		pmtInf.appendChild(ctrlSum);

		// PmtTpInf
		Element pmtTpInf = doc.createElement("PmtTpInf");

		Element svcLvl = doc.createElement("SvcLvl");
		Element cd1 = doc.createElement("Cd");
		cd1.appendChild(doc.createTextNode("SEPA"));
		svcLvl.appendChild(cd1);
		pmtTpInf.appendChild(svcLvl);

		Element lclInstrm = doc.createElement("LclInstrm");
		Element cd2 = doc.createElement("Cd");
		cd2.appendChild(doc.createTextNode("CORE"));
		lclInstrm.appendChild(cd2);
		pmtTpInf.appendChild(lclInstrm);

		Element seqTp = doc.createElement("SeqTp");
		seqTp.appendChild(doc.createTextNode("FRST"));
		pmtTpInf.appendChild(seqTp);

		pmtInf.appendChild(pmtTpInf);

		// ReqdColltnDt
		Element reqdColltnDt = doc.createElement("ReqdColltnDt");
		reqdColltnDt.appendChild(doc.createTextNode(props
				.getProperty(p_execDate)));
		pmtInf.appendChild(reqdColltnDt);
	}

	private void appendCdtr(Element pmtInf, Document doc) {
		// Cdtr
		Element cdtr = doc.createElement("Cdtr");
		Element nm = doc.createElement("Nm");
		nm.appendChild(doc.createTextNode(props.getProperty(p_credName)));
		cdtr.appendChild(nm);
		pmtInf.appendChild(cdtr);

		// CdtrAcct
		Element cdtrAcct = doc.createElement("CdtrAcct");
		Element id1 = doc.createElement("Id");
		Element iban = doc.createElement("IBAN");
		iban.appendChild(doc.createTextNode(props.getProperty(p_credIBAN)));
		id1.appendChild(iban);
		cdtrAcct.appendChild(id1);
		pmtInf.appendChild(cdtrAcct);

		// CdtrAgt
		Element cdtrAgt = doc.createElement("CdtrAgt");
		Element finInstnId = doc.createElement("FinInstnId");
		Element bic = doc.createElement("BIC");
		bic.appendChild(doc.createTextNode(props.getProperty(p_credBIC)));
		finInstnId.appendChild(bic);
		cdtrAgt.appendChild(finInstnId);
		pmtInf.appendChild(cdtrAgt);

		// ChrgBr
		Element chrgBr = doc.createElement("ChrgBr");
		chrgBr.appendChild(doc.createTextNode("SLEV"));
		pmtInf.appendChild(chrgBr);

		// CdtrSchmeId
		Element cdtrSchmeId = doc.createElement("CdtrSchmeId");
		Element id2 = doc.createElement("Id");
		Element prvtId = doc.createElement("PrvtId");
		Element othr = doc.createElement("Othr");
		Element id3 = doc.createElement("Id");
		id3.appendChild(doc.createTextNode(props.getProperty(p_credID)));
		Element schmeNm = doc.createElement("SchmeNm");
		Element prtry = doc.createElement("Prtry");
		prtry.appendChild(doc.createTextNode("SEPA"));
		schmeNm.appendChild(prtry);
		othr.appendChild(id3);
		othr.appendChild(schmeNm);
		prvtId.appendChild(othr);
		id2.appendChild(prvtId);
		cdtrSchmeId.appendChild(id2);
		pmtInf.appendChild(cdtrSchmeId);
	}

	private Element createDbtr(Mitglied m, Document doc) {
		Element dbtr = doc.createElement("Dbtr");

		// FIXME fertig machen!

		return dbtr;
	}
}
