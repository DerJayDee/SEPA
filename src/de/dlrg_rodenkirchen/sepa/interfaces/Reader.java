package de.dlrg_rodenkirchen.sepa.interfaces;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import de.dlrg_rodenkirchen.sepa.helper.Person;

import jxl.read.biff.BiffException;

public interface Reader {

	public ArrayList<Person> read(File file) throws ParseException,
			NumberFormatException, BiffException, IndexOutOfBoundsException,
			IOException;

	public void setSheet(int sheetNr);
}
