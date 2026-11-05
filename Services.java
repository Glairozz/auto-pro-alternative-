import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Services {
    private DefaultTableModel appointmentModel;
    private DefaultTableModel productModel;
    private DefaultTableModel cartModel;
    private DefaultTableModel billModel;
    private DefaultTableModel transModel;

    private JComboBox<String> appointmentServiceCMB;
    private JTextField appointmentVehicleTXT;
    private JTable table1;
    private JTextField appointmentTimeTXT;
    private JSpinner appointmentDateSPN;
    private JButton appointmentSubmitAppBT;
    private JComboBox<String> appointmentProblemCMB;
    private JSpinner appointmentTimeSPN;

    private JSpinner productQuantitySPN;
    private JButton productAddToCartBT;
    private JTable table2;
    private JTable table3;
    private JTextField productSearchTXT;
    private JTextField productTotalTXT;
    private JButton productRemoveBT;
    private JButton productConfirmProdOrderBT;

    private JTextField billingOutstandingBalanceTXT;
    private JTable billingMyBillsTable;
    private JComboBox<String> billingPaymentMethodCMB;
    private JTable billingTransacRecordTable;
    private JButton billingViewRecieptBT;
    private JButton billingReviewBillBT;

    private JTabbedPane servicesPanel;
    private JButton billingLogOutBT;
    private JScrollPane appointmentTable;
    private JScrollPane productProductTable;
    private JScrollPane productCartTable;

    private AutomotivePasswords sharedCred;
    private AutomotiveData sharedData;
    private String currentClientName;
    private String currentClientUsername;

    public Services() {
        this(new AutomotivePasswords());
    }

    public Services(AutomotivePasswords sharedCred) {
        this(sharedCred, sharedCred.getAutomotiveData());
    }

    public Services(AutomotivePasswords sharedCred, AutomotiveData sharedData) {
        this.sharedCred = sharedCred;
        this.sharedData = sharedData;
        currentClientUsername = sharedCred.getClientUsername();
        if (currentClientUsername == null || currentClientUsername.trim().isEmpty()) {
            currentClientUsername = "guest";
        }
        currentClientName = sharedCred.getClientName();
        if (currentClientName == null || currentClientName.trim().isEmpty()) {
            currentClientName = currentClientUsername;
        }

        appointmentModel = sharedData.getAppointmentModel();
        productModel = sharedData.getProductModel();
        cartModel = sharedData.getCartModel(currentClientUsername);
        billModel = sharedData.getBillModel();
        transModel = sharedData.getTransactionModel();

        table1.setModel(appointmentModel);
        table2.setModel(productModel);
        table3.setModel(cartModel);
        billingMyBillsTable.setModel(billModel);
        billingTransacRecordTable.setModel(transModel);
        hideTableColumn(table1, 9);
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
        table1.setRowSorter(appointmentSorter);

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

        appointmentServiceCMB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedService = (String) appointmentServiceCMB.getSelectedItem();

                if (selectedService == null) {
                    appointmentProblemCMB.setModel(new DefaultComboBoxModel<String>());
                } else if (selectedService.equals("Repair Services")) {
                    RepairService repairService = new RepairService();
                    appointmentProblemCMB.setModel(new DefaultComboBoxModel<String>(repairService.getServiceDetails()));
                } else if (selectedService.equals("Installation Services")) {
                    InstallationService installationService = new InstallationService();
                    appointmentProblemCMB.setModel(new DefaultComboBoxModel<String>(installationService.getServiceDetails()));
                } else {
                    appointmentProblemCMB.setModel(new DefaultComboBoxModel<String>());
                }
            }
        });

        if (appointmentServiceCMB.getSelectedItem() != null) {
            if (appointmentServiceCMB.getSelectedItem().equals("Repair Services")) {
                RepairService repairService = new RepairService();
                appointmentProblemCMB.setModel(new DefaultComboBoxModel<String>(repairService.getServiceDetails()));
            } else if (appointmentServiceCMB.getSelectedItem().equals("Installation Services")) {
                InstallationService installationService = new InstallationService();
                appointmentProblemCMB.setModel(new DefaultComboBoxModel<String>(installationService.getServiceDetails()));
            }
        }

        appointmentSubmitAppBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String serviceName = (String) appointmentServiceCMB.getSelectedItem();
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
                        appointment.getServiceDetail(), schedule, appointment.getDefaultAppointmentStatus(),
                        appointment.getDefaultServiceStatus(), String.format("₱%.2f", estimate), currentClientUsername
                });

                int billID = sharedData.nextBillID();
                String amount = String.format("₱%.2f", estimate);
                String billDate = new SimpleDateFormat("dd/MM/yyyy hh:mm a").format(new Date());

                billModel.addRow(new Object[]{
                        billID, currentClientName, "Service", serviceName + " - " + problem + " (" + vehicle + ")",
                        amount, "Pending", "Unpaid", billDate, currentClientUsername
                });

                sharedData.linkServiceBill(billID, appointmentID);

                double balance = 0;
                for (int i = 0; i < billModel.getRowCount(); i++) {
                    if (billModel.getValueAt(i, 8).toString().equalsIgnoreCase(currentClientUsername)
                            && billModel.getValueAt(i, 6).toString().equals("Unpaid")) {
                        String billAmount = billModel.getValueAt(i, 4).toString().replace("₱", "").replace(",", "");
                        balance += Double.parseDouble(billAmount);
                    }
                }

                billingOutstandingBalanceTXT.setText(String.format("₱%.2f", balance));
                JOptionPane.showMessageDialog(servicesPanel, "Appointment submitted successfully!\nPlease wait for admin approval before payment.");
            }
        });

        productAddToCartBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table2.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(servicesPanel, "Select a product first.");
                    return;
                }

                selectedRow = table2.convertRowIndexToModel(selectedRow);
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

                billModel.addRow(new Object[]{
                        billID, currentClientName, "Product", "Product Order", String.format("₱%.2f", total),
                        "Not Required", "Unpaid", new SimpleDateFormat("dd/MM/yyyy hh:mm a").format(new Date()), currentClientUsername
                });

                cartModel.setRowCount(0);
                productTotalTXT.setText("₱0.00");

                double balance = 0;
                for (int i = 0; i < billModel.getRowCount(); i++) {
                    if (billModel.getValueAt(i, 8).toString().equalsIgnoreCase(currentClientUsername)
                            && billModel.getValueAt(i, 6).toString().equals("Unpaid")) {
                        String amount = billModel.getValueAt(i, 4).toString().replace("₱", "").replace(",", "");
                        balance += Double.parseDouble(amount);
                    }
                }

                billingOutstandingBalanceTXT.setText(String.format("₱%.2f", balance));
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
                String billNumber = billModel.getValueAt(selectedRow, 0).toString();
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

                String serviceStatus = "Not Applicable";

                if (billType.equals("Service")) {
                    Integer appointmentID = sharedData.getAppointmentIDForBill(Integer.parseInt(billNumber));

                    if (appointmentID == null) {
                        JOptionPane.showMessageDialog(servicesPanel, "The appointment record could not be found.");
                        return;
                    }

                    String currentAppointmentStatus = sharedData.getAppointmentStatus(appointmentID);
                    String currentServiceStatus = sharedData.getServiceStatus(appointmentID);

                    if (currentAppointmentStatus == null || currentServiceStatus == null) {
                        JOptionPane.showMessageDialog(servicesPanel, "The appointment record could not be found.");
                        return;
                    }

                    approvalStatus = currentAppointmentStatus;
                    serviceStatus = currentServiceStatus;
                    billModel.setValueAt(approvalStatus, selectedRow, 5);

                    String paymentBlockReason = sharedData.getPaymentBlockReason(Integer.parseInt(billNumber));

                    if (paymentBlockReason != null) {
                        JOptionPane.showMessageDialog(
                                servicesPanel,
                                "Payment is not available yet.\n\n" + paymentBlockReason,
                                "Payment Not Available",
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

                    billingPaymentMethodCMB.setSelectedItem(selectedMethod);
                    billModel.setValueAt("Paid", selectedRow, 6);

                    int receiptID = sharedData.nextReceiptID();
                    String paymentDate = new SimpleDateFormat("dd/MM/yyyy hh:mm a").format(new Date());
                    transModel.addRow(new Object[]{receiptID, billNumber, clientName, amount, selectedMethod, paymentDate, currentClientUsername});

                    double balance = 0;
                    for (int i = 0; i < billModel.getRowCount(); i++) {
                        if (billModel.getValueAt(i, 8).toString().equalsIgnoreCase(currentClientUsername)
                            && billModel.getValueAt(i, 6).toString().equals("Unpaid")) {
                            String unpaidAmount = billModel.getValueAt(i, 4).toString().replace("₱", "").replace(",", "");
                            balance += Double.parseDouble(unpaidAmount);
                        }
                    }

                    billingOutstandingBalanceTXT.setText(String.format("₱%.2f", balance));

                    JOptionPane.showMessageDialog(
                            servicesPanel,
                            "PAYMENT RECEIPT\n\n"
                                    + "Receipt #: " + receiptID
                                    + "\nBill #: " + billNumber
                                    + "\nClient: " + clientName
                                    + "\nBill Type: " + billType
                                    + "\nDescription: " + description
                                    + "\nApproval Status: " + approvalStatus
                                    + "\nService Status: " + serviceStatus
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
                JOptionPane.showMessageDialog(
                        servicesPanel,
                        "RECEIPT\n"
                                + "Receipt #: " + transModel.getValueAt(selectedRow, 0)
                                + "\nBill #: " + transModel.getValueAt(selectedRow, 1)
                                + "\nClient: " + transModel.getValueAt(selectedRow, 2)
                                + "\nAmount: " + transModel.getValueAt(selectedRow, 3)
                                + "\nMethod: " + transModel.getValueAt(selectedRow, 4)
                                + "\nDate: " + transModel.getValueAt(selectedRow, 5),
                        "Receipt",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        productSearchTXT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String search = productSearchTXT.getText().trim().toLowerCase();
                if (search.isEmpty()) {
                    table2.clearSelection();
                    return;
                }

                for (int i = 0; i < productModel.getRowCount(); i++) {
                    String name = productModel.getValueAt(i, 1).toString().toLowerCase();
                    String category = productModel.getValueAt(i, 2).toString().toLowerCase();

                    if (name.contains(search) || category.contains(search)) {
                        int viewRow = table2.convertRowIndexToView(i);
                        table2.setRowSelectionInterval(viewRow, viewRow);
                        table2.scrollRectToVisible(table2.getCellRect(viewRow, 0, true));
                        return;
                    }
                }

                table2.clearSelection();
                JOptionPane.showMessageDialog(servicesPanel, "Product not found.");
            }
        });

        billingLogOutBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = JOptionPane.showConfirmDialog(servicesPanel, "Are you sure you want to log out?", "Confirm Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (choice == JOptionPane.YES_OPTION) {
                    JFrame servicesFrame = (JFrame) SwingUtilities.getWindowAncestor(servicesPanel);
                    AutomotiveServiceLogin login = new AutomotiveServiceLogin(sharedCred);
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

    public JTabbedPane getServicesPanel() {
        return servicesPanel;
    }

    private void hideTableColumn(JTable table, int modelColumn) {
        int viewColumn = table.convertColumnIndexToView(modelColumn);
        if (viewColumn >= 0) {
            table.removeColumn(table.getColumnModel().getColumn(viewColumn));
        }
    }

}
