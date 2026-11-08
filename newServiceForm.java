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

public class newServiceForm {
    private DefaultTableModel appointmentModel;
    private DefaultTableModel productModel;
    private DefaultTableModel cartModel;
    private DefaultTableModel billModel;
    private DefaultTableModel transModel;

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

    private final AutomotiveData sharedData = AutomotiveData.getInstance();
    private final String currentClientUsername = SessionUser.getUsername();
    private final String currentClientName = SessionUser.getDisplayName();


    public newServiceForm() {
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
        hideTableColumn(appointmentTable, 9);
        hideTableColumn(billingMyBillsTable, 8);
        hideTableColumn(billingTransacRecordTable, 6);

        final String clientFilter = currentClientUsername;
        TableRowSorter<DefaultTableModel> appointmentSorter = new TableRowSorter<>(appointmentModel);
        appointmentSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                return clientFilter.equalsIgnoreCase(entry.getStringValue(9));
            }
        });
        appointmentTable.setRowSorter(appointmentSorter);

        TableRowSorter<DefaultTableModel> billSorter = new TableRowSorter<>(billModel);
        billSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                return clientFilter.equalsIgnoreCase(entry.getStringValue(8));
            }
        });
        billingMyBillsTable.setRowSorter(billSorter);

        TableRowSorter<DefaultTableModel> transactionSorter = new TableRowSorter<>(transModel);
        transactionSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                return clientFilter.equalsIgnoreCase(entry.getStringValue(6));
            }
        });
        billingTransacRecordTable.setRowSorter(transactionSorter);

        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        appointmentDateSPN.setModel(dateModel);
        appointmentDateSPN.setEditor(new JSpinner.DateEditor(appointmentDateSPN, "dd/MM/yyyy"));

        productQuantitySPN.setModel(new SpinnerNumberModel(1, 1, 100, 1));
        productTotalTXT.setText("₱0.00");
        productTotalTXT.setEditable(false);
        billingOutstandingBalanceTXT.setEditable(false);

        Font reviewFont = new Font("Monospaced", Font.PLAIN, 12);
        Insets reviewMargin = new Insets(8, 8, 8, 8);
        billingTXTArea.setEditable(false);
        billingTXTArea.setLineWrap(true);
        billingTXTArea.setWrapStyleWord(true);
        billingTXTArea.setFont(reviewFont);
        billingTXTArea.setMargin(reviewMargin);

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

        ButtonGroup serviceTypeGroup = new ButtonGroup();
        serviceTypeGroup.add(repairServicesRadioButton);
        serviceTypeGroup.add(installationServicesRadioButton);

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

        appointmentSubmitAppBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String serviceName = repairServicesRadioButton.isSelected() ? "Repair Services" : "Installation Services";
                String vehicle = appointmentVehicleTXT.getText().trim();
                String problem = (String) appointmentProblemCMB.getSelectedItem();
                String time = appointmentTimeSPN.getValue().toString();

                if (serviceName == null || problem == null || vehicle.isEmpty()) {
                    JOptionPane.showMessageDialog(servicesPanel, "Please complete all appointment details.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                AutomotiveService service;
                if (serviceName.equals("Repair Services")) {
                    service = new RepairService();
                } else if (serviceName.equals("Installation Services")) {
                    service = new InstallationService();
                } else {
                    JOptionPane.showMessageDialog(servicesPanel, "Please select a valid service.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                AppointmentDetails appointment = new AppointmentDetails(service, vehicle, problem);
                if (!appointment.validateServiceRequest().equals("Valid Appointment")) {
                    JOptionPane.showMessageDialog(servicesPanel, appointment.validateServiceRequest(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String date = new SimpleDateFormat("dd/MM/yyyy").format((Date) appointmentDateSPN.getValue());
                String schedule = date + " " + time;
                double estimate = appointment.getEstimatedPrice();
                int appointmentID = sharedData.nextAppointmentID();

                appointmentModel.addRow(new Object[]{
                        appointmentID, currentClientName, appointment.getServiceName(), appointment.getVehicleInfo(),
                        appointment.getServiceDetail(), schedule, "Pending", "Not Started", String.format("₱%.2f", estimate), currentClientUsername
                });

                int billID = sharedData.nextBillID();
                String amount = String.format("₱%.2f", estimate);
                String billDate = new SimpleDateFormat("dd/MM/yyyy hh:mm a").format(new Date());

                billModel.addRow(new Object[]{
                        billID, currentClientName, "Service", serviceName + " - " + problem + " (" + vehicle + ")",
                        amount, "Pending", "Unpaid", billDate, currentClientUsername
                });

                sharedData.linkServiceBill(billID, appointmentID);

                double balance = calculateOutstandingBalance();

                billingOutstandingBalanceTXT.setText(String.format("₱%.2f", balance));
                updateBillingReviewText();
                JOptionPane.showMessageDialog(servicesPanel, "Appointment submitted successfully!\nPlease wait for admin approval before payment.");
            }
        });

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

                OrderCalculator calculator = new OrderCalculator();
                calculator.computeOrder(price, quantity);
                cartModel.addRow(new Object[]{id, name, quantity, String.format("%.2f", calculator.getTotalAmount())});
                productModel.setValueAt(stock - quantity, selectedRow, 4);
                productTotalTXT.setText(String.format("₱%.2f", calculator.calculateCartTotal(cartModel)));
                productQuantitySPN.setValue(1);
            }
        });

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
                OrderCalculator calculator = new OrderCalculator();
                productTotalTXT.setText(String.format("₱%.2f", calculator.calculateCartTotal(cartModel)));
            }
        });

        productConfirmProdOrderBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cartModel.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(servicesPanel, "Your cart is empty.");
                    return;
                }

                OrderCalculator calculator = new OrderCalculator();
                double total = calculator.calculateCartTotal(cartModel);
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

                billModel.addRow(new Object[]{
                        billID, currentClientName, "Product", productDetails.toString(), String.format("₱%.2f", total),
                        "Not Required", "Unpaid", new SimpleDateFormat("dd/MM/yyyy hh:mm a").format(new Date()), currentClientUsername
                });

                cartModel.setRowCount(0);
                productTotalTXT.setText("₱0.00");

                double balance = calculateOutstandingBalance();

                billingOutstandingBalanceTXT.setText(String.format("₱%.2f", balance));
                updateBillingReviewText();
                JOptionPane.showMessageDialog(servicesPanel, "Product order confirmed!\nYou can pay for this product order immediately.");
            }
        });

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
                    OrderCalculator calculator = new OrderCalculator();
                    String selectedMethod = paymentMethodCMB.getSelectedItem().toString();

                    if (!calculator.validatePayment(selectedMethod, cardNameTXT.getText(), cardNumberTXT.getText())) {
                        if (selectedMethod.equals("Debit") || selectedMethod.equals("Credit")) {
                            if (cardNameTXT.getText().trim().isEmpty() || cardNumberTXT.getText().trim().isEmpty()) {
                                JOptionPane.showMessageDialog(servicesPanel, "Please enter the cardholder name and card number.");
                            } else {
                                JOptionPane.showMessageDialog(servicesPanel, "Please enter a valid card number.");
                            }
                        }
                        return;
                    }

                    if (selectedMethod.equals("Debit")) {
                        selectedMethod = "Debit Card";
                    } else if (selectedMethod.equals("Credit")) {
                        selectedMethod = "Credit Card";
                    }

                    billModel.setValueAt("Paid", selectedRow, 6);

                    int receiptID = sharedData.nextReceiptID();
                    String paymentDate = new SimpleDateFormat("dd/MM/yyyy hh:mm a").format(new Date());
                    transModel.addRow(new Object[]{receiptID, billNumber, clientName, amount, selectedMethod, paymentDate, currentClientUsername});

                    double balance = calculateOutstandingBalance();

                    billingOutstandingBalanceTXT.setText(String.format("₱%.2f", balance));
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

        billingMyBillsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateBillingReviewText();
                }
            }
        });

        billingTransacRecordTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateBillingReviewText();
                }
            }
        });

        updateBillingReviewText();

        billingLogOutBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = JOptionPane.showConfirmDialog(servicesPanel, "Are you sure you want to log out?", "Confirm Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (choice == JOptionPane.YES_OPTION) {
                    JFrame servicesFrame = (JFrame) SwingUtilities.getWindowAncestor(servicesPanel);
                    SessionUser.logout();
                    AutomotiveServiceLogin login = new AutomotiveServiceLogin();
                    JFrame loginFrame = new JFrame("AutoCare Pro - Login");
                    loginFrame.setContentPane(login.getMainPanel());
                    loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    loginFrame.pack();
                    loginFrame.setLocationRelativeTo(null);
                    loginFrame.setVisible(true);
                    if (servicesFrame != null) {
                        servicesFrame.dispose();
                    }
                }
            }
        });
    }

    private void updateBillingReviewText() {
        if (billingTXTArea == null) {
            return;
        }

        StringBuilder text = new StringBuilder("BILLING REVIEW\n");
        text.append("========================================\n");
        text.append("Client: ").append(currentClientName).append("\n");
        text.append("Username: ").append(currentClientUsername).append("\n");
        text.append("Outstanding Balance: ").append(String.format("₱%.2f", calculateOutstandingBalance())).append("\n\n");

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

        billingTXTArea.setText(text.toString());
        billingTXTArea.setCaretPosition(0);
    }

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

    public JPanel getServicesPanel() {
        return servicesPanel;
    }

    private void hideTableColumn(JTable table, int modelColumn) {
        int viewColumn = table.convertColumnIndexToView(modelColumn);
        if (viewColumn >= 0) {
            table.removeColumn(table.getColumnModel().getColumn(viewColumn));
        }
    }

}