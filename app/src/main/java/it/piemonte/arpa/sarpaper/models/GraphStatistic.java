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

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.arpacell.R;
import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewDataInterface;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;

import it.piemonte.arpa.sarpaper.utilities.Utility;

public class GraphStatistic extends LinearLayout {
	private static TextView labtotL, labtotM, labtotH, labtotdev, labtotnodev,
			titgraph, titgraphperiod, titgraph1, titgraph2;
	

	public GraphStatistic(Context context, Statistics stat,String tit,String titperiod) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.graphs, this, true);

		titgraph = (TextView) findViewById(R.id.titgraph);
		titgraph1 = (TextView) findViewById(R.id.titgraph1);
		titgraph2 = (TextView) findViewById(R.id.titgraph2);
		titgraphperiod = (TextView) findViewById(R.id.titgraphperiod);
		labtotdev = (TextView) findViewById(R.id.labtotdev);
		labtotnodev = (TextView) findViewById(R.id.labtotnodev);
		labtotL = (TextView) findViewById(R.id.labtotL);
		labtotM = (TextView) findViewById(R.id.labtotM);
		labtotH = (TextView) findViewById(R.id.labtotH);

		titgraph.setText(tit);
		titgraphperiod.setText(titperiod);
		titgraph1.setText("Ausilio di Auricolare/Vivavoce");
		long tdev = stat.getDeviceTime();
		long tnodev = stat.getNoDeviceTime();
		// testi
		labtotdev.setText(Utility.getHMSTime(stat
                .getDeviceTime()));
		labtotnodev.setText(Utility.getHMSTime(stat
				.getNoDeviceTime()));
		// preparazione serie grafico set colori alle barre
		GraphViewSeriesStyle seriesStyle = new GraphViewSeriesStyle();
		seriesStyle.setValueDependentColor(new ValueDependentColor() {
			public int get(GraphViewDataInterface data) {
				int color = 0;
				if (data.getX() == 1)
					color = Color.rgb(0, 153, 0);
				if (data.getX() == 2)
					color = Color.rgb(153, 0, 0);
				return color;
			}

		});
		// Dati in percentuale
		float f1 = (float) tdev / (tnodev + tdev) * 100;
		float f2 = (float) tnodev / (tnodev + tdev) * 100;
		long v1 = Math.round(f1);
		long v2 = Math.round(f2);
		GraphViewSeries s1 = new GraphViewSeries("con-senza", seriesStyle,
				new GraphViewData[] { new GraphViewData(1, v1),
						new GraphViewData(2, v2)

				});
		// creazione obj grafico barre
		GraphView graphView;
		graphView = new BarGraphView(context, "Dati in %");
		((BarGraphView) graphView).setDrawValuesOnTop(true);

		graphView.addSeries(s1); // data
		graphView.setHorizontalLabels(new String[] { "CON", "SENZA" });
		graphView
				.setVerticalLabels(new String[] { "100", "75", "50", "25", "0" });
		graphView.setManualYAxisBounds(100d, 0d);
		graphView.setBackgroundColor(Color.rgb(0, 0, 64)); // blu scuro
		graphView.getGraphViewStyle().setGridColor(Color.YELLOW);
		graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.YELLOW);
		graphView.getGraphViewStyle().setVerticalLabelsColor(Color.YELLOW);
		graphView.getGraphViewStyle().setTextSize(18);
		graphView.getGraphViewStyle().setGridColor(Color.rgb(0, 0, 64));
		LinearLayout layout = (LinearLayout) findViewById(R.id.graph1);
		layout.removeAllViews();
		layout.addView(graphView);

		// graph2

		titgraph2.setText("Esposizione SENZA ausili");

		long tL = stat.getLowExp();
		long tM = stat.getMediumExp();
		long tH = stat.getHighExp();

		labtotL.setText(Utility.getHMSTime(stat.getLowExp()));
		labtotM.setText(Utility.getHMSTime(stat.getMediumExp()));
		labtotH.setText(Utility.getHMSTime(stat.getHighExp()));
		
		// preparazione serie grafico set colori alle barre
		seriesStyle = new GraphViewSeriesStyle();
		seriesStyle.setValueDependentColor(new ValueDependentColor() {
			public int get(GraphViewDataInterface data) {
				int color = 0;
				if (data.getX() == 1)
					color = Color.rgb(255, 132, 0);
				if (data.getX() == 2)
					color = Color.rgb(170, 0, 0);
				if (data.getX() == 3)
					color = Color.rgb(146, 0, 166);
				return color;
			}

		});
		// Dati in percentuale
		float fL = (float) tL / (tM + tH + tL) * 100;
		float fM = (float) tM / (tM + tH + tL) * 100;
		float fH = (float) tH / (tM + tH + tL) * 100;
		long vL = Math.round(fL);
		long vM = Math.round(fM);
		long vH = Math.round(fH);
		GraphViewSeries s2 = new GraphViewSeries("exp", seriesStyle,
				new GraphViewData[] { new GraphViewData(1, vL),
						new GraphViewData(2, vM), new GraphViewData(3, vH)

				});
		// creazione obj grafico barre
		GraphView graphViewExp;
		graphViewExp = new BarGraphView(context, "Dati in %");
		((BarGraphView) graphViewExp).setDrawValuesOnTop(true);

		graphViewExp.addSeries(s2); // data
		graphViewExp.setHorizontalLabels(new String[] { "BASSA", "MEDIA",
				"ALTA" });
		graphView
				.setVerticalLabels(new String[] { "100", "75", "50", "25", "0" });
		graphViewExp.setManualYAxisBounds(100d, 0d);
		graphViewExp.setBackgroundColor(Color.rgb(0, 0, 64)); // blu scuro
		graphViewExp.getGraphViewStyle().setGridColor(Color.YELLOW);
		graphViewExp.getGraphViewStyle().setHorizontalLabelsColor(Color.YELLOW);
		graphViewExp.getGraphViewStyle().setVerticalLabelsColor(Color.YELLOW);
		graphViewExp.getGraphViewStyle().setTextSize(18);
		graphViewExp.getGraphViewStyle().setGridColor(Color.rgb(0, 0, 64));
		LinearLayout layoutExp = (LinearLayout) findViewById(R.id.graph2);
		layoutExp.removeAllViews();
		layoutExp.addView(graphViewExp);

	}

}
