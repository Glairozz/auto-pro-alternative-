import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Login and registration entry point.
 *
 * The session object passed to this screen carries the one shared
 * AutomotiveData instance that is later handed to the Admin and Client panels.
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
    public JPanel getMainPanel(){
        return mainPanel;
    }
    String adminPass, adminUser, clientPass, clientUser;
    AutomotivePasswords verifyCred;
    public AutomotiveServiceLogin(AutomotivePasswords sharedCred) {
        this.verifyCred =sharedCred;
        btnAdminLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adminPass=txtAdminPass.getText();
                adminUser=txtAdminUser.getText();
                verifyCred.setAdminPass(adminPass);
                verifyCred.setAdminUsername(adminUser);

                if(verifyCred.checkAdminCred(verifyCred.getAdminUsername(), verifyCred.getAdminPass()).equals("Valid")){
                    // The Admin panel receives the same shared data instance.
                    AdminPanel myForm = new AdminPanel(verifyCred.getAutomotiveData());
                    JFrame frame = new JFrame("AutoCare - Services");
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
                clientUser=txtClientUser.getText();
                verifyCred.setClientUsername(clientUser);
                verifyCred.setClientPass(clientPass);

                if(verifyCred.checkClientCred(verifyCred.getClientUsername(), verifyCred.getClientPass()).equals("Valid")){
                    // The Client panel receives the same data instance plus the
                    // username used to filter that client's own records.
                    Services myForm = new Services(verifyCred.getAutomotiveData(), verifyCred.getClientUsername());
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
                    AutomotiveClientInfo clientInfo = new AutomotiveClientInfo(verifyCred);

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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // One shared data store for the whole running application.
                AutomotiveData sharedData = new AutomotiveData();
                AutomotivePasswords session = new AutomotivePasswords(sharedData);

                JFrame frame = new JFrame();
                AutomotiveServiceLogin myForm = new AutomotiveServiceLogin(session);
                frame.setContentPane(myForm.mainPanel);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }
}
