import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Client dashboard.
 * It contains the appointment form, product cart, and billing/payment screens.
 * Each client sees only their own appointments, bills, and transactions.
 */
public class newServiceForm {
    // Shared table models loaded from AutomotiveData.
    private DefaultTableModel appointmentModel;
    private DefaultTableModel productModel;
    private DefaultTableModel cartModel;
    private DefaultTableModel billModel;
    private DefaultTableModel transModel;

    // Components created by the IntelliJ GUI Designer .form file.
    private JTabbedPane tabbedPane1;
    private JPanel servicesPanel;
    private JButton billingLogOutBT;
    private JTextField appointmentVehicleTXT;
    private JRadioButton repairServicesRadioButton;
    private JRadioButton installationServicesRadioButton;
    private JComboBox appointmentProblemCMB;
    private JSpinner appointmentDateSPN;
    private JSpinner appointmentTimeSPN;
    private JButton appointmentSubmitAppBT;
    private JTable appointmentTable;
    private JSpinner productQuantitySPN;
    private JButton productAddToCartBT;
    private JTable productProductTable;
    private JTable table3;
    private JButton productRemoveBT;
    private JTextField productTotalTXT;
    private JButton productConfirmProdOrderBT;
    private JTable billingMyBillsTable;
    private JTextField billingOutstandingBalanceTXT;
    private JButton billingReviewBillBT;
    private JTable billingTransacRecordTable;
    private JButton billingViewRecieptBT;
    private JTextArea billingTXTArea;
    private JScrollPane pane3;
    private JScrollPane pane2;

    // Shared data and the currently logged-in client.
    private final AutomotiveData sharedData = AutomotiveData.getInstance();
    private final String currentClientUsername = SessionUser.getUsername();
    private final String currentClientName = SessionUser.getDisplayName();


    public newServiceForm() {
        // Connect the visual tables to the shared data models.
        appointmentModel = sharedData.getAppointmentModel();
        productModel = sharedData.getProductModel();
        cartModel = sharedData.getCartModel(currentClientUsername);
        billModel = sharedData.getBillModel();
        transModel = sharedData.getTransactionModel();

        appointmentTable.setModel(appointmentModel);
        productProductTable.setModel(productModel);
        table3.setModel(cartModel);
        billingMyBillsTable.setModel(billModel);
        billingTransacRecordTable.setModel(transModel);

        // Client username columns are only needed for filtering, so hide them.
        hideTableColumn(appointmentTable, 9);
        hideTableColumn(billingMyBillsTable, 8);
        hideTableColumn(billingTransacRecordTable, 6);

        // Show only records that belong to the logged-in client.
        filterByClient(appointmentTable, appointmentModel, 9);
        filterByClient(billingMyBillsTable, billModel, 8);
        filterByClient(billingTransacRecordTable, transModel, 6);

        // Appointment date picker.
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        appointmentDateSPN.setModel(dateModel);
        appointmentDateSPN.setEditor(new JSpinner.DateEditor(appointmentDateSPN, "dd/MM/yyyy"));

        // Cart quantity starts at 1 and the totals are read-only.
        productQuantitySPN.setModel(new SpinnerNumberModel(1, 1, 100, 1));
        productTotalTXT.setText("₱0.00");
        productTotalTXT.setEditable(false);
        billingOutstandingBalanceTXT.setEditable(false);

        // Billing review text area styling.
        Font reviewFont = new Font("Monospaced", Font.PLAIN, 12);
        Insets reviewMargin = new Insets(8, 8, 8, 8);
        billingTXTArea.setEditable(false);
        billingTXTArea.setLineWrap(true);
        billingTXTArea.setWrapStyleWord(true);
        billingTXTArea.setFont(reviewFont);
        billingTXTArea.setMargin(reviewMargin);

        // Time picker works in 30-minute steps from 08:30.
        appointmentTimeSPN.setModel(new AbstractSpinnerModel() {
            private int currentMinutes = (8 * 60) + 30;

            @Override
            public Object getValue() {
                return String.format("%02d:%02d", currentMinutes / 60, currentMinutes % 60);
            }

            @Override
            public void setValue(Object value) {
                if (value instanceof String) {
                    String[] timeParts = value.toString().split(":");
                    if (timeParts.length == 2) {
                        int hour = Integer.parseInt(timeParts[0]);
                        int minute = Integer.parseInt(timeParts[1]);
                        if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                            currentMinutes = (hour * 60) + minute;
                            fireStateChanged();
                        }
                    }
                }
            }

            @Override
            public Object getNextValue() {
                int nextMinutes = (currentMinutes + 30) % 1440;
                return String.format("%02d:%02d", nextMinutes / 60, nextMinutes % 60);
            }

            @Override
            public Object getPreviousValue() {
                int previousMinutes = (currentMinutes - 30 + 1440) % 1440;
                return String.format("%02d:%02d", previousMinutes / 60, previousMinutes % 60);
            }
        });

        appointmentTimeSPN.setEditor(new JSpinner.DefaultEditor(appointmentTimeSPN));
        ((JSpinner.DefaultEditor) appointmentTimeSPN.getEditor()).getTextField().setEditable(false);

        // The two radio buttons share one group, so only one service type can be selected.
        ButtonGroup serviceTypeGroup = new ButtonGroup();
        serviceTypeGroup.add(repairServicesRadioButton);
        serviceTypeGroup.add(installationServicesRadioButton);

        // Reload the problem dropdown whenever the service type changes.
        ActionListener serviceTypeListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (repairServicesRadioButton.isSelected()) {
                    appointmentProblemCMB.setModel(new DefaultComboBoxModel<String>(new RepairService().getServiceDetails()));
                } else {
                    appointmentProblemCMB.setModel(new DefaultComboBoxModel<String>(new InstallationService().getServiceDetails()));
                }
            }
        };
        repairServicesRadioButton.addActionListener(serviceTypeListener);
        installationServicesRadioButton.addActionListener(serviceTypeListener);
        repairServicesRadioButton.setSelected(true);
        serviceTypeListener.actionPerformed(null);

        // Submit a new appointment and create its matching pending service bill.
        appointmentSubmitAppBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String vehicle = appointmentVehicleTXT.getText().trim();
                String problem = (String) appointmentProblemCMB.getSelectedItem();
                String time = appointmentTimeSPN.getValue().toString();

                if (problem == null || vehicle.isEmpty()) {
                    JOptionPane.showMessageDialog(servicesPanel, "Please complete all appointment details.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                AutomotiveService service = repairServicesRadioButton.isSelected()
                        ? new RepairService()
                        : new InstallationService();
                String serviceName = service.getServiceName();

                AppointmentDetails appointment = new AppointmentDetails(service, vehicle, problem);
                String validation = appointment.validateServiceRequest();
                if (!validation.equals("Valid Appointment")) {
                    JOptionPane.showMessageDialog(servicesPanel, validation, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String date = new SimpleDateFormat("dd/MM/yyyy").format((Date) appointmentDateSPN.getValue());
                String schedule = date + " " + time;
                double estimate = appointment.getEstimatedPrice();
                int appointmentID = sharedData.nextAppointmentID();

                // Appointment columns: 0 ID, 1 client, 2 service, 3 vehicle, 4 details,
                // 5 schedule, 6 approval status, 7 service status, 8 estimate, 9 username.
                appointmentModel.addRow(new Object[]{
                        appointmentID, currentClientName, appointment.getServiceName(), appointment.getVehicleInfo(),
                        appointment.getServiceDetail(), schedule, "Pending", "Not Started", String.format("₱%.2f", estimate), currentClientUsername
                });

                int billID = sharedData.nextBillID();
                String amount = String.format("₱%.2f", estimate);
                String billDate = new SimpleDateFormat("dd/MM/yyyy hh:mm a").format(new Date());

                // Service bills start as Pending approval and Unpaid.
                billModel.addRow(new Object[]{
                        billID, currentClientName, "Service", serviceName + " - " + problem + " (" + vehicle + ")",
                        amount, "Pending", "Unpaid", billDate, currentClientUsername
                });

                sharedData.linkServiceBill(billID, appointmentID);

                updateBillingReviewText();
                JOptionPane.showMessageDialog(servicesPanel, "Appointment submitted successfully!\nPlease wait for admin approval before payment.");
            }
        });

        // Add the selected product to the client's cart and reduce its stock.
        productAddToCartBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = productProductTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(servicesPanel, "Select a product first.");
                    return;
                }

                selectedRow = productProductTable.convertRowIndexToModel(selectedRow);
                int quantity = (Integer) productQuantitySPN.getValue();
                int stock = Integer.parseInt(productModel.getValueAt(selectedRow, 4).toString());

                if (quantity > stock) {
                    JOptionPane.showMessageDialog(servicesPanel, "Not enough stock available.");
                    return;
                }

                String id = productModel.getValueAt(selectedRow, 0).toString();
                String name = productModel.getValueAt(selectedRow, 1).toString();
                double price = Double.parseDouble(productModel.getValueAt(selectedRow, 3).toString());

                // Cart columns: 0 ID, 1 name, 2 quantity, 3 subtotal.
                double subtotal = price * quantity;
                cartModel.addRow(new Object[]{id, name, quantity, String.format("%.2f", subtotal)});
                productModel.setValueAt(stock - quantity, selectedRow, 4);
                productTotalTXT.setText(String.format("₱%.2f", OrderCalculator.calculateCartTotal(cartModel)));
                productQuantitySPN.setValue(1);
            }
        });

        // Remove the selected cart row and return its quantity to product stock.
        productRemoveBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table3.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(servicesPanel, "Select an item in the cart.");
                    return;
                }

                selectedRow = table3.convertRowIndexToModel(selectedRow);
                String id = cartModel.getValueAt(selectedRow, 0).toString();
                int quantity = Integer.parseInt(cartModel.getValueAt(selectedRow, 2).toString());

                for (int i = 0; i < productModel.getRowCount(); i++) {
                    if (productModel.getValueAt(i, 0).toString().equals(id)) {
                        int stock = Integer.parseInt(productModel.getValueAt(i, 4).toString());
                        productModel.setValueAt(stock + quantity, i, 4);
                        break;
                    }
                }

                cartModel.removeRow(selectedRow);
                productTotalTXT.setText(String.format("₱%.2f", OrderCalculator.calculateCartTotal(cartModel)));
            }
        });

        // Turn the cart into a product bill that can be paid immediately.
        productConfirmProdOrderBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cartModel.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(servicesPanel, "Your cart is empty.");
                    return;
                }

                double total = OrderCalculator.calculateCartTotal(cartModel);
                int billID = sharedData.nextBillID();

                StringBuilder productDetails = new StringBuilder("Product Order: ");
                for (int i = 0; i < cartModel.getRowCount(); i++) {
                    String productID = cartModel.getValueAt(i, 0).toString();
                    String productName = cartModel.getValueAt(i, 1).toString();
                    int quantity = Integer.parseInt(cartModel.getValueAt(i, 2).toString());
                    double subtotal = Double.parseDouble(cartModel.getValueAt(i, 3).toString());
                    double price = 0;

                    for (int j = 0; j < productModel.getRowCount(); j++) {
                        if (productModel.getValueAt(j, 0).toString().equals(productID)) {
                            price = Double.parseDouble(productModel.getValueAt(j, 3).toString());
                            break;
                        }
                    }

                    if (i > 0) {
                        productDetails.append("; ");
                    }

                    productDetails.append(productName)
                            .append(" x").append(quantity)
                            .append(" @ ").append(String.format("₱%.2f", price))
                            .append(" = ").append(String.format("₱%.2f", subtotal));
                }

                // Product bills do not need admin approval, so approval is "Not Required".
                billModel.addRow(new Object[]{
                        billID, currentClientName, "Product", productDetails.toString(), String.format("₱%.2f", total),
                        "Not Required", "Unpaid", new SimpleDateFormat("dd/MM/yyyy hh:mm a").format(new Date()), currentClientUsername
                });

                cartModel.setRowCount(0);
                productTotalTXT.setText("₱0.00");

                updateBillingReviewText();
                JOptionPane.showMessageDialog(servicesPanel, "Product order confirmed!\nYou can pay for this product order immediately.");
            }
        });

        // Pay the selected bill after checking ownership, approval, and service completion.
        billingReviewBillBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = billingMyBillsTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(servicesPanel, "Select a bill first.");
                    return;
                }

                selectedRow = billingMyBillsTable.convertRowIndexToModel(selectedRow);
                updateBillingReviewText();
                int billNumber = Integer.parseInt(billModel.getValueAt(selectedRow, 0).toString());
                String clientName = billModel.getValueAt(selectedRow, 1).toString();
                String billType = billModel.getValueAt(selectedRow, 2).toString();
                String description = billModel.getValueAt(selectedRow, 3).toString();
                String amount = billModel.getValueAt(selectedRow, 4).toString();
                String approvalStatus = billModel.getValueAt(selectedRow, 5).toString();
                String paymentStatus = billModel.getValueAt(selectedRow, 6).toString();

                if (!billModel.getValueAt(selectedRow, 8).toString().equalsIgnoreCase(currentClientUsername)) {
                    JOptionPane.showMessageDialog(servicesPanel, "You can only pay your own bills.");
                    return;
                }

                if (paymentStatus.equals("Paid")) {
                    JOptionPane.showMessageDialog(servicesPanel, "This bill has already been paid.");
                    return;
                }

                if (billType.equals("Service")) {
                    Integer appointmentID = sharedData.getAppointmentIDForBill(billNumber);
                    String currentStatus = appointmentID == null ? null : sharedData.getAppointmentStatus(appointmentID);


                    if (currentStatus == null) {
                        JOptionPane.showMessageDialog(servicesPanel, "The appointment record could not be found.");
                        return;
                    }

                    // Keep the bill's approval status in sync with the appointment.
                    approvalStatus = currentStatus;
                    billModel.setValueAt(approvalStatus, selectedRow, 5);


                    if (!approvalStatus.equals("Approved")) {
                        JOptionPane.showMessageDialog(
                                servicesPanel,
                                "Payment is not available yet.\n\nYour service appointment must be approved by the admin first.\nCurrent Approval Status: " + approvalStatus,
                                "Admin Approval Required",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }

                    String serviceStatus = sharedData.getServiceStatus(appointmentID);
                    if (serviceStatus == null || !serviceStatus.equals("Completed")) {
                        JOptionPane.showMessageDialog(
                                servicesPanel,
                                "Payment is not available yet.\n\nYour service must be completed before you can pay.\nCurrent Service Status: " + serviceStatus,
                                "Service Not Completed",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
                } else if (!billType.equals("Product")) {
                    JOptionPane.showMessageDialog(servicesPanel, "This bill type cannot be paid.");
                    return;
                }

                // Small payment form shown inside a confirm dialog.
                JComboBox<String> paymentMethodCMB = new JComboBox<>(new String[]{"Cash", "Debit", "Credit"});
                JTextField cardNameTXT = new JTextField();
                JTextField cardNumberTXT = new JTextField();
                JPanel paymentPanel = new JPanel(new java.awt.GridLayout(4, 2, 8, 8));

                paymentPanel.add(new JLabel("Payment Amount:"));
                paymentPanel.add(new JLabel(amount));
                paymentPanel.add(new JLabel("Payment Method:"));
                paymentPanel.add(paymentMethodCMB);
                paymentPanel.add(new JLabel("Cardholder Name:"));
                paymentPanel.add(cardNameTXT);
                paymentPanel.add(new JLabel("Card Number:"));
                paymentPanel.add(cardNumberTXT);

                // Card fields are only enabled for Debit and Credit payments.
                paymentMethodCMB.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String method = paymentMethodCMB.getSelectedItem().toString();
                        boolean cardPayment = method.equals("Debit") || method.equals("Credit");
                        cardNameTXT.setEnabled(cardPayment);
                        cardNumberTXT.setEnabled(cardPayment);

                        if (!cardPayment) {
                            cardNameTXT.setText("");
                            cardNumberTXT.setText("");
                        }
                    }
                });

                cardNameTXT.setEnabled(false);
                cardNumberTXT.setEnabled(false);

                int choice = JOptionPane.showConfirmDialog(servicesPanel, paymentPanel, "Payment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (choice == JOptionPane.OK_OPTION) {
                    String selectedMethod = paymentMethodCMB.getSelectedItem().toString();

                    boolean cardPayment = selectedMethod.equals("Debit") || selectedMethod.equals("Credit");
                    if (cardPayment) {
                        // Card payments require a cardholder name and a 12-19 digit number.
                        if (cardNameTXT.getText().trim().isEmpty() || cardNumberTXT.getText().trim().isEmpty()) {
                            JOptionPane.showMessageDialog(servicesPanel, "Please enter the cardholder name and card number.");
                            return;
                        }
                        if (!cardNumberTXT.getText().trim().matches("\\d{12,19}")) {
                            JOptionPane.showMessageDialog(servicesPanel, "Please enter a valid card number.");
                            return;
                        }
                    }

                    if (selectedMethod.equals("Debit")) {
                        selectedMethod = "Debit Card";
                    } else if (selectedMethod.equals("Credit")) {
                        selectedMethod = "Credit Card";
                    }

                    // Mark the bill as paid and save the payment transaction.
                    billModel.setValueAt("Paid", selectedRow, 6);

                    int receiptID = sharedData.nextReceiptID();
                    String paymentDate = new SimpleDateFormat("dd/MM/yyyy hh:mm a").format(new Date());
                    transModel.addRow(new Object[]{receiptID, billNumber, clientName, amount, selectedMethod, paymentDate, currentClientUsername});

                    updateBillingReviewText();

                    JOptionPane.showMessageDialog(
                            servicesPanel,
                            "PAYMENT RECEIPT\n\n"
                                    + "Receipt #: " + receiptID
                                    + "\nBill #: " + billNumber
                                    + "\nClient: " + clientName
                                    + "\nBill Type: " + billType
                                    + "\nDescription: " + description
                                    + "\nApproval Status: " + approvalStatus
                                    + "\nPayment Status: Paid"
                                    + "\nAmount Paid: " + amount
                                    + "\nPayment Method: " + selectedMethod
                                    + "\nPayment Date: " + paymentDate
                                    + "\n\nPayment successful!",
                            "Payment Receipt",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
        });

        // Build and show a full receipt from the selected transaction and its bill.
        billingViewRecieptBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = billingTransacRecordTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(servicesPanel, "Select a transaction first.");
                    return;
                }

                selectedRow = billingTransacRecordTable.convertRowIndexToModel(selectedRow);
                StringBuilder receipt = new StringBuilder();

                receipt.append("PAYMENT RECEIPT\n");
                receipt.append("========================================\n");
                receipt.append("Receipt #: ").append(transModel.getValueAt(selectedRow, 0)).append("\n");
                receipt.append("Transaction Date: ").append(transModel.getValueAt(selectedRow, 5)).append("\n");
                receipt.append("Client: ").append(transModel.getValueAt(selectedRow, 2)).append("\n");
                receipt.append("Amount Paid: ").append(transModel.getValueAt(selectedRow, 3)).append("\n");
                receipt.append("Payment Method: ").append(transModel.getValueAt(selectedRow, 4)).append("\n");
                receipt.append("Payment Status: Paid\n\n");

                int billNumber = Integer.parseInt(transModel.getValueAt(selectedRow, 1).toString());
                int billRow = -1;
                for (int i = 0; i < billModel.getRowCount(); i++) {
                    if (Integer.parseInt(billModel.getValueAt(i, 0).toString()) == billNumber) {
                        billRow = i;
                        break;
                    }
                }

                if (billRow >= 0) {
                    String billType = billModel.getValueAt(billRow, 2).toString();

                    receipt.append("BILL DETAILS\n");
                    receipt.append("----------------------------------------\n");
                    receipt.append("Bill #: ").append(billNumber).append("\n");
                    receipt.append("Bill Type: ").append(billType).append("\n");
                    receipt.append("Bill Amount: ").append(billModel.getValueAt(billRow, 4)).append("\n");
                    receipt.append("Approval Status: ").append(billModel.getValueAt(billRow, 5)).append("\n");
                    receipt.append("Bill Date: ").append(billModel.getValueAt(billRow, 7)).append("\n\n");

                    if (billType.equals("Service")) {
                        receipt.append("SERVICE DETAILS\n");
                        receipt.append("----------------------------------------\n");
                        receipt.append("Description: ").append(billModel.getValueAt(billRow, 3)).append("\n");

                        Integer appointmentID = sharedData.getAppointmentIDForBill(billNumber);
                        if (appointmentID != null) {
                            for (int i = 0; i < appointmentModel.getRowCount(); i++) {
                                if (Integer.parseInt(appointmentModel.getValueAt(i, 0).toString()) == appointmentID) {
                                    receipt.append("Service Type: ").append(appointmentModel.getValueAt(i, 2)).append("\n");
                                    receipt.append("Vehicle: ").append(appointmentModel.getValueAt(i, 3)).append("\n");
                                    receipt.append("Service Detail: ").append(appointmentModel.getValueAt(i, 4)).append("\n");
                                    receipt.append("Schedule: ").append(appointmentModel.getValueAt(i, 5)).append("\n");
                                    receipt.append("Appointment Status: ").append(appointmentModel.getValueAt(i, 6)).append("\n");
                                    receipt.append("Service Status: ").append(appointmentModel.getValueAt(i, 7)).append("\n");
                                    receipt.append("Service Estimate: ").append(appointmentModel.getValueAt(i, 8)).append("\n");
                                    break;
                                }
                            }
                        }
                    } else {
                        String details = billModel.getValueAt(billRow, 3).toString();

                        receipt.append("PRODUCT DETAILS\n");
                        receipt.append("----------------------------------------\n");

                        if (details.startsWith("Product Order: ")) {
                            details = details.substring("Product Order: ".length());
                        }

                        if (details.equals("Product Order") || details.trim().isEmpty()) {
                            receipt.append("No itemized product details available.\n");
                        } else {
                            for (String item : details.split("; ")) {
                                receipt.append("- ").append(item).append("\n");
                            }
                        }
                    }
                }

                receipt.append("\nThank you for your payment.\n");
                JOptionPane.showMessageDialog(
                        servicesPanel,
                        receipt.toString(),
                        "Receipt",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        // Refresh the review area whenever a bill or transaction selection changes.
        ListSelectionListener reviewListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateBillingReviewText();
                }
            }
        };
        billingMyBillsTable.getSelectionModel().addListSelectionListener(reviewListener);
        billingTransacRecordTable.getSelectionModel().addListSelectionListener(reviewListener);

        updateBillingReviewText();

        // Ask for confirmation before clearing the session and returning to login.
        billingLogOutBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = JOptionPane.showConfirmDialog(servicesPanel, "Are you sure you want to log out?", "Confirm Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (choice == JOptionPane.YES_OPTION) {
                    JFrame servicesFrame = (JFrame) SwingUtilities.getWindowAncestor(servicesPanel);
                    SessionUser.logout();
                    AutomotiveServiceLogin.showLoginWindow("AutoCare Pro - Login");

                    if (servicesFrame != null) {
                        servicesFrame.dispose();
                    }
                }
            }
        });
    }

    /**
     * Rebuilds the billing review text area from the selected bill/transaction,
     * or shows a summary of all of the client's records when nothing is selected.
     */
    private void updateBillingReviewText() {
        if (billingTXTArea == null) {
            return;
        }

        double balance = calculateOutstandingBalance();
        StringBuilder text = new StringBuilder("BILLING REVIEW\n");
        text.append("========================================\n");
        text.append("Client: ").append(currentClientName).append("\n");
        text.append("Username: ").append(currentClientUsername).append("\n");
        text.append("Outstanding Balance: ").append(String.format("₱%.2f", balance)).append("\n\n");

        int billRow = billingMyBillsTable.getSelectedRow();
        if (billRow >= 0) {
            billRow = billingMyBillsTable.convertRowIndexToModel(billRow);
            text.append("SELECTED BILL\n");
            text.append("----------------------------------------\n");
            text.append("Bill #: ").append(billModel.getValueAt(billRow, 0)).append("\n");
            text.append("Type: ").append(billModel.getValueAt(billRow, 2)).append("\n");
            text.append("Description: ").append(billModel.getValueAt(billRow, 3)).append("\n");
            text.append("Amount: ").append(billModel.getValueAt(billRow, 4)).append("\n");
            text.append("Approval: ").append(billModel.getValueAt(billRow, 5)).append("\n");
            text.append("Payment: ").append(billModel.getValueAt(billRow, 6)).append("\n");
            text.append("Date: ").append(billModel.getValueAt(billRow, 7)).append("\n\n");
        } else {
            text.append("BILLS\n");
            text.append("----------------------------------------\n");

            boolean hasBill = false;
            for (int i = 0; i < billModel.getRowCount(); i++) {
                if (currentClientUsername.equalsIgnoreCase(billModel.getValueAt(i, 8).toString())) {
                    text.append("Bill #").append(billModel.getValueAt(i, 0))
                            .append(" | ").append(billModel.getValueAt(i, 2))
                            .append(" | ").append(billModel.getValueAt(i, 4)).append("\n");
                    text.append("  ").append(billModel.getValueAt(i, 3)).append("\n");
                    text.append("  Approval: ").append(billModel.getValueAt(i, 5))
                            .append(" | Payment: ").append(billModel.getValueAt(i, 6)).append("\n");
                    hasBill = true;
                }
            }

            if (!hasBill) {
                text.append("No bills found.\n");
            }
            text.append("\n");
        }

        int transactionRow = billingTransacRecordTable.getSelectedRow();
        if (transactionRow >= 0) {
            transactionRow = billingTransacRecordTable.convertRowIndexToModel(transactionRow);
            text.append("SELECTED TRANSACTION\n");
            text.append("----------------------------------------\n");
            text.append("Receipt #: ").append(transModel.getValueAt(transactionRow, 0)).append("\n");
            text.append("Bill #: ").append(transModel.getValueAt(transactionRow, 1)).append("\n");
            text.append("Amount: ").append(transModel.getValueAt(transactionRow, 3)).append("\n");
            text.append("Method: ").append(transModel.getValueAt(transactionRow, 4)).append("\n");
            text.append("Date: ").append(transModel.getValueAt(transactionRow, 5)).append("\n");
        } else {
            text.append("TRANSACTIONS\n");
            text.append("----------------------------------------\n");

            boolean hasTransaction = false;
            for (int i = 0; i < transModel.getRowCount(); i++) {
                if (currentClientUsername.equalsIgnoreCase(transModel.getValueAt(i, 6).toString())) {
                    text.append("Receipt #").append(transModel.getValueAt(i, 0))
                            .append(" | Bill #").append(transModel.getValueAt(i, 1))
                            .append(" | ").append(transModel.getValueAt(i, 3)).append("\n");
                    text.append("  Method: ").append(transModel.getValueAt(i, 4))
                            .append(" | Date: ").append(transModel.getValueAt(i, 5)).append("\n");
                    hasTransaction = true;
                }
            }

            if (!hasTransaction) {
                text.append("No transactions found.\n");
            }
        }

        billingOutstandingBalanceTXT.setText(String.format("₱%.2f", balance));
        billingTXTArea.setText(text.toString());
        billingTXTArea.setCaretPosition(0);
    }

    /** Adds all unpaid bills that belong to the logged-in client. */
    private double calculateOutstandingBalance() {
        double balance = 0;

        for (int i = 0; i < billModel.getRowCount(); i++) {
            if (currentClientUsername.equalsIgnoreCase(billModel.getValueAt(i, 8).toString())
                    && "Unpaid".equals(billModel.getValueAt(i, 6).toString())) {
                balance += Double.parseDouble(billModel.getValueAt(i, 4).toString().replace("₱", "").replace(",", "").trim());
            }
        }

        return balance;
    }

    /** Returns the client dashboard panel so it can be placed inside a JFrame. */
    public JPanel getServicesPanel() {
        return servicesPanel;
    }

    /** Hides a model column from a JTable while keeping the data in the model. */
    private void hideTableColumn(JTable table, int modelColumn) {
        int viewColumn = table.convertColumnIndexToView(modelColumn);
        if (viewColumn >= 0) {
            table.removeColumn(table.getColumnModel().getColumn(viewColumn));
        }
    }

    /** Filters a table so only rows for the current client are visible. */
    private void filterByClient(JTable table, DefaultTableModel model, int clientColumn) {
        final String client = currentClientUsername;
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                return client.equalsIgnoreCase(entry.getStringValue(clientColumn));
            }
        });
        table.setRowSorter(sorter);
    }

}