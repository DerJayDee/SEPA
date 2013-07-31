package de.dlrg_rodenkirchen.sepa;

public class Mitglied {
	String mitgliedsNr;
	String name;
	String vorname;
	String iban;
	String bic;
	String kontoinhaber;
	String mandatsref;
	String betrag;
	String zweck1;
	String zweck2;

	public Mitglied(String nr, String name, String vorname, String iban,
			String bic, String inhaber, String mandatsref, String betrag, String zweck1, String zweck2) {
		this.mitgliedsNr = nr;
		this.name = name;
		this.vorname = vorname;
		this.iban = iban;
		this.bic = bic;
		this.kontoinhaber = inhaber;
		this.mandatsref = mandatsref;
		this.betrag = betrag;
		this.zweck1 = zweck1;
		this.zweck2 = zweck2;
	}
}