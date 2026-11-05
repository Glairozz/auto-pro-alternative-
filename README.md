# AutoCare Service Management

Java Swing application for automotive service appointments, product orders, billing, and admin management.

## Latest Update: Centralized Data Reverted

This update reverts the centralized-data refactor from commit `94396d6` while keeping the admin UI simplifications. The comment-only follow-up commit `c8804df` was also superseded for the files restored to the previous architecture.

The project still uses the pre-existing static `AutomotiveData` instance for operational records. What was reverted is the newer refactor that moved account handling and the additional account, customer-ID, and payment orchestration into a single passed-in `AutomotiveData` instance.

### What was reverted

- Restored `AutomotivePasswordsList.java` with its static admin and client credential tables.
- Restored `AutomotivePasswords` as a login/session helper that extends `AutomotivePasswordsList`.
- Restored the previous constructors and data flow:
  - `AdminPanel(AutomotivePasswords)`
  - `Services(AutomotivePasswords)`
  - `AutomotiveServiceLogin(AutomotivePasswords)`
  - `AutomotiveClientInfo(AutomotivePasswords)`
- Restored `AutomotiveData` to its previous responsibilities: appointment, product, bill, and transaction table models; per-client carts; service-bill links; statuses; and ID counters.
- Removed the centralized account models, registration logic, customer IDs, and newer payment orchestration that had been added to `AutomotiveData`.
- Restored the previous client and admin screen logic for appointments, products, billing, and payments.
- Restored literal appointment/service status values in `AppointmentDetails`.

### What was kept

- The **Approve Appointments** tab remains removed.
- Admin approval continues through **Manage Appointments -> Confirm Appointment**.
- The **Update Work Status** button, service-status combo box, and related label remain removed.
- Service Management keeps **Start Service**, **Mark as Completed**, and **Cancel Service**.
- `AdminPanel.form` remains in its simplified layout.

## Files Changed

- `AdminPanel.java` - restored the previous admin data flow and removed references to the deleted approval-tab and status controls.
- `AppointmentDetails.java` - restored the previous status defaults.
- `AutomotiveClientInfo.java` - restored registration through the previous session helper.
- `AutomotiveData.java` - reverted to the previous operational-data implementation.
- `AutomotivePasswords.java` - restored the previous credential/session helper.
- `AutomotivePasswordsList.java` - restored the static credential tables.
- `AutomotiveServiceLogin.java` - restored the previous login and panel construction flow.
- `Services.java` - restored the previous client appointment, cart, billing, and payment flow.
- `README.md` - documents this revert and the retained UI changes.

`AdminPanel.form` was left in its simplified state because it already reflects the removed controls.

## Build

```powershell
javac -encoding UTF-8 -d build *.java
```

Run the application:

```powershell
java -cp build AutomotiveServiceLogin
```

If using IntelliJ GUI Designer, run **Build -> Rebuild Project** after pulling so the `.form` bindings are regenerated.

## Verification

- Project compiled successfully with `javac 17`.
- No references remain to the removed admin controls:
  - `tblApproveAppointments`
  - `btnApproveAppointment`
  - `btnCancelPendingAppointment`
  - `cmbServiceStatus`
  - `btnUpdateWorkStatus`
