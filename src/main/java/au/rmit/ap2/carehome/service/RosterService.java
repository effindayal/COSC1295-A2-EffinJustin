package au.rmit.ap2.carehome.service;

import au.rmit.ap2.carehome.exceptions.NotRosteredException;
import au.rmit.ap2.carehome.model.*;

import java.io.Serializable;
import java.time.*;
import java.util.*;

public class RosterService implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<String, List<Shift>> roster = new HashMap<>();

    public void assignShift(Staff s, DayOfWeek day, LocalTime start, LocalTime end){
        roster.computeIfAbsent(s.getId(), k->new ArrayList<>()).add(new Shift(s.getId(), day, start, end));
    }
    public boolean isOnDuty(String staffId, LocalDateTime t){
        return roster.getOrDefault(staffId, List.of()).stream().anyMatch(sh->sh.covers(t));
    }
    public void requireOnDuty(Staff s, LocalDateTime t){
        if (s==null || !isOnDuty(s.getId(), t)) throw new NotRosteredException("Not on duty at "+t);
    }

    /** Check: two nurse coverages (8–16 and 14–22) & at least 1 doctor hour per day; no nurse > 8h/day. */
    public void assertCompliantOrThrow(Collection<Staff> staffList) {
        for (DayOfWeek d : DayOfWeek.values()) {
            // Use a sample date that is ON the same day-of-week 'd'
            LocalDate sample = LocalDate.now().with(java.time.temporal.TemporalAdjusters.nextOrSame(d));
            LocalDateTime nineAM  = sample.atTime(9, 0);
            LocalDateTime threePM = sample.atTime(15, 0);

            int nurseMorning = 0;
            int nurseEvening = 0;
            int doctorHours  = 0;

            for (Staff s : staffList) {
                for (Shift sh : roster.getOrDefault(s.getId(), List.of())) {
                    if (sh.getDay() != d) continue;

                    if (s.getRole() == Role.NURSE) {
                        if (sh.hours() > 8)
                            throw new IllegalStateException("Nurse exceeds 8h on " + d);

                        // Count coverage windows on THIS day
                        if (sh.covers(nineAM))  nurseMorning++;
                        if (sh.covers(threePM)) nurseEvening++;
                    } else if (s.getRole() == Role.DOCTOR) {
                        doctorHours += sh.hours();
                    }
                }
            }

            if (nurseMorning < 1 || nurseEvening < 1)
                throw new IllegalStateException("Nurse coverage missing on " + d);
            if (doctorHours < 1)
                throw new IllegalStateException("Doctor coverage < 1h on " + d);
        }
    }

}
