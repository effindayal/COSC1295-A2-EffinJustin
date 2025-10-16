package au.rmit.ap2.carehome.service;

import au.rmit.ap2.carehome.exceptions.OccupiedBedException;
import au.rmit.ap2.carehome.model.*;

import java.io.*;
import java.time.*;
import java.util.*;

public final class CareHome implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String SAVE_FILE = "carehome.dat";
    private static transient CareHome INSTANCE;

    public static synchronized CareHome get(){ if(INSTANCE==null) INSTANCE=loadOrSeed(); return INSTANCE; }
    // test support
    public static synchronized void resetForTests(){ try{ new File(SAVE_FILE).delete(); }catch(Exception ignored){} INSTANCE=null; }

    // state
    private final Map<String,Ward> wards = new LinkedHashMap<>();
    private final Map<String,Resident> residents = new LinkedHashMap<>();
    private final Map<String,Staff> staff = new LinkedHashMap<>();
    private final Map<String,String> usernameToId = new HashMap<>();

    // services
    private transient AuthService auth = new AuthService();
    private final RosterService roster = new RosterService();
    private final AuditLog audit = new AuditLog();
    private final ArchiveService archive = new ArchiveService("archive.csv");

    private CareHome(){}

    // persistence
    public synchronized void save() throws IOException{ try(ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(SAVE_FILE))){ oos.writeObject(this);} }
    private static CareHome loadOrSeed(){
        File f=new File(SAVE_FILE);
        if(f.exists()){
            try(ObjectInputStream ois=new ObjectInputStream(new FileInputStream(f))){
                Object o=ois.readObject();
                if(o instanceof CareHome ch){ ch.rebind(); return ch; }
            }catch(Exception ignored){}
        }
        CareHome ch=new CareHome(); ch.seed(); return ch;
    }
    private void rebind(){
        if (auth == null) auth = new AuthService();

        usernameToId.clear();
        for (Staff s : staff.values())
            usernameToId.put(s.getUsername(), s.getId());
    }


    // seed layout and data
    private void seed(){
        java.util.function.BiConsumer<Ward,int[]> addRooms=(ward,arr)->{ for(int i=0;i<arr.length;i++) ward.addRoom(new Room(ward.getWardId()+"-R"+(i+1), arr[i])); };
        Ward w1=new Ward("W1"); addRooms.accept(w1,new int[]{1,2,4,3,4,3});
        Ward w2=new Ward("W2"); addRooms.accept(w2,new int[]{1,2,3,4,3,4});
        wards.put("W1",w1); wards.put("W2",w2);

        Manager m=new Manager("Alice Manager","manager","pass");
        Doctor d =new Doctor ("Dr. Chan","doctor","pass");
        Nurse  n1=new Nurse  ("Nurse Kim","nurse1","pass");
        Nurse  n2=new Nurse  ("Nurse Lee","nurse2","pass");
        addStaffInternal(m); addStaffInternal(d); addStaffInternal(n1); addStaffInternal(n2);

        for(DayOfWeek day: DayOfWeek.values()){
            roster.assignShift(n1,day,LocalTime.of(8,0),LocalTime.of(16,0));
            roster.assignShift(n2,day,LocalTime.of(14,0),LocalTime.of(22,0));
            roster.assignShift(d, day,LocalTime.of(10,0),LocalTime.of(11,0));
        }

        Resident john=new Resident("John Lee",Gender.M);
        Resident maya=new Resident("Maya Rao",Gender.F);
        residents.put(john.getId(),john); residents.put(maya.getId(),maya);
        bed("W1-R1-B1").setOccupant(john); bed("W2-R1-B1").setOccupant(maya);
    }

    // helpers
    public Optional<Bed> findBed(String bedId){
        return wards.values().stream().flatMap(w->w.getRooms().stream()).flatMap(r->r.getBeds().stream())
                .filter(b->b.getBedId().equals(bedId)).findFirst();
    }
    private Bed bed(String bedId){ return findBed(bedId).orElseThrow(()->new NoSuchElementException("No bed: "+bedId)); }
    public Optional<String> anyVacantBedId(){
        return wards.values().stream().flatMap(w->w.getRooms().stream()).flatMap(r->r.getBeds().stream())
                .filter(Bed::isVacant).map(Bed::getBedId).findFirst();
    }
    private void addStaffInternal(Staff s){ staff.put(s.getId(),s); usernameToId.put(s.getUsername(), s.getId()); }

    // public getters
    public Map<String,Ward> getWards(){ return wards; }
    public Map<String,Resident> getResidents(){ return residents; }
    public Collection<Staff> getAllStaff(){ return staff.values(); }
    public List<String> recentAudit(){ return audit.last(20); }

    // actions
    public synchronized void assignToBed(Resident r, String bedId){
        Bed b=bed(bedId); if(!b.isVacant()) throw new OccupiedBedException("Bed already occupied: "+bedId);
        b.setOccupant(r); residents.putIfAbsent(r.getId(), r); audit.log("SYSTEM","ASSIGN "+r.getName()+" -> "+bedId);
    }
    public synchronized void moveResident(Staff actor, String residentId, String newBedId, LocalDateTime when){
        new AuthService().requireAny(actor, Role.MANAGER, Role.NURSE); roster.requireOnDuty(actor, when);
        Resident res=residents.get(residentId); if(res==null) throw new NoSuchElementException("Unknown resident");
        Bed target=bed(newBedId); if(!target.isVacant()) throw new OccupiedBedException("Target not vacant: "+newBedId);
        wards.values().forEach(w->w.getRooms().forEach(r->r.getBeds().forEach(b->{ if(res.equals(b.getOccupant())) b.setOccupant(null);})));
        target.setOccupant(res); audit.log(actor.getId(),"MOVE "+res.getName()+" -> "+newBedId);
    }
    public synchronized void addPrescription(Staff doctor, String residentId, Prescription p, LocalDateTime when){
        auth.require(doctor, Role.DOCTOR); roster.requireOnDuty(doctor, when);
        Resident res=residents.get(residentId); if(res==null) throw new NoSuchElementException("Unknown resident");
        res.getPrescriptions().add(p); audit.log(doctor.getId(),"PRESCRIBE "+residentId+" "+p.summary());
    }
    public synchronized void administer(Staff nurse, String residentId, String med, String dose, LocalDateTime when){
        auth.require(nurse, Role.NURSE); roster.requireOnDuty(nurse, when);
        Resident res=residents.get(residentId); if(res==null) throw new NoSuchElementException("Unknown resident");
        res.getAdministrations().add(new AdministrationEvent(residentId, nurse.getId(), med, dose, when, ""));
        audit.log(nurse.getId(),"ADMIN "+residentId+" "+med+" "+dose);
    }
    public synchronized void discharge(Staff manager, String residentId){
        auth.require(manager, Role.MANAGER);
        Resident res=residents.remove(residentId); if(res==null) throw new NoSuchElementException("Unknown resident");
        wards.values().forEach(w->w.getRooms().forEach(r->r.getBeds().forEach(b->{ if(res.equals(b.getOccupant())) b.setOccupant(null);})));
        try{ archive.append(res);}catch(IOException e){ throw new RuntimeException(e); }
        audit.log(manager.getId(),"DISCHARGE "+residentId+" "+res.getName());
    }
    public synchronized void addStaff(Staff manager, Staff newStaff){ auth.require(manager, Role.MANAGER); addStaffInternal(newStaff); audit.log(manager.getId(),"ADD_STAFF "+newStaff.getUsername()); }
    public synchronized void changePassword(Staff manager, String username, String newPass){
        auth.require(manager, Role.MANAGER);
        Staff s = login(username, newPass+"_dummy")==null ? getByUsername(username) : null; // find without validating pw
        if (s==null) s=getByUsername(username);
        s.setPassword(newPass); audit.log(manager.getId(),"CHANGE_PASSWORD "+username);
    }
    private Staff getByUsername(String u){ String id=usernameToId.get(u); if(id==null) throw new NoSuchElementException("No such user: "+u); return staff.get(id); }
    public synchronized void assignShift(Staff manager, String username, DayOfWeek day, LocalTime start, LocalTime end){
        auth.require(manager, Role.MANAGER); roster.assignShift(getByUsername(username), day, start, end);
        audit.log(manager.getId(),"ASSIGN_SHIFT "+username+" "+day+" "+start+"-"+end);
    }
    public void checkCompliance(){ roster.assertCompliantOrThrow(staff.values()); }
    public Staff login(String username, String password){ String id=usernameToId.get(username); if(id==null) return null; Staff s=staff.get(id); return s!=null && s.checkPassword(password)? s : null; }
}
