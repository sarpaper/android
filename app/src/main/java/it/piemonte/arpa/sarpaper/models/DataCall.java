package it.piemonte.arpa.sarpaper.models;

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
 */public class DataCall {

	private int type, signal;
	private int seconds;
	private String note;
	private DeviceType device;
	
	public DataCall(int type, int signal, DeviceType device, String note) {
		super();
		this.type = type;
		this.signal = signal;
		this.device = device;
		this.note = note;
	}

	public int getType() {
		return type;
	}

	public int getSignal() {
		return signal;
	}

	public DeviceType getDevice() {
		return device;
	}

	public String getDeviceDescr(){
		return device.name();
	}
	
	public int getSeconds() {
		return seconds;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Override
	public String toString() {
		return "DataCall [type=" + type + ", signal=" + signal + ", device="
				+ device + ", seconds=" + seconds + ", note=" + note + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + device.ordinal();
		result = prime * result + signal;
		result = prime * result + type;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataCall other = (DataCall) obj;
		if (device != other.device)
			return false;
		if (signal != other.signal)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	public String[] getGUICurrent() {

		return new String[] { this.getNote(), String.valueOf(this.getSignal()),
				this.device.name() };
	}

	public String getGUIDescription() {
		return "\n- " + this.getSeconds() + " sec. in " +  this.getNote()  + " "
				+ this.device.name() + " (" + this.getSignal() +")";
	}
}
