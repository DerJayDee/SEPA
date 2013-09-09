package de.dlrg_rodenkirchen.sepa;

public class Person {
	String number;
	String name;
	String vorname;
	String unterschrieben;
	String iban;
	String bic;
	String kontoinhaber;
	String mandatsref;
	String betrag;
	String zweck;

	public Person(String nr, String name, String vorname, String unterschrieben,
			String iban, String bic, String inhaber, String mandatsref,
			String betrag, String zweck) {
		this.number = nr;
		this.name = name;
		this.vorname = vorname;
		this.unterschrieben = unterschrieben;
		this.iban = iban;
		this.bic = bic;
		this.kontoinhaber = inhaber;
		this.mandatsref = mandatsref;
		this.betrag = betrag;
		this.zweck = zweck;
	}
}