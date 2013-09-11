package de.dlrg_rodenkirchen.sepa.xls;

import de.dlrg_rodenkirchen.sepa.helper.MyFileFilter;

public final class XLSFilter extends MyFileFilter {
	private final static String XLS = ".xls";

	public XLSFilter() {
		super(XLS);
	}

	@Override
	public final String getDescription() {
		return texte.getString("FF_XLS");
	}
}