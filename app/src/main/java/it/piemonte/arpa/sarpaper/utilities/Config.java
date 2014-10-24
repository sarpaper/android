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
 */package it.piemonte.arpa.sarpaper.utilities;

public class Config {

	public static final int SPEAKERPHONE_CHECK_INTERVAL = 5;
	public static final String FILE_STAT = "filestat.dat";
	public static final String FILE_DAY_STAT = "fileDaystat.dat";
	public static final String FILE_LAST_STAT = "fileLaststat.dat";
	public static final String FTP_UPLOAD_FOLDER = "sarpaperUploadFolder";
	public static final String FTP_SEND_FOLDER = FTP_UPLOAD_FOLDER + "/toSend";
	public static final int FTP_INTERVAL = 120;
	public static final int NEW_FILE_INTERVAL = 60;
	
	public static final String FTP_HOST = "";
	public static final int FTP_PORT = 0;
	public static final String FTP_USR = "";
	public static final String FTP_PWD = "";
	public static final String FTP_DIR = "";

	private Config() {
	}

	public static boolean ftpEnabled() {

		return !((FTP_HOST == null) || FTP_HOST.trim().length()==0);
	}
}
