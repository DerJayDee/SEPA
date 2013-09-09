package de.dlrg_rodenkirchen.sepa.interfaces;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import jxl.read.biff.BiffException;
import de.dlrg_rodenkirchen.sepa.helper.Person;

public interface Reader {

	public ArrayList<Person> read() throws ParseException,
			NumberFormatException, IndexOutOfBoundsException;

	public void setFile(File file) throws IOException, BiffException;

	public void setSheet(int sheetNr);
}
