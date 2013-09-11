package de.dlrg_rodenkirchen.sepa.xlsx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jxl.read.biff.BiffException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.dlrg_rodenkirchen.sepa.helper.Person;
import de.dlrg_rodenkirchen.sepa.helper.Reader;

public final class XLSXReader extends Reader {

	private XSSFWorkbook w;

	public XLSXReader(File f, int sheetNr) throws IOException, BiffException {
		super(f, sheetNr);
	}

	public XLSXReader(File f) throws IOException, BiffException {
		super(f);
	}

	public XLSXReader() throws IOException, BiffException {
		super();
	}

	public final ArrayList<Person> read() throws ParseException,
			NumberFormatException, IndexOutOfBoundsException,
			IllegalStateException {
		if (fileIsNotSet || sheetIsNotSet) {
			throw new IllegalStateException();
		}
		ArrayList<Person> persons = new ArrayList<Person>();
		XSSFSheet sheet = w.getSheetAt(sheetNr);
		for (int i = 1; i < sheet.getLastRowNum(); i++) {
			XSSFRow row = null;
			if ((row = sheet.getRow(i)) != null) {
				// Mitgliedsnummer
				XSSFCell cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(Z_NUMMER)),
						Row.RETURN_BLANK_AS_NULL);
				if (cell == null) {
					break;
				}
				String nr = Double.toString(cell.getNumericCellValue());
				nr = nr.substring(0, nr.length() - 2);
				// Nachname
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(Z_NACHNAME)));
				String name = cell.getStringCellValue();
				// Vorname
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(Z_VORNAME)));
				String vorname = cell.getStringCellValue();
				// Eintrittsdatum
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(Z_EINTRITTSDATUM)));
				Date eintritt_datum = cell.getDateCellValue();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				sdf.applyPattern("yyyy-MM-dd");
				String eintritt = sdf.format(eintritt_datum);
				// IBAN
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(Z_IBAN)));
				String iban = cell.getStringCellValue();
				// BIC
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(Z_BIC)));
				String bic = cell.getStringCellValue();
				// Inhaber
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(Z_KONTOINHABER)));
				String inhaber = cell.getStringCellValue();
				// Mandatsref
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(Z_MANDATSREFERENZ)));
				String mandatsref = cell.getStringCellValue();
				// Betrag
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(Z_BEITRAG)));
				String betrag = Double.toString(cell.getNumericCellValue());
				while (betrag.split("\\.")[1].length() < 2) {
					betrag += "0";
				}
				betrag = betrag.replaceAll("\\.", ",");
				// Zweck
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(Z_VERWENDUNGSZWECK)));
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
			w = new XSSFWorkbook(is);
			fileIsNotSet = false;
		} else {
			fileIsNotSet = true;
		}
	}
}
