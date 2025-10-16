package au.rmit.ap2.carehome.service;

import au.rmit.ap2.carehome.model.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ArchiveService implements Serializable {
    private static final long serialVersionUID = 1L;
    private final File csv;
    public ArchiveService(String fileName){
        csv=new File(fileName);
        if(!csv.exists()){
            try(PrintWriter pw=new PrintWriter(new OutputStreamWriter(new FileOutputStream(csv,true), StandardCharsets.UTF_8))){
                pw.println("residentId,name,gender,prescriptions,administrations");
            }catch(IOException ignored){}
        }
    }
    public void append(Resident r) throws IOException{
        String pres = String.valueOf(r.getPrescriptions().stream().map(Prescription::summary).toList());
        String admins = String.valueOf(r.getAdministrations().stream().map(AdministrationEvent::medName).toList());
        try(PrintWriter pw=new PrintWriter(new OutputStreamWriter(new FileOutputStream(csv,true), StandardCharsets.UTF_8))){
            pw.printf("%s,%s,%s,%s,%s%n", r.getId(), quote(r.getName()), r.getGender(), quote(pres), quote(admins));
        }
    }
    private String quote(String s){ return "\""+s.replace("\"","'")+"\""; }
}
