package de.dlrg_rodenkirchen.sepa.excel;

import java.io.File;
import java.util.ResourceBundle;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.dlrg_rodenkirchen.sepa.helper.StaticString;

public final class ExcelFilter extends FileFilter {

	private ResourceBundle texte;

	private final FileNameExtensionFilter filter;

	public ExcelFilter() {
		loadStrings();
		filter = new FileNameExtensionFilter(texte.getString("FF_EXCEL"),
				"xls", "xlsx");
	}

	public boolean accept(File file) {
		return filter.accept(file);
	}

	@Override
	public String getDescription() {
		return filter.getDescription();
	}

	private final void loadStrings() {
		if (texte == null) {
			texte = ResourceBundle.getBundle(StaticString.STRINGS_BUNDLE);
		}
	}
}