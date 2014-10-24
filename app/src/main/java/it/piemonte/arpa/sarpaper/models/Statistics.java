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

import java.io.Serializable;

public class Statistics implements Serializable {

	private static final long serialVersionUID = 1L;
	protected int numCalls; // numCalls
	protected long totalTime; // totalTime
	protected long deviceTime; // deviceTime
	protected long noDeviceTime; // noDeviceTime
	protected long lowExp, mediumExp, highExp; //

	public Statistics() {
	}

	public int getNumCalls() {
		return numCalls;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public long getDeviceTime() {
		return deviceTime;
	}

	public long getNoDeviceTime() {
		return noDeviceTime;
	}

	public long getLowExp() {
		return lowExp;
	}

	public long getMediumExp() {
		return mediumExp;
	}

	public long getHighExp() {
		return highExp;
	}

	public void update(EntryCall entry) {
		this.numCalls++;
		this.totalTime += entry.getCallDuration();
		this.deviceTime += entry.getCallDuration(DeviceType.VIVA_VOCE);
		this.deviceTime += entry.getCallDuration(DeviceType.CUFFIE);
		this.noDeviceTime += entry.getCallDuration(DeviceType.NO_DEVICE);
		this.lowExp += entry.getCallDuration(DeviceType.NO_DEVICE,
				SignalRange.LOW);
		this.mediumExp += entry.getCallDuration(DeviceType.NO_DEVICE,
				SignalRange.MEDIUM);
		this.highExp += entry.getCallDuration(DeviceType.NO_DEVICE,
				SignalRange.HIGH);

	}

	public void plus(Statistics stat) {
		this.numCalls += stat.numCalls;
		this.totalTime += stat.getTotalTime();
		this.deviceTime += stat.getDeviceTime();
		this.noDeviceTime += stat.getNoDeviceTime();
		this.lowExp += stat.lowExp;
		this.mediumExp += stat.mediumExp;
		this.highExp += stat.highExp;
	}

	@Override
	public String toString() {
		return "Statistics [numCalls=" + numCalls + ", totalTime=" + totalTime
				+ ", deviceTime=" + deviceTime + ", noDeviceTime="
				+ noDeviceTime + ", lowExp=" + lowExp + ", mediumExp="
				+ mediumExp + ", highExp=" + highExp + "]";
	}

}