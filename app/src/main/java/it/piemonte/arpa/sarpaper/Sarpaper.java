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

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.arpacell.R;

import it.piemonte.arpa.sarpaper.models.DataCall;
import it.piemonte.arpa.sarpaper.models.EntryCall;
import it.piemonte.arpa.sarpaper.models.GraphStatistic;
import it.piemonte.arpa.sarpaper.services.MonSrv;

public class Sarpaper extends Activity {

	boolean bvvoice;
	private static TextView textOut, tvtitle, tvftp, tvmodel, tvstatus,
			tvcurrent, tvlastcall, tvStats, numtotcalls, timetotcalls;
	private static TextView tvnetwork, tvdevice, tvsignal, timetotsrv;
	public static boolean bactiveUI = true;
	LinearLayout llgrah,llgrahlastcall;
	TelephonyManager telephonyManager;
	PhoneStateListener listenerCall, listenerSignal;
	AudioManager audioManager;
	Context context = null;
	AlertDialog.Builder builder;
	private MonSrv monSrv;
	public static MonSrv statmonsrv;
	private Timer timer;
	private long tdev = 0;
	private long tnodev = 0;
	Date start;
	String datastart;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		context = this;
		// Get the UI
		textOut = (TextView) findViewById(R.id.textOut);
		tvtitle = (TextView) findViewById(R.id.tvtitle);
		tvnetwork = (TextView) findViewById(R.id.tvnetwork);
		tvsignal = (TextView) findViewById(R.id.tvsignal);
		tvdevice = (TextView) findViewById(R.id.tvdevice);
		tvlastcall = (TextView) findViewById(R.id.tvlastcall);
		numtotcalls = (TextView) findViewById(R.id.numtotcall);
		timetotcalls = (TextView) findViewById(R.id.timetot);
		timetotsrv = (TextView) findViewById(R.id.timetotsrv);
		tvStats = (TextView) findViewById(R.id.titgraph);
		llgrah = (LinearLayout) findViewById(R.id.lineargraph);
		llgrahlastcall = (LinearLayout) findViewById(R.id.lineargraphlastcall);
		startService(new Intent(Sarpaper.this, MonSrv.class));

	}

	@Override
	protected void onResume() {
		super.onResume();
		bactiveUI = true;
		bindService(new Intent(this, MonSrv.class), mConnection,
				Context.BIND_AUTO_CREATE);
		//ogni 4 secondi aggiorna i dati dell'interfaccia
		timer = new Timer();
		timer.scheduleAtFixedRate(new FetchData(), 1000, 4000);

	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			monSrv = ((MonSrv.MonSrvBinder) binder).getService();
			statmonsrv=monSrv;
			Toast.makeText(Sarpaper.this, "Connected", Toast.LENGTH_SHORT)
					.show();
		}

		public void onServiceDisconnected(ComponentName className) {
			monSrv = null;
		}
	};

	/**
	 * Richiede al servizio le statistiche ed aggiorna l'interfaccia
	 */
	class FetchData extends TimerTask {
		@Override
		public void run() {
			runOnUiThread(new Runnable() {
				public void run() {
					if (monSrv != null) {
						MonSrvReport rpt = monSrv.getMonSrvReport();
						start = new Date(rpt.getGlobalStatistics().getStarttime());
						datastart = start.toLocaleString();
						updateGUI(rpt);
						updateChartTot(rpt, start);
					}
				}
			});
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		bactiveUI = false;
		timer.cancel();
		timer = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.telephonydemo, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.esci:
			esci();
			return true;
		case R.id.credits:
			showCredits();
			return true;
			
			
			
		case R.id.vstat:
			Intent vst = new Intent(Sarpaper.this, ViewStatistics.class);
			startActivity(vst);

			
			return true;
			
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void showCredits() {
		LayoutInflater inflater = this.getLayoutInflater();
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_info).setTitle("Credits")
				.setView(inflater.inflate(R.layout.credits, null))
				.setPositiveButton("OK...", null).show();
	}

	private void esci() {
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Chiusura SARPAPER")
				.setMessage(
						"Sei sicuro di voler chiudere l'applicazione?\nSi chiudera' anche il servizio di monitoring SARPAPER.")
				.setPositiveButton("Si", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						// Ferma l'activity e il service
						stopService(new Intent(Sarpaper.this, MonSrv.class));

						finish();

					}

				}).setNegativeButton("Non esco", null).show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Programma il bottone esci del telefono per uscire de l'applicazione
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// Chiede a l'utente se vuole uscire
			moveTaskToBack(true);
			return true;

		} else {
			return super.onKeyDown(keyCode, event);
		}

	}


	/**
	 * Presenta a video le statistiche in forma grafica
	 * @param rpt
	 * @param start
     */
	private void updateChartTot(MonSrvReport rpt, Date start) {
		try {
			// verifica dati con o senza device uguali , ne segue no refresh grafici e return
			if (tdev == rpt.getGlobalStatistics().getDeviceTime()
					&& tnodev == rpt.getGlobalStatistics().getNoDeviceTime())
				return;
			else {
				tdev = rpt.getGlobalStatistics().getDeviceTime();
				tnodev = rpt.getGlobalStatistics().getNoDeviceTime();

			}
			// creo grafico
			GraphStatistic graphstat = new GraphStatistic(this, rpt.getGlobalStatistics(),"STATISTICHE TOTALI", "Dall'attivazione a oggi");
			llgrah.removeAllViews();
			llgrah.addView(graphstat);
			GraphStatistic graphstatlastcall = new GraphStatistic(this, rpt.getLastCallStatistics(),"STATISTICHE", "Ultima chiamata effettuata");
			llgrahlastcall.removeAllViews();
			llgrahlastcall.addView(graphstatlastcall);

		} catch (Exception e) {

		}

		// //graph

	}

	/**
	 * Presenta a video le statistiche
	 * @param rpt
     */
	private void updateGUI(MonSrvReport rpt) {
		try {
			if (rpt != null && bactiveUI) {
				tvtitle.setText(datastart + "\nid: " + rpt.getGlobalStatistics().getUid());// rpt.getUptime());
				textOut.setText(rpt.getLog());
				// tvstatus.setText(rpt.getPhoneStatus());
				tvnetwork.setText(rpt.getNetworkType());
				tvsignal.setText(rpt.getSignal());
				tvdevice.setText(rpt.getDevice().name());
				int val = rpt.getGlobalStatistics().getNumCalls();
				String numtot = String.valueOf(val);
				numtotcalls.setText(numtot);
				timetotcalls.setText(rpt.getGlobalStatistics().getCallTimeDescr());

				timetotsrv.setText(rpt.getUptime());
				if (rpt.getLastCall() != null) {
					updateLastCall(rpt);
				} else {
					tvlastcall.setText("");
				}
			}
		} catch (Exception e) {
			Log.d("updateGUI", "UpdateGUI fail. " + e.getMessage());
		}

	}

	private static void updateLastCall(MonSrvReport rpt) {
		EntryCall entry = rpt.getLastCall();
		StringBuilder result = new StringBuilder("Data: "
				+ entry.getStartCall().toLocaleString() + "\n"
				+ entry.getCallDurationDescr());
		for (DataCall item : entry.getDataCall()) {

			result.append(item.getGUIDescription());
		}
		tvlastcall.setText("Report Ultima Chiamata\n\n" +result);

	}

}
