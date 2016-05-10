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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.content.Context;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import it.piemonte.arpa.sarpaper.models.DeviceType;
import it.piemonte.arpa.sarpaper.models.EntryCall;
import it.piemonte.arpa.sarpaper.models.GlobalStatistics;
import it.piemonte.arpa.sarpaper.models.Statistics;
import it.piemonte.arpa.sarpaper.services.MonSrv;
import it.piemonte.arpa.sarpaper.utilities.Config;

/**
 * Gestione Statiche giornaliere, globali e ultima chiamata
 */
public class MonSrvReport {

	private enum StatisticsType {
		DAY_STAT, GLOBAL_STAT, LAST_STAT
	};

	private String phoneStatus, networkType, signal;
	private DeviceType device;
	private StringBuilder log;
	private EntryCall lastCall;

	private GlobalStatistics globalStatistics;
	private Statistics lastCallStatistics;

	private int currentLogLine;
	private static final int MAX_LOG_ROW = 20;
	private MonSrv monSrv;
	private Map<Date, Statistics> dayStatistics;

	public MonSrvReport(MonSrv srv) {
		resetVoiceParameters();
		log = new StringBuilder("---Start Log----");
		currentLogLine = 1;
		monSrv = srv;

		// Init statistics
		globalStatistics = new GlobalStatistics();
		dayStatistics = new HashMap<Date, Statistics>();

		if (isStatPresent(StatisticsType.LAST_STAT)) {
			restoreStat(StatisticsType.LAST_STAT);
		}

		if (!isStatPresent(StatisticsType.GLOBAL_STAT)) {
			createUID();
			storeStatJson(StatisticsType.GLOBAL_STAT);
		} else {
			restoreStatJson(StatisticsType.GLOBAL_STAT);
		}
	}

	public String getUptime() {
		return calcUptime(new Date(globalStatistics.getStarttime()));
	}

	public String getPhoneStatus() {
		return phoneStatus;
	}

	public void setPhoneStatus(String phoneStatus) {
		this.phoneStatus = phoneStatus;
	}

	public String getNetworkType() {
		return networkType;
	}

	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}

	public String getSignal() {
		return signal;
	}

	public void setSignal(String signal) {
		this.signal = signal;
	}

	public DeviceType getDevice() {
		return device;
	}

	public void setDevice(DeviceType device) {
		this.device = device;
	}

	public EntryCall getLastCall() {
		return lastCall;
	}

	/**
	 * Aggiorna le statistiche con i dati dell'ultima chiamata
	 * Il servizio in background chiama questo metodo al termine di una chiamata
	 * @param lastCall ultima chiamata effettuata
     */
	public void setLastCall(EntryCall lastCall) {
		this.lastCall = lastCall;
		lastCallStatistics = new Statistics();
		lastCallStatistics.update(lastCall);
		globalStatistics.update(lastCall);
		Log.d("GlobalStatistics", globalStatistics.toString());
		storeStat(StatisticsType.LAST_STAT);
		storeStatJson(StatisticsType.GLOBAL_STAT);
		updateDayStatistics(lastCall);
		// test
		// joinDayStatistics(null, new Date());
	}

	public void createUID() {
		globalStatistics.setStarttime(new Date().getTime());
		TelephonyManager tm;
		tm = (TelephonyManager) monSrv.getApplicationContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		/*
		 * getSubscriberId() function Returns the unique subscriber ID, for
		 * example, the IMSI for a GSM phone.
		 */
		String id = tm.getSubscriberId();
		if (id != null) {
			globalStatistics.setUid(id);
			return;
		}
		/*
		 * getDeviceId() function Returns the unique device ID. for example,the
		 * IMEI for GSM and the MEID or ESN for CDMA phones.
		 */
		id = tm.getDeviceId();
		if (id != null) {
			globalStatistics.setUid("IM" + id);
			return;
		}
		/*
		 * Settings.Secure.ANDROID_ID returns the unique DeviceID Works for
		 * Android 2.2 and above
		 */
		id = Settings.Secure.getString(monSrv.getApplicationContext()
				.getContentResolver(), Settings.Secure.ANDROID_ID);
		if (id != null) {
			globalStatistics.setUid("AI" + id);
			return;
		}
		/*
		 * Nostro id
		 */
		id = String.valueOf(new Date().getTime());
		globalStatistics.setUid("TI" + id);
	}

	public void addLog(String row) {
		if (currentLogLine >= MAX_LOG_ROW) {
			int index = this.log.length() / 2;
			int indexEndLine = this.log.indexOf("\n", index);
			this.log = new StringBuilder(this.log.substring(0, indexEndLine));
			currentLogLine = MAX_LOG_ROW / 2;
		}
		this.log.insert(0, row + "\n");
		currentLogLine++;
	}

	public String getLog() {
		if (log == null) {
			return "";
		} else {
			return this.log.toString();
		}

	}

	public GlobalStatistics getGlobalStatistics() {
		return globalStatistics;
	}

	public Statistics getLastCallStatistics() {
		return lastCallStatistics;
	}

	public Map<Date, Statistics> getDayStatistics() {
		restoreStat(StatisticsType.DAY_STAT);
		Map<Date, Statistics> result = dayStatistics;
		dayStatistics = null;
		return result;
	}

	public void resetVoiceParameters() {
		phoneStatus = "Idle";
		signal = "-";
		networkType = "-";
		device = DeviceType.NO_DEVICE;

	}

	public void update(String phoneStatus, String networkType, int signal,
			DeviceType device, String log) {
		this.phoneStatus = phoneStatus != null ? phoneStatus : this.phoneStatus;
		this.networkType = networkType;
		this.signal = String.valueOf(signal);
		;
		this.device = device;
		addLog(log);
	}

	/**
	 * Tempo di updatime del servizio in giorni, ore, minuti e secondi
	 * @param start
	 * @return
     */
	private String calcUptime(Date start) {
		String result = "";
		try {

			// in milliseconds
			Long diff = new Date().getTime() - start.getTime();
			Long diffSeconds = diff / 1000 % 60;
			Long diffMinutes = diff / (60 * 1000) % 60;
			Long diffHours = diff / (60 * 60 * 1000) % 24;
			Long diffDays = diff / (24 * 60 * 60 * 1000);
			String testo;
			testo = (diffHours < 10 ? "0" + String.valueOf(diffHours) : String
					.valueOf(diffHours));
			testo += ":"
					+ (diffMinutes < 10 ? "0" + String.valueOf(diffMinutes)
							: String.valueOf(diffMinutes));
			testo += ":"
					+ (diffSeconds < 10 ? "0" + String.valueOf(diffSeconds)
							: String.valueOf(diffSeconds));

			result = String.format("%s gg %s", diffDays.toString(), testo);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}


	private void updateDayStatistics(EntryCall lastCall) {
		Date callDate = removeTimeFromDate(lastCall.getStartCall());
		if (isStatPresent(StatisticsType.DAY_STAT)) {
			restoreStat(StatisticsType.DAY_STAT);
		} else {
			dayStatistics = new HashMap<Date, Statistics>();
		}
		Statistics stat;
		if (dayStatistics.containsKey(callDate)) {
			stat = dayStatistics.get(callDate);
			stat.update(lastCall);
		} else {
			stat = new Statistics();
			stat.update(lastCall);
			dayStatistics.put(callDate, stat);
		}
		Log.d("DayStatistics", dayStatistics.toString());
		storeStat(StatisticsType.DAY_STAT);
	}

	/**
	 * Serializza su file il tipo di statistica indicata in formato binario
	 * @param type
	 */
	private void storeStat(StatisticsType type) {

		File root = Environment.getExternalStorageDirectory();
		File filestat = null;
		if (type == StatisticsType.GLOBAL_STAT) {
			filestat = new File(root.getAbsolutePath() + "/"
					+ Config.FTP_UPLOAD_FOLDER + "/" + Config.FILE_STAT);
		} else if (type == StatisticsType.DAY_STAT) {
			filestat = new File(root.getAbsolutePath() + "/"
					+ Config.FTP_UPLOAD_FOLDER + "/" + Config.FILE_DAY_STAT);
		} else if (type == StatisticsType.LAST_STAT) {
			filestat = new File(root.getAbsolutePath() + "/"
					+ Config.FTP_UPLOAD_FOLDER + "/" + Config.FILE_LAST_STAT);
		}
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(filestat);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(
					fileOutputStream);
			if (type == StatisticsType.GLOBAL_STAT) {
				objectOutputStream.writeObject(globalStatistics);
			} else if (type == StatisticsType.DAY_STAT) {
				resizeDayStatistics();
				objectOutputStream.writeObject(dayStatistics);
				dayStatistics = null;
			} else if (type == StatisticsType.LAST_STAT) {
				objectOutputStream.writeObject(lastCallStatistics);
			}
			objectOutputStream.close();
		} catch (FileNotFoundException e) {

			Log.d("errore store", e.toString());
		} catch (IOException e) {

			Log.d("errore store", e.toString());
		}

	}

	/**
	 * Serializza su file il tipo di statistica indicata in formato json
	 * @param type
     */
	private void storeStatJson(StatisticsType type) {

		File root = Environment.getExternalStorageDirectory();
		File filestat = null;
		if (type == StatisticsType.GLOBAL_STAT) {
			filestat = new File(root.getAbsolutePath() + "/"
					+ Config.FTP_UPLOAD_FOLDER + "/" + Config.FILE_STAT);
		} else if (type == StatisticsType.DAY_STAT) {
			filestat = new File(root.getAbsolutePath() + "/"
					+ Config.FTP_UPLOAD_FOLDER + "/" + Config.FILE_DAY_STAT);
		} else if (type == StatisticsType.LAST_STAT) {
			filestat = new File(root.getAbsolutePath() + "/"
					+ Config.FTP_UPLOAD_FOLDER + "/" + Config.FILE_LAST_STAT);
		}
		try {
			Gson gson = new Gson();
			String jsonObject = null;
			FileOutputStream fileOutputStream = new FileOutputStream(filestat);
			if (type == StatisticsType.GLOBAL_STAT) {
				jsonObject = gson.toJson(globalStatistics);
			} else if (type == StatisticsType.DAY_STAT) {
				resizeDayStatistics();
				jsonObject = gson.toJson(dayStatistics,
						new TypeToken<HashMap<Date, Statistics>>() {
						}.getType());
				dayStatistics = null;
			} else if (type == StatisticsType.LAST_STAT) {
				jsonObject = gson.toJson(lastCallStatistics);
			}
			if (jsonObject != null)
				fileOutputStream.write(jsonObject.getBytes());
			fileOutputStream.close();
		} catch (FileNotFoundException e) {

			Log.d("errore store", e.toString());
		} catch (IOException e) {
			Log.d("errore store", e.toString());
		}

	}

	/**
	 * Le statistiche giornaliere vengono mantenute per 30 gg
	 */
	private void resizeDayStatistics() {
		if (dayStatistics == null)
			return;
		if (dayStatistics.isEmpty())
			return;
		if (dayStatistics.size() < 2)
			return;
		Set<Date> keySet = dayStatistics.keySet();
		Date[] dates = new Date[keySet.size()];
		keySet.toArray(dates);
		Calendar cal = Calendar.getInstance();
		cal.setTime(dates[0]);
		int startMonth = cal.get(Calendar.MONTH);
		cal.setTime(dates[dates.length - 1]);
		int curMonth = cal.get(Calendar.MONTH);
		if (curMonth - startMonth > 1) {
			for (Iterator<Map.Entry<Date, Statistics>> it = dayStatistics
					.entrySet().iterator(); it.hasNext();) {
				Map.Entry<Date, Statistics> entry = it.next();
				cal.setTime(entry.getKey());
				if (cal.get(Calendar.MONTH) == startMonth) {
					it.remove();
				}
			}
		}
	}

	/**
	 * Restore in memoria del tipo di statistica indicato da file (Deserializzazione degli oggetti da formato json)
	 * @param type
     */
	private void restoreStatJson(StatisticsType type) {

		if (isStatPresent(type)) {// //crea file .dat
			File root = Environment.getExternalStorageDirectory();
			File filestat = null;
			if (type == StatisticsType.GLOBAL_STAT) {
				filestat = new File(root.getAbsolutePath() + "/"
						+ Config.FTP_UPLOAD_FOLDER + "/" + Config.FILE_STAT);
			} else if (type == StatisticsType.DAY_STAT) {
				filestat = new File(root.getAbsolutePath() + "/"
						+ Config.FTP_UPLOAD_FOLDER + "/" + Config.FILE_DAY_STAT);
			} else if (type == StatisticsType.LAST_STAT) {
				filestat = new File(root.getAbsolutePath() + "/"
						+ Config.FTP_UPLOAD_FOLDER + "/"
						+ Config.FILE_LAST_STAT);
			}
			try {
				FileInputStream fileinputStream = new FileInputStream(filestat);
				InputStreamReader isr = new InputStreamReader(fileinputStream);
				BufferedReader br = new BufferedReader(isr);
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				String jsonObject = sb.toString();
				Gson gson = new Gson();
				if (type == StatisticsType.GLOBAL_STAT) {
					globalStatistics = gson.fromJson(jsonObject,
							GlobalStatistics.class);
				} else if (type == StatisticsType.DAY_STAT) {
					dayStatistics = gson.fromJson(jsonObject,
							new TypeToken<HashMap<Date, Statistics>>() {
							}.getType());
				} else if (type == StatisticsType.LAST_STAT) {
					lastCallStatistics = gson.fromJson(jsonObject,
							Statistics.class);
				}
				br.close();
				isr.close();
				fileinputStream.close();
			} catch (FileNotFoundException e) {
				Log.d("errore restore", e.toString());
			} catch (IOException e) {
				Log.d("errore restore", e.toString());
			} catch (ClassCastException e) {
				Log.d("errore restore", e.toString());
				if (type == StatisticsType.GLOBAL_STAT) {
					createUID();
					storeStat(StatisticsType.GLOBAL_STAT);
				} else {
					dayStatistics = new HashMap<Date, Statistics>();
				}
			} catch (Exception e) {
				Log.d("errore restore", e.toString());
				restoreStat(type);
			}
		}

	}

	/**
	 * Restore in memoria del tipo di statistica indicato da file (Deserializzazione degli oggetti da formato binario)
	 * @param type
     */
    @SuppressWarnings("unchecked")
	private void restoreStat(StatisticsType type) {

		if (isStatPresent(type)) {// //crea file .dat
			File root = Environment.getExternalStorageDirectory();
			File filestat = null;
			if (type == StatisticsType.GLOBAL_STAT) {
				filestat = new File(root.getAbsolutePath() + "/"
						+ Config.FTP_UPLOAD_FOLDER + "/" + Config.FILE_STAT);
			} else if (type == StatisticsType.DAY_STAT) {
				filestat = new File(root.getAbsolutePath() + "/"
						+ Config.FTP_UPLOAD_FOLDER + "/" + Config.FILE_DAY_STAT);
			} else if (type == StatisticsType.LAST_STAT) {
				filestat = new File(root.getAbsolutePath() + "/"
						+ Config.FTP_UPLOAD_FOLDER + "/"
						+ Config.FILE_LAST_STAT);
			}
			try { 
				FileInputStream fileinputStream = new FileInputStream(filestat);
				ObjectInputStream objectInputStream = new ObjectInputStream(
						fileinputStream);
				if (type == StatisticsType.GLOBAL_STAT) {
					globalStatistics = (GlobalStatistics) objectInputStream
							.readObject(); 
					if (globalStatistics == null) {
						globalStatistics = new GlobalStatistics();
						createUID();
					} else if (globalStatistics.getStarttime() == 0
							|| globalStatistics.getUid() == null
							|| globalStatistics.getUid().trim() == "") {
						createUID();
					}

				} else if (type == StatisticsType.DAY_STAT) {
					dayStatistics = (Map<Date, Statistics>) objectInputStream
							.readObject();
				} else if (type == StatisticsType.LAST_STAT) {
					lastCallStatistics = (Statistics) objectInputStream
							.readObject();
				}
				objectInputStream.close();
			} catch (Exception e) {
				createUID();
				dayStatistics = new HashMap<Date, Statistics>();
			}
		}

	}

	/**
	 * Verifica se esiste un salvataggio su file per il tipo di statistica indicata
	 * @param type tipo di statistica
	 * @return
     */
	private boolean isStatPresent(StatisticsType type) {

		File root = Environment.getExternalStorageDirectory();
		File filestat;
		if (type == StatisticsType.GLOBAL_STAT) {
			filestat = new File(root.getAbsolutePath() + "/"
					+ Config.FTP_UPLOAD_FOLDER + "/" + Config.FILE_STAT);
		} else {
			filestat = new File(root.getAbsolutePath() + "/"
					+ Config.FTP_UPLOAD_FOLDER + "/" + Config.FILE_DAY_STAT);
		}
		return filestat.exists();
	}

	private Date removeTimeFromDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	private Date removeTimeFromDateAlternative(Date date) {
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		String d = df.format(date);
		return new Date(Date.parse(d));
	}

	/**
	 * Unisce le statistiche per un certo periodo
	 * @param start giorno inizio periodo
	 * @param end giorno fine periodo
     * @return statistiche aggregate nel periodo
     */
	public Statistics joinDayStatistics(Date start, Date end) {
		restoreStat(StatisticsType.DAY_STAT);
		Statistics result = new Statistics();
		Date d1 = removeTimeFromDate(start != null ? start : new Date(0));
		Date d2 = removeTimeFromDate(end != null ? end : new Date());
		for (Iterator<Map.Entry<Date, Statistics>> it = dayStatistics
				.entrySet().iterator(); it.hasNext();) {
			Map.Entry<Date, Statistics> entry = it.next();
			if ((entry.getKey().equals(d1) || entry.getKey().after(d1))
					&& (entry.getKey().equals(d2) || entry.getKey().before(d2))) {
				result.plus(entry.getValue());
			}
		}
		dayStatistics = null;
		Log.d("joinDayStatistics", result.toString());
		return result;
	}
}