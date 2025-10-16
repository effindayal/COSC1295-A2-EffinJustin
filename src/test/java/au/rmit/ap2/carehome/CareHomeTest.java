package au.rmit.ap2.carehome;

import au.rmit.ap2.carehome.model.*;
import au.rmit.ap2.carehome.service.CareHome;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class CareHomeTest {
    @BeforeEach void reset(){ CareHome.resetForTests(); }

    @Test
    void assignToVacantBed_succeeds(){
        CareHome ch=CareHome.get();
        String bedId=ch.anyVacantBedId().orElseThrow();
        Resident r=new Resident("Test User", Gender.M);
        ch.assignToBed(r, bedId);
        assertNotNull(ch.findBed(bedId).orElseThrow().getOccupant());
    }
}
