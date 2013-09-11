package de.dlrg_rodenkirchen.sepa.xls;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import de.dlrg_rodenkirchen.sepa.helper.Person;
import de.dlrg_rodenkirchen.sepa.helper.Reader;
import de.dlrg_rodenkirchen.sepa.helper.StaticString;

public final class XLSReader extends Reader {

	private Workbook w;

	public XLSReader(File f, int sheetNr) throws IOException, BiffException {
		super(f, sheetNr);
	}

	public XLSReader(File f) throws BiffException, IOException {
		super(f);
	}

	public XLSReader() throws BiffException, IOException {
		super();
	}

	public final ArrayList<Person> read() throws ParseException,
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
					.getProperty(StaticString.Z_NUMMER)), i);
			String nr = cell.getContents();
			if (nr.equals(""))
				break;
			// Nachname
			cell = sheet.getCell(Integer.parseInt(zuordnung
					.getProperty(StaticString.Z_NACHNAME)), i);
			String name = cell.getContents();
			// Vorname
			cell = sheet.getCell(Integer.parseInt(zuordnung
					.getProperty(StaticString.Z_VORNAME)), i);
			String vorname = cell.getContents();
			// Eintrittsdatum
			cell = sheet.getCell(Integer.parseInt(zuordnung
					.getProperty(StaticString.Z_EINTRITTSDATUM)), i);
			String eintritt = cell.getContents();
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy");
			Date eintritt_datum = sdf.parse(eintritt);
			sdf.applyPattern("yyyy-MM-dd");
			eintritt = sdf.format(eintritt_datum);
			// IBAN
			cell = sheet.getCell(Integer.parseInt(zuordnung
					.getProperty(StaticString.Z_IBAN)), i);
			String iban = cell.getContents();
			// BIC
			cell = sheet
					.getCell(Integer.parseInt(zuordnung
							.getProperty(StaticString.Z_BIC)), i);
			String bic = cell.getContents();
			// Inhaber
			cell = sheet.getCell(Integer.parseInt(zuordnung
					.getProperty(StaticString.Z_KONTOINHABER)), i);
			String inhaber = cell.getContents();
			// Mandatsref
			cell = sheet.getCell(Integer.parseInt(zuordnung
					.getProperty(StaticString.Z_MANDATSREFERENZ)), i);
			String mandatsref = cell.getContents();
			// Betrag
			cell = sheet.getCell(Integer.parseInt(zuordnung
					.getProperty(StaticString.Z_BEITRAG)), i);
			String betrag = cell.getContents();
			betrag = betrag.substring(1);
			// Zweck
			cell = sheet.getCell(Integer.parseInt(zuordnung
					.getProperty(StaticString.Z_VERWENDUNGSZWECK)), i);
			String zweck = cell.getContents();
			Person tmp = new Person(nr, name, vorname, eintritt, iban, bic,
					inhaber, mandatsref, betrag, zweck);
			persons.add(tmp);
		}
		return persons;
	}

	public final void setFile(File file) throws IOException, BiffException {
		if (file != null) {
			w = Workbook.getWorkbook(file);
			fileIsNotSet = false;
		} else {
			fileIsNotSet = true;
		}
	}
}
