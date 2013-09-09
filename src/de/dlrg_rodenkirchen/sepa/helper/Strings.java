package de.dlrg_rodenkirchen.sepa.helper;

public enum Strings {
	// Config-File Properties
	PROPS_NAME("SEPA_config.cfg"),
	P_CRED_NAME("credName"),
	P_CRED_ID("credId"),
	P_CRED_IBAN("credIBAN"),
	P_CRED_BIC("credBIC"),
	P_EXEC_DATE("execDate"),
	P_EXCEL_SHEET("excelSheet"),
	
	// Zuordnung-File Properties
	ZUORDNUNGS_PROPS_NAME("SEPA_Zuordnung.cfg"),
	Z_NUMMER("mitgliedsnummer"),
	Z_NACHNAME("nachname"),
	Z_VORNAME("vorname"),
	Z_EINTRITTSDATUM("eintrittsdatum"),
	Z_IBAN("iban"),
	Z_BIC("bic"),
	Z_KONTOINHABER("kontoinhaber"),
	Z_BEITRAG("beitrag"),
	Z_MANDATSREFERENZ("mandatsreferenz"),
	Z_VERWENDUNGSZWECK("verwendungszweck"),

	// TextFeld-Labels
	TFL_CRED_NAME("Gläubiger:"),
	TFL_CRED_ID("Gläubiger-ID:"),
	TFL_CRED_IBAN("Gläubiger-IBAN:"),
	TFL_CRED_BIC("Gläubiger-BIC:"),
	TFL_EXEC_DATE("Datum der Ausführung:"),
	TFL_EXCEL_SHEET("Excel Sheet Nr.:");

	private final String stringValue;

	private Strings(final String s) {
		stringValue = s;
	}

	public String toString() {
		return stringValue;
	}

}