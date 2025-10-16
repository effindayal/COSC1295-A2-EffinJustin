package au.rmit.ap2.carehome;

import au.rmit.ap2.carehome.exceptions.NotRosteredException;
import au.rmit.ap2.carehome.model.*;
import au.rmit.ap2.carehome.service.CareHome;
import org.junit.jupiter.api.*;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

public class CareHomeRosterNegativeTest {
    @BeforeEach void reset(){ CareHome.resetForTests(); }

    @Test
    void nurse_outside_shift_cannot_administer(){
        CareHome ch=CareHome.get();
        Staff nurse=ch.getAllStaff().stream().filter(s->s.getRole()==Role.NURSE).findFirst().orElseThrow();
        String residentId=ch.getResidents().values().stream().findFirst().orElseThrow().getId();
        LocalDateTime threeAM=LocalDateTime.now().withHour(3).withMinute(0).withSecond(0).withNano(0);
        assertThrows(NotRosteredException.class, ()-> ch.administer(nurse, residentId, "Panadol", "500mg", threeAM));
    }

    @Test
    void compliance_fails_when_nurse_shift_exceeds_8_hours(){
        CareHome ch=CareHome.get();
        Staff manager=ch.login("manager","pass"); assertNotNull(manager);
        Staff nurse=ch.getAllStaff().stream().filter(s->s.getRole()==Role.NURSE).findFirst().orElseThrow();
        ch.assignShift(manager, nurse.getUsername(), DayOfWeek.MONDAY, LocalTime.of(6,0), LocalTime.of(16,0));
        assertThrows(IllegalStateException.class, ch::checkCompliance);
    }
}
