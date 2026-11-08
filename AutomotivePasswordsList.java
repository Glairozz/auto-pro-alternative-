import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Vector;

public class AutomotivePasswordsList {
    // Static models keep registrations available even when a new login screen is created.
    static final DefaultTableModel adminCredList = new DefaultTableModel();
    static final DefaultTableModel clientCredList = new DefaultTableModel();
    int userCheck;

    static {
        adminCredList.addColumn("Username");
        adminCredList.addColumn("Password");
        adminCredList.addRow(new Object[]{"admin", "admin123"});
        adminCredList.addRow(new Object[]{"Jervey", "bossRica"});
        adminCredList.addRow(new Object[]{"Blair", "ILoveYouMaam"});
        adminCredList.addRow(new Object[]{"Liam", "Uarai"});

        clientCredList.addColumn("Username");
        clientCredList.addColumn("Password");
        clientCredList.addColumn("Name");
        clientCredList.addColumn("Appointments");
        clientCredList.addColumn("Bills");
        clientCredList.addColumn("Transaction Records");
        // No sample client is inserted. Every client row comes from registration.
    }

    public AutomotivePasswordsList() { }

    public Vector<Vector> getRegisteredClients() {
        return clientCredList.getDataVector();
    }

    String checkAdminCred(String inputAdminUser, String inputAdminPass) {
        if (inputAdminUser == null || inputAdminPass == null) return "Invalid";

        for (int row = 0; row < adminCredList.getRowCount(); row++) {
            String savedUser = String.valueOf(adminCredList.getValueAt(row, 0));
            String savedPass = String.valueOf(adminCredList.getValueAt(row, 1));
            if (inputAdminUser.trim().equalsIgnoreCase(savedUser) && inputAdminPass.equals(savedPass)) {
                return "Valid";
            }
        }
        return "Invalid";
    }

    String checkClientCred(String inputClientUser, String inputClientPass) {
        if (inputClientUser == null || inputClientPass == null) return "Invalid";

        for (int row = 0; row < clientCredList.getRowCount(); row++) {
            String savedUser = String.valueOf(clientCredList.getValueAt(row, 0));
            String savedPass = String.valueOf(clientCredList.getValueAt(row, 1));
            if (inputClientUser.trim().equalsIgnoreCase(savedUser) && inputClientPass.equals(savedPass)) {
                return "Valid";
            }
        }
        return "Invalid";
    }

    // Return 0 after a successful registration, or 1 when the username already exists.
    int checkClientCred(String inputClientUser, String inputClientPass, String inputClientName) {
        String username = inputClientUser == null ? "" : inputClientUser.trim();
        String password = inputClientPass == null ? "" : inputClientPass;
        String name = inputClientName == null ? "" : inputClientName.trim();

        if (username.isEmpty() || password.trim().isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please complete all registration fields.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            userCheck = 1;
            return userCheck;
        }

        for (int row = 0; row < adminCredList.getRowCount(); row++) {
            if (username.equalsIgnoreCase(String.valueOf(adminCredList.getValueAt(row, 0)))) {
                JOptionPane.showMessageDialog(null, "This username is already reserved.", "Username Exists", JOptionPane.WARNING_MESSAGE);
                userCheck = 1;
                return userCheck;
            }
        }

        for (int row = 0; row < clientCredList.getRowCount(); row++) {
            if (username.equalsIgnoreCase(String.valueOf(clientCredList.getValueAt(row, 0)))) {
                JOptionPane.showMessageDialog(null, "Username already exists. Please choose another username.", "Username Exists", JOptionPane.WARNING_MESSAGE);
                userCheck = 1;
                return userCheck;
            }
        }

        clientCredList.addRow(new Object[]{username, password, name, "No Appointments", "No Bills", "No Transactions"});
        userCheck = 0;
        return userCheck;
    }
}