package de.dlrg_rodenkirchen.sepa;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
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

	private static final String zuordnungsPropsName = "SEPA_Zuordnung.cfg";

	private static final String z_nummer = "mitgliedsnummer";
	private static final String z_nachname = "nachname";
	private static final String z_vorname = "vorname";
	private static final String z_iban = "iban";
	private static final String z_bic = "bic";
	private static final String z_kontoinhaber = "kontoinhaber";
	private static final String z_beitrag = "beitrag";
	private static final String z_mandatsreferenz = "mandatsreferenz";
	private static final String z_verwendungszweck = "verwendungszweck";

	private Properties props;
	private Properties zuordnung;

	public XMLCreator(Properties props) throws IOException {
		this.props = props;
		loadProps();
	}

	public int readExcel(File xlsFile, int excelSheet) throws BiffException,
			IOException, NumberFormatException {
		mitglieder = new ArrayList<Mitglied>();
		File inputWorkbook = xlsFile;
		Workbook w = Workbook.getWorkbook(inputWorkbook);
		Sheet sheet = w.getSheet(excelSheet);
		for (int i = 1; i < sheet.getRows(); i++) {
			// Mitgliedsnummer
			Cell cell = sheet.getCell(Integer.parseInt(zuordnung.getProperty(z_nummer)), i);
			String nr = cell.getContents();
			if (nr.equals(""))
				break;
			// Nachname
			cell = sheet.getCell(Integer.parseInt(zuordnung.getProperty(z_nachname)), i);
			String name = cell.getContents();
			// Vorname
			cell = sheet.getCell(Integer.parseInt(zuordnung.getProperty(z_vorname)), i);
			String vorname = cell.getContents();
			// IBAN
			cell = sheet.getCell(Integer.parseInt(zuordnung.getProperty(z_iban)), i);
			String iban = cell.getContents();
			// BIC
			cell = sheet.getCell(Integer.parseInt(zuordnung.getProperty(z_bic)), i);
			String bic = cell.getContents();
			// Inhaber
			cell = sheet.getCell(Integer.parseInt(zuordnung.getProperty(z_kontoinhaber)), i);
			String inhaber = cell.getContents();
			// Mandatsref
			cell = sheet.getCell(Integer.parseInt(zuordnung.getProperty(z_mandatsreferenz)), i);
			String mandatsref = cell.getContents();
			// Betrag
			cell = sheet.getCell(Integer.parseInt(zuordnung.getProperty(z_beitrag)), i);
			String betrag = cell.getContents();
			betrag = betrag.substring(1);
			// Zweck
			cell = sheet.getCell(Integer.parseInt(zuordnung.getProperty(z_verwendungszweck)), i);
			String zweck1 = cell.getContents();
			Mitglied tmp = new Mitglied(nr, name, vorname, iban, bic, inhaber,
					mandatsref, betrag, zweck1);
			mitglieder.add(tmp);
		}
		return mitglieder.size();
	}

	public void writeXML(File xmlFile) throws Exception {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		Document doc = docBuilder.newDocument();
		doc.setXmlStandalone(true);

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
		for (Mitglied m : mitglieder) {
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

	public void setProps(Properties props) {
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

	private Element createGrpHdrElem(Document doc) throws Exception {
		Element header = doc.createElement("GrpHdr");

		SimpleDateFormat sdf = new SimpleDateFormat();

		// MsgID
		Element msgId = doc.createElement("MsgId");
		sdf.applyPattern("yyyyMMddHHmmssSS");
		msgId.appendChild(doc.createTextNode("MID" + sdf.format(new Date())));
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

	private String getTotalSum() throws Exception {
		double sum = 0.0;
		NumberFormat format = NumberFormat.getInstance(Locale.GERMAN);
		Number number;
		for (Mitglied mitglied : mitglieder) {
			number = format.parse(mitglied.betrag);
			sum += number.doubleValue();
		}
		return String.format(Locale.ENGLISH, "%1$,.2f", sum);
	}

	private void appendPmtInfHdr(Element pmtInf, Document doc) throws Exception {
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

	private Element createDbtr(Mitglied m, Document doc) throws Exception {
		Element drctDbtTxInf = doc.createElement("DrctDbtTxInf");

		SimpleDateFormat sdf = new SimpleDateFormat();

		// PmtId
		Element pmtId = doc.createElement("PmtId");
		Element endToEndId = doc.createElement("EndToEndId");
		sdf.applyPattern("yyyyMMddHHmmss");
		endToEndId.appendChild(doc.createTextNode("EToE"
				+ sdf.format(new Date()) + "-" + m.mitgliedsNr));
		pmtId.appendChild(endToEndId);
		drctDbtTxInf.appendChild(pmtId);

		// InstdAmt
		Element instdAmt = doc.createElement("InstdAmt");
		instdAmt.setAttribute("Ccy", "EUR");
		NumberFormat format = NumberFormat.getInstance(Locale.GERMAN);
		Number number;
		double betrag = 0.0;
		number = format.parse(m.betrag);
		betrag = number.doubleValue();
		instdAmt.appendChild(doc.createTextNode(String.format(Locale.ENGLISH,
				"%1$,.2f", betrag)));
		drctDbtTxInf.appendChild(instdAmt);

		// DrctDbtTx
		Element drctDbtTx = doc.createElement("DrctDbtTx");
		Element mndtRltdInf = doc.createElement("MndtRltdInf");
		Element mndtId = doc.createElement("MndtId");
		mndtId.appendChild(doc.createTextNode(m.mandatsref));
		mndtRltdInf.appendChild(mndtId);
		Element dtOfSgntr = doc.createElement("DtOfSgntr");
		dtOfSgntr.appendChild(doc.createTextNode("2001-01-01"));
		mndtRltdInf.appendChild(dtOfSgntr);
		Element amdmntInd = doc.createElement("AmdmntInd");
		amdmntInd.appendChild(doc.createTextNode("false"));
		mndtRltdInf.appendChild(amdmntInd);
		drctDbtTx.appendChild(mndtRltdInf);
		drctDbtTxInf.appendChild(drctDbtTx);

		// DbtrAgt
		Element dbtrAgt = doc.createElement("DbtrAgt");
		Element finInstnId = doc.createElement("FinInstnId");
		Element bic = doc.createElement("BIC");
		bic.appendChild(doc.createTextNode(m.bic));
		finInstnId.appendChild(bic);
		dbtrAgt.appendChild(finInstnId);
		drctDbtTxInf.appendChild(dbtrAgt);

		// Dbtr
		Element dbtr = doc.createElement("Dbtr");
		Element nm = doc.createElement("Nm");
		nm.appendChild(doc.createTextNode(m.kontoinhaber));
		dbtr.appendChild(nm);
		drctDbtTxInf.appendChild(dbtr);

		// DbtrAcct
		Element dbtrAcct = doc.createElement("DbtrAcct");
		Element id = doc.createElement("Id");
		Element iban = doc.createElement("IBAN");
		iban.appendChild(doc.createTextNode(m.iban));
		id.appendChild(iban);
		dbtrAcct.appendChild(id);
		drctDbtTxInf.appendChild(dbtrAcct);

		// RmtInf
		Element rmtInf = doc.createElement("RmtInf");
		Element ustrd = doc.createElement("Ustrd");
		ustrd.appendChild(doc.createTextNode(m.zweck));
		rmtInf.appendChild(ustrd);
		drctDbtTxInf.appendChild(rmtInf);

		return drctDbtTxInf;
	}

	@SuppressWarnings("resource")
	private void loadProps() throws IOException {
		if (zuordnung == null) {
			zuordnung = new Properties();
		}
		InputStream in;
		File propsFile = new File(zuordnungsPropsName);
		if (propsFile.exists()) {
			in = new FileInputStream(propsFile);
		} else {
			in = Xsl2Xml.class.getClassLoader().getResourceAsStream(
					zuordnungsPropsName);
		}
		zuordnung.load(in);
		in.close();
	}
}
