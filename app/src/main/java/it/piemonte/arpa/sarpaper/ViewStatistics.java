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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.LinearLayout;

import com.example.arpacell.R;

import it.piemonte.arpa.sarpaper.models.GraphStatistic;

/**
 * Activity per la visualizzazione delle statistiche in formato grafico
 */
public class ViewStatistics extends Activity {

	LinearLayout lg1,lg2,lg3,lg4,lg5,lg6;
	private Date start;
	private String datastart;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_statistics);
		if (Sarpaper.statmonsrv != null) {
			MonSrvReport rpt = Sarpaper.statmonsrv.getMonSrvReport();
			start = new Date(rpt.getGlobalStatistics().getStarttime());
			datastart = start.toLocaleString();
			lg1 = (LinearLayout) findViewById(R.id.lg1);
			lg2 = (LinearLayout) findViewById(R.id.lg2);
			lg3 = (LinearLayout) findViewById(R.id.lg3);
			lg4 = (LinearLayout) findViewById(R.id.lg4);
			lg5 = (LinearLayout) findViewById(R.id.lg5);
			lg6 = (LinearLayout) findViewById(R.id.lg6);
			
			updateChart(rpt, start);
		}
		
		
	}


	private void updateChart(MonSrvReport rpt, Date start) {
		try {
						// creo grafico
			Date dateFrom,dateTo;
			Calendar calendar = new GregorianCalendar(/* remember about timezone! */);
			GraphStatistic graphstat=null;
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
			
			lg1.removeAllViews();
			lg2.removeAllViews();
			lg3.removeAllViews();
			lg4.removeAllViews();
			lg5.removeAllViews();
			lg6.removeAllViews();
					
			//oggi
			calendar.setTime(new Date());
			calendar.add(Calendar.DATE, 0);
			dateFrom = calendar.getTime();
			dateTo=dateFrom;
			graphstat = new GraphStatistic(this, rpt.joinDayStatistics(dateFrom,dateTo),getString(R.string.TitOggi),	getString(R.string.TitUnaGiornata)+ sdf.format(dateFrom));
			lg1.addView(graphstat);
			//ieri
			calendar.setTime(new Date());
			calendar.add(Calendar.DATE, -1);
			dateFrom = calendar.getTime();
			dateTo=dateFrom;
			graphstat = new GraphStatistic(this, rpt.joinDayStatistics(dateFrom,dateTo),getString(R.string.TitIeri),	getString(R.string.TitUnaGiornata)+ sdf.format(dateFrom));
			lg2.addView(graphstat);
			//settimana
			calendar.setTime(new Date());
			calendar.add(Calendar.DATE, -7);
			dateFrom = calendar.getTime();
			dateTo=new Date();
			graphstat = new GraphStatistic(this, rpt.joinDayStatistics(dateFrom,dateTo),getString(R.string.TitQuestaSettimana),	" "+ sdf.format(dateFrom)+" - " + sdf.format(dateTo));
			lg3.addView(graphstat);
			//2 settimane fa 
			calendar.setTime(new Date());
			calendar.add(Calendar.DATE, -8);
			dateTo = calendar.getTime();
			calendar.add(Calendar.DATE, -7);
			dateFrom=calendar.getTime();
			graphstat = new GraphStatistic(this, rpt.joinDayStatistics(dateFrom,dateTo),getString(R.string.TitScorsaSettimana),	""+ sdf.format(dateFrom)+" - " + sdf.format(dateTo));
			lg4.addView(graphstat);
			
			//mese  
			calendar.setTime(new Date());
			calendar.add(Calendar.DATE, 0);
			dateTo = calendar.getTime();
			calendar.add(Calendar.MONTH, -1);
			dateFrom=calendar.getTime();
			graphstat = new GraphStatistic(this, rpt.joinDayStatistics(dateFrom,dateTo),getString(R.string.TitQuestoMese),	""+ sdf.format(dateFrom)+" - " + sdf.format(dateTo));
			lg5.addView(graphstat);
			//mese scorso  
			calendar.setTime(new Date());
			calendar.add(Calendar.MONTH, -1);
			dateTo = calendar.getTime();
			calendar.add(Calendar.MONTH, -1);
			dateFrom=calendar.getTime();
			graphstat = new GraphStatistic(this, rpt.joinDayStatistics(dateFrom,dateTo),getString(R.string.TitScorsoMese),	""+ sdf.format(dateFrom)+" - " + sdf.format(dateTo));
			lg6.addView(graphstat);
			
			

		} catch (Exception e) {

		}

		// //graph

	}


	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_statistics, menu);
		return true;
	}

}
