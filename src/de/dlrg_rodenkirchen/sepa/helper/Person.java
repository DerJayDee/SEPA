package de.dlrg_rodenkirchen.sepa.helper;

public class Person {
	String id;
	String name;
	String vorname;
	String signed;
	String iban;
	String bic;
	String kontoinhaber;
	String mandatsref;
	String betrag;
	String zweck;

	public Person(String nr, String name, String vorname,
			String unterschrieben, String iban, String bic, String inhaber,
			String mandatsref, String betrag, String zweck) {
		this.id = nr;
		this.name = name;
		this.vorname = vorname;
		this.signed = unterschrieben;
		this.iban = iban;
		this.bic = bic;
		this.kontoinhaber = inhaber;
		this.mandatsref = mandatsref;
		this.betrag = betrag;
		this.zweck = zweck;
	}

	public final String getId() {
		return id;
	}

	public final String getName() {
		return name;
	}

	public final String getVorname() {
		return vorname;
	}

	public final String getSigned() {
		return signed;
	}

	public final String getIban() {
		return iban;
	}

	public final String getBic() {
		return bic;
	}

	public final String getKontoinhaber() {
		return kontoinhaber;
	}

	public final String getMandatsref() {
		return mandatsref;
	}

	public final String getBetrag() {
		return betrag;
	}

	public final String getZweck() {
		return zweck;
	}

}