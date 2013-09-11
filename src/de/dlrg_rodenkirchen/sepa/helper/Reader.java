package de.dlrg_rodenkirchen.sepa.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;

import jxl.read.biff.BiffException;
import de.dlrg_rodenkirchen.sepa.interfaces.IReader;

public abstract class Reader implements IReader {

	protected boolean fileIsNotSet;

	protected int sheetNr;
	protected boolean sheetIsNotSet;

	protected Properties zuordnung;

	protected static final String ZUORDNUNGS_PROPS_NAME = "SEPA_Zuordnung.cfg";
	protected static final String Z_NUMMER = "mitgliedsnummer";
	protected static final String Z_NACHNAME = "nachname";
	protected static final String Z_VORNAME = "vorname";
	protected static final String Z_EINTRITTSDATUM = "eintrittsdatum";
	protected static final String Z_IBAN = "iban";
	protected static final String Z_BIC = "bic";
	protected static final String Z_KONTOINHABER = "kontoinhaber";
	protected static final String Z_BEITRAG = "beitrag";
	protected static final String Z_MANDATSREFERENZ = "mandatsreferenz";
	protected static final String Z_VERWENDUNGSZWECK = "verwendungszweck";

	public Reader(File f, int sheetNr) throws IOException, BiffException {
		loadProps();
		setFile(f);
		setSheet(sheetNr);
	}

	public Reader(File f) throws IOException, BiffException {
		this(f, -1);
	}

	public Reader() throws IOException, BiffException {
		this(null, -1);
	}

	public abstract ArrayList<Person> read() throws ParseException,
			NumberFormatException, IndexOutOfBoundsException;

	public abstract void setFile(File file) throws IOException, BiffException;

	public final void setSheet(int sheetNr) {
		if (sheetNr >= 0) {
			this.sheetNr = sheetNr;
			this.sheetIsNotSet = false;
		} else {
			sheetIsNotSet = true;
		}
	}

	@SuppressWarnings("resource")
	protected final void loadProps() throws IOException {
		if (zuordnung == null) {
			zuordnung = new Properties();
		}
		InputStream in;
		File propsFile = new File(ZUORDNUNGS_PROPS_NAME);
		if (propsFile.exists()) {
			in = new FileInputStream(propsFile);
		} else {
			in = ClassLoader.getSystemClassLoader().getResourceAsStream(
					"de/dlrg_rodenkirchen/sepa/config/"+ZUORDNUNGS_PROPS_NAME);
		}
		zuordnung.load(in);
		in.close();
	}

}
