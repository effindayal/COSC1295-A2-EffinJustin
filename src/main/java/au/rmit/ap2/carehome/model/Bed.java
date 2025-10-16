package au.rmit.ap2.carehome.model;

import java.io.Serializable;

public class Bed implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String bedId;
    private Resident occupant;
    public Bed(String bedId){ this.bedId=bedId; }
    public String getBedId(){ return bedId; }
    public Resident getOccupant(){ return occupant; }
    public boolean isVacant(){ return occupant==null; }
    public void setOccupant(Resident r){ this.occupant=r; }
}
