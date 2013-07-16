package de.dlrg_rodenkirchen.sepa;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class XLSFilter extends FileFilter {
	public final static String XLS = ".xls";

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		if (f.getName().endsWith(XLS)) {
			return true;
		}
		return false;
	}

	@Override
	public String getDescription() {
		return "XLS-Dateien";
	}
}