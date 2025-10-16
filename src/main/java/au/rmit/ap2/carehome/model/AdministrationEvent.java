package au.rmit.ap2.carehome.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AdministrationEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String residentId, nurseId, medName, dose;
    private final LocalDateTime time; private final String notes;
    public AdministrationEvent(String residentId,String nurseId,String medName,String dose,LocalDateTime time,String notes){
        this.residentId=residentId; this.nurseId=nurseId; this.medName=medName; this.dose=dose; this.time=time; this.notes=notes;
    }
    public String medName(){ return medName; }
}
