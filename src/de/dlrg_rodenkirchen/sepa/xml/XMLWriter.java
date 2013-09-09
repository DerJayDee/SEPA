package de.dlrg_rodenkirchen.sepa.xml;

import java.io.File;
import java.io.IOException;
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.dlrg_rodenkirchen.sepa.helper.Person;
import de.dlrg_rodenkirchen.sepa.helper.Strings;
import de.dlrg_rodenkirchen.sepa.interfaces.Writer;

public class XMLWriter implements Writer {

	private Properties props;

	public XMLWriter(Properties props) throws IOException {
		this.props = props;
	}

	public final void write(File xmlFile, ArrayList<Person> persons)
			throws Exception {
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
		Element header = createGrpHdrElem(doc, persons);
		root.appendChild(header);

		// PmtInf
		Element pmtInf = doc.createElement("PmtInf");
		appendPmtInfHdr(pmtInf, doc, persons);
		appendCdtr(pmtInf, doc);

		Element dbtr;
		for (Person p : persons) {
			dbtr = createDbtr(p, doc);
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

	public final void setProps(Properties props) {
		this.props = props;
	}

	private final Element createDocumentElem(Document doc) {
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

	private final Element createGrpHdrElem(Document doc,
			ArrayList<Person> persons) throws Exception {
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
		nbOfTxs.appendChild(doc.createTextNode("" + persons.size()));
		header.appendChild(nbOfTxs);

		// CtrlSum
		Element ctrlSum = doc.createElement("CtrlSum");
		ctrlSum.appendChild(doc.createTextNode(getTotalSum(persons)));
		header.appendChild(ctrlSum);

		// InitgPty
		Element initgPty = doc.createElement("InitgPty");
		Element nm = doc.createElement("Nm");
		nm.appendChild(doc.createTextNode(props.getProperty(Strings.P_CRED_NAME.toString())));
		initgPty.appendChild(nm);
		header.appendChild(initgPty);

		return header;
	}

	private final String getTotalSum(ArrayList<Person> persons)
			throws Exception {
		double sum = 0.0;
		NumberFormat format = NumberFormat.getInstance(Locale.GERMAN);
		Number number;
		for (Person person : persons) {
			number = format.parse(person.getBetrag());
			sum += number.doubleValue();
		}
		return String.format(Locale.ENGLISH, "%1$.2f", sum);
	}

	private final void appendPmtInfHdr(Element pmtInf, Document doc,
			ArrayList<Person> persons) throws Exception {
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
		nbOfTxs.appendChild(doc.createTextNode("" + persons.size()));
		pmtInf.appendChild(nbOfTxs);

		// CtrlSum
		Element ctrlSum = doc.createElement("CtrlSum");
		ctrlSum.appendChild(doc.createTextNode(getTotalSum(persons)));
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
				.getProperty(Strings.P_EXEC_DATE.toString())));
		pmtInf.appendChild(reqdColltnDt);
	}

	private final void appendCdtr(Element pmtInf, Document doc) {
		// Cdtr
		Element cdtr = doc.createElement("Cdtr");
		Element nm = doc.createElement("Nm");
		nm.appendChild(doc.createTextNode(props.getProperty(Strings.P_CRED_NAME.toString())));
		cdtr.appendChild(nm);
		pmtInf.appendChild(cdtr);

		// CdtrAcct
		Element cdtrAcct = doc.createElement("CdtrAcct");
		Element id1 = doc.createElement("Id");
		Element iban = doc.createElement("IBAN");
		iban.appendChild(doc.createTextNode(props.getProperty(Strings.P_CRED_IBAN.toString())));
		id1.appendChild(iban);
		cdtrAcct.appendChild(id1);
		pmtInf.appendChild(cdtrAcct);

		// CdtrAgt
		Element cdtrAgt = doc.createElement("CdtrAgt");
		Element finInstnId = doc.createElement("FinInstnId");
		Element bic = doc.createElement("BIC");
		bic.appendChild(doc.createTextNode(props.getProperty(Strings.P_CRED_BIC.toString())));
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
		id3.appendChild(doc.createTextNode(props.getProperty(Strings.P_CRED_ID.toString())));
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

	private final Element createDbtr(Person p, Document doc) throws Exception {
		Element drctDbtTxInf = doc.createElement("DrctDbtTxInf");

		SimpleDateFormat sdf = new SimpleDateFormat();

		// PmtId
		Element pmtId = doc.createElement("PmtId");
		Element endToEndId = doc.createElement("EndToEndId");
		sdf.applyPattern("yyyyMMddHHmmss");
		endToEndId.appendChild(doc.createTextNode("EToE"
				+ sdf.format(new Date()) + "-" + p.getNumber()));
		pmtId.appendChild(endToEndId);
		drctDbtTxInf.appendChild(pmtId);

		// InstdAmt
		Element instdAmt = doc.createElement("InstdAmt");
		instdAmt.setAttribute("Ccy", "EUR");
		NumberFormat format = NumberFormat.getInstance(Locale.GERMAN);
		Number number;
		double betrag = 0.0;
		number = format.parse(p.getBetrag());
		betrag = number.doubleValue();
		instdAmt.appendChild(doc.createTextNode(String.format(Locale.ENGLISH,
				"%1$.2f", betrag)));
		drctDbtTxInf.appendChild(instdAmt);

		// DrctDbtTx
		Element drctDbtTx = doc.createElement("DrctDbtTx");
		Element mndtRltdInf = doc.createElement("MndtRltdInf");
		Element mndtId = doc.createElement("MndtId");
		mndtId.appendChild(doc.createTextNode(p.getMandatsref()));
		mndtRltdInf.appendChild(mndtId);
		Element dtOfSgntr = doc.createElement("DtOfSgntr");
		dtOfSgntr.appendChild(doc.createTextNode(p.getUnterschrieben()));
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
		bic.appendChild(doc.createTextNode(p.getBic()));
		finInstnId.appendChild(bic);
		dbtrAgt.appendChild(finInstnId);
		drctDbtTxInf.appendChild(dbtrAgt);

		// Dbtr
		Element dbtr = doc.createElement("Dbtr");
		Element nm = doc.createElement("Nm");
		nm.appendChild(doc.createTextNode(p.getKontoinhaber()));
		dbtr.appendChild(nm);
		drctDbtTxInf.appendChild(dbtr);

		// DbtrAcct
		Element dbtrAcct = doc.createElement("DbtrAcct");
		Element id = doc.createElement("Id");
		Element iban = doc.createElement("IBAN");
		iban.appendChild(doc.createTextNode(p.getIban()));
		id.appendChild(iban);
		dbtrAcct.appendChild(id);
		drctDbtTxInf.appendChild(dbtrAcct);

		// RmtInf
		Element rmtInf = doc.createElement("RmtInf");
		Element ustrd = doc.createElement("Ustrd");
		ustrd.appendChild(doc.createTextNode(p.getZweck()));
		rmtInf.appendChild(ustrd);
		drctDbtTxInf.appendChild(rmtInf);

		return drctDbtTxInf;
	}
}