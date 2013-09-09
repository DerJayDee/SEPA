package de.dlrg_rodenkirchen.sepa.interfaces;

import java.io.File;
import java.util.ArrayList;

import de.dlrg_rodenkirchen.sepa.helper.Person;

public interface Writer {
	public void write(File file, ArrayList<Person> persons) throws Exception;
}
