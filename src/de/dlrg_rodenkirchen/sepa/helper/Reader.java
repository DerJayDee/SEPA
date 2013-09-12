package de.dlrg_rodenkirchen.sepa.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;

import de.dlrg_rodenkirchen.sepa.interfaces.IReader;

public abstract class Reader implements IReader {

	protected boolean fileIsNotSet;
	protected boolean sheetIsNotSet;

	protected Properties zuordnung;

	public Reader(File f) throws IOException {
		loadProps();
		setFile(f);
	}

	public Reader() throws IOException {
		this(null);
	}

	public abstract ArrayList<Person> read() throws ParseException,
			NumberFormatException, IndexOutOfBoundsException;

	public abstract void setFile(File file) throws IOException;

	public abstract void setSheet(int sheetNr) throws IllegalArgumentException;
	
	public abstract void setSheet(String sheetName) throws IllegalArgumentException;

	public abstract int getSheetCount();

	public abstract String[] getSheetNames();

	protected final void loadProps() throws IOException {
		if (zuordnung == null) {
			zuordnung = new Properties();
		}
		InputStream in;
		File propsFile = new File(StaticString.ZUORDNUNGS_PROPS_NAME);
		if (propsFile.exists()) {
			in = new FileInputStream(propsFile);
			zuordnung.load(in);
			in.close();
		} else {
			in = ClassLoader.getSystemClassLoader().getResourceAsStream(
					StaticString.CONFIG_PATH
							+ StaticString.ZUORDNUNGS_PROPS_NAME);
			zuordnung.load(in);
			in.close();
		}

	}

}
