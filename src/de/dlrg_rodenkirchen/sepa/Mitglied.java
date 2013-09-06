package de.dlrg_rodenkirchen.sepa;

public class Mitglied {
	String mitgliedsNr;
	String name;
	String vorname;
	String eintritt;
	String iban;
	String bic;
	String kontoinhaber;
	String mandatsref;
	String betrag;
	String zweck;

	public Mitglied(String nr, String name, String vorname, String eiritt,
			String iban, String bic, String inhaber, String mandatsref,
			String betrag, String zweck) {
		this.mitgliedsNr = nr;
		this.name = name;
		this.vorname = vorname;
		this.eintritt = eiritt;
		this.iban = iban;
		this.bic = bic;
		this.kontoinhaber = inhaber;
		this.mandatsref = mandatsref;
		this.betrag = betrag;
		this.zweck = zweck;
	}
}