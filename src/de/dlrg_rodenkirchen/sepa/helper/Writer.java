package de.dlrg_rodenkirchen.sepa.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import de.dlrg_rodenkirchen.sepa.interfaces.IWriter;

public abstract class Writer implements IWriter {
	
	protected Properties props;
	
	protected static final String PROPS_NAME = "SEPA_config.cfg";
	protected static final String P_CRED_NAME = "credName";
	protected static final String P_CRED_ID = "credId";
	protected static final String P_CRED_IBAN = "credIBAN";
	protected static final String P_CRED_BIC = "credBIC";
	protected static final String P_EXEC_DATE = "execDate";
	protected static final String P_EXCEL_SHEET = "excelSheet";

	public abstract void write(File xmlFile, ArrayList<Person> persons)
			throws Exception;
	
	public final void setProps(Properties props) {
		this.props = props;
	}

}
