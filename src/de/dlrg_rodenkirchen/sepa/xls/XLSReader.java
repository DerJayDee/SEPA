package de.dlrg_rodenkirchen.sepa.xls;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

import de.dlrg_rodenkirchen.sepa.helper.Person;
import de.dlrg_rodenkirchen.sepa.helper.Reader;
import de.dlrg_rodenkirchen.sepa.helper.StaticString;

public final class XLSReader extends Reader {

	private HSSFWorkbook w;

	public XLSReader(File f, int sheetNr) throws IOException {
		super(f, sheetNr);
	}

	public XLSReader(File f) throws IOException {
		super(f);
	}

	public XLSReader() throws IOException {
		super();
	}

	public final ArrayList<Person> read() throws ParseException,
			NumberFormatException, IndexOutOfBoundsException,
			IllegalStateException {
		if (fileIsNotSet || sheetIsNotSet) {
			throw new IllegalStateException();
		}
		ArrayList<Person> persons = new ArrayList<Person>();
		HSSFSheet sheet = w.getSheetAt(sheetNr);
		for (int i = 1; i < sheet.getLastRowNum(); i++) {
			HSSFRow row = null;
			if ((row = sheet.getRow(i)) != null) {
				// Mitgliedsnummer
				HSSFCell cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(StaticString.Z_NUMMER)),
						Row.RETURN_BLANK_AS_NULL);
				if (cell == null) {
					break;
				}
				String nr = Double.toString(cell.getNumericCellValue());
				nr = nr.substring(0, nr.length() - 2);
				// Nachname
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(StaticString.Z_NACHNAME)));
				String name = cell.getStringCellValue();
				// Vorname
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(StaticString.Z_VORNAME)));
				String vorname = cell.getStringCellValue();
				// Eintrittsdatum
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(StaticString.Z_EINTRITTSDATUM)));
				Date eintritt_datum = cell.getDateCellValue();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				sdf.applyPattern("yyyy-MM-dd");
				String eintritt = sdf.format(eintritt_datum);
				// IBAN
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(StaticString.Z_IBAN)));
				String iban = cell.getStringCellValue();
				// BIC
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(StaticString.Z_BIC)));
				String bic = cell.getStringCellValue();
				// Inhaber
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(StaticString.Z_KONTOINHABER)));
				String inhaber = cell.getStringCellValue();
				// Mandatsref
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(StaticString.Z_MANDATSREFERENZ)));
				String mandatsref = cell.getStringCellValue();
				// Betrag
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(StaticString.Z_BEITRAG)));
				String betrag = Double.toString(cell.getNumericCellValue());
				while (betrag.split("\\.")[1].length() < 2) {
					betrag += "0";
				}
				betrag = betrag.replaceAll("\\.", ",");
				// Zweck
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(StaticString.Z_VERWENDUNGSZWECK)));
				String zweck = cell.getStringCellValue();
				Person tmp = new Person(nr, name, vorname, eintritt, iban, bic,
						inhaber, mandatsref, betrag, zweck);
				persons.add(tmp);
			}
		}
		return persons;
	}

	public final void setFile(File file) throws IOException {
		if (file != null) {
			InputStream is = new FileInputStream(file);
			w = new HSSFWorkbook(is);
			fileIsNotSet = false;
		} else {
			fileIsNotSet = true;
		}
	}
}
