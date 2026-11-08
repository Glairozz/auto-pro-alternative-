import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PaymentDialog extends JDialog {
    private JComboBox<String> paymentMethodCMB;
    private JTextField cardNameTXT;
    private JTextField cardNumberTXT;
    private JPasswordField cardPasswordTXT;
    private boolean paymentSuccessful;
    private String selectedMethod;

    public PaymentDialog(JFrame parent, String billAmount, String initialMethod) {
        super(parent, "Process Payment - Amount: " + billAmount, true);

        paymentSuccessful = false;
        selectedMethod = "";

        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        formPanel.add(new JLabel("Payment Method:"));
        paymentMethodCMB = new JComboBox<String>();
        paymentMethodCMB.addItem("Cash");
        paymentMethodCMB.addItem("Debit Card");
        paymentMethodCMB.addItem("Credit Card");
        if (initialMethod != null) {
            paymentMethodCMB.setSelectedItem(initialMethod);
        }
        formPanel.add(paymentMethodCMB);

        JPanel cardDetailsPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        cardDetailsPanel.setBorder(BorderFactory.createTitledBorder("Card Details"));

        cardDetailsPanel.add(new JLabel("Card Name:"));
        cardNameTXT = new JTextField();
        cardDetailsPanel.add(cardNameTXT);

        cardDetailsPanel.add(new JLabel("Card Number:"));
        cardNumberTXT = new JTextField();
        cardDetailsPanel.add(cardNumberTXT);

        cardDetailsPanel.add(new JLabel("Card Password:"));
        cardPasswordTXT = new JPasswordField();
        cardDetailsPanel.add(cardPasswordTXT);

        add(formPanel, BorderLayout.NORTH);
        add(cardDetailsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton confirmPaymentBT = new JButton("Confirm Payment");
        JButton cancelBT = new JButton("Cancel");
        buttonPanel.add(confirmPaymentBT);
        buttonPanel.add(cancelBT);
        add(buttonPanel, BorderLayout.SOUTH);

        toggleCardFields();

        paymentMethodCMB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleCardFields();
            }
        });

        confirmPaymentBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedMethod = (String) paymentMethodCMB.getSelectedItem();

                if (selectedMethod == null) {
                    JOptionPane.showMessageDialog(PaymentDialog.this,
                            "Select a payment method.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!selectedMethod.equals("Cash")) {
                    if (cardNameTXT.getText().trim().isEmpty()
                            || cardNumberTXT.getText().trim().isEmpty()
                            || new String(cardPasswordTXT.getPassword()).trim().isEmpty()) {
                        JOptionPane.showMessageDialog(PaymentDialog.this,
                                "Please fill in all card details.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (!cardNumberTXT.getText().trim().matches("[0-9]+")) {
                        JOptionPane.showMessageDialog(PaymentDialog.this,
                                "Card number must contain numbers only.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                paymentSuccessful = true;
                dispose();
            }
        });

        cancelBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void toggleCardFields() {
        String method = (String) paymentMethodCMB.getSelectedItem();
        boolean isCard = method != null && !method.equals("Cash");

        cardNameTXT.setEnabled(isCard);
        cardNumberTXT.setEnabled(isCard);
        cardPasswordTXT.setEnabled(isCard);

        if (!isCard) {
            cardNameTXT.setText("");
            cardNumberTXT.setText("");
            cardPasswordTXT.setText("");
        }
    }

    public boolean isPaymentSuccessful() {
        return paymentSuccessful;
    }

    public String getSelectedMethod() {
        return selectedMethod;
    }
}
