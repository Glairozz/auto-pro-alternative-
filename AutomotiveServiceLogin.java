import javax.swing.*;
import java.awt.Dimension;

public class AutomotiveServiceLogin {
    private static final Dimension ADMIN_CLIENT_WINDOW_SIZE = new Dimension(1100, 700);

    private JTabbedPane tabbedPane1;
    private JTextField txtAdminUser;
    private JTextField txtAdminPass;
    private JButton btnAdminLogin;
    private JTextField txtClientUser;
    private JTextField txtClientPass;
    private JButton btnClientLogin;
    private JButton btnClientRegister;
    private JPanel mainPanel;
    public JPanel getMainPanel(){
        return mainPanel;
    }

    public AutomotiveServiceLogin() {
        btnAdminLogin.addActionListener(e -> {
            String adminPass = txtAdminPass.getText();
            String adminUser = txtAdminUser.getText().trim();

            if (StoreAccounts.getInstance().validAdmin(adminUser, adminPass)) {
                showFixedSizeWindow("AutoCare - Admin", new AdminPanel().getAdminPanel());
                closeLoginWindow();
            } else {
                JOptionPane.showMessageDialog(null, "Invalid Username or Password", "Invalid", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnClientLogin.addActionListener(e -> {
            String clientPass = txtClientPass.getText();
            String clientUser = txtClientUser.getText().trim();
            StoreAccounts accounts = StoreAccounts.getInstance();

            if (accounts.validClient(clientUser, clientPass)) {
                SessionUser.loginClient(clientUser, accounts.getClientName(clientUser));
                showFixedSizeWindow("AutoCare - Services", new newServiceForm().getServicesPanel());
                closeLoginWindow();
            } else {
                JOptionPane.showMessageDialog(null, "Invalid Username or Password", "Invalid", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnClientRegister.addActionListener(e -> {
            try {
                JFrame registerFrame = new JFrame("Registration");
                AutomotiveClientInfo clientInfo = new AutomotiveClientInfo();
                JPanel panel = clientInfo.getRegisterPanel();

                if (panel == null) {
                    JOptionPane.showMessageDialog(null, "Error: getRegisterPanel() returned null. Check AutomotiveClientInfo.java!", "Null Panel Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                registerFrame.setContentPane(panel);
                registerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                registerFrame.pack();
                registerFrame.setLocationRelativeTo(null);
                registerFrame.setVisible(true);
                closeLoginWindow();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void closeLoginWindow() {
        JFrame loginFrame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
        if (loginFrame != null) {
            loginFrame.dispose();
        }
    }

    private static void applyFixedWindowSize(JFrame frame) {
        frame.setPreferredSize(ADMIN_CLIENT_WINDOW_SIZE);
        frame.pack();
        frame.setSize(ADMIN_CLIENT_WINDOW_SIZE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
    }

    private static void showFixedSizeWindow(String title, JPanel panel) {
        JFrame frame = new JFrame(title);
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        applyFixedWindowSize(frame);
        frame.setVisible(true);
    }

    static void showLoginWindow(String title) {
        JPanel panel = new AutomotiveServiceLogin().getMainPanel();
        if (panel == null) {
            JOptionPane.showMessageDialog(null, "Error: getMainPanel() returned null. Check AutomotiveServiceLogin.java!", "Null Panel Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFrame frame = new JFrame(title);
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args){
        Theme.init();
        SwingUtilities.invokeLater(() -> showLoginWindow(""));
    }
}