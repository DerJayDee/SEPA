package de.dlrg_rodenkirchen.sepa;

public class Mitglied {
	// TODO
	String mitgliedsNr;
	String name;
	String vorname;
	String iban;
	String bic;
	String kontoinhaber;
	String betrag;

	public Mitglied(String nr, String name, String vorname, String iban,
			String bic, String inhaber, String betrag) {
		this.mitgliedsNr = nr;
		this.name = name;
		this.vorname = vorname;
		this.iban = iban;
		this.bic = bic;
		this.kontoinhaber = inhaber;
		this.betrag = betrag;
	}
}