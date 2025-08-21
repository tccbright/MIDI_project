package com.tccb.virtualband.entity;

import lombok.Data;


public class InstrumentStatus {
    private String instrument; // Piano, Bass, Violin, Strings, Drums
    private String status;     // online, offline, ready

    public String getInstrument() { return instrument; }
    public void setInstrument(String instrument) { this.instrument = instrument; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
