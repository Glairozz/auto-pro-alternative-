import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Central in-memory data store for the whole application.
 * It keeps the appointment, product, bill, transaction, and per-client cart tables
 * so every screen works on the same live data.
 */
public class AutomotiveData {
    private static final AutomotiveData INSTANCE = new AutomotiveData();

    public static AutomotiveData getInstance() {
        return INSTANCE;
    }

    // Shared tables shown by the admin and client screens.
    private final DefaultTableModel appointmentModel = new DefaultTableModel();
    private final DefaultTableModel productModel = new DefaultTableModel();
    private final DefaultTableModel billModel = new DefaultTableModel();
    private final DefaultTableModel transactionModel = new DefaultTableModel();

    // Each client gets a separate cart table.
    private final Map<String, DefaultTableModel> clientCartModels = new HashMap<>();

    // Counters used to generate the next unique IDs.
    private int appointmentCount = 0;
    private int billCount = 0;
    private int receiptCount = 0;
    private int productCount = 0;

    // Links a service bill to the appointment that created it.
    private final Map<Integer, Integer> serviceBillAppointmentIDs = new HashMap<>();

    private AutomotiveData() {

        // Appointment columns: 0 ID, 6 appointment status, 7 service status.
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

        // Product columns: 0 ID, 1 name, 2 category, 3 price, 4 stock.
        productModel.addColumn("ID");
        productModel.addColumn("Product");
        productModel.addColumn("Category");
        productModel.addColumn("Price");
        productModel.addColumn("Stock");

        // Bill columns: 0 bill number, 5 approval status, 6 payment status, 8 client.
        billModel.addColumn("Bill #");
        billModel.addColumn("Client");
        billModel.addColumn("Type");
        billModel.addColumn("Description");
        billModel.addColumn("Amount");
        billModel.addColumn("Approval Status");
        billModel.addColumn("Payment Status");
        billModel.addColumn("Date");
        billModel.addColumn("Client Username");

        // Transaction columns: 0 receipt number, 1 bill number, 6 client username.
        transactionModel.addColumn("Receipt #");
        transactionModel.addColumn("Bill #");
        transactionModel.addColumn("Client");
        transactionModel.addColumn("Amount");
        transactionModel.addColumn("Method");
        transactionModel.addColumn("Date");
        transactionModel.addColumn("Client Username");

        // Starting product inventory.
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

    /** Returns the shared appointment table. */
    public DefaultTableModel getAppointmentModel() {
        return appointmentModel;
    }

    /** Returns the shared product inventory table. */
    public DefaultTableModel getProductModel() {
        return productModel;
    }

    /** Returns the shared bill table. */
    public DefaultTableModel getBillModel() {
        return billModel;
    }

    /** Returns the shared payment transaction table. */
    public DefaultTableModel getTransactionModel() {
        return transactionModel;
    }

    /**
     * Returns the cart for one client, creating an empty cart the first time.
     * Usernames are normalized so "Juan" and "juan" share the same cart.
     */
    public synchronized DefaultTableModel getCartModel(String clientUsername) {
        String key = normalizeUsername(clientUsername);

        DefaultTableModel model = clientCartModels.get(key);

        if (model == null) {
            model = new DefaultTableModel();

            // Cart columns: 0 product ID, 1 name, 2 quantity, 3 subtotal.
            model.addColumn("ID");
            model.addColumn("Product");
            model.addColumn("Qty");
            model.addColumn("Subtotal");

            clientCartModels.put(key, model);
        }

        return model;
    }

    /** Generates the next appointment ID. */
    public int nextAppointmentID() {
        return ++appointmentCount;
    }

    /** Generates the next bill number. */
    public int nextBillID() {
        return ++billCount;
    }

    /** Generates the next receipt number. */
    public int nextReceiptID() {
        return ++receiptCount;
    }

    /** Generates the next product ID. */
    public int nextProductID() {
        return ++productCount;
    }

    /** Remembers which appointment created a service bill. */
    public void linkServiceBill(int billID, int appointmentID) {
        serviceBillAppointmentIDs.put(billID, appointmentID);
    }

    /** Returns the appointment linked to a service bill, or null if there is none. */
    public Integer getAppointmentIDForBill(int billID) {
        return serviceBillAppointmentIDs.get(billID);
    }

    /** Reads the repair/installation status from appointment column 7. */
    public String getServiceStatus(int appointmentID) {
        int row = findAppointmentRow(appointmentID);
        return row < 0 ? null : appointmentModel.getValueAt(row, 7).toString();
    }

    /** Reads the approval status from appointment column 6. */
    public String getAppointmentStatus(int appointmentID) {
        int row = findAppointmentRow(appointmentID);
        return row < 0 ? null : appointmentModel.getValueAt(row, 6).toString();
    }

    /** Updates the repair/installation status for one appointment. */
    public boolean updateServiceStatus(int appointmentID, String status) {
        int row = findAppointmentRow(appointmentID);
        if (row < 0) {
            return false;
        }

        appointmentModel.setValueAt(status, row, 7);
        return true;
    }

    /**
     * Updates the approval status on the appointment and on its service bill.
     * The bill's approval status is stored in column 5.
     */
    public boolean updateAppointmentStatus(int appointmentID, String status) {
        int row = findAppointmentRow(appointmentID);
        if (row < 0) {
            return false;
        }

        appointmentModel.setValueAt(status, row, 6);

        // Keep the matching service bill in sync with the appointment status.
        for (int i = 0; i < billModel.getRowCount(); i++) {
            if ("Service".equals(billModel.getValueAt(i, 2).toString())) {
                int billID = Integer.parseInt(billModel.getValueAt(i, 0).toString());
                Integer linkedID = serviceBillAppointmentIDs.get(billID);

                if (linkedID != null && linkedID == appointmentID) {
                    billModel.setValueAt(status, i, 5);
                }
            }
        }

        return true;
    }

    /** Finds the table row for an appointment ID, or returns -1 when it is missing. */
    private int findAppointmentRow(int appointmentID) {
        for (int i = 0; i < appointmentModel.getRowCount(); i++) {
            int currentID = Integer.parseInt(appointmentModel.getValueAt(i, 0).toString());
            if (currentID == appointmentID) {
                return i;
            }
        }

        return -1;
    }

    /** Converts a username into the key used by the per-client cart map. */
    private String normalizeUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "guest";
        }

        return username.trim().toLowerCase();
    }
}