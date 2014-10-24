/*   Sarpaper Android Application
 *   Copyright (C) 2014  Arpa Piemonte
 *
 *   This file is part of Sarpaper Android Application.

 * 	This software is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *   This software is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */package it.piemonte.arpa.sarpaper.models;

public class GlobalStatistics extends Statistics {

	private static final long serialVersionUID = 1L;

	private String uid;
	private long starttime;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public long getStarttime() {
		return starttime;
	}

	public void setStarttime(long starttime) {
		this.starttime = starttime;
	}

	public String getCallTimeDescr() {
		long diffSeconds = totalTime % 60;
		long diffMinutes = totalTime / 60 % 60;
		long diffHours = (totalTime / (60 * 60) % 24);
		long diffDays = (totalTime / (60 * 60 * 24) % 24);
		return String.format("%s gg %s:%s:%s", diffDays, diffHours,
				diffMinutes, diffSeconds);
	}

	public String getCallTimeDescrDevice() {
		long diffSeconds = deviceTime % 60;
		long diffMinutes = deviceTime / 60 % 60;
		long diffHours = deviceTime / (60 * 60) % 24;
		String testo;
		testo = (diffHours < 10 ? "0" + String.valueOf(diffHours) : String
				.valueOf(diffHours));
		testo += ":"
				+ (diffMinutes < 10 ? "0" + String.valueOf(diffMinutes)
						: String.valueOf(diffMinutes));
		testo += ":"
				+ (diffSeconds < 10 ? "0" + String.valueOf(diffSeconds)
						: String.valueOf(diffSeconds));
		return testo;
	}

	public String getCallTimeDescrNoDevice() {
		long diffSeconds = noDeviceTime % 60;
		long diffMinutes = noDeviceTime / 60 % 60;
		long diffHours = noDeviceTime / (60 * 60) % 24;
		String testo;
		testo = (diffHours < 10 ? "0" + String.valueOf(diffHours) : String
				.valueOf(diffHours));
		testo += ":"
				+ (diffMinutes < 10 ? "0" + String.valueOf(diffMinutes)
						: String.valueOf(diffMinutes));
		testo += ":"
				+ (diffSeconds < 10 ? "0" + String.valueOf(diffSeconds)
						: String.valueOf(diffSeconds));
		return testo;
	}

	public String getGUIDescription() {
		return String.format("\nTotale Chiamate: %s \nDurata Totale: %s"
				+ "\nDurata all'orecchio : %s\nDurata in sicurezza: %s",
				this.numCalls, this.getCallTimeDescr(),
				this.getCallTimeDescrNoDevice(), this.getCallTimeDescrDevice());
	}

}
