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
 */package it.piemonte.arpa.sarpaper.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import it.piemonte.arpa.sarpaper.services.MonSrv;

public class MonSrvStartReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context cnt, Intent intent) {
		Intent s=new Intent(cnt,MonSrv.class);
		cnt.startService(s);
		
	}

}
