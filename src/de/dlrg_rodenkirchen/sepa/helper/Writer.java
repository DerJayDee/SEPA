package de.dlrg_rodenkirchen.sepa.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import de.dlrg_rodenkirchen.sepa.interfaces.IWriter;

public abstract class Writer implements IWriter {

	public Writer(Properties props) {
		setProps(props);
	}

	protected Properties props;

	public abstract void write(File xmlFile, ArrayList<Person> persons)
			throws Exception;

	public final void setProps(Properties props) {
		this.props = props;
	}

}
