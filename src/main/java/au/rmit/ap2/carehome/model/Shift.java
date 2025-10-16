package au.rmit.ap2.carehome.model;

import java.io.Serializable;
import java.time.*;

public class Shift implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String staffId; private final DayOfWeek day; private final LocalTime start,end;
    public Shift(String staffId, DayOfWeek day, LocalTime start, LocalTime end){
        this.staffId=staffId; this.day=day; this.start=start; this.end=end;
    }
    public boolean covers(LocalDateTime t){ return t.getDayOfWeek()==day && !t.toLocalTime().isBefore(start) && !t.toLocalTime().isAfter(end); }
    public DayOfWeek getDay(){ return day; }
    public int hours(){ return end.getHour()-start.getHour(); }
}
