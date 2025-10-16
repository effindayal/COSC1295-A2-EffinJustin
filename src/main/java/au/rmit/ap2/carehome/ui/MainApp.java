package au.rmit.ap2.carehome.ui;

import au.rmit.ap2.carehome.model.*;
import au.rmit.ap2.carehome.service.CareHome;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.*;

public class MainApp extends Application {
    private final CareHome careHome = CareHome.get();

    @Override public void start(Stage stage){
        BorderPane root = new BorderPane();

        Label title = new Label("RMIT Care Home — Bed Map");
        title.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");
        HBox top = new HBox(title); top.setPadding(new Insets(10)); root.setTop(top);

        ScrollPane centerPane = new ScrollPane(buildWardGrid()); centerPane.setFitToWidth(true); root.setCenter(centerPane);

        TabPane tabs = buildSideTabs(root); tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); tabs.setPrefWidth(360);
        root.setRight(tabs);

        Scene scene=new Scene(root,1180,720);
        stage.setTitle("Resident HealthCare System"); stage.setScene(scene); stage.show();
    }

    private VBox buildWardGrid(){
        VBox container=new VBox(16); container.setPadding(new Insets(12));
        for(Ward w: careHome.getWards().values()){
            Label wl=new Label("Ward "+w.getWardId()); wl.setStyle("-fx-font-size:15px; -fx-font-weight:bold;");
            GridPane rooms=new GridPane(); rooms.setHgap(12); rooms.setVgap(12);
            int idx=0; for(Room r: w.getRooms()){
                VBox room=new VBox(8); room.setPadding(new Insets(8));
                room.setStyle("-fx-background-color:#fff; -fx-background-radius:8; -fx-border-color:#cfd4d9; -fx-border-radius:8;");
                GridPane bedRow=new GridPane(); bedRow.setHgap(8); int c=0; for(Bed b: r.getBeds()) bedRow.add(makeBedTile(b), c++, 0);
                room.getChildren().add(bedRow);
                rooms.add(room, idx%2, idx/2); idx++;
            }
            VBox wardBox=new VBox(8, wl, rooms);
            wardBox.setStyle("-fx-background-color:#f6f8fa; -fx-padding:10; -fx-background-radius:10;");
            container.getChildren().add(wardBox);
        }
        return container;
    }

    private Button makeBedTile(Bed bed){
        Button btn=new Button(labelFor(bed));
        btn.setMinSize(120,64); btn.setMaxSize(120,64); btn.setWrapText(true); btn.setAlignment(Pos.CENTER);
        btn.setStyle(colorFor(bed));
        btn.setOnAction(e->{
            Resident occ=bed.getOccupant();
            if(occ==null) new Alert(Alert.AlertType.INFORMATION, bed.getBedId()+" is vacant.").showAndWait();
            else new Alert(Alert.AlertType.INFORMATION, bed.getBedId()+" — "+occ.getName()+" ("+occ.getGender()+")\nID: "+occ.getId()).showAndWait();
        });
        return btn;
    }
    private String colorFor(Bed bed){
        if(bed.getOccupant()==null) return "-fx-background-color:#e0e0e0; -fx-background-radius:10; -fx-font-weight:bold;";
        String color = (bed.getOccupant().getGender()==Gender.M) ? "#4da3ff" : "#ff6b6b";
        return "-fx-background-color:"+color+"; -fx-background-radius:10; -fx-font-weight:bold;";
    }
    private String labelFor(Bed bed){
        if(bed.getOccupant()==null) return bed.getBedId()+"\n(Vacant)";
        Resident r=bed.getOccupant(); return bed.getBedId()+"\n"+r.getName()+" ("+r.getGender()+")";
    }

    private TabPane buildSideTabs(BorderPane root){
        // Manager
        VBox mgr=new VBox(8); mgr.setPadding(new Insets(12));
        TextField rName=new TextField(); rName.setPromptText("Resident name");
        ComboBox<Gender> rGen=new ComboBox<>(); rGen.getItems().addAll(Gender.M, Gender.F); rGen.setPromptText("Gender");
        TextField rBed=new TextField(); rBed.setPromptText("Target Bed (W1-R2-B1)");
        Button add=new Button("Add Resident to Bed"); add.setMaxWidth(Double.MAX_VALUE);
        add.setOnAction(e->{ try{
            if(rName.getText().isBlank()||rGen.getValue()==null||rBed.getText().isBlank()){ alert(Alert.AlertType.WARNING,"Fill all fields"); return; }
            Resident r=new Resident(rName.getText().trim(), rGen.getValue());
            careHome.assignToBed(r, rBed.getText().trim()); careHome.save();
            root.setCenter(new ScrollPane(buildWardGrid())); rName.clear(); rGen.setValue(null); rBed.clear();
        }catch(Exception ex){ alert(Alert.AlertType.ERROR, ex.getMessage()); }});

        // Move (Manager/Nurse login)
        TextField mvUser=new TextField("nurse1"); PasswordField mvPw=new PasswordField(); mvPw.setText("pass");
        TextField mvRes=new TextField(); mvRes.setPromptText("Resident ID"); TextField mvNew=new TextField(); mvNew.setPromptText("New Bed");
        Button move=new Button("Move Resident"); move.setMaxWidth(Double.MAX_VALUE);
        move.setOnAction(e->{ try{
            Staff s=careHome.login(mvUser.getText().trim(), mvPw.getText().trim()); if(s==null) throw new IllegalStateException("Login failed");
            careHome.moveResident(s, mvRes.getText().trim(), mvNew.getText().trim(), LocalDateTime.now()); careHome.save();
            root.setCenter(new ScrollPane(buildWardGrid())); alert(Alert.AlertType.INFORMATION,"Moved.");
        }catch(Exception ex){ alert(Alert.AlertType.ERROR, ex.getMessage()); }});

        // Discharge
        TextField disUser=new TextField("manager"); PasswordField disPw=new PasswordField(); disPw.setText("pass");
        TextField disRes=new TextField(); disRes.setPromptText("Resident ID");
        Button discharge=new Button("Discharge + Archive"); discharge.setMaxWidth(Double.MAX_VALUE);
        discharge.setOnAction(e->{ try{
            Staff m=careHome.login(disUser.getText().trim(), disPw.getText().trim()); if(m==null) throw new IllegalStateException("Login failed");
            careHome.discharge(m, disRes.getText().trim()); careHome.save(); root.setCenter(new ScrollPane(buildWardGrid()));
            alert(Alert.AlertType.INFORMATION,"Archived & discharged.");
        }catch(Exception ex){ alert(Alert.AlertType.ERROR, ex.getMessage()); }});

        // Add staff / assign shift / change password
        TextField mUser=new TextField("manager"); PasswordField mPw=new PasswordField(); mPw.setText("pass");
        ComboBox<Role> stRole=new ComboBox<>(); stRole.getItems().addAll(Role.DOCTOR, Role.NURSE); stRole.setPromptText("Role");
        TextField stName=new TextField(); stName.setPromptText("Full name");
        TextField stU=new TextField(); stU.setPromptText("Username"); PasswordField stP=new PasswordField(); stP.setPromptText("Password");
        Button addStaff=new Button("Add Staff (Manager)"); addStaff.setMaxWidth(Double.MAX_VALUE);
        addStaff.setOnAction(e->{ try{
            Staff m=careHome.login(mUser.getText().trim(), mPw.getText().trim()); if(m==null) throw new IllegalStateException("Manager login failed");
            Staff s=(stRole.getValue()==Role.DOCTOR) ? new Doctor(stName.getText().trim(), stU.getText().trim(), stP.getText())
                                                     : new Nurse (stName.getText().trim(), stU.getText().trim(), stP.getText());
            careHome.addStaff(m, s); careHome.save(); alert(Alert.AlertType.INFORMATION,"Staff added.");
        }catch(Exception ex){ alert(Alert.AlertType.ERROR, ex.getMessage()); }});

        TextField shUser=new TextField(); shUser.setPromptText("username");
        ComboBox<DayOfWeek> shDay=new ComboBox<>(); shDay.getItems().addAll(DayOfWeek.values()); shDay.setPromptText("Day");
        TextField shStart=new TextField("08:00"); TextField shEnd=new TextField("16:00");
        Button assignShift=new Button("Assign Shift"); assignShift.setMaxWidth(Double.MAX_VALUE);
        assignShift.setOnAction(e->{ try{
            Staff m=careHome.login(mUser.getText().trim(), mPw.getText().trim()); if(m==null) throw new IllegalStateException("Manager login failed");
            careHome.assignShift(m, shUser.getText().trim(), shDay.getValue(), LocalTime.parse(shStart.getText().trim()), LocalTime.parse(shEnd.getText().trim()));
            careHome.checkCompliance(); careHome.save(); alert(Alert.AlertType.INFORMATION,"Shift assigned.");
        }catch(Exception ex){ alert(Alert.AlertType.ERROR, ex.getMessage()); }});

        ListView<String> audit=new ListView<>(); audit.getItems().addAll(careHome.recentAudit()); audit.setPrefHeight(140);

        mgr.getChildren().addAll(new Label("Manager — Add Resident"), rName,rGen,rBed,add, new Separator(),
                new Label("Move (Manager/Nurse login)"), mvUser,mvPw,mvRes,mvNew,move, new Separator(),
                new Label("Discharge (Manager login)"), disUser,disPw,disRes,discharge, new Separator(),
                new Label("Add Staff / Assign Shift"), mUser,mPw, stRole,stName,stU,stP, addStaff,
                new HBox(8, shUser, shDay), new HBox(8, shStart, shEnd), assignShift, new Separator(),
                new Label("Recent Actions"), audit);

        // Doctor
        VBox doc=new VBox(8); doc.setPadding(new Insets(12));
        TextField dUser=new TextField("doctor"); PasswordField dPw=new PasswordField(); dPw.setText("pass");
        TextField dRes=new TextField(); dRes.setPromptText("Resident ID"); TextField dMed=new TextField(); dMed.setPromptText("Medication");
        TextField dDose=new TextField(); dDose.setPromptText("Dose");
        Button pres=new Button("Add Prescription"); pres.setMaxWidth(Double.MAX_VALUE);
        pres.setOnAction(e->{ try{
            Staff s=careHome.login(dUser.getText().trim(), dPw.getText().trim()); if(s==null) throw new IllegalStateException("Login failed");
            Prescription p=new Prescription(dRes.getText().trim(), s.getId()); p.getOrders().add(new MedicationOrder(dMed.getText().trim(), dDose.getText().trim()));
            careHome.addPrescription(s, dRes.getText().trim(), p, LocalDateTime.now()); careHome.save();
            alert(Alert.AlertType.INFORMATION,"Prescription added.");
        }catch(Exception ex){ alert(Alert.AlertType.ERROR, ex.getMessage()); }});
        doc.getChildren().addAll(new Label("Doctor — Prescribe"), dUser,dPw,dRes,dMed,dDose,pres);

        // Nurse
        VBox nur=new VBox(8); nur.setPadding(new Insets(12));
        TextField nUser=new TextField("nurse1"); PasswordField nPw=new PasswordField(); nPw.setText("pass");
        TextField nRes=new TextField(); nRes.setPromptText("Resident ID"); TextField nMed=new TextField(); nMed.setPromptText("Medication");
        TextField nDose=new TextField(); nDose.setPromptText("Dose");
        Button admin=new Button("Record Administration"); admin.setMaxWidth(Double.MAX_VALUE);
        admin.setOnAction(e->{ try{
            Staff s=careHome.login(nUser.getText().trim(), nPw.getText().trim()); if(s==null) throw new IllegalStateException("Login failed");
            careHome.administer(s, nRes.getText().trim(), nMed.getText().trim(), nDose.getText().trim(), LocalDateTime.now()); careHome.save();
            alert(Alert.AlertType.INFORMATION,"Administration recorded.");
        }catch(Exception ex){ alert(Alert.AlertType.ERROR, ex.getMessage()); }});
        nur.getChildren().addAll(new Label("Nurse — Administer"), nUser,nPw,nRes,nMed,nDose,admin);

        return new TabPane(new Tab("Manager", new ScrollPane(mgr)), new Tab("Doctor", new ScrollPane(doc)), new Tab("Nurse", new ScrollPane(nur)));
    }

    private void alert(Alert.AlertType t, String m){ new Alert(t,m).showAndWait(); }

    public static void main(String[] args){ launch(args); }
}
