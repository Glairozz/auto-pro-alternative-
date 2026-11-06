public class AutomotivePasswords extends AutomotivePasswordsList {
    private String clientUsername, clientPass, clientName, adminUsername, adminPass;
    // One shared in-memory data store for every login and screen in this running application.
    private static final AutomotiveData automotiveData = AutomotiveData.getInstance();

    void setClientUsername(String inputClientUsername) {
        clientUsername = inputClientUsername == null ? null : inputClientUsername.trim();
        clientName = findRegisteredClientName(clientUsername);
    }

    void setClientPass(String inputClientPass) { clientPass = inputClientPass; }
    void setClientName(String inputClientName) { clientName = inputClientName == null ? null : inputClientName.trim(); }
    void setAdminUsername(String inputAdminUsername) { adminUsername = inputAdminUsername; }
    void setAdminPass(String inputAdminPass) { adminPass = inputAdminPass; }

    String getClientUsername() { return clientUsername; }
    String getClientPass() { return clientPass; }

    String getClientName() {
        String registeredName = findRegisteredClientName(clientUsername);
        if (registeredName != null) return registeredName;
        return clientName;
    }

    String getAdminUsername() { return adminUsername; }
    String getAdminPass() { return adminPass; }

    private String findRegisteredClientName(String username) {
        if (username == null || username.trim().isEmpty()) return null;

        for (int i = 0; i < clientCredList.getRowCount(); i++) {
            if (username.equalsIgnoreCase(String.valueOf(clientCredList.getValueAt(i, 0)))) {
                return String.valueOf(clientCredList.getValueAt(i, 2));
            }
        }
        return null;
    }

    public AutomotiveData getAutomotiveData() {
        return automotiveData;
    }
}
