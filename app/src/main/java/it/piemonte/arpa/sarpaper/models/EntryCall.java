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
package it.piemonte.arpa.sarpaper.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Rappresenta una intera chiamata vocale, tiene traccia dei vari cambiamenti di stato tramite la lista dataCalls
 */
public class EntryCall {

    private Date startCall;
    private int curType, curSignal;
    private DeviceType curDevice;
    private Date curTime;
    private List<DataCall> dataCalls;

    public EntryCall(DataCall data) {
        super();
        this.dataCalls = new ArrayList<DataCall>();
        this.startCall = new Date();
        addFirstDataCall(data);
    }

    public int getCurType() {
        return curType;
    }

    public void setCurType(int curType) {
        this.curType = curType;
    }

    public int getCurSignal() {
        return curSignal;
    }

    public void setCurSignal(int curSignal) {
        this.curSignal = curSignal;
    }

    public DeviceType getCurDevice() {
        return curDevice;
    }

    public void setCurDevice(DeviceType curDevice) {
        this.curDevice = curDevice;
    }

    public List<DataCall> getDataCall() {
        return dataCalls;
    }

    public Date getStartCall() {
        return startCall;
    }

    /**
     * Aggiunge un nuovo stato alla chiamata.
     * Se esiste già, ne aggiorna la durata, altrimenti aggiorna la durata dello stato corrente e
     * data diventa lo stato corrente
     * @param data
     */
    public void addDataCall(DataCall data) {
        if (dataCalls.contains(data)) {
            updateSeconds(dataCalls.indexOf(data));
        } else {
            updateSeconds(dataCalls.size() - 1);
            dataCalls.add(data);
        }
        setCurrentState(data);
    }

    /**
     * aggiorna la durata dello stato corrente
     */
    public void finish() {
        updateSeconds(dataCalls.size() - 1);
    }

    /**
     * Calcola la durata totale in secondi della chiamata
     * @return
     */
    public int getCallDuration() {
        int totDuration = 0;
        for (DataCall item : this.dataCalls) {
            totDuration += item.getSeconds();
        }
        return totDuration;
    }

    /**
     * Calcola la durata in sec. della chiamata in una certa modalità di utilizzo
     * @param type cuffie/vivavoce/nessuno
     * @return
     */
    public int getCallDuration(DeviceType type) {
        int totDuration = 0;
        for (DataCall item : this.dataCalls) {
            if (item.getDevice() == type) {
                totDuration += item.getSeconds();
            }

        }
        return totDuration;
    }

    /**
     * Calcola la durata in sec. della chiamata in una certa modalità di utilizzo in una certa fascia di esposizione
     * @param type cuffie/vivavoce/nessuno
     * @param range hight/medium/low
     * @return
     */
    public int getCallDuration(DeviceType type, SignalRange range) {
        int totDuration = 0;

        for (DataCall item : this.dataCalls) {
            if (item.getDevice() == type) {
                if (item.getSignal() == 99) {
                    return item.getSeconds() / 3;
                }
                switch (range) {
                    case LOW:
                        if (item.getSignal() >= range.getValue()) {
                            totDuration += item.getSeconds();
                        }
                        break;
                    case MEDIUM:
                        if (item.getSignal() <= range.getValue()
                                && item.getSignal() >= SignalRange.HIGH.getValue()) {
                            totDuration += item.getSeconds();
                        }
                        break;
                    case HIGH:
                        if (item.getSignal() < range.getValue()) {
                            totDuration += item.getSeconds();
                        }
                        break;
                }
            }

        }

        return totDuration;
    }

    /**
     * Rappresentazione stringa della durata totale di una chiamata
     * @return
     */
    public String getCallDurationDescr() {

        int totDuration = getCallDuration();

        long diffSeconds = totDuration % 60;
        long diffMinutes = totDuration / 60 % 60;
        long diffHours = totDuration / (60 * 60) % 24;
        String testo;
        testo = (diffHours < 10 ? "0" + String.valueOf(diffHours) : String
                .valueOf(diffHours));
        testo += ":"
                + (diffMinutes < 10 ? "0" + String.valueOf(diffMinutes)
                : String.valueOf(diffMinutes));
        testo += ":"
                + (diffSeconds < 10 ? "0" + String.valueOf(diffSeconds)
                : String.valueOf(diffSeconds));

        return String.format("Durata:  %s", testo);
    }

    @Override
    public String toString() {
        return "EntryCall [startCall=" + startCall + ", dataCalls=" + dataCalls
                + "]";
    }

    private void addFirstDataCall(DataCall data) {
        dataCalls.add(data);
        setCurrentState(data);
    }

    private void setCurrentState(DataCall data) {
        this.curDevice = data.getDevice();
        this.curType = data.getType();
        this.curSignal = data.getSignal();
        curTime = new Date();
    }

    /**
     * Aggiorna la durata in sec. dello stato
     * @param index indice dello stato nella lista
     */
    private void updateSeconds(int index) {
        dataCalls.get(index)
                .setSeconds(
                        dataCalls.get(index).getSeconds()
                                + ((int) new Date().getTime() - (int) curTime
                                .getTime()) / 1000);
    }
}
