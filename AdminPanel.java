import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class AdminPanel {
    private JTabbedPane tabbedPane1;
    private JButton btnLogOut;
    private JTable tblClientAppointmentMastList;
    private JButton btnConfirmApp;
    private JButton btnCancelAppointment;
    private JButton btnInspectEstimate;
    private JTable tblProdMasterList;
    private JScrollPane scrollPane;
    private JComboBox cmbCashPayment;
    private JButton btnRecordPayment;
    private JButton btnRefreshBillHistory;
    private JTable tblAdminBillHistory;
    private JScrollPane tblBillingRecords;
    private JTable tblTransactionRecords;
    private JButton btnViewReceipt;
    private JPanel adminPanel;
    private JTextField txtProdName;
    private JTextField txtProdCat;
    private JTextField txtProdPrice;
    private JTextField txtProdStock;
    private JButton btnProdAdd;
    private JButton btnProdUpdate;
    private JButton btnProdDelete;
    private JButton btnProdClear;
    private JButton startServiceButton;
    private JButton maskAsCompletedButton;
    private JButton cancelServiceButton;
    private JTable adminServiceManagementTabel;
    private JTable tblApproveAppointments;
    private JButton btnApproveAppointment;
    private JButton btnCancelPendingAppointment;
    private JComboBox<String> cmbServiceStatus;
    private JButton btnUpdateWorkStatus;

    private DefaultTableModel clientAppointmentListTable;
    private DefaultTableModel productMasterlistTable;
    private DefaultTableModel transactionRecordsTable;
    private DefaultTableModel billingRecordsTable;
    private AutomotiveData sharedData;

    String prodName, prodCategory;
    float prodPrice;
    int prodStock;

    public JPanel getAdminPanel() {
        return adminPanel;
    }

    public AdminPanel(AutomotivePasswords clientInfo) {
        this.sharedData = clientInfo.getAutomotiveData();

        clientAppointmentListTable = sharedData.getAppointmentModel();
        productMasterlistTable = sharedData.getProductModel();
        transactionRecordsTable = sharedData.getTransactionModel();
        billingRecordsTable = sharedData.getBillModel();

        tblClientAppointmentMastList.setModel(clientAppointmentListTable);
        tblApproveAppointments.setModel(clientAppointmentListTable);
        adminServiceManagementTabel.setModel(clientAppointmentListTable);
        tblProdMasterList.setModel(productMasterlistTable);
        tblTransactionRecords.setModel(transactionRecordsTable);
        tblAdminBillHistory.setModel(billingRecordsTable);

        // The approval tab only shows appointments that are still pending.
        TableRowSorter<DefaultTableModel> pendingAppointmentSorter =
                new TableRowSorter<>(clientAppointmentListTable);
        pendingAppointmentSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                return "Pending".equalsIgnoreCase(entry.getStringValue(6));
            }
        });
        tblApproveAppointments.setRowSorter(pendingAppointmentSorter);

        hideTableColumn(tblClientAppointmentMastList, 9);
        hideTableColumn(tblApproveAppointments, 9);
        hideTableColumn(adminServiceManagementTabel, 9);
        hideTableColumn(tblAdminBillHistory, 8);
        hideTableColumn(tblTransactionRecords, 6);

        // Selecting a product automatically loads its values into the edit fields.
        tblProdMasterList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;

                int selectedRow = tblProdMasterList.getSelectedRow();
                if (selectedRow == -1) return;

                selectedRow = tblProdMasterList.convertRowIndexToModel(selectedRow);
                txtProdName.setText(productMasterlistTable.getValueAt(selectedRow, 1).toString());
                txtProdCat.setText(productMasterlistTable.getValueAt(selectedRow, 2).toString());
                txtProdPrice.setText(productMasterlistTable.getValueAt(selectedRow, 3).toString());
                txtProdStock.setText(productMasterlistTable.getValueAt(selectedRow, 4).toString());
            }
        });

        btnConfirmApp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = tblClientAppointmentMastList.getSelectedRow();

                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(adminPanel, "Please select an appointment to approve.", "No Appointment Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                selectedRow = tblClientAppointmentMastList.convertRowIndexToModel(selectedRow);
                int appointmentID = Integer.parseInt(clientAppointmentListTable.getValueAt(selectedRow, 0).toString());
                String appointmentStatus = sharedData.getAppointmentStatus(appointmentID);
                String serviceStatus = sharedData.getServiceStatus(appointmentID);

                if ("Cancelled".equals(appointmentStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "Cancelled appointments cannot be approved.", "Cancelled Appointment", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if ("Cancelled".equals(serviceStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "This appointment's service was cancelled and cannot be approved.", "Cancelled Service", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (sharedData.updateAppointmentStatus(appointmentID, "Approved")) {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment approved. The client can now pay the service bill once the work is completed.");
                } else {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment record not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnCancelAppointment.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = tblClientAppointmentMastList.getSelectedRow();

                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(adminPanel, "Please select an appointment to cancel.", "No Appointment Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                selectedRow = tblClientAppointmentMastList.convertRowIndexToModel(selectedRow);
                int appointmentID = Integer.parseInt(clientAppointmentListTable.getValueAt(selectedRow, 0).toString());
                String currentStatus = sharedData.getAppointmentStatus(appointmentID);

                if ("Cancelled".equals(currentStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "This appointment is already cancelled.", "Already Cancelled", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                if (sharedData.updateAppointmentStatus(appointmentID, "Cancelled")) {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment cancelled. The client cannot pay this service bill.");
                } else {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment record not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnApproveAppointment.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = tblApproveAppointments.getSelectedRow();

                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(adminPanel, "Please select a pending appointment to approve.", "No Appointment Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                selectedRow = tblApproveAppointments.convertRowIndexToModel(selectedRow);
                int appointmentID = Integer.parseInt(clientAppointmentListTable.getValueAt(selectedRow, 0).toString());
                String appointmentStatus = sharedData.getAppointmentStatus(appointmentID);
                String serviceStatus = sharedData.getServiceStatus(appointmentID);

                if (!"Pending".equalsIgnoreCase(appointmentStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "Only pending appointments can be approved.", "Not Pending", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if ("Cancelled".equals(serviceStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "This appointment's service was cancelled and cannot be approved.", "Cancelled Service", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (sharedData.updateAppointmentStatus(appointmentID, "Approved")) {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment #" + appointmentID + " approved. The client can now see the approval.");
                } else {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment record not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnCancelPendingAppointment.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = tblApproveAppointments.getSelectedRow();

                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(adminPanel, "Please select a pending appointment to cancel.", "No Appointment Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                selectedRow = tblApproveAppointments.convertRowIndexToModel(selectedRow);
                int appointmentID = Integer.parseInt(clientAppointmentListTable.getValueAt(selectedRow, 0).toString());

                if (sharedData.updateAppointmentStatus(appointmentID, "Cancelled")) {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment #" + appointmentID + " cancelled. The client cannot pay this service bill.");
                } else {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment record not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnInspectEstimate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = tblClientAppointmentMastList.getSelectedRow();

                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(adminPanel, "Please select an appointment to inspect.", "No Appointment Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                selectedRow = tblClientAppointmentMastList.convertRowIndexToModel(selectedRow);

                JOptionPane.showMessageDialog(
                        adminPanel,
                        "APPOINTMENT DETAILS\n\n"
                                + "Appointment ID: " + clientAppointmentListTable.getValueAt(selectedRow, 0)
                                + "\nClient: " + clientAppointmentListTable.getValueAt(selectedRow, 1)
                                + "\nService: " + clientAppointmentListTable.getValueAt(selectedRow, 2)
                                + "\nVehicle: " + clientAppointmentListTable.getValueAt(selectedRow, 3)
                                + "\nDetails: " + clientAppointmentListTable.getValueAt(selectedRow, 4)
                                + "\nSchedule: " + clientAppointmentListTable.getValueAt(selectedRow, 5)
                                + "\nAppointment Status: " + clientAppointmentListTable.getValueAt(selectedRow, 6)
                                + "\nService Status: " + clientAppointmentListTable.getValueAt(selectedRow, 7)
                                + "\nEstimate: " + clientAppointmentListTable.getValueAt(selectedRow, 8),
                        "Inspect Estimate",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        btnRefreshBillHistory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                billingRecordsTable.fireTableDataChanged();
                transactionRecordsTable.fireTableDataChanged();
                JOptionPane.showMessageDialog(adminPanel, "Billing records refreshed.");
            }
        });

        btnViewReceipt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = tblTransactionRecords.getSelectedRow();

                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(adminPanel, "Please select a transaction first.", "No Transaction Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                selectedRow = tblTransactionRecords.convertRowIndexToModel(selectedRow);
                JOptionPane.showMessageDialog(
                        adminPanel,
                        "RECEIPT\n"
                                + "Receipt #: " + transactionRecordsTable.getValueAt(selectedRow, 0)
                                + "\nBill #: " + transactionRecordsTable.getValueAt(selectedRow, 1)
                                + "\nClient: " + transactionRecordsTable.getValueAt(selectedRow, 2)
                                + "\nAmount: " + transactionRecordsTable.getValueAt(selectedRow, 3)
                                + "\nMethod: " + transactionRecordsTable.getValueAt(selectedRow, 4)
                                + "\nDate: " + transactionRecordsTable.getValueAt(selectedRow, 5),
                        "Receipt",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        btnLogOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JFrame loginFrame = new JFrame("Login");
                    AutomotiveServiceLogin loginPanel = new AutomotiveServiceLogin(clientInfo);
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

                    JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(adminPanel);
                    if (currentFrame != null) {
                        currentFrame.dispose();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btnProdAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    prodName = txtProdName.getText().trim();
                    prodCategory = txtProdCat.getText().trim();

                    for (int rowSearch = 0; rowSearch < productMasterlistTable.getRowCount(); rowSearch++) {
                        if (prodName.equalsIgnoreCase(productMasterlistTable.getValueAt(rowSearch, 1).toString())) {
                            JOptionPane.showMessageDialog(adminPanel, "Product Already Exists!", "ERROR", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    }

                    if (prodName.isEmpty() || prodCategory.isEmpty()) {
                        JOptionPane.showMessageDialog(adminPanel, "Invalid Input!", "ERROR", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    prodPrice = Float.parseFloat(txtProdPrice.getText().trim());
                    prodStock = Integer.parseInt(txtProdStock.getText().trim());

                    if (prodPrice < 0 || prodStock < 0) {
                        JOptionPane.showMessageDialog(adminPanel, "Price and stock cannot be negative.", "ERROR", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    int prodID = sharedData.nextProductID();
                    Object[] prodList = {prodID, prodName, prodCategory, prodPrice, prodStock};
                    productMasterlistTable.addRow(prodList);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(adminPanel, "Invalid Input!", "ERROR", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        btnProdUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int selectedRow = tblProdMasterList.getSelectedRow();

                    if (selectedRow == -1) {
                        JOptionPane.showMessageDialog(adminPanel, "Please Select an item to Update", "ERROR", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    selectedRow = tblProdMasterList.convertRowIndexToModel(selectedRow);
                    prodName = txtProdName.getText().trim();
                    prodCategory = txtProdCat.getText().trim();
                    prodPrice = Float.parseFloat(txtProdPrice.getText().trim());
                    prodStock = Integer.parseInt(txtProdStock.getText().trim());

                    if (prodName.isEmpty() || prodCategory.isEmpty() || prodPrice < 0 || prodStock < 0) {
                        JOptionPane.showMessageDialog(adminPanel, "Invalid Input!", "ERROR", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    for (int i = 0; i < productMasterlistTable.getRowCount(); i++) {
                        if (i != selectedRow && prodName.equalsIgnoreCase(productMasterlistTable.getValueAt(i, 1).toString())) {
                            JOptionPane.showMessageDialog(adminPanel, "Product Already Exists!", "ERROR", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    }

                    productMasterlistTable.setValueAt(prodName, selectedRow, 1);
                    productMasterlistTable.setValueAt(prodCategory, selectedRow, 2);
                    productMasterlistTable.setValueAt(prodPrice, selectedRow, 3);
                    productMasterlistTable.setValueAt(prodStock, selectedRow, 4);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(adminPanel, "Invalid Input!", "ERROR", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        btnProdDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = tblProdMasterList.getSelectedRow();

                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(adminPanel, "Please Select an item to delete", "ERROR", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int row = tblProdMasterList.convertRowIndexToModel(selectedRow);
                productMasterlistTable.removeRow(row);
                txtProdName.setText("");
                txtProdCat.setText("");
                txtProdPrice.setText("");
                txtProdStock.setText("");
            }
        });

        btnProdClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtProdName.setText("");
                txtProdCat.setText("");
                txtProdPrice.setText("");
                txtProdStock.setText("");
                tblProdMasterList.clearSelection();
            }
        });

        // Selecting a service row loads its current service status into the combo box.
        adminServiceManagementTabel.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;

                int selectedRow = adminServiceManagementTabel.getSelectedRow();
                if (selectedRow == -1) return;

                selectedRow = adminServiceManagementTabel.convertRowIndexToModel(selectedRow);
                String serviceStatus = String.valueOf(clientAppointmentListTable.getValueAt(selectedRow, 7));

                if ("Not Started".equals(serviceStatus)
                        || "In Progress".equals(serviceStatus)
                        || "Completed".equals(serviceStatus)) {
                    cmbServiceStatus.setSelectedItem(serviceStatus);
                }
            }
        });

        startServiceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int appointmentID = getSelectedAppointmentID(adminServiceManagementTabel);

                if (appointmentID == -1) {
                    JOptionPane.showMessageDialog(adminPanel, "Please select an appointment or service first.", "No Service Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String appointmentStatus = sharedData.getAppointmentStatus(appointmentID);
                String serviceStatus = sharedData.getServiceStatus(appointmentID);

                if ("Cancelled".equals(appointmentStatus) || "Cancelled".equals(serviceStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "Cancelled appointments or services cannot be started.", "Cancelled Service", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (!"Approved".equals(appointmentStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "The appointment must be approved before the service can start.\nCurrent Appointment Status: " + appointmentStatus, "Approval Required", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if ("Completed".equals(serviceStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "This service is already completed.", "Service Completed", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                if ("In Progress".equals(serviceStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "This service is already in progress.", "Service In Progress", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                if (sharedData.updateServiceStatus(appointmentID, "In Progress")) {
                    JOptionPane.showMessageDialog(adminPanel, "Service started. The client can see that the work is now In Progress.");
                } else {
                    JOptionPane.showMessageDialog(adminPanel, "The service status could not be updated.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        maskAsCompletedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int appointmentID = getSelectedAppointmentID(adminServiceManagementTabel);

                if (appointmentID == -1) {
                    JOptionPane.showMessageDialog(adminPanel, "Please select an appointment or service first.", "No Service Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String appointmentStatus = sharedData.getAppointmentStatus(appointmentID);
                String serviceStatus = sharedData.getServiceStatus(appointmentID);

                if ("Cancelled".equals(appointmentStatus) || "Cancelled".equals(serviceStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "Cancelled appointments or services cannot be completed.", "Cancelled Service", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (!"Approved".equals(appointmentStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "The appointment must be approved before the service can be completed.\nCurrent Appointment Status: " + appointmentStatus, "Approval Required", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if ("Completed".equals(serviceStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "This service is already completed.", "Service Completed", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                if (!"In Progress".equals(serviceStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "Start the service first, or use Update Work Status to set it to Completed.", "Service Not Started", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (sharedData.updateServiceStatus(appointmentID, "Completed")) {
                    JOptionPane.showMessageDialog(adminPanel, "Service marked as completed. The client can now pay the service bill.");
                } else {
                    JOptionPane.showMessageDialog(adminPanel, "The service status could not be updated.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelServiceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int appointmentID = getSelectedAppointmentID(adminServiceManagementTabel);

                if (appointmentID == -1) {
                    JOptionPane.showMessageDialog(adminPanel, "Please select an appointment or service first.", "No Service Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String serviceStatus = sharedData.getServiceStatus(appointmentID);

                if ("Cancelled".equals(serviceStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "This service is already cancelled.", "Already Cancelled", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                if ("Completed".equals(serviceStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "A completed service cannot be cancelled.", "Service Completed", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (sharedData.cancelService(appointmentID)) {
                    JOptionPane.showMessageDialog(adminPanel, "Service cancelled. The record is kept for history and its bill can no longer be paid.");
                } else {
                    JOptionPane.showMessageDialog(adminPanel, "The service could not be cancelled.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnUpdateWorkStatus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int appointmentID = getSelectedAppointmentID(adminServiceManagementTabel);

                if (appointmentID == -1) {
                    JOptionPane.showMessageDialog(adminPanel, "Please select an appointment or service first.", "No Service Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Object selectedStatusObject = cmbServiceStatus.getSelectedItem();

                if (selectedStatusObject == null) {
                    JOptionPane.showMessageDialog(adminPanel, "Please select a service status.", "No Status Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String selectedStatus = selectedStatusObject.toString();
                String appointmentStatus = sharedData.getAppointmentStatus(appointmentID);
                String currentServiceStatus = sharedData.getServiceStatus(appointmentID);

                if ("Cancelled".equals(appointmentStatus) || "Cancelled".equals(currentServiceStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "Cancelled appointments or services cannot be updated.", "Cancelled Service", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (("In Progress".equals(selectedStatus) || "Completed".equals(selectedStatus))
                        && !"Approved".equals(appointmentStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "The appointment must be approved before the work can start or finish.\nCurrent Appointment Status: " + appointmentStatus, "Approval Required", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (selectedStatus.equals(currentServiceStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "The service is already marked as " + selectedStatus + ".", "No Change", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                if (sharedData.updateServiceStatus(appointmentID, selectedStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "Service status updated to " + selectedStatus + ".\nThe client can see this change immediately.");
                } else {
                    JOptionPane.showMessageDialog(adminPanel, "The service status could not be updated.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private int getSelectedAppointmentID(JTable table) {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            return -1;
        }

        selectedRow = table.convertRowIndexToModel(selectedRow);

        return Integer.parseInt(
                clientAppointmentListTable.getValueAt(selectedRow, 0).toString()
        );
    }

    private void hideTableColumn(JTable table, int modelColumn) {
        int viewColumn = table.convertColumnIndexToView(modelColumn);
        if (viewColumn >= 0) {
            table.removeColumn(table.getColumnModel().getColumn(viewColumn));
        }
    }

}
