import javax.swing.*;

public class Services {
    private final newServiceFrom serviceForm;

    public Services() {
        serviceForm = new newServiceFrom();
    }

    public JPanel getServicesPanel() {
        return serviceForm.getServicesPanel();
    }
}
