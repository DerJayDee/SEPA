package de.dlrg_rodenkirchen.sepa.xlsx;

import de.dlrg_rodenkirchen.sepa.helper.MyFileFilter;

public final class XLSXFilter extends MyFileFilter {
	private final static String XLSX = ".xlsx";

	public XLSXFilter() {
		super(XLSX);
	}

	@Override
	public final String getDescription() {
		return texte.getString("FF_XLSX");
	}
}