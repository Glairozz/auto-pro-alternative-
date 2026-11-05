import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Client screen.
 *
 * All appointment, product, bill and transaction data comes from the shared
 * AutomotiveData instance. The current username is used to filter the client's
 * own appointments, bills and transactions and to select their own cart.
 */
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

    private AutomotiveData sharedData;
    private String currentClientName;
    private String currentClientUsername;

    public Services(AutomotiveData sharedData) {
        this(sharedData, null);
    }

    // Binds this screen to the application-wide data store. The username is
    // the client's unique identity; display names are never used as keys.
    public Services(AutomotiveData sharedData, String clientUsername) {
        if (sharedData == null) {
            throw new IllegalArgumentException("AutomotiveData is required.");
        }

        this.sharedData = sharedData;
        currentClientUsername = clientUsername == null ? "guest" : clientUsername.trim();

        if (currentClientUsername.isEmpty()) {
            currentClientUsername = "guest";
        }

        currentClientName = sharedData.getClientName(currentClientUsername);

        if (currentClientName == null || currentClientName.trim().isEmpty()) {
            currentClientName = currentClientUsername;
        }

        // These are references to the central models, not separate copies.
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

        // Match only records whose hidden username column belongs to this
        // client. This prevents one client from seeing another client's data.
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
        billingOutstandingBalanceTXT.setText(String.format("₱%.2f", sharedData.getOutstandingBalance(currentClientUsername)));

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

        // Creates the appointment and its service bill together through the
        // centralized data class behind one duplicate-click guard.
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

                // A repeated click for the same pending request is rejected.
                if (sharedData.hasDuplicatePendingAppointment(currentClientUsername, serviceName, vehicle, problem, schedule)) {
                    JOptionPane.showMessageDialog(servicesPanel, "This appointment request was already submitted.", "Duplicate Appointment", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // AutomotiveData generates the IDs, inserts both records and
                // links the new service bill to this appointment.
                int appointmentID = sharedData.createAppointment(
                        currentClientUsername,
                        currentClientName,
                        serviceName,
                        vehicle,
                        problem,
                        schedule,
                        appointment.getEstimatedPrice()
                );

                if (appointmentID == -1) {
                    JOptionPane.showMessageDialog(servicesPanel, "The appointment could not be submitted.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                billingOutstandingBalanceTXT.setText(String.format("₱%.2f", sharedData.getOutstandingBalance(currentClientUsername)));
                JOptionPane.showMessageDialog(servicesPanel, "Appointment submitted successfully!\nPlease wait for admin approval before payment.");
            }
        });

        // Cart operations are performed by AutomotiveData so each client's
        // cart, stock changes and totals stay consistent.
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
                int productID = Integer.parseInt(
                        productModel.getValueAt(selectedRow, 0).toString()
                );

                String error = sharedData.addProductToCart(currentClientUsername, productID, quantity);

                if (error != null) {
                    JOptionPane.showMessageDialog(servicesPanel, error);
                    return;
                }

                productTotalTXT.setText(String.format("₱%.2f", sharedData.getCartTotal(currentClientUsername)));
                productQuantitySPN.setValue(1);
            }
        });

        // Removing an item restores its quantity to the shared product stock.
        productRemoveBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table3.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(servicesPanel, "Select an item in the cart.");
                    return;
                }

                selectedRow = table3.convertRowIndexToModel(selectedRow);

                if (!sharedData.removeProductFromCart(currentClientUsername, selectedRow)) {
                    JOptionPane.showMessageDialog(servicesPanel, "The cart item could not be removed.");
                    return;
                }

                productTotalTXT.setText(String.format("₱%.2f", sharedData.getCartTotal(currentClientUsername)));
            }
        });

        // Converts the selected client's cart into one product bill. Product
        // bills do not need appointment approval.
        productConfirmProdOrderBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cartModel.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(servicesPanel, "Your cart is empty.");
                    return;
                }

                double total = sharedData.getCartTotal(currentClientUsername);
                int billID = sharedData.createProductBill(
                        currentClientUsername,
                        "Product Order",
                        total
                );

                if (billID == -1) {
                    JOptionPane.showMessageDialog(servicesPanel, "The product order could not be created.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                sharedData.clearCart(currentClientUsername);
                productTotalTXT.setText("₱0.00");
                billingOutstandingBalanceTXT.setText(String.format("₱%.2f", sharedData.getOutstandingBalance(currentClientUsername)));
                JOptionPane.showMessageDialog(servicesPanel, "Product order confirmed!\nYou can pay for this product order immediately.");
            }
        });

        // Payment review uses the central validation method. The same rules are
        // checked again inside payBill before any record is changed.
        billingReviewBillBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = billingMyBillsTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(servicesPanel, "Select a bill first.");
                    return;
                }

                selectedRow = billingMyBillsTable.convertRowIndexToModel(selectedRow);

                int billNumber = Integer.parseInt(
                        billModel.getValueAt(selectedRow, 0).toString()
                );
                String billNumberText = String.valueOf(billNumber);
                String clientName = billModel.getValueAt(selectedRow, 1).toString();
                String billType = billModel.getValueAt(selectedRow, 2).toString();
                String description = billModel.getValueAt(selectedRow, 3).toString();
                String amount = billModel.getValueAt(selectedRow, 4).toString();

                if (!sharedData.isBillOwnedByClient(billNumber, currentClientUsername)) {
                    JOptionPane.showMessageDialog(servicesPanel, "You can only pay your own bills.");
                    return;
                }

                if (sharedData.isBillPaid(billNumber)) {
                    JOptionPane.showMessageDialog(servicesPanel, "This bill has already been paid.");
                    return;
                }

                // Returns null for a payable bill. Service bills require an
                // approved appointment and completed work; product bills do not.
                String paymentBlockReason = sharedData.getPaymentBlockReason(billNumber, currentClientUsername);

                if (paymentBlockReason != null) {
                    JOptionPane.showMessageDialog(
                            servicesPanel,
                            "Payment is not available yet.\n\n" + paymentBlockReason,
                            "Payment Not Available",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                String approvalStatus = sharedData.getBillApprovalStatus(billNumber);
                String serviceStatus = "Not Applicable";

                if ("Service".equals(billType)) {
                    Integer appointmentID = sharedData.getAppointmentIDForBill(billNumber);

                    if (appointmentID == null) {
                        JOptionPane.showMessageDialog(servicesPanel, "The appointment record could not be found.");
                        return;
                    }

                    String currentServiceStatus = sharedData.getServiceStatus(appointmentID);

                    if (currentServiceStatus == null) {
                        JOptionPane.showMessageDialog(servicesPanel, "The appointment record could not be found.");
                        return;
                    }

                    serviceStatus = currentServiceStatus;
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

                    // AutomotiveData marks the bill paid and creates the
                    // transaction/receipt row in one validated operation.
                    int receiptID = sharedData.payBill(billNumber, currentClientUsername, selectedMethod);

                    if (receiptID <= 0) {
                        String reason = sharedData.getLastPaymentError();
                        JOptionPane.showMessageDialog(
                                servicesPanel,
                                reason == null ? "The payment could not be completed." : reason,
                                "Payment Failed",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }

                    billingOutstandingBalanceTXT.setText(String.format("₱%.2f", sharedData.getOutstandingBalance(currentClientUsername)));

                    JOptionPane.showMessageDialog(
                            servicesPanel,
                            "PAYMENT RECEIPT\n\n"
                                    + "Receipt #: " + receiptID
                                    + "\nBill #: " + billNumberText
                                    + "\nClient: " + clientName
                                    + "\nBill Type: " + billType
                                    + "\nDescription: " + description
                                    + "\nApproval Status: " + approvalStatus
                                    + "\nService Status: " + serviceStatus
                                    + "\nPayment Status: Paid"
                                    + "\nAmount Paid: " + amount
                                    + "\nPayment Method: " + selectedMethod
                                    + "\nPayment Date: " + new SimpleDateFormat("dd/MM/yyyy hh:mm a").format(new Date())
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

        // Logging out keeps the shared data alive for the next login.
        billingLogOutBT.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = JOptionPane.showConfirmDialog(servicesPanel, "Are you sure you want to log out?", "Confirm Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (choice == JOptionPane.YES_OPTION) {
                    JFrame servicesFrame = (JFrame) SwingUtilities.getWindowAncestor(servicesPanel);
                    AutomotivePasswords session = new AutomotivePasswords(sharedData);
                    AutomotiveServiceLogin login = new AutomotiveServiceLogin(session);
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

    // Hides the ownership username column while keeping it in the model for
    // filtering and record matching.
    private void hideTableColumn(JTable table, int modelColumn) {
        int viewColumn = table.convertColumnIndexToView(modelColumn);
        if (viewColumn >= 0) {
            table.removeColumn(table.getColumnModel().getColumn(viewColumn));
        }
    }

}
