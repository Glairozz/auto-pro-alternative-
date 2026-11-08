import javax.swing.table.DefaultTableModel;

/**
 * Stores all admin and client accounts used by the login and registration screens.
 * Only one instance exists so the same accounts are visible everywhere.
 */
public final class StoreAccounts {
    private static final StoreAccounts INSTANCE = new StoreAccounts();
    public static StoreAccounts getInstance() { return INSTANCE; }

    private final DefaultTableModel admins = new DefaultTableModel();
    private final DefaultTableModel clients = new DefaultTableModel();

    private StoreAccounts() {
        // Built-in admin accounts: column 0 is the username, column 1 is the password.
        admins.addColumn("Username");
        admins.addColumn("Password");
        admins.addRow(new Object[]{"admin", "admin123"});
        admins.addRow(new Object[]{"Jervey", "bossRica"});
        admins.addRow(new Object[]{"Blair", "ILoveYouMaam"});
        admins.addRow(new Object[]{"Liam", "Uarai"});

        // Registered clients: 0 username, 1 password, 2 full name, 3-5 summary text.
        clients.addColumn("Username");
        clients.addColumn("Password");
        clients.addColumn("Name");
        clients.addColumn("Appointment's");
        clients.addColumn("Bills");
        clients.addColumn("Transaction Records");
    }

    /** Checks the admin table for a matching username and password. */
    public boolean validAdmin(String inputUser, String inputPass) {
        return validCredential(admins, inputUser, inputPass);
    }

    /** Checks the client table for a matching username and password. */
    public boolean validClient(String inputUser, String inputPass) {
        return validCredential(clients, inputUser, inputPass);
    }

    /** Shared username/password check used by both admin and client login. */
    private boolean validCredential(DefaultTableModel accounts, String inputUser, String inputPass) {
        for (int row = 0; row < accounts.getRowCount(); row++) {
            if (inputUser.equals(accounts.getValueAt(row, 0))
                    && inputPass.equals(accounts.getValueAt(row, 1))) {
                return true;
            }
        }

        return false;
    }

    /** Finds the client's full name from the username stored in column 0. */
    public String getClientName(String inputName) {
        for (int rowSearch = 0; rowSearch < clients.getRowCount(); rowSearch++) {
            if (inputName.equalsIgnoreCase(clients.getValueAt(rowSearch, 0).toString())) {
                return clients.getValueAt(rowSearch, 2).toString();
            }
        }
        return null;
    }

    /**
     * Adds a new client account.
     * Returns null on success, or an error message when the data is invalid or duplicated.
     */
    public String registerClient(String user, String pass, String name) {
        String username = user == null ? "" : user.trim();
        String password = pass == null ? "" : pass;
        String fullName = name == null ? "" : name.trim();

        if (username.isEmpty() || password.trim().isEmpty() || fullName.isEmpty()) {
            return "Please complete all registration fields.";
        }

        // Admin usernames are reserved and cannot be used by clients.
        for (int row = 0; row < admins.getRowCount(); row++) {
            if (username.equalsIgnoreCase(String.valueOf(admins.getValueAt(row, 0)))) {
                return "This username is already reserved.";
            }
        }

        // Client usernames must also be unique.
        for (int row = 0; row < clients.getRowCount(); row++) {
            if (username.equalsIgnoreCase(String.valueOf(clients.getValueAt(row, 0)))) {
                return "Username already exists. Please choose another username.";
            }
        }

        clients.addRow(new Object[]{username, password, fullName,
                "No Appointments", "No Bills", "No Transaction Records"});
        return null;
    }
}