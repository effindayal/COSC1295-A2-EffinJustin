package au.rmit.ap2.carehome.model;

import java.io.Serializable;
import java.util.*;

public class Resident implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id = UUID.randomUUID().toString();
    private String name; private Gender gender;
    private final List<Prescription> prescriptions = new ArrayList<>();
    private final List<AdministrationEvent> administrations = new ArrayList<>();
    public Resident(String name, Gender gender){ this.name=name; this.gender=gender; }
    public String getId(){ return id; } public String getName(){ return name; } public Gender getGender(){ return gender; }
    public void setName(String n){ this.name=n; } public void setGender(Gender g){ this.gender=g; }
    public List<Prescription> getPrescriptions(){ return prescriptions; }
    public List<AdministrationEvent> getAdministrations(){ return administrations; }
}
