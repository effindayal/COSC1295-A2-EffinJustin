package au.rmit.ap2.carehome.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

public class Prescription implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String residentId, doctorId;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private final List<MedicationOrder> orders = new ArrayList<>();
    public Prescription(String residentId, String doctorId){ this.residentId=residentId; this.doctorId=doctorId; }
    public List<MedicationOrder> getOrders(){ return orders; }
    public String summary(){ return "orders="+orders.size(); }
    public String residentId(){ return residentId; }
}
