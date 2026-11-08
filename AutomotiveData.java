import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.Map;

// Shared in-memory data for the whole app.
public class AutomotiveData {
    private static final AutomotiveData INSTANCE = new AutomotiveData();

    public static AutomotiveData getInstance() {
        return INSTANCE;
    }

    // Shared tables.
    private final DefaultTableModel appointmentModel = new DefaultTableModel();
    private final DefaultTableModel productModel = new DefaultTableModel();
    private final DefaultTableModel billModel = new DefaultTableModel();
    private final DefaultTableModel transactionModel = new DefaultTableModel();

    // One cart per client.
    private final Map<String, DefaultTableModel> clientCartModels = new HashMap<>();

    // Next IDs.
    private int appointmentCount = 0;
    private int billCount = 0;
    private int receiptCount = 0;
    private int productCount = 0;

    // Maps service bill numbers to appointment IDs.
    private final Map<Integer, Integer> serviceBillAppointmentIDs = new HashMap<>();

    private AutomotiveData() {

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

        // Starting products.
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
        }
        productCount = products.length;
    }

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

    // Returns the client's cart, creating it on first use.
    public synchronized DefaultTableModel getCartModel(String clientUsername) {
        String key = clientUsername == null || clientUsername.trim().isEmpty()
                ? "guest"
                : clientUsername.trim().toLowerCase();

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

    public int nextAppointmentID() {
        return ++appointmentCount;
    }

    public int nextBillID() {
        return ++billCount;
    }

    public int nextReceiptID() {
        return ++receiptCount;
    }

    // Product IDs stay unique even when product rows are deleted.
    public int nextProductID() {
        return ++productCount;
    }

    // Remembers which appointment created a service bill.
    public void linkServiceBill(int billID, int appointmentID) {
        serviceBillAppointmentIDs.put(billID, appointmentID);
    }

    public Integer getAppointmentIDForBill(int billID) {
        return serviceBillAppointmentIDs.get(billID);
    }

    public String getServiceStatus(int appointmentID) {
        int row = findAppointmentRow(appointmentID);
        return row < 0 ? null : appointmentModel.getValueAt(row, 7).toString();
    }

    public String getAppointmentStatus(int appointmentID) {
        int row = findAppointmentRow(appointmentID);
        return row < 0 ? null : appointmentModel.getValueAt(row, 6).toString();
    }

    public boolean updateServiceStatus(int appointmentID, String status) {
        int row = findAppointmentRow(appointmentID);
        if (row < 0) {
            return false;
        }

        appointmentModel.setValueAt(status, row, 7);
        return true;
    }

    // Updates the appointment and keeps its service bill in sync.
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

    // Returns the appointment row, or -1 if it is not found.
    private int findAppointmentRow(int appointmentID) {
        for (int i = 0; i < appointmentModel.getRowCount(); i++) {
            int currentID = Integer.parseInt(appointmentModel.getValueAt(i, 0).toString());
            if (currentID == appointmentID) {
                return i;
            }
        }

        return -1;
    }
}
