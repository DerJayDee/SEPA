package de.dlrg_rodenkirchen.sepa.xlsx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.dlrg_rodenkirchen.sepa.Xsl2Xml;
import de.dlrg_rodenkirchen.sepa.helper.Person;
import de.dlrg_rodenkirchen.sepa.helper.Strings;
import de.dlrg_rodenkirchen.sepa.interfaces.Reader;

public class XLSXReader implements Reader {

	private XSSFWorkbook w;
	private boolean fileIsNotSet;

	private int sheetNr;
	private boolean sheetIsNotSet;

	private Properties zuordnung;

	public XLSXReader(File f, int sheetNr) throws IOException {
		loadProps();
		setFile(f);
		setSheet(sheetNr);
	}

	public XLSXReader(File f) throws IOException {
		this(f, -1);
	}

	public XLSXReader() throws IOException {
		this(null, -1);
	}

	@Override
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
						.getProperty(Strings.Z_NUMMER.toString())),
						Row.RETURN_BLANK_AS_NULL);
				if (cell == null) {
					break;
				}
				String nr = Double.toString(cell.getNumericCellValue());
				nr = nr.substring(0, nr.length() - 2);
				// Nachname
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(Strings.Z_NACHNAME.toString())));
				String name = cell.getStringCellValue();
				// Vorname
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(Strings.Z_VORNAME.toString())));
				String vorname = cell.getStringCellValue();
				// Eintrittsdatum
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(Strings.Z_EINTRITTSDATUM.toString())));
				Date eintritt_datum = cell.getDateCellValue();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				sdf.applyPattern("yyyy-MM-dd");
				String eintritt = sdf.format(eintritt_datum);
				// IBAN
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(Strings.Z_IBAN.toString())));
				String iban = cell.getStringCellValue();
				// BIC
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(Strings.Z_BIC.toString())));
				String bic = cell.getStringCellValue();
				// Inhaber
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(Strings.Z_KONTOINHABER.toString())));
				String inhaber = cell.getStringCellValue();
				// Mandatsref
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(Strings.Z_MANDATSREFERENZ.toString())));
				String mandatsref = cell.getStringCellValue();
				// Betrag
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(Strings.Z_BEITRAG.toString())));
				String betrag = Double.toString(cell.getNumericCellValue());
				while (betrag.split("\\.")[1].length() < 2) {
					betrag += "0";
				}
				betrag = betrag.replaceAll("\\.", ",");
				// Zweck
				cell = row.getCell(Integer.parseInt(zuordnung
						.getProperty(Strings.Z_VERWENDUNGSZWECK.toString())));
				String zweck = cell.getStringCellValue();
				Person tmp = new Person(nr, name, vorname, eintritt, iban, bic,
						inhaber, mandatsref, betrag, zweck);
				persons.add(tmp);
			}
		}
		return persons;
	}

	@Override
	public void setFile(File file) throws IOException {
		if (file != null) {
			InputStream is = new FileInputStream(file);
			w = new XSSFWorkbook(is);
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
