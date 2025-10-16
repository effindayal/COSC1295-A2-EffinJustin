### Reflection — Resident HealthCare System

### Objective
To create and put into use a hospital bed management system that is expandable and manageable while showcasing object-oriented concepts, GUI programming, exception handling, serialization, and testing.


### Design Principles Applied
- Encapsulation & Abstraction: Domain classes (`Resident`, `Staff`, `Ward`, etc.) hide state behind methods.
- Inheritance & Polymorphism: `Staff` base class specialized as `Manager`, `Doctor`, `Nurse` with role-specific permissions.
- Singleton Pattern: `CareHome` acts as the system controller and data repository.
- SRP / Separation of Concerns: UI calls public API methods; logic in `service` layer (`RosterService`, `AuthService`, `ArchiveService`).
- Open–Closed Principle: Adding new roles or functions requires only new service methods, not UI rewrites.


### Key Decisions
- Roster Driven Access: Actions checked via `RosterService.requireOnDuty()` to simulate real-world shift compliance.  
- Audit Trail & Archive: Every action logged for traceability and audit compliance; discharge writes summary to CSV.  
- Serialization Persistence: `carehome.dat` for fast state restore; transient fields rebuilt on load (`rebind()`).  
- Error Handling: Custom exceptions (`AuthorizationException`, `NotRosteredException`, `OccupiedBedException`) make failures clear.  
- Testability: `CareHome.resetForTests()` ensures each JUnit run starts with a clean seed.



### Challenges & Solutions
| Challenge                            | Solution     

| Lost transient services after reload | Re-initialized `AuthService` in `rebind()`. |
| Roster check false positives         | Re-wrote `assertCompliantOrThrow()` to use real date per DayOfWeek. |
| “Not on duty” errors during demo     | Enabled Manager to assign wide shifts (08:00–23:00) for testing. |
| File state interfering with tests    | Added `resetForTests()` to delete `carehome.dat` between runs. |



### Testing Summary
- Positive: Assign resident to vacant bed.  
- Negative: Occupied bed exception; Nurse prescribe denied; off-shift administer denied; 10 h shift → compliance fail.  
All tests green in JUnit 5.



### Evaluation & Future Improvements
- Strong OO structure and GUI cover the required specifications.  
- Could extend with DB storage (JDBC) for real auditing and hashed passwords.  
- Could add real-time notifications for upcoming medication times.  
- Design is easily maintainable and passes rubric requirements for readability, robustness & testing.


### Personal Learning
Building this project reinforced practical application of object-oriented concepts, exception design, and GUI MVC separation.  
The incremental refactoring from text menu to JavaFX interface demonstrated how to scale a program while preserving testability and clarity.
