import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    public JPanel getMainPanel(){
        return mainPanel;
    }
    String adminPass, adminUser, clientPass, clientUser;

    public AutomotiveServiceLogin() {
        btnAdminLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adminPass=txtAdminPass.getText();
                adminUser=txtAdminUser.getText().trim();

                if(StoreAccounts.getInstance().validAdmin(adminUser, adminPass)){
                    AdminPanel myForm = new AdminPanel();
                    JFrame frame = new JFrame("AutoCare - Admin");
                    frame.setContentPane(myForm.getAdminPanel());
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                    JFrame loginFrame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);

                    if(loginFrame != null){
                        loginFrame.dispose();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid Username or Password", "Invalid", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        btnClientLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clientPass=txtClientPass.getText();
                clientUser=txtClientUser.getText().trim();
                StoreAccounts accounts = StoreAccounts.getInstance();

                if(accounts.validClient(clientUser,clientPass)){
                    SessionUser.loginClient(clientUser, accounts.getClientName(clientUser));
                    newServiceForm myForm = new newServiceForm();
                    JFrame frame = new JFrame("AutoCare - Services");
                    frame.setContentPane(myForm.getServicesPanel());
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                    JFrame loginFrame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);

                    if(loginFrame != null){
                        loginFrame.dispose();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid Username or Password", "Invalid", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        btnClientRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    JFrame registerFrame = new JFrame("Registration");
                    AutomotiveClientInfo clientInfo = new AutomotiveClientInfo();

                    JPanel panel = clientInfo.getRegisterPanel();
                    if (panel==null) {
                        JOptionPane.showMessageDialog(null, "Error: getRegisterPanel() returned null. Check AutomotiveClientInfo.java!", "Null Panel Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    registerFrame.setContentPane(panel);
                    registerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    registerFrame.pack();
                    registerFrame.setLocationRelativeTo(null);
                    registerFrame.setVisible(true);

                    JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
                    if (currentFrame != null) {
                        currentFrame.dispose();
                    }
                } catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() ->{
            JFrame frame = new JFrame();
            AutomotiveServiceLogin myForm = new AutomotiveServiceLogin();
            frame.setContentPane(myForm.mainPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

    }
}
