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
 */package it.piemonte.arpa.sarpaper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import it.piemonte.arpa.sarpaper.models.DataCall;
import it.piemonte.arpa.sarpaper.models.EntryCall;
import it.piemonte.arpa.sarpaper.services.MonSrv;
import it.piemonte.arpa.sarpaper.utilities.Config;

public class MonFileManager {

	private final String SEP = ",";
	private final String FINE = "\n";
	FileWriter writer = null;
	private StringBuffer dataBuffer;
	private boolean bentry;
	private MonSrvReport rpt;
	private MonSrv monSrv;
	
	public MonFileManager(Context cntx, MonSrvReport rpt,MonSrv monSrv) {
		this.monSrv=monSrv;
		this.rpt=rpt;
		dataBuffer = new StringBuffer();
		Timer t = new Timer();
		t.scheduleAtFixedRate(new TtFile(), 0, Config.NEW_FILE_INTERVAL * 1000);
	}

	private String getModel() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return model;
		} else {
			return manufacturer + " " + model;
		}
	}

	private void openWriter() {
		log("---------start openWriter() current.log");
		File root = Environment.getExternalStorageDirectory();
		File file = new File(root.getAbsolutePath() + "/" + Config.FTP_UPLOAD_FOLDER
				+ "/curLog.log");

		try {
			if (root.canWrite()) {
				writer = new FileWriter(file);
				writer.append(getModel() + "," + monSrv.getMonSrvReport().getGlobalStatistics().getUid() + "\n");
				log( "---------end openWriter() current.log");
			}
		} catch (IOException e) {
			log( e.getMessage());
		}
	}

	public void write(EntryCall entryCall) {
		log( "---------start write() to current.log");
		SimpleDateFormat dt = new SimpleDateFormat("yyMMddHHmmss");
		String start = dt.format(entryCall.getStartCall());
		for (DataCall data : entryCall.getDataCall()) {
			dataBuffer.append(format(start,data));
		}
		try {
			writer.append(dataBuffer.toString());
			log( "Write entry " + dataBuffer );
			dataBuffer = new StringBuffer();
			bentry = true;
			log("---------end  write() to current.log");
		} catch (IOException e) {
			log( e.getMessage());
		}

	}

	private String format(String start, DataCall data) {
		return start + SEP + data.getSeconds() + SEP + data.getType() + SEP
				+ data.getSignal() + SEP + data.getDevice() + FINE;
	}

	public void closeWriter() {
		if (writer != null) {
			try {
				log( "---------start closeWriter() current.log");
				if (bentry) {
					writer.close();
					File tmp = new File(Environment
							.getExternalStorageDirectory().getAbsolutePath()
							+ "/" + Config.FTP_UPLOAD_FOLDER + "/curLog.log");

					SimpleDateFormat dt = new SimpleDateFormat("yyMMddHHmmss");
					String logFilename = monSrv.getMonSrvReport().getGlobalStatistics().getUid() + "_" + getModel()
							+ "_" + dt.format(new Date()) + ".log";
					tmp.renameTo(new File(Environment
							.getExternalStorageDirectory().getAbsolutePath()
							+ "/" + Config.FTP_SEND_FOLDER + "/" + logFilename));
					bentry = false;
					log("---------closeWriter() create file " +  logFilename);				
					openWriter();
				}
				log("---------end closeWriter() current.log");
			} catch (Throwable t) {
				log( t.getMessage());
			}

		} else
			openWriter();
	}

	private void log(String msg){
		Log.d(this.getClass().getSimpleName(),msg);
		rpt.addLog(msg);
	}
	
	class TtFile extends TimerTask {

		@Override
		public void run() {

			closeWriter();

		}

	}
}
