package de.dlrg_rodenkirchen.sepa.xlsx;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class XLSXFilter extends FileFilter {
	public final static String XLSX = ".xlsx";

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		if (f.getName().endsWith(XLSX)) {
			return true;
		}
		return false;
	}

	@Override
	public String getDescription() {
		return "XLSX-Dateien";
	}
}