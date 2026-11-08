import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

/**
 * Admin dashboard.
 * The tabs manage appointments, products, billing history, transactions, and service status.
 */
public class AdminPanel {
    // Components created by the IntelliJ GUI Designer .form file.
    private JTabbedPane tabbedPane1;
    private JButton btnLogOut;
    private JTable tblClientAppointmentMastList;
    private JButton btnConfirmApp;
    private JButton btnCancelAppointment;
    private JButton btnInspectEstimate;
    private JTable tblProdMasterList;
    private JScrollPane scrollPane;
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
    private JButton btnStartService;
    private JButton btnCompleted;
    private JButton btnCancelService;
    private JTable tblServiceManagement;

    // Shared table models owned by AutomotiveData.
    private DefaultTableModel clientAppointmentListTable;
    private DefaultTableModel productMasterlistTable;
    private DefaultTableModel transactionRecordsTable;
    private DefaultTableModel billingRecordsTable;


    /** Returns the admin panel so it can be placed inside a JFrame. */
    public JPanel getAdminPanel() {
        return adminPanel;
    }

    private final AutomotiveData sharedData = AutomotiveData.getInstance();

    public AdminPanel() {

        // Connect the visual tables to the shared data models.
        clientAppointmentListTable = sharedData.getAppointmentModel();
        productMasterlistTable = sharedData.getProductModel();
        transactionRecordsTable = sharedData.getTransactionModel();
        billingRecordsTable = sharedData.getBillModel();

        tblServiceManagement.setModel(clientAppointmentListTable);
        tblClientAppointmentMastList.setModel(clientAppointmentListTable);
        tblProdMasterList.setModel(productMasterlistTable);
        tblTransactionRecords.setModel(transactionRecordsTable);
        tblAdminBillHistory.setModel(billingRecordsTable);

        // Hide the client-username columns in the admin views.
        hideTableColumn(tblClientAppointmentMastList, 9);
        hideTableColumn(tblServiceManagement, 9);
        hideTableColumn(tblAdminBillHistory, 8);
        hideTableColumn(tblTransactionRecords, 6);

        // Show only bills that have not been paid yet.
        TableRowSorter<DefaultTableModel> unpaidSorter = new TableRowSorter<>(billingRecordsTable);
        unpaidSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                return !"Paid".equals(entry.getStringValue(6));
            }
        });
        tblAdminBillHistory.setRowSorter(unpaidSorter);

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

        // Approve the selected appointment.
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
                String currentStatus = sharedData.getAppointmentStatus(appointmentID);

                if (currentStatus == null) {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment record not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (currentStatus.equals("Cancelled")) {
                    JOptionPane.showMessageDialog(adminPanel, "This appointment was cancelled and cannot be approved.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (sharedData.updateAppointmentStatus(appointmentID, "Approved")) {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment approved. The client can now pay the service bill.");
                } else {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment record not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Cancel the selected appointment unless it was already approved.
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

                if (currentStatus == null) {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment record not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (currentStatus.equals("Approved")) {
                    JOptionPane.showMessageDialog(adminPanel, "This appointment was approved and cannot be cancelled.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (sharedData.updateAppointmentStatus(appointmentID, "Cancelled")) {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment cancelled. The client cannot pay this service bill.");
                } else {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment record not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Show all stored details for the selected appointment.
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
                                + "\nStatus: " + clientAppointmentListTable.getValueAt(selectedRow, 6)
                                + "\nService Status: " + clientAppointmentListTable.getValueAt(selectedRow, 7)
                                + "\nEstimate: " + clientAppointmentListTable.getValueAt(selectedRow, 8),
                        "Inspect Estimate",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        // Repaint the billing and transaction tables.
        btnRefreshBillHistory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                billingRecordsTable.fireTableDataChanged();
                transactionRecordsTable.fireTableDataChanged();
                JOptionPane.showMessageDialog(adminPanel, "Billing records refreshed.");
            }
        });

        // Show the receipt for the selected payment transaction.
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

        // Clear the session and return to the login screen.
        btnLogOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    SessionUser.logout();
                    AutomotiveServiceLogin.showLoginWindow("Login");

                    JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(adminPanel);
                    if (currentFrame != null) {
                        currentFrame.dispose();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Add a new product after validating the fields and checking for duplicates.
        btnProdAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String prodName = txtProdName.getText().trim();
                    String prodCategory = txtProdCat.getText().trim();

                    if (prodName.isEmpty() || prodCategory.isEmpty()) {
                        JOptionPane.showMessageDialog(adminPanel, "Invalid Input!", "ERROR", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    float prodPrice = Float.parseFloat(txtProdPrice.getText().trim());
                    int prodStock = Integer.parseInt(txtProdStock.getText().trim());

                    if (prodPrice < 0 || prodStock < 0) {
                        JOptionPane.showMessageDialog(adminPanel, "Price and stock cannot be negative.", "ERROR", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    for (int rowSearch = 0; rowSearch < productMasterlistTable.getRowCount(); rowSearch++) {
                        if (prodName.equalsIgnoreCase(productMasterlistTable.getValueAt(rowSearch, 1).toString())) {
                            JOptionPane.showMessageDialog(adminPanel, "Product Already Exists!", "ERROR", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    }

                    productMasterlistTable.addRow(new Object[]{
                            sharedData.nextProductID(), prodName, prodCategory, prodPrice, prodStock
                    });
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(adminPanel, "Invalid Input!", "ERROR", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // Save the edited values back into the selected product row.
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
                    String prodName = txtProdName.getText().trim();
                    String prodCategory = txtProdCat.getText().trim();

                    if (prodName.isEmpty() || prodCategory.isEmpty()) {
                        JOptionPane.showMessageDialog(adminPanel, "Invalid Input!", "ERROR", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    float prodPrice = Float.parseFloat(txtProdPrice.getText().trim());
                    int prodStock = Integer.parseInt(txtProdStock.getText().trim());

                    if (prodPrice < 0 || prodStock < 0) {
                        JOptionPane.showMessageDialog(adminPanel, "Price and stock cannot be negative.", "ERROR", JOptionPane.WARNING_MESSAGE);
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

        // Remove the selected product and clear the edit fields.
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

        // Clear the product form without deleting anything.
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
        // Start the selected service after checking approval and completion state.
        btnStartService.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = tblServiceManagement.getSelectedRow();

                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(adminPanel, "Please select an appointment to start", "ERROR", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                selectedRow = tblServiceManagement.convertRowIndexToModel(selectedRow);
                int appointmentID = Integer.parseInt(clientAppointmentListTable.getValueAt(selectedRow, 0).toString());
                String currentAppointStatus = sharedData.getAppointmentStatus(appointmentID);
                String currentServiceStatus = sharedData.getServiceStatus(appointmentID);

                if (currentAppointStatus == null) {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment record not found.", "ERROR", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if ("Cancelled".equals(currentAppointStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "This appointment was cancelled and cannot be started.", "ERROR", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if ("Pending".equals(currentAppointStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "This appointment has not been approved by the admin yet.", "ERROR", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if ("Completed".equals(currentServiceStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment is already completed.", "ERROR", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (sharedData.updateServiceStatus(appointmentID, "In progress")) {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment has been started, waiting for completion of appointment.");
                } else {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment record not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            }
        });
        // Mark the selected approved service as completed.
        btnCompleted.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = tblServiceManagement.getSelectedRow();

                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(adminPanel, "Please select an appointment to complete", "ERROR", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                selectedRow = tblServiceManagement.convertRowIndexToModel(selectedRow);
                int appointmentID = Integer.parseInt(clientAppointmentListTable.getValueAt(selectedRow, 0).toString());
                String currentStatus = sharedData.getAppointmentStatus(appointmentID);

                if (currentStatus == null) {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment record not found.", "ERROR", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if ("Cancelled".equals(currentStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "This appointment was cancelled and cannot be completed.", "ERROR", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if ("Pending".equals(currentStatus)) {
                    JOptionPane.showMessageDialog(adminPanel, "This appointment has not been approved by the admin yet.", "ERROR", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (sharedData.updateServiceStatus(appointmentID, "Completed")) {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment Completed.");
                } else {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment record not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        // Cancel a service appointment that has not been approved yet.
        btnCancelService.addActionListener(new ActionListener() {
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

                if (currentStatus == null) {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment record not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (currentStatus.equals("Approved")) {
                    JOptionPane.showMessageDialog(adminPanel, "This appointment was approved and cannot be cancelled.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (sharedData.updateAppointmentStatus(appointmentID, "Cancelled")) {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment cancelled. The client cannot pay this service bill.");
                } else {
                    JOptionPane.showMessageDialog(adminPanel, "Appointment record not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    /** Hides a model column from a JTable while keeping the data in the model. */
    private void hideTableColumn(JTable table, int modelColumn) {
        int viewColumn = table.convertColumnIndexToView(modelColumn);
        if (viewColumn >= 0) {
            table.removeColumn(table.getColumnModel().getColumn(viewColumn));
        }
    }

}