import javax.swing.*;
import java.util.Vector;

/**
 * Session helper for the login screens.
 *
 * This class no longer stores accounts or shared records. Every account and
 * record operation is delegated to the one AutomotiveData instance that was
 * created when the application started.
 */
public class AutomotivePasswords {
    private final AutomotiveData automotiveData;

    private String clientUsername, clientPass, clientName, adminUsername, adminPass;
    int userCheck;

    public AutomotivePasswords(AutomotiveData automotiveData) {
        if (automotiveData == null) {
            throw new IllegalArgumentException("AutomotiveData is required.");
        }

        this.automotiveData = automotiveData;
    }

    // Stores the active session username and resolves the registered display
    // name from the centralized client account records.
    void setClientUsername(String inputClientUsername) {
        clientUsername = inputClientUsername == null ? null : inputClientUsername.trim();
        clientName = automotiveData.getClientName(clientUsername);
    }

    void setClientPass(String inputClientPass) { clientPass = inputClientPass; }
    void setClientName(String inputClientName) { clientName = inputClientName == null ? null : inputClientName.trim(); }
    void setAdminUsername(String inputAdminUsername) { adminUsername = inputAdminUsername; }
    void setAdminPass(String inputAdminPass) { adminPass = inputAdminPass; }

    String getClientUsername() { return clientUsername; }
    String getClientPass() { return clientPass; }

    String getClientName() {
        String registeredName = automotiveData.getClientName(clientUsername);

        if (registeredName != null) {
            return registeredName;
        }

        return clientName;
    }

    String getAdminUsername() { return adminUsername; }
    String getAdminPass() { return adminPass; }

    // Admin credentials are validated against the centralized admin account
    // records, not against a separate table in the login screen.
    String checkAdminCred(String inputAdminUser, String inputAdminPass) {
        return automotiveData.validateAdmin(inputAdminUser, inputAdminPass)
                ? "Valid"
                : "Invalid";
    }

    // Client credentials are validated against the centralized client accounts.
    String checkClientCred(String inputClientUser, String inputClientPass) {
        return automotiveData.validateClient(inputClientUser, inputClientPass)
                ? "Valid"
                : "Invalid";
    }

    // Validates and registers one client through AutomotiveData.
    // Return 0 after a successful registration, or 1 when registration fails.
    int checkClientCred(String inputClientUser, String inputClientPass, String inputClientName) {
        String username = inputClientUser == null ? "" : inputClientUser.trim();
        String password = inputClientPass == null ? "" : inputClientPass;
        String name = inputClientName == null ? "" : inputClientName.trim();

        // Reject incomplete registration data before modifying shared records.
        if (username.isEmpty() || password.trim().isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please complete all registration fields.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            userCheck = 1;
            return userCheck;
        }

        // One username cannot be shared by an admin and a client account.
        if (automotiveData.isAdminUsername(username)) {
            JOptionPane.showMessageDialog(null, "This username is already reserved.", "Username Exists", JOptionPane.WARNING_MESSAGE);
            userCheck = 1;
            return userCheck;
        }

        if (automotiveData.isClientUsername(username)) {
            JOptionPane.showMessageDialog(null, "Username already exists. Please choose another username.", "Username Exists", JOptionPane.WARNING_MESSAGE);
            userCheck = 1;
            return userCheck;
        }

        // AutomotiveData performs the final duplicate check and assigns the
        // unique customer ID for the new account.
        if (automotiveData.registerClient(username, password, name) == -1) {
            JOptionPane.showMessageDialog(null, "The account could not be registered. Please try again.", "Registration Failed", JOptionPane.WARNING_MESSAGE);
            userCheck = 1;
            return userCheck;
        }

        userCheck = 0;
        return userCheck;
    }

    // Exposes the centralized account list for screens that still need a
    // read-only view of registered clients.
    public Vector<Vector> getRegisteredClients() {
        return automotiveData.getRegisteredClients();
    }

    // Returns the exact same shared instance used by every screen.
    public AutomotiveData getAutomotiveData() {
        return automotiveData;
    }
}
