package de.dlrg_rodenkirchen.sepa.xls;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import de.dlrg_rodenkirchen.sepa.Xsl2Xml;
import de.dlrg_rodenkirchen.sepa.helper.Person;
import de.dlrg_rodenkirchen.sepa.helper.Strings;
import de.dlrg_rodenkirchen.sepa.interfaces.Reader;

public class XLSReader implements Reader {

	private Workbook w;
	private boolean fileIsNotSet;

	private int sheetNr;
	private boolean sheetIsNotSet;

	private Properties zuordnung;

	public XLSReader(File f, int sheetNr) throws IOException, BiffException{
		loadProps();
		setFile(f);
		setSheet(sheetNr);
	}
	
	public XLSReader(File f) throws BiffException, IOException{
		this(f, -1);
	}
	
	public XLSReader() throws BiffException, IOException{
		this(null, -1);
	}
	

	@Override
	public ArrayList<Person> read() throws ParseException,
			NumberFormatException, IndexOutOfBoundsException,
			IllegalStateException {
		if (fileIsNotSet || sheetIsNotSet) {
			throw new IllegalStateException();
		}
		ArrayList<Person> persons = new ArrayList<Person>();
		Sheet sheet = w.getSheet(sheetNr);
		for (int i = 1; i < sheet.getRows(); i++) {
			// Mitgliedsnummer
			Cell cell = sheet.getCell(Integer.parseInt(zuordnung
					.getProperty(Strings.Z_NUMMER.toString())), i);
			String nr = cell.getContents();
			if (nr.equals(""))
				break;
			// Nachname
			cell = sheet.getCell(Integer.parseInt(zuordnung
					.getProperty(Strings.Z_NACHNAME.toString())), i);
			String name = cell.getContents();
			// Vorname
			cell = sheet.getCell(Integer.parseInt(zuordnung
					.getProperty(Strings.Z_VORNAME.toString())), i);
			String vorname = cell.getContents();
			// Eintrittsdatum
			cell = sheet.getCell(Integer.parseInt(zuordnung
					.getProperty(Strings.Z_EINTRITTSDATUM.toString())), i);
			String eintritt = cell.getContents();
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy");
			Date eintritt_datum = sdf.parse(eintritt);
			sdf.applyPattern("yyyy-MM-dd");
			eintritt = sdf.format(eintritt_datum);
			// IBAN
			cell = sheet.getCell(Integer.parseInt(zuordnung
					.getProperty(Strings.Z_IBAN.toString())), i);
			String iban = cell.getContents();
			// BIC
			cell = sheet.getCell(Integer.parseInt(zuordnung
					.getProperty(Strings.Z_BIC.toString())), i);
			String bic = cell.getContents();
			// Inhaber
			cell = sheet.getCell(Integer.parseInt(zuordnung
					.getProperty(Strings.Z_KONTOINHABER.toString())), i);
			String inhaber = cell.getContents();
			// Mandatsref
			cell = sheet.getCell(Integer.parseInt(zuordnung
					.getProperty(Strings.Z_MANDATSREFERENZ.toString())), i);
			String mandatsref = cell.getContents();
			// Betrag
			cell = sheet.getCell(Integer.parseInt(zuordnung
					.getProperty(Strings.Z_BEITRAG.toString())), i);
			String betrag = cell.getContents();
			betrag = betrag.substring(1);
			// Zweck
			cell = sheet.getCell(Integer.parseInt(zuordnung
					.getProperty(Strings.Z_VERWENDUNGSZWECK.toString())), i);
			String zweck = cell.getContents();
			Person tmp = new Person(nr, name, vorname, eintritt, iban, bic,
					inhaber, mandatsref, betrag, zweck);
			persons.add(tmp);
		}
		return persons;
	}

	@Override
	public void setFile(File file) throws IOException, BiffException {
		if (file != null) {
			w = Workbook.getWorkbook(file);
			fileIsNotSet = false;
		} else {
			fileIsNotSet = true;
		}
	}

	@Override
	public final void setSheet(int sheetNr) {
		if (sheetNr >= 0) {
			this.sheetNr = sheetNr;
			this.sheetIsNotSet = false;
		} else {
			sheetIsNotSet = true;
		}
	}

	@SuppressWarnings("resource")
	private final void loadProps() throws IOException {
		if (zuordnung == null) {
			zuordnung = new Properties();
		}
		InputStream in;
		File propsFile = new File(Strings.ZUORDNUNGS_PROPS_NAME.toString());
		if (propsFile.exists()) {
			in = new FileInputStream(propsFile);
		} else {
			in = Xsl2Xml.class.getClassLoader().getResourceAsStream(
					Strings.ZUORDNUNGS_PROPS_NAME.toString());
		}
		zuordnung.load(in);
		in.close();
	}

}