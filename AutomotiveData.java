import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class AutomotiveData {
    private final DefaultTableModel appointmentModel = new DefaultTableModel();
    private final DefaultTableModel productModel = new DefaultTableModel();
    private final DefaultTableModel billModel = new DefaultTableModel();
    private final DefaultTableModel transactionModel = new DefaultTableModel();

    // Each client has a separate shopping cart.
    private final Map<String, DefaultTableModel> clientCartModels = new HashMap<>();

    private int appointmentCount = 0;
    private int billCount = 0;
    private int receiptCount = 0;
    private int productCount = 0;

    // Connects service bills to their corresponding appointments.
    private final Map<Integer, Integer> serviceBillAppointmentIDs = new HashMap<>();

    public AutomotiveData() {

        // APPOINTMENT TABLE
        appointmentModel.addColumn("ID");
        appointmentModel.addColumn("Client");
        appointmentModel.addColumn("Service");
        appointmentModel.addColumn("Vehicle");
        appointmentModel.addColumn("Details");
        appointmentModel.addColumn("Schedule");
        appointmentModel.addColumn("Status");
        appointmentModel.addColumn("Service Status");
        appointmentModel.addColumn("Estimate");
        appointmentModel.addColumn("Client Username");

        // PRODUCT TABLE
        productModel.addColumn("ID");
        productModel.addColumn("Product");
        productModel.addColumn("Category");
        productModel.addColumn("Price");
        productModel.addColumn("Stock");

        // BILL TABLE
        billModel.addColumn("Bill #");
        billModel.addColumn("Client");
        billModel.addColumn("Type");
        billModel.addColumn("Description");
        billModel.addColumn("Amount");
        billModel.addColumn("Approval Status");
        billModel.addColumn("Payment Status");
        billModel.addColumn("Date");
        billModel.addColumn("Client Username");

        // TRANSACTION TABLE
        transactionModel.addColumn("Receipt #");
        transactionModel.addColumn("Bill #");
        transactionModel.addColumn("Client");
        transactionModel.addColumn("Amount");
        transactionModel.addColumn("Method");
        transactionModel.addColumn("Date");
        transactionModel.addColumn("Client Username");

        // DEFAULT PRODUCT INVENTORY
        String[][] products = {
                {"1", "Engine Oil", "Fluids", "450.00", "50"},
                {"2", "Oil Filter", "Parts", "225.00", "40"},
                {"3", "Air Filter", "Parts", "375.00", "30"},
                {"4", "Fuel Filter", "Parts", "225.00", "25"},
                {"5", "Spark Plug", "Parts", "215.00", "60"},
                {"6", "Brake Pads", "Parts", "725.00", "35"},
                {"7", "Brake Shoes", "Parts", "450.00", "20"},
                {"8", "Brake Fluid", "Fluids", "275.00", "45"},
                {"9", "Motorcycle Tires", "Tires", "1600.00", "15"},
                {"10", "Car Tires", "Tires", "4000.00", "20"},
                {"11", "Batteries", "Electrical", "3100.00", "10"},
                {"12", "Coolant", "Fluids", "325.00", "50"},
                {"13", "Gear Oil", "Fluids", "235.00", "30"},
                {"14", "Transmission Oil", "Fluids", "475.00", "25"},
                {"15", "Motorcycle Chains", "Parts", "575.00", "18"}
        };

        for (String[] product : products) {
            productModel.addRow(product);
            productCount++;
        }
    }

    // GET TABLE MODELS

    public DefaultTableModel getAppointmentModel() {
        return appointmentModel;
    }

    public DefaultTableModel getProductModel() {
        return productModel;
    }

    public DefaultTableModel getBillModel() {
        return billModel;
    }

    public DefaultTableModel getTransactionModel() {
        return transactionModel;
    }

    // GET A CLIENT'S SHOPPING CART

    public synchronized DefaultTableModel getCartModel(String clientUsername) {
        String key = normalizeUsername(clientUsername);

        DefaultTableModel model = clientCartModels.get(key);

        if (model == null) {
            model = new DefaultTableModel();

            model.addColumn("ID");
            model.addColumn("Product");
            model.addColumn("Qty");
            model.addColumn("Subtotal");

            clientCartModels.put(key, model);
        }

        return model;
    }

    // Compatibility method for older code.
    public DefaultTableModel getCartModel() {
        return getCartModel("guest");
    }

    // GET VECTOR RECORDS

    public Vector<Vector> getAppointmentRecords() {
        return appointmentModel.getDataVector();
    }

    public Vector<Vector> getProductRecords() {
        return productModel.getDataVector();
    }

    public Vector<Vector> getCartRecords(String clientUsername) {
        return getCartModel(clientUsername).getDataVector();
    }

    public Vector<Vector> getCartRecords() {
        return getCartModel().getDataVector();
    }

    public Vector<Vector> getBillRecords() {
        return billModel.getDataVector();
    }

    public Vector<Vector> getTransactionRecords() {
        return transactionModel.getDataVector();
    }

    // GENERATE AUTOMATIC ID NUMBERS

    public int nextAppointmentID() {
        return ++appointmentCount;
    }

    public int nextBillID() {
        return ++billCount;
    }

    public int nextReceiptID() {
        return ++receiptCount;
    }

    public int nextProductID() {
        return ++productCount;
    }

    public void resetProductCount(int count) {
        productCount = count;
    }

    // LINK A SERVICE BILL TO AN APPOINTMENT

    public void linkServiceBill(int billID, int appointmentID) {
        serviceBillAppointmentIDs.put(billID, appointmentID);
    }

    public Integer getAppointmentIDForBill(int billID) {
        return serviceBillAppointmentIDs.get(billID);
    }

    // GET APPOINTMENT STATUS

    public String getAppointmentStatus(int appointmentID) {
        for (int i = 0; i < appointmentModel.getRowCount(); i++) {

            int currentID = Integer.parseInt(
                    appointmentModel.getValueAt(i, 0).toString()
            );

            if (currentID == appointmentID) {
                return appointmentModel.getValueAt(i, 6).toString();
            }
        }

        return null;
    }

    // GET SERVICE STATUS

    public String getServiceStatus(int appointmentID) {
        for (int i = 0; i < appointmentModel.getRowCount(); i++) {

            int currentID = Integer.parseInt(
                    appointmentModel.getValueAt(i, 0).toString()
            );

            if (currentID == appointmentID) {
                return appointmentModel.getValueAt(i, 7).toString();
            }
        }

        return null;
    }

    public String getServiceStatusForBill(int billID) {
        Integer appointmentID = serviceBillAppointmentIDs.get(billID);

        if (appointmentID == null) {
            return null;
        }

        return getServiceStatus(appointmentID);
    }

    // UPDATE APPOINTMENT AND RELATED BILL STATUS

    public boolean updateAppointmentStatus(int appointmentID, String status) {
        boolean found = false;

        for (int i = 0; i < appointmentModel.getRowCount(); i++) {

            int currentID = Integer.parseInt(
                    appointmentModel.getValueAt(i, 0).toString()
            );

            if (currentID == appointmentID) {
                appointmentModel.setValueAt(status, i, 6);

                // A cancelled appointment cannot continue as active work.
                // A completed service keeps its completion record for history.
                if ("Cancelled".equals(status)) {
                    String serviceStatus = String.valueOf(
                            appointmentModel.getValueAt(i, 7)
                    );

                    if (!"Completed".equals(serviceStatus)) {
                        appointmentModel.setValueAt("Cancelled", i, 7);
                    }
                }

                found = true;
                break;
            }
        }

        // Update the approval status of the associated service bill.
        if (found) {
            updateLinkedBillApproval(appointmentID, status);
        }

        return found;
    }

    // UPDATE ONLY THE SERVICE STATUS (NEVER TOUCH THE APPOINTMENT STATUS)

    public boolean updateServiceStatus(int appointmentID, String serviceStatus) {
        for (int i = 0; i < appointmentModel.getRowCount(); i++) {

            int currentID = Integer.parseInt(
                    appointmentModel.getValueAt(i, 0).toString()
            );

            if (currentID == appointmentID) {
                String appointmentStatus = String.valueOf(
                        appointmentModel.getValueAt(i, 6)
                );
                String existingServiceStatus = String.valueOf(
                        appointmentModel.getValueAt(i, 7)
                );

                // Cancelled work must stay cancelled.
                if ("Cancelled".equals(existingServiceStatus)) {
                    return false;
                }

                // Cancelled appointments must not progress to completed work.
                if ("Cancelled".equals(appointmentStatus)) {
                    return false;
                }

                // Work cannot start or finish before the appointment is approved.
                if (!"Approved".equals(appointmentStatus)
                        && ("In Progress".equals(serviceStatus)
                        || "Completed".equals(serviceStatus))) {
                    return false;
                }

                appointmentModel.setValueAt(serviceStatus, i, 7);
                return true;
            }
        }

        return false;
    }

    // CANCEL A SERVICE WHILE KEEPING THE RECORD FOR HISTORY

    public boolean cancelService(int appointmentID) {
        for (int i = 0; i < appointmentModel.getRowCount(); i++) {

            int currentID = Integer.parseInt(
                    appointmentModel.getValueAt(i, 0).toString()
            );

            if (currentID == appointmentID) {
                String serviceStatus = String.valueOf(
                        appointmentModel.getValueAt(i, 7)
                );

                if ("Completed".equals(serviceStatus)
                        || "Cancelled".equals(serviceStatus)) {
                    return false;
                }

                appointmentModel.setValueAt("Cancelled", i, 7);
                appointmentModel.setValueAt("Cancelled", i, 6);

                updateLinkedBillApproval(appointmentID, "Cancelled");

                return true;
            }
        }

        return false;
    }

    // PAYMENT RULES FOR SERVICE BILLS

    public boolean isServiceBillPayable(int billID) {
        return getPaymentBlockReason(billID) == null;
    }

    public String getPaymentBlockReason(int billID) {
        Integer appointmentID = serviceBillAppointmentIDs.get(billID);

        if (appointmentID == null) {
            return "The linked appointment record could not be found.";
        }

        String appointmentStatus = getAppointmentStatus(appointmentID);
        String serviceStatus = getServiceStatus(appointmentID);

        if (appointmentStatus == null || serviceStatus == null) {
            return "The linked appointment record could not be found.";
        }

        if (!"Approved".equals(appointmentStatus)) {
            return "Your service appointment must be approved by the admin first."
                    + "\nCurrent Appointment Status: " + appointmentStatus;
        }

        if (!"Completed".equals(serviceStatus)) {
            return "The service work must be completed before this bill can be paid."
                    + "\nCurrent Service Status: " + serviceStatus;
        }

        return null;
    }

    // UPDATE THE APPROVAL COLUMN OF THE SERVICE BILL LINKED TO AN APPOINTMENT

    private void updateLinkedBillApproval(int appointmentID, String status) {
        for (int i = 0; i < billModel.getRowCount(); i++) {

            if ("Service".equals(
                    billModel.getValueAt(i, 2).toString()
            )) {

                int billID = Integer.parseInt(
                        billModel.getValueAt(i, 0).toString()
                );

                Integer linkedAppointmentID =
                        serviceBillAppointmentIDs.get(billID);

                if (linkedAppointmentID != null
                        && linkedAppointmentID.intValue() == appointmentID) {

                    billModel.setValueAt(status, i, 5);
                }
            }
        }
    }

    // NORMALIZE USERNAME TO KEEP ACCOUNT IDENTIFICATION CONSISTENT

    private String normalizeUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "guest";
        }

        return username.trim().toLowerCase();
    }
}