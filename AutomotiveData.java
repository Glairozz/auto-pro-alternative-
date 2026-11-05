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

    // UPDATE APPOINTMENT AND RELATED BILL STATUS

    public boolean updateAppointmentStatus(int appointmentID, String status) {
        boolean found = false;

        for (int i = 0; i < appointmentModel.getRowCount(); i++) {

            int currentID = Integer.parseInt(
                    appointmentModel.getValueAt(i, 0).toString()
            );

            if (currentID == appointmentID) {
                appointmentModel.setValueAt(status, i, 6);
                found = true;
                break;
            }
        }

        // Update the approval status of the associated service bill.
        if (found) {
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
                            && linkedAppointmentID == appointmentID) {

                        billModel.setValueAt(status, i, 5);
                    }
                }
            }
        }

        return found;
    }

    // NORMALIZE USERNAME TO KEEP ACCOUNT IDENTIFICATION CONSISTENT

    private String normalizeUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "guest";
        }

        return username.trim().toLowerCase();
    }
}