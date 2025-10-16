package au.rmit.ap2.carehome;

import au.rmit.ap2.carehome.exceptions.*;
import au.rmit.ap2.carehome.model.*;
import au.rmit.ap2.carehome.service.CareHome;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class CareHomeNegativeTests {
    @BeforeEach void reset(){ CareHome.resetForTests(); }

    @Test
    void assign_to_occupied_throws(){
        CareHome ch=CareHome.get();
        Resident another=new Resident("Another", Gender.M);
        assertThrows(OccupiedBedException.class, ()-> ch.assignToBed(another, "W1-R1-B1"));
    }

    @Test
    void nurse_cannot_add_prescription(){
        CareHome ch=CareHome.get();
        Staff nurse=ch.getAllStaff().stream().filter(s->s.getRole()==Role.NURSE).findFirst().orElseThrow();
        String residentId=ch.getResidents().values().stream().findFirst().orElseThrow().getId();
        assertThrows(AuthorizationException.class, ()->
            ch.addPrescription(nurse, residentId, new Prescription(residentId, nurse.getId()), LocalDateTime.now()));
    }
}
