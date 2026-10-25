package dev.createport.equipment;

import java.util.Locale;

public record KineticReadout(double rpm,double theoreticalRpm,double capacity,double stress,int members,String status) {
    public KineticReadout {
        if(!Double.isFinite(rpm) || !Double.isFinite(theoreticalRpm) || !Double.isFinite(capacity)
                || !Double.isFinite(stress) || capacity<0 || stress<0 || members<0 || status==null)
            throw new IllegalArgumentException("Invalid kinetic readout");
    }
    public double remaining() { return Math.max(0,capacity-stress); }
    public String load() { return capacity==0 ? "n/a":String.format(Locale.ROOT,"%.1f%%",stress/capacity*100); }
    public static String number(double value) { return value==Math.rint(value) ? String.format(Locale.ROOT,"%,.0f",value):String.format(Locale.ROOT,"%,.1f",value); }
}
