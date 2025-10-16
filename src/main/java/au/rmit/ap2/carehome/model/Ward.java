package au.rmit.ap2.carehome.model;

import java.io.Serializable;
import java.util.*;

public class Ward implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String wardId;
    private final List<Room> rooms = new ArrayList<>();
    public Ward(String wardId){ this.wardId=wardId; }
    public String getWardId(){ return wardId; }
    public List<Room> getRooms(){ return rooms; }
    public void addRoom(Room r){ rooms.add(r); }
}
