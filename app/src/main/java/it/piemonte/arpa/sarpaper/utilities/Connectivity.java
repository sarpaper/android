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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * Check device's network connectivity and speed
 * 
 * @author emil http://stackoverflow.com/users/220710/emil
 * 
 */
public class Connectivity {

	/**
	 * Get the network info
	 * 
	 * @param context
	 * @return
	 */
	public static NetworkInfo getNetworkInfo(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo();
	}

	/**
	 * Check if there is any connectivity
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isConnected(Context context) {
		NetworkInfo info = Connectivity.getNetworkInfo(context);
		return (info != null && info.isConnected());
	}

    /**
     *
     * @param context
     * @return
     */
	public static boolean isConnectedWifi(Context context) {
		NetworkInfo info = Connectivity.getNetworkInfo(context);
		return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
	}

    /**
     *
     * @param context
     * @return
     */
	public static boolean isConnectedMobile(Context context) {
		NetworkInfo info = Connectivity.getNetworkInfo(context);
		return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
	}

	/**
	 * Check if there is fast connectivity
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isConnectedFast(Context context) {
		NetworkInfo info = Connectivity.getNetworkInfo(context);
		return (info != null && info.isConnected() && Connectivity
				.isConnectionFast(info.getType(), info.getSubtype()));
	}

	/**
	 * Check if the connection is fast
	 * 
	 * @param type
	 * @param subType
	 * @return
	 */
	public static boolean isConnectionFast(int type, int subType) {
		if (type == ConnectivityManager.TYPE_WIFI) {
			return true;
		} else if (type == ConnectivityManager.TYPE_MOBILE) {
			switch (subType) {
			case TelephonyManager.NETWORK_TYPE_1xRTT:
				return false; // ~ 50-100 kbps
			case TelephonyManager.NETWORK_TYPE_CDMA:
				return false; // ~ 14-64 kbps
			case TelephonyManager.NETWORK_TYPE_EDGE:
				return false; // ~ 50-100 kbps
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
				return true; // ~ 400-1000 kbps
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
				return true; // ~ 600-1400 kbps
			case TelephonyManager.NETWORK_TYPE_GPRS:
				return false; // ~ 100 kbps
			case TelephonyManager.NETWORK_TYPE_HSDPA:
				return true; // ~ 2-14 Mbps
			case TelephonyManager.NETWORK_TYPE_HSPA:
				return true; // ~ 700-1700 kbps
			case TelephonyManager.NETWORK_TYPE_HSUPA:
				return true; // ~ 1-23 Mbps
			case TelephonyManager.NETWORK_TYPE_UMTS:
				return true; // ~ 400-7000 kbps
			case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
				return false; // ~25 kbps
				/*
				 * Above API level 7, make sure to set android:targetSdkVersion
				 * to appropriate level to use these
				 * 
				 * case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
				 * return true; // ~ 1-2 Mbps case
				 * TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9 return
				 * true; // ~ 5 Mbps case TelephonyManager.NETWORK_TYPE_HSPAP:
				 * // API level 13 return true; // ~ 10-20 Mbps case
				 * TelephonyManager.NETWORK_TYPE_LTE: // API level 11 return
				 * true; // ~ 10+ Mbps // Unknown
				 */
			case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			default:
				return false;
			}
		} else {
			return false;
		}
	}

    /**
     *
     * @param context
     * @return
     */
	public static String whatConnection(Context context) {
		NetworkInfo info = Connectivity.getNetworkInfo(context);
		String tipo = "";
		try {
			tipo = info.getSubtypeName();
		} catch (Exception e) {

		}
		return tipo;
	}

}