package de.dlrg_rodenkirchen.sepa.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.dlrg_rodenkirchen.sepa.helper.Person;
import de.dlrg_rodenkirchen.sepa.helper.Reader;
import de.dlrg_rodenkirchen.sepa.helper.StaticString;

public final class ExcelReader extends Reader {

	private Workbook w;
	private Sheet sheet;

	public ExcelReader(File f, int sheetNr) throws IOException {
		super(f, sheetNr);
	}

	public ExcelReader(File f) throws IOException {
		super(f);
	}

	public ExcelReader() throws IOException {
		super();
	}

	public final ArrayList<Person> read() throws ParseException,
			NumberFormatException, IndexOutOfBoundsException,
			IllegalStateException {
		if (fileIsNotSet || sheetIsNotSet) {
			throw new IllegalStateException();
		}
		ArrayList<Person> persons = new ArrayList<Person>();
		for (int i = 1; i < sheet.getLastRowNum(); i++) {
			Row row = null;
			if ((row = sheet.getRow(i)) != null) {
				// Mitgliedsnummer
				Cell cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(StaticString.Z_NUMMER)),
						Row.RETURN_BLANK_AS_NULL);
				if (cell == null) {
					break;
				}
				String id = Double.toString(cell.getNumericCellValue());
				id = id.substring(0, id.length() - 2);
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
				String signed = sdf.format(eintritt_datum);
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
				Person tmp = new Person(id, name, vorname, signed, iban, bic,
						inhaber, mandatsref, betrag, zweck);
				persons.add(tmp);
			}
		}
		return persons;
	}

	public final void setFile(File file) throws IOException {
		if (file != null) {
			InputStream is = new FileInputStream(file);
			String filename = file.getName();
			if (filename.endsWith("xls")) {
				w = new HSSFWorkbook(is);
				fileIsNotSet = false;
			} else if (filename.endsWith("xlsx")) {
				w = new XSSFWorkbook(is);
				fileIsNotSet = false;
			}
		} else {
			fileIsNotSet = true;
		}
	}

	@Override
	public final void setSheet(int sheetNr) throws IllegalArgumentException {
		if (sheetNr >= 0) {
			sheet = w.getSheetAt(sheetNr);
			this.sheetIsNotSet = false;
		} else {
			sheetIsNotSet = true;
			throw new IllegalArgumentException();
		}
	}

	@Override
	public final void setSheet(String sheetName)
			throws IllegalArgumentException {
		sheet = w.getSheet(sheetName);
		if (sheet != null) {
			this.sheetIsNotSet = false;
		} else {
			sheetIsNotSet = true;
			throw new IllegalArgumentException();
		}
	}

	public final int getSheetCount() {
		return w.getNumberOfSheets();
	}

	public final String[] getSheetNames() {
		int sheetCount = w.getNumberOfSheets();
		String[] sheets = new String[sheetCount];
		for (int i = 0; i < sheetCount; i++) {
			sheets[i] = w.getSheetName(i);
		}
		return sheets;
	}
}
