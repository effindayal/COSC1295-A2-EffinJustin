# Resident HealthCare System — COSC1295 Assignment 2  
Student: Effin Justin , Course: COSC1295 Advanced Programming  Semester: 2025 S2  



##  How to Run
### In Eclipse (using Maven plugin)
1. Right-click project → Run As → Maven build…
2. In Goals, type `javafx:run` → Run  
   → GUI opens with Bed Map + Manager/Doctor/Nurse tabs.  

(You can also use “Run As → Java Application” with VM args  
`--add-modules javafx.controls,javafx.base` if preferred.)

### Java / Maven versions  
- Java 21, JavaFX 21.0.4, JUnit 5.10.2



## 👤 Seeded Login Accounts

| Role    | Username  | Password| Default Shift      |

| Manager | `manager` | `pass` | N/A                 |
| Doctor  | `doctor`  | `pass` | 10:00 – 11:00 daily |
| Nurse 1 | `nurse1`  | `pass` | 08:00 – 16:00 daily |
| Nurse 2 | `nurse2`  | `pass` | 14:00 – 22:00 daily |

*(Manager → Assign Shift can extend hours for testing.)*

---

## 🏗️ Implemented Requirements Mapping

| Requirement | Implementation |
|--------------|----------------|
| **2 Wards × 6 Rooms (1–4 beds)** | `CareHome.seed()` builds wards W1/W2 with 6 rooms each. |
| **Colour map (M/F/Vacant)** | JavaFX tiles — blue (M), red (F), grey (vacant). |
| **Add Residents** | Manager tab → Add Resident form → adds to vacant bed. |
| **Move Resident** | Manager/Nurse login → “Move Resident” checks roster & bed vacancy. |
| **Discharge + Archive** | Manager → “Discharge + Archive” writes to `archive.csv`. |
| **Add Staff / Change Password / Assign Shift** | Manager tab controls invoke `CareHome.addStaff()`, `changePassword()`, `assignShift()`. |
| **Prescriptions (by Doctor)** | Doctor tab → `CareHome.addPrescription()` with roster check. |
| **Administration (by Nurse)** | Nurse tab → `CareHome.administer()` with roster check. |
| **Audit Log** | `AuditLog` records timestamp + staff id for each action; shown in UI. |
| **Roster Compliance** | `RosterService.assertCompliantOrThrow()` verifies two nurse shifts, ≥1 doctor hour, ≤8 h nurse limit. |
| **Serialization Persistence** | State saved to `carehome.dat`; reloaded on next run. |
| **Archive Export** | Discharge → `archive.csv` for external audit. |
| **Unit Tests (Positive + Negative)** | All green tests under `src/test/java`; reset state each run. |

---

## 🧪 Testing Summary

| Type | Test Class | Purpose |
|------|-------------|---------|
| Positive | `CareHomeTest` | Assign resident to vacant bed succeeds. |
| Negative | `CareHomeNegativeTests` | (1) Assign to occupied bed → throws <br>(2) Nurse prescribes → throws. |
| Negative Roster | `CareHomeRosterNegativeTest` | (1) Nurse off-shift → throws <br>(2) 10 h shift → compliance fails. |

---

## 📁 Project Structure
src/
├── main/java/au/rmit/ap2/carehome/
│ ├── model/ → Resident, Staff hierarchy, Ward/Room/Bed, Prescription etc.
│ ├── service/ → CareHome singleton + Auth/Roster/Audit/Archive services
│ ├── exceptions/ → custom exceptions (Authorization, NotRostered …)
│ └── ui/ → JavaFX MainApp
└── test/java/au/rmit/ap2/carehome/ → JUnit 5 tests


---

## 🧠 Usage Tips
- Click any bed tile → see Resident name + ID (for Doctor/Nurse actions).
- To simulate off-shift errors, try actions outside the assigned hours.
- All changes (auto-save to `carehome.dat`) reload next launch.  
- Discharged residents recorded in `archive.csv`.

---

## 🖼️ Screenshots (to include in submission)
Place them in `/Screenshots` and embed in Canvas or PDF:
1. Bed Map 2. Add Resident 3. Doctor Prescribe 4. Nurse Administer 5. Assign Shift 6. Audit Log 7. JUnit Green Bar.

---

## 📚 References
- RMIT Canvas notes on OO Design & Patterns.  
- Oracle JavaFX Docs v21.  
- JUnit 5 User Guide.
