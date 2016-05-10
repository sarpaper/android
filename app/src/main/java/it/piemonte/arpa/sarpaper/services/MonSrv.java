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
 */package it.piemonte.arpa.sarpaper.services;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import it.piemonte.arpa.sarpaper.utilities.Config;
import it.piemonte.arpa.sarpaper.utilities.Connectivity;
import it.piemonte.arpa.sarpaper.utilities.Ftp;
import it.piemonte.arpa.sarpaper.MonFileManager;
import it.piemonte.arpa.sarpaper.MonSrvReport;
import it.piemonte.arpa.sarpaper.models.DataCall;
import it.piemonte.arpa.sarpaper.models.DeviceType;
import it.piemonte.arpa.sarpaper.models.EntryCall;

/**
 * Monitora il traffico voce
 * Servizio in background, avviato in automatico all'accensione del telefono
 */
public class MonSrv extends Service {

	private final IBinder mBinder = new MonSrvBinder();

	private AudioManager audioManager;
	private byte bSpeakerPhone, bHeadSet;
	private Context cntx;
	private TelephonyManager telephonyManager;
	private EntryCall entryCall;
	private DataCall data;
	private int curSignal;
	private MonFileManager fManager;
	private Ftp ftpClient;
	SharedPreferences prefs;
	private MonSrvReport monSrvReport;

	@Override
	public void onCreate() {
		super.onCreate();
		cntx = this.getBaseContext();
		prepareFolders();
		monSrvReport = new MonSrvReport(this);
		ftpClient = new Ftp(Config.FTP_UPLOAD_FOLDER);
		Timer tFtp = new Timer();
		// leggere codice telefono!!!
		if (Config.ftpEnabled()) {
			tFtp.scheduleAtFixedRate(new TtFtp(), 0, Config.FTP_INTERVAL * 1000);
			fManager = new MonFileManager(cntx, monSrvReport, this);
		}
		Log.d("MonSrv", "oncreate()");

		audioManager = (AudioManager) cntx
				.getSystemService(Context.AUDIO_SERVICE);

		// Get the telephony manager
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		//Listener chiamate vocali, intercetta le varie fasi di una chiamata
		telephonyManager.listen(new HandleCallStateChange(),
				PhoneStateListener.LISTEN_CALL_STATE);
		//Listener cambiamento ricezione segnale
		telephonyManager.listen(new HandleSignalChange(),
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		//Listener utilizzo auricolari
		IntentFilter headsetFilter = new IntentFilter(
				Intent.ACTION_HEADSET_PLUG);
		cntx.registerReceiver(new HandleHeadset(), headsetFilter);
		//Listener utilizzo vivavoce
		IntentFilter btHeadsetFilter = new IntentFilter(
				BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
		cntx.registerReceiver(new HandleBluetoothHeadset(), btHeadsetFilter);
	}

	private void prepareFolders() {
		File root = Environment.getExternalStorageDirectory();
		File file = new File(root.getAbsolutePath() + "/"
				+ Config.FTP_SEND_FOLDER);
		boolean ok = file.mkdirs();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (Config.ftpEnabled())
			fManager.closeWriter();
	}

	public MonSrvReport getMonSrvReport() {
		return monSrvReport;
	}

	private int getCurType() {
		int tipo = 0;
		try {
			tipo = Connectivity.getNetworkInfo(cntx).getSubtype();
		} catch (Exception e) {
			//ignored
		}
		return tipo;
	}

	// verifica device 0 nulla; 1 speakerphone vivavoce; 2 headset cuffia;
	private DeviceType getCurDevice() {

		DeviceType result;

		if (bSpeakerPhone == 1)
			result = DeviceType.VIVA_VOCE;
		else if (bHeadSet == 1)
			result = DeviceType.CUFFIE;
		else
			result = DeviceType.NO_DEVICE;

		return result;
	}

	/*
	 * Listener classes
	 */

	/*
	 * CallState Listener
	 */
	class HandleCallStateChange extends PhoneStateListener {

		Timer tCheckSpeakerPhone = new Timer();

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			String phoneStatus = "Idle";
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				if (tCheckSpeakerPhone != null) {
					tCheckSpeakerPhone.cancel();
					tCheckSpeakerPhone = null;
				}
				//Chiusura chiamata, aggiornamento statistiche e scrittura file da inviare
				if (entryCall != null) {
					entryCall.finish();
					Log.d("MonSrv", entryCall.toString());
					if (Config.ftpEnabled())
						fManager.write(entryCall);
					monSrvReport.setLastCall(entryCall);
					entryCall = null;
				}
				phoneStatus = "Idle";
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				tCheckSpeakerPhone = new Timer();
				bSpeakerPhone = audioManager.isSpeakerphoneOn() ? (byte) 1
						: (byte) 0;
				tCheckSpeakerPhone.scheduleAtFixedRate(new CheckSpeakerPhone(),
						0, Config.SPEAKERPHONE_CHECK_INTERVAL * 1000);
				bHeadSet = audioManager.isWiredHeadsetOn() ? (byte) 1
						: (byte) 0;

				//Nuova chiamata
				data = new DataCall(getCurType(), curSignal, getCurDevice(),
						Connectivity.whatConnection(cntx));
				entryCall = new EntryCall(data);
				phoneStatus = "Off Hook" + " - " + incomingNumber;
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				phoneStatus = "Ringing" + " - " + incomingNumber;
				break;
			}
			monSrvReport.update(phoneStatus, Connectivity.whatConnection(cntx),
					curSignal, getCurDevice(), "onCallStateChanged: " + " - "
							+ incomingNumber);
			Log.d("mmsg", "oncallstatechanged " + " - " + incomingNumber + "-"
					+ bSpeakerPhone);
		}

	}

	/*
	 * Signal Change Listener
	 */
	class HandleSignalChange extends PhoneStateListener {

		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			String tipo = Connectivity.whatConnection(cntx);
			if (signalStrength.isGsm())
				curSignal = signalStrength.getGsmSignalStrength() != 99 ? signalStrength
						.getGsmSignalStrength() * 2 - 113 : 99;
			else
				curSignal = signalStrength.getCdmaDbm();
			//Aggiornamento chiamata e statistiche correnti
			if (entryCall != null) {
				data = new DataCall(getCurType(), curSignal, getCurDevice(),
						tipo);
				entryCall.addDataCall(data);
			}
			monSrvReport.update(null, Connectivity.whatConnection(cntx),
					curSignal, getCurDevice(), "onSignalCh: " + curSignal
							+ " - type: " + tipo + " - vvoice: "
							+ bSpeakerPhone);

			Log.d("mmsg", "onsignalchanged " + tipo + "-" + bSpeakerPhone + "-"
					+ curSignal + " " + signalStrength);

		}
	}

	/*
	 * Plug/Unplug Headset listener
	 */
	class HandleHeadset extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.hasExtra("state")) {
				Log.d("MonSrv",
						"headsetManager: " + intent.getIntExtra("state", 99));
				bHeadSet = (byte) intent.getIntExtra("state", 99);
				String tipo = Connectivity.whatConnection(cntx);

				//Aggiornamento chiamata e statistiche correnti
				if (entryCall != null) {
					data = new DataCall(getCurType(), curSignal,
							getCurDevice(), tipo);
					entryCall.addDataCall(data);
				}
				monSrvReport.update(null, Connectivity.whatConnection(cntx),
						curSignal, getCurDevice(),
						"headsetListener->OnReceive(): " + curSignal
								+ " - type: " + tipo + " - headset: "
								+ bHeadSet);
			} else
				Log.d("MonSrv", "headsetManager: not extra");

		}
	}

	/*
	 * BluetoothHeadset listener
	 */
	class HandleBluetoothHeadset extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			int res = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, 0);
			Log.d("MonSrv", "BluetoothHeadsetManager: " + res);

			if (res == BluetoothHeadset.STATE_AUDIO_CONNECTING)
				return;
			if (res == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
				bHeadSet = 1;
			} else if (res == BluetoothHeadset.STATE_AUDIO_DISCONNECTED) {
				bHeadSet = 0;
			}
			String tipo = Connectivity.whatConnection(cntx);
			//Aggiornamento chiamata e statistiche correnti
			if (entryCall != null) {
				data = new DataCall(getCurType(), curSignal, getCurDevice(),
						tipo);
				entryCall.addDataCall(data);
			}
			monSrvReport.update(null, Connectivity.whatConnection(cntx),
					curSignal, getCurDevice(),
					"bluetoothHeadsetListener->OnReceive(): " + curSignal
							+ " - type: " + tipo + " - headset: " + bHeadSet);
		}

	}

	/*
	 * Timer classes
	 */

	/*
	 * Spedisce i file con i dati delle chiamate via ftp se la rete è disponibile
	 */
	class TtFtp extends TimerTask {

		@Override
		public void run() {

			if (!monSrvReport.getPhoneStatus().equals("Idle")) {
				Log.d("MonSrv",
						"FTP Connection skipped because telephone status not Idle but "
								+ monSrvReport.getPhoneStatus());
				return;
			}
			boolean status = false;

			try {
				Log.d("MonSrv", "Try FTP Connection to ftp.arpa.piemonte.it");
				// Replace your UID & PW here
				status = ftpClient.ftpConnect(Config.FTP_HOST, Config.FTP_USR,
						Config.FTP_PWD, Config.FTP_PORT);

				if (status == true) {
					status = ftpClient.ftpChangeDirectory("/" + Config.FTP_DIR);
					if (!status) {
						ftpClient.ftpMakeDirectory("/" + Config.FTP_DIR);
					}
					status = ftpClient.ftpuploadfolder();
					status = ftpClient.ftpDisconnect();

				} else {

					Log.d("MonSrv", "FTP Connection failed");
				}
			} catch (Exception e) {
				Log.d("FtpTimer", "Ftp fail. " + e.getMessage());
			}
		}

	}

	/*
	 * Check on/off SpeakerPhone during voice call
	 */
	class CheckSpeakerPhone extends TimerTask {

		@Override
		public void run() {
			Log.d("ChangeSpeaker->",
					String.valueOf(audioManager.isSpeakerphoneOn()));

			byte newValue = audioManager.isSpeakerphoneOn() == true ? (byte) 1
					: (byte) 0;

			if (bSpeakerPhone == newValue)
				return;

			String tipo = Connectivity.whatConnection(cntx);
			bSpeakerPhone = newValue;
			//Aggiornamento chiamata e statistiche correnti
			if (entryCall != null) {
				data = new DataCall(getCurType(), curSignal, getCurDevice(),
						tipo);
				entryCall.addDataCall(data);
				monSrvReport.update(null, Connectivity.whatConnection(cntx),
						curSignal, getCurDevice(), "onSignalCh: " + curSignal
								+ " - type: " + tipo + " - vvoice: "
								+ bSpeakerPhone);
			}
		}

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY;
	}

	public class MonSrvBinder extends Binder {

		public MonSrv getService() {
			return MonSrv.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

}
