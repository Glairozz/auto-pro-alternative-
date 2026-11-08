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
        btnSignUp.addActionListener(e -> {
            String pass = new String(txtPass.getPassword());
            String rePass = new String(txtRePass.getPassword());
            String user = txtUser.getText().trim();
            String name = txtName.getText().trim();

            if (pass.trim().isEmpty() || rePass.trim().isEmpty() || user.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(registerPanel, "Fill out all blanks!", "Missing Information", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!pass.equals(rePass)) {
                JOptionPane.showMessageDialog(registerPanel, "Passwords do not match!", "Incorrect Password", JOptionPane.WARNING_MESSAGE);
                return;
            }

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

        btnCancel.addActionListener(e -> {
            try {
                returnToLogin();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void returnToLogin() {
        AutomotiveServiceLogin.showLoginWindow("Login");
        JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(registerPanel);
        if (currentFrame != null) {
            currentFrame.dispose();
        }
    }

    public JPanel getRegisterPanel() { return registerPanel; }
}