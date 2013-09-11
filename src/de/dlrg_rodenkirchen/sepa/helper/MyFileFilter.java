package de.dlrg_rodenkirchen.sepa.helper;

import java.io.File;
import java.util.ResourceBundle;

import javax.swing.filechooser.FileFilter;

public abstract class MyFileFilter extends FileFilter {
	private String ENDING;

	protected ResourceBundle texte;

	public MyFileFilter(String ending) {
		loadStrings();
		setEnding(ending);
	}

	public MyFileFilter() {
		this("");
	}

	@Override
	public final boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		if (f.getName().endsWith(ENDING)) {
			return true;
		}
		return false;
	}

	public abstract String getDescription();

	private final void loadStrings() {
		if (texte == null) {
			texte = ResourceBundle
					.getBundle(StaticString.STRINGS_BUNDLE);
		}
	}

	private final void setEnding(String ending) {
		ENDING = ending;
	}
}
