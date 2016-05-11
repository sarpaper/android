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
 */
package it.piemonte.arpa.sarpaper.utilities;

public class Utility {

	public static String getHMSTime(long time) {
		long diffSeconds = time % 60;
		long diffMinutes = time / 60 % 60;
		long diffHours = time / (60 * 60);
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
}
