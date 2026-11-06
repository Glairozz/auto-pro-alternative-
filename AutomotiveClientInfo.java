import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AutomotiveClientInfo {
    private JTextField txtName;
    private JTextField txtUser;
    private JButton btnSignUp;
    private JPasswordField txtPass;
    private JPasswordField txtRePass;
    private JPanel registerPanel;
    private JButton btnCancel;
    String pass, rePass, user, name;

    public AutomotiveClientInfo() {
        btnSignUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pass = new String(txtPass.getPassword());
                rePass = new String(txtRePass.getPassword());
                user = txtUser.getText().trim();
                name = txtName.getText().trim();

                if (pass.trim().isEmpty() || rePass.trim().isEmpty() || user.isEmpty() || name.isEmpty()) {
                    JOptionPane.showMessageDialog(registerPanel, "Fill out all blanks!", "Missing Information", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!pass.equals(rePass)) {
                    JOptionPane.showMessageDialog(registerPanel, "Passwords do not match!", "Incorrect Password", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String error = StoreAccounts.getInstance().registerClient(user, pass, name);
                if(error != null){
                    JOptionPane.showMessageDialog(registerPanel, error, "Registration Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    JFrame loginFrame = new JFrame("Login");
                    AutomotiveServiceLogin loginPanel = new AutomotiveServiceLogin();
                    JPanel panel = loginPanel.getMainPanel();

                    if (panel == null) {
                        JOptionPane.showMessageDialog(null, "Error: getMainPanel() returned null. Check AutomotiveServiceLogin.java!", "Null Panel Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    loginFrame.setContentPane(panel);
                    loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    loginFrame.pack();
                    loginFrame.setLocationRelativeTo(null);
                    loginFrame.setVisible(true);

                    JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(registerPanel);
                    if (currentFrame != null) currentFrame.dispose();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(registerPanel, "Unable to open the login screen: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JFrame loginFrame = new JFrame("Login");
                    AutomotiveServiceLogin loginPanel = new AutomotiveServiceLogin();
                    JPanel panel = loginPanel.getMainPanel();
                    if (panel == null) {
                        JOptionPane.showMessageDialog(null, "Error: getMainPanel() returned null. Check AutomotiveServiceLogin.java!", "Null Panel Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    loginFrame.setContentPane(panel);
                    loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    loginFrame.pack();
                    loginFrame.setLocationRelativeTo(null);
                    loginFrame.setVisible(true);

                    JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(registerPanel);
                    if (currentFrame != null) currentFrame.dispose();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public JPanel getRegisterPanel() { return registerPanel; }
}