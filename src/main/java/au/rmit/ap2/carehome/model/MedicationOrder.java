package au.rmit.ap2.carehome.model;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.*;

public class MedicationOrder implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String medName, dose;
    private final List<LocalTime> times = new ArrayList<>();
    public MedicationOrder(String medName, String dose){ this.medName=medName; this.dose=dose; }
    public String getMedName(){ return medName; } public String getDose(){ return dose; }
    public List<LocalTime> getTimes(){ return times; }
}
