import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.Map;

public class AutomotiveData {
    private static final AutomotiveData INSTANCE = new AutomotiveData();

    public static AutomotiveData getInstance() {
        return INSTANCE;
    }
    private final DefaultTableModel appointmentModel = new DefaultTableModel();
    private final DefaultTableModel productModel = new DefaultTableModel();
    private final DefaultTableModel billModel = new DefaultTableModel();
    private final DefaultTableModel transactionModel = new DefaultTableModel();

    private final Map<String, DefaultTableModel> clientCartModels = new HashMap<>();

    private int appointmentCount = 0;
    private int billCount = 0;
    private int receiptCount = 0;
    private int productCount = 0;

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

        productModel.addColumn("ID");
        productModel.addColumn("Product");
        productModel.addColumn("Category");
        productModel.addColumn("Price");
        productModel.addColumn("Stock");

        billModel.addColumn("Bill #");
        billModel.addColumn("Client");
        billModel.addColumn("Type");
        billModel.addColumn("Description");
        billModel.addColumn("Amount");
        billModel.addColumn("Approval Status");
        billModel.addColumn("Payment Status");
        billModel.addColumn("Date");
        billModel.addColumn("Client Username");

        transactionModel.addColumn("Receipt #");
        transactionModel.addColumn("Bill #");
        transactionModel.addColumn("Client");
        transactionModel.addColumn("Amount");
        transactionModel.addColumn("Method");
        transactionModel.addColumn("Date");
        transactionModel.addColumn("Client Username");

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

    public void linkServiceBill(int billID, int appointmentID) {
        serviceBillAppointmentIDs.put(billID, appointmentID);
    }

    public Integer getAppointmentIDForBill(int billID) {
        return serviceBillAppointmentIDs.get(billID);
    }

    public String getServiceStatus(int appointmentID) {
        for(int i = 0; i <appointmentModel.getRowCount(); i++){
            int currentID = Integer.parseInt(appointmentModel.getValueAt(i, 0).toString());
            if (currentID == appointmentID) {
                return appointmentModel.getValueAt(i, 7).toString();
            }
        }
        return null;
    }

    public String getAppointmentStatus(int appointmentID) {
        for (int i = 0; i < appointmentModel.getRowCount(); i++) {
            int currentID = Integer.parseInt(appointmentModel.getValueAt(i, 0).toString());
            if (currentID == appointmentID) {
                return appointmentModel.getValueAt(i, 6).toString();
            }
        }
        return null;
    }


    public boolean getServiceStatus(int appointmentID, String status){
        boolean found = false;
        for(int i=0; i< appointmentModel.getRowCount(); i++){
            int currentID = Integer.parseInt(appointmentModel.getValueAt(i, 0).toString());
            if(currentID == appointmentID){
                appointmentModel.setValueAt(status, i, 7);
                found = true;
                break;
            }
        }
        return found;
    }

    public boolean getAppointmentStatus(int appointmentID, String status) {
        boolean found = false;

        for (int i = 0; i < appointmentModel.getRowCount(); i++) {

            int currentID = Integer.parseInt(appointmentModel.getValueAt(i, 0).toString());

            if (currentID == appointmentID) {
                appointmentModel.setValueAt(status, i, 6);
                found = true;
                break;
            }
        }

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

    private String normalizeUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "guest";
        }

        return username.trim().toLowerCase();
    }
}