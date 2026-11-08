import javax.swing.*;

/**
 * Login screen for admins and clients.
 * A successful login opens the matching dashboard and closes this window.
 */
public class AutomotiveServiceLogin {

    private JTabbedPane tabbedPane1;
    private JTextField txtAdminUser;
    private JTextField txtAdminPass;
    private JButton btnAdminLogin;
    private JTextField txtClientUser;
    private JTextField txtClientPass;
    private JButton btnClientLogin;
    private JButton btnClientRegister;
    private JPanel mainPanel;

    /** Returns the login panel so it can be placed inside a JFrame. */
    public JPanel getMainPanel() {
        return mainPanel;
    }

    public AutomotiveServiceLogin() {

        // Admin login: check the admin account table, then open the admin dashboard.
        btnAdminLogin.addActionListener(e -> {
            String adminPass = txtAdminPass.getText();
            String adminUser = txtAdminUser.getText().trim();

            if (StoreAccounts.getInstance().validAdmin(adminUser, adminPass)) {
                showWindow("AutoCare - Admin", new AdminPanel().getAdminPanel());
                closeLoginWindow();
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Invalid Username or Password",
                        "Invalid",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        // Client login: save the session user, then open the client dashboard.
        btnClientLogin.addActionListener(e -> {
            String clientPass = txtClientPass.getText();
            String clientUser = txtClientUser.getText().trim();
            StoreAccounts accounts = StoreAccounts.getInstance();

            if (accounts.validClient(clientUser, clientPass)) {
                SessionUser.loginClient(clientUser, accounts.getClientName(clientUser));

                showWindow(
                        "AutoCare - Services",
                        new newServiceForm().getServicesPanel()
                );

                closeLoginWindow();
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Invalid Username or Password",
                        "Invalid",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        // Registration button: open the client registration form.
        btnClientRegister.addActionListener(e -> {
            try {
                JFrame registerFrame = new JFrame("Registration");
                AutomotiveClientInfo clientInfo = new AutomotiveClientInfo();
                JPanel panel = clientInfo.getRegisterPanel();

                // The form must provide a panel before the window can be shown.
                if (panel == null) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Error: getRegisterPanel() returned null. Check AutomotiveClientInfo.java!",
                            "Null Panel Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                registerFrame.setContentPane(panel);
                registerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                registerFrame.pack();
                registerFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                registerFrame.setResizable(true);
                registerFrame.setLocationRelativeTo(null);
                registerFrame.setVisible(true);

                closeLoginWindow();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    /** Closes the login window after another screen has opened. */
    private void closeLoginWindow() {
        JFrame loginFrame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);

        if (loginFrame != null) {
            loginFrame.dispose();
        }
    }

    /** Opens a dashboard panel in a maximized, resizable window. */
    private static void showWindow(String title, JPanel panel) {
        JFrame frame = new JFrame(title);

        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /** Opens a fresh login window; used after registration or logout. */
    static void showLoginWindow(String title) {
        JPanel panel = new AutomotiveServiceLogin().getMainPanel();

        if (panel == null) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error: getMainPanel() returned null. Check AutomotiveServiceLogin.java!",
                    "Null Panel Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        JFrame frame = new JFrame(title);
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /** Starts the application by applying the theme and showing the login screen. */
    public static void main(String[] args) {
        Theme.init();

        SwingUtilities.invokeLater(() ->
                showLoginWindow("")
        );
    }
}