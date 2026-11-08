import javax.swing.*;

public class AutomotiveClientInfo {
    private JTextField txtName;
    private JTextField txtUser;
    private JButton btnSignUp;
    private JPasswordField txtPass;
    private JPasswordField txtRePass;
    private JPanel registerPanel;
    private JButton btnCancel;

    public AutomotiveClientInfo() {
        // Validate the form and create the client account.
        btnSignUp.addActionListener(e -> {
            String pass = new String(txtPass.getPassword());
            String rePass = new String(txtRePass.getPassword());
            String user = txtUser.getText().trim();
            String name = txtName.getText().trim();

            // All fields are required.
            if (pass.trim().isEmpty() || rePass.trim().isEmpty() || user.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(registerPanel, "Fill out all blanks!", "Missing Information", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // The two password fields must match.
            if (!pass.equals(rePass)) {
                JOptionPane.showMessageDialog(registerPanel, "Passwords do not match!", "Incorrect Password", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // StoreAccounts checks for duplicate/reserved usernames.
            String error = StoreAccounts.getInstance().registerClient(user, pass, name);
            if (error != null) {
                JOptionPane.showMessageDialog(registerPanel, error, "Registration Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                returnToLogin();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(registerPanel, "Unable to open the login screen: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Cancel closes registration and goes back to login.
        btnCancel.addActionListener(e -> {
            try {
                returnToLogin();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    /** Opens the login window and closes the registration window. */
    private void returnToLogin() {
        AutomotiveServiceLogin.showLoginWindow("Login");
        JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(registerPanel);
        if (currentFrame != null) {
            currentFrame.dispose();
        }
    }

    /** Returns the registration panel so it can be placed inside a JFrame. */
    public JPanel getRegisterPanel() { return registerPanel; }
}