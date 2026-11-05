import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Single source of truth for every shared record in the application.
 *
 * The Admin and Client screens never own their own copies of these records.
 * They receive the same AutomotiveData instance and bind their JTables to the
 * models returned by this class.
 */
public class AutomotiveData {

    // =========================================================
    // SHARED STATUS VALUES
    // =========================================================

    public static final String APPOINTMENT_PENDING = "Pending";
    public static final String APPOINTMENT_APPROVED = "Approved";
    public static final String APPOINTMENT_CANCELLED = "Cancelled";

    public static final String SERVICE_NOT_STARTED = "Not Started";
    public static final String SERVICE_IN_PROGRESS = "In Progress";
    public static final String SERVICE_COMPLETED = "Completed";
    public static final String SERVICE_CANCELLED = "Cancelled";

    private static final String BILL_TYPE_SERVICE = "Service";
    private static final String BILL_TYPE_PRODUCT = "Product";

    private static final String BILL_APPROVAL_PENDING = "Pending";
    private static final String BILL_APPROVAL_NOT_REQUIRED = "Not Required";
    private static final String BILL_UNPAID = "Unpaid";
    private static final String BILL_PAID = "Paid";

    private static final String DATE_FORMAT = "dd/MM/yyyy hh:mm a";

    // =========================================================
    // CENTRAL TABLE MODELS
    // =========================================================

    private final DefaultTableModel appointmentModel = new DefaultTableModel();
    private final DefaultTableModel productModel = new DefaultTableModel();
    private final DefaultTableModel billModel = new DefaultTableModel();
    private final DefaultTableModel transactionModel = new DefaultTableModel();
    private final DefaultTableModel clientAccountModel = new DefaultTableModel();
    private final DefaultTableModel adminAccountModel = new DefaultTableModel();

    // Each registered client gets exactly one cart model in this map.
    private final Map<String, DefaultTableModel> clientCartModels = new HashMap<>();

    // Connects a service bill to the appointment that created it.
    private final Map<Integer, Integer> serviceBillAppointmentIDs = new HashMap<>();

    // =========================================================
    // ID COUNTERS
    // =========================================================

    private int appointmentCount = 0;
    private int billCount = 0;
    private int receiptCount = 0;
    private int productCount = 0;
    private int customerCount = 1000;

    // Message from the most recent failed payment attempt.
    private String lastPaymentError;
    private int lastReceiptID = -1;

    public AutomotiveData() {
        createAppointmentColumns();
        createProductColumns();
        createBillColumns();
        createTransactionColumns();
        createClientAccountColumns();
        createAdminAccountColumns();

        seedAdminAccounts();
        seedDefaultProducts();
    }

    // =========================================================
    // A. USER MANAGEMENT
    // =========================================================

    private void createClientAccountColumns() {
        clientAccountModel.addColumn("Username");
        clientAccountModel.addColumn("Password");
        clientAccountModel.addColumn("Name");
        clientAccountModel.addColumn("Customer ID");
        clientAccountModel.addColumn("Appointments");
        clientAccountModel.addColumn("Bills");
        clientAccountModel.addColumn("Transaction Records");
    }

    private void createAdminAccountColumns() {
        adminAccountModel.addColumn("Username");
        adminAccountModel.addColumn("Password");
    }

    private void seedAdminAccounts() {
        adminAccountModel.addRow(new Object[]{"admin", "admin123"});
        adminAccountModel.addRow(new Object[]{"Jervey", "bossRica"});
        adminAccountModel.addRow(new Object[]{"Blair", "ILoveYouMaam"});
        adminAccountModel.addRow(new Object[]{"Liam", "Uarai"});
    }

    /**
     * Registers one client account.
     *
     * @return the new customer ID, or -1 when the account is invalid or the
     *         username is already used by an admin/client.
     */
    public int registerClient(String username, String password, String name) {
        String cleanUsername = username == null ? "" : username.trim();
        String cleanPassword = password == null ? "" : password;
        String cleanName = name == null ? "" : name.trim();

        if (cleanUsername.isEmpty()
                || cleanPassword.trim().isEmpty()
                || cleanName.isEmpty()) {
            return -1;
        }

        if (isUsernameTaken(cleanUsername)) {
            return -1;
        }

        int customerID = nextCustomerID();
        clientAccountModel.addRow(new Object[]{
                cleanUsername,
                cleanPassword,
                cleanName,
                String.valueOf(customerID),
                "No Appointments",
                "No Bills",
                "No Transactions"
        });

        return customerID;
    }

    public boolean isUsernameTaken(String username) {
        return isAdminUsername(username) || isClientUsername(username);
    }

    public boolean isAdminUsername(String username) {
        String value = username == null ? "" : username.trim();

        for (int row = 0; row < adminAccountModel.getRowCount(); row++) {
            if (value.equalsIgnoreCase(
                    String.valueOf(adminAccountModel.getValueAt(row, 0))
            )) {
                return true;
            }
        }

        return false;
    }

    public boolean isClientUsername(String username) {
        return getClientRow(username) != -1;
    }

    public boolean validateClient(String username, String password) {
        String user = username == null ? "" : username.trim();
        String pass = password == null ? "" : password;

        if (user.isEmpty() || pass.isEmpty()) {
            return false;
        }

        for (int row = 0; row < clientAccountModel.getRowCount(); row++) {
            String savedUser = String.valueOf(clientAccountModel.getValueAt(row, 0));
            String savedPass = String.valueOf(clientAccountModel.getValueAt(row, 1));

            if (user.equalsIgnoreCase(savedUser) && pass.equals(savedPass)) {
                return true;
            }
        }

        return false;
    }

    public boolean validateAdmin(String username, String password) {
        String user = username == null ? "" : username.trim();
        String pass = password == null ? "" : password;

        if (user.isEmpty() || pass.isEmpty()) {
            return false;
        }

        for (int row = 0; row < adminAccountModel.getRowCount(); row++) {
            String savedUser = String.valueOf(adminAccountModel.getValueAt(row, 0));
            String savedPass = String.valueOf(adminAccountModel.getValueAt(row, 1));

            if (user.equalsIgnoreCase(savedUser) && pass.equals(savedPass)) {
                return true;
            }
        }

        return false;
    }

    public String getClientName(String username) {
        int row = getClientRow(username);
        return row == -1
                ? null
                : String.valueOf(clientAccountModel.getValueAt(row, 2));
    }

    public String getCustomerID(String username) {
        int row = getClientRow(username);
        return row == -1
                ? null
                : String.valueOf(clientAccountModel.getValueAt(row, 3));
    }

    public String getUsernameByCustomerID(String customerID) {
        String id = customerID == null ? "" : customerID.trim();

        for (int row = 0; row < clientAccountModel.getRowCount(); row++) {
            if (id.equals(String.valueOf(clientAccountModel.getValueAt(row, 3)))) {
                return String.valueOf(clientAccountModel.getValueAt(row, 0));
            }
        }

        return null;
    }

    public int getClientRow(String username) {
        String value = username == null ? "" : username.trim();

        for (int row = 0; row < clientAccountModel.getRowCount(); row++) {
            if (value.equalsIgnoreCase(
                    String.valueOf(clientAccountModel.getValueAt(row, 0))
            )) {
                return row;
            }
        }

        return -1;
    }

    public DefaultTableModel getClientAccountModel() {
        return clientAccountModel;
    }

    public DefaultTableModel getAdminAccountModel() {
        return adminAccountModel;
    }

    public Vector<Vector> getRegisteredClients() {
        return clientAccountModel.getDataVector();
    }

    // =========================================================
    // B. APPOINTMENT MANAGEMENT
    // =========================================================

    private void createAppointmentColumns() {
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
    }

    /**
     * Creates an appointment and its matching service bill in one operation.
     *
     * @return the new appointment ID, or -1 when the data is invalid.
     */
    public int createAppointment(String clientUsername,
                                 String clientName,
                                 String serviceName,
                                 String vehicle,
                                 String details,
                                 String schedule,
                                 double estimate) {
        String username = clientUsername == null ? "" : clientUsername.trim();
        String name = clientName == null ? "" : clientName.trim();
        String service = serviceName == null ? "" : serviceName.trim();
        String vehicleInfo = vehicle == null ? "" : vehicle.trim();
        String serviceDetail = details == null ? "" : details.trim();
        String appointmentSchedule = schedule == null ? "" : schedule.trim();

        if (username.isEmpty()
                || name.isEmpty()
                || service.isEmpty()
                || vehicleInfo.isEmpty()
                || serviceDetail.isEmpty()
                || appointmentSchedule.isEmpty()
                || estimate <= 0) {
            return -1;
        }

        if (hasDuplicatePendingAppointment(
                username,
                service,
                vehicleInfo,
                serviceDetail,
                appointmentSchedule
        )) {
            return -1;
        }

        int appointmentID = nextAppointmentID();

        appointmentModel.addRow(new Object[]{
                appointmentID,
                name,
                service,
                vehicleInfo,
                serviceDetail,
                appointmentSchedule,
                APPOINTMENT_PENDING,
                SERVICE_NOT_STARTED,
                formatPeso(estimate),
                username
        });

        int billID = createServiceBill(
                username,
                name,
                service,
                serviceDetail,
                vehicleInfo,
                estimate
        );

        linkServiceBill(billID, appointmentID);
        return appointmentID;
    }

    /**
     * Prevents an accidental duplicate appointment from repeated button clicks.
     */
    public boolean hasDuplicatePendingAppointment(String clientUsername,
                                                  String serviceName,
                                                  String vehicle,
                                                  String details,
                                                  String schedule) {
        String username = normalizeUsername(clientUsername);
        String service = serviceName == null ? "" : serviceName.trim();
        String vehicleInfo = vehicle == null ? "" : vehicle.trim();
        String serviceDetail = details == null ? "" : details.trim();
        String appointmentSchedule = schedule == null ? "" : schedule.trim();

        for (int row = 0; row < appointmentModel.getRowCount(); row++) {
            if (!username.equalsIgnoreCase(
                    String.valueOf(appointmentModel.getValueAt(row, 9))
            )) {
                continue;
            }

            if (!APPOINTMENT_PENDING.equals(
                    String.valueOf(appointmentModel.getValueAt(row, 6))
            )) {
                continue;
            }

            if (service.equals(String.valueOf(appointmentModel.getValueAt(row, 2)))
                    && vehicleInfo.equals(String.valueOf(appointmentModel.getValueAt(row, 3)))
                    && serviceDetail.equals(String.valueOf(appointmentModel.getValueAt(row, 4)))
                    && appointmentSchedule.equals(String.valueOf(appointmentModel.getValueAt(row, 5)))) {
                return true;
            }
        }

        return false;
    }

    public String getAppointmentStatus(int appointmentID) {
        int row = getAppointmentRow(appointmentID);
        return row == -1
                ? null
                : String.valueOf(appointmentModel.getValueAt(row, 6));
    }

    public String getServiceStatus(int appointmentID) {
        int row = getAppointmentRow(appointmentID);
        return row == -1
                ? null
                : String.valueOf(appointmentModel.getValueAt(row, 7));
    }

    public String getServiceStatusForBill(int billID) {
        Integer appointmentID = serviceBillAppointmentIDs.get(billID);

        if (appointmentID == null) {
            return null;
        }

        return getServiceStatus(appointmentID);
    }

    public int getAppointmentRow(int appointmentID) {
        return findRowByID(appointmentModel, appointmentID, 0);
    }

    /**
     * Updates the appointment status and keeps the linked service bill in sync.
     */
    public boolean updateAppointmentStatus(int appointmentID, String status) {
        if (!isValidAppointmentStatus(status)) {
            return false;
        }

        int row = getAppointmentRow(appointmentID);

        if (row == -1) {
            return false;
        }

        String currentStatus = String.valueOf(
                appointmentModel.getValueAt(row, 6)
        );

        if (APPOINTMENT_CANCELLED.equals(currentStatus)
                && !APPOINTMENT_CANCELLED.equals(status)) {
            return false;
        }

        if (APPOINTMENT_APPROVED.equals(status)
                && !APPOINTMENT_PENDING.equals(currentStatus)) {
            return false;
        }

        if (APPOINTMENT_APPROVED.equals(status)
                && SERVICE_CANCELLED.equals(
                String.valueOf(appointmentModel.getValueAt(row, 7))
        )) {
            return false;
        }

        if (APPOINTMENT_PENDING.equals(status)
                && !APPOINTMENT_PENDING.equals(currentStatus)) {
            return false;
        }

        appointmentModel.setValueAt(status, row, 6);

        // A cancelled appointment cannot continue as active work. A completed
        // service keeps its completion record for history.
        if (APPOINTMENT_CANCELLED.equals(status)) {
            String serviceStatus = String.valueOf(
                    appointmentModel.getValueAt(row, 7)
            );

            if (!SERVICE_COMPLETED.equals(serviceStatus)) {
                appointmentModel.setValueAt(SERVICE_CANCELLED, row, 7);
            }
        }

        updateLinkedBillApproval(appointmentID, status);
        return true;
    }

    public boolean approveAppointment(int appointmentID) {
        if (!APPOINTMENT_PENDING.equals(getAppointmentStatus(appointmentID))) {
            return false;
        }

        if (SERVICE_CANCELLED.equals(getServiceStatus(appointmentID))) {
            return false;
        }

        return updateAppointmentStatus(appointmentID, APPOINTMENT_APPROVED);
    }

    public boolean cancelAppointment(int appointmentID) {
        String status = getAppointmentStatus(appointmentID);

        if (status == null || APPOINTMENT_CANCELLED.equals(status)) {
            return false;
        }

        return updateAppointmentStatus(appointmentID, APPOINTMENT_CANCELLED);
    }

    /**
     * Updates only the service work status. The appointment status is never
     * changed here.
     */
    public boolean updateServiceStatus(int appointmentID, String serviceStatus) {
        if (!isValidServiceStatus(serviceStatus)) {
            return false;
        }

        int row = getAppointmentRow(appointmentID);

        if (row == -1) {
            return false;
        }

        String appointmentStatus = String.valueOf(
                appointmentModel.getValueAt(row, 6)
        );
        String existingServiceStatus = String.valueOf(
                appointmentModel.getValueAt(row, 7)
        );

        if (SERVICE_CANCELLED.equals(existingServiceStatus)
                || APPOINTMENT_CANCELLED.equals(appointmentStatus)) {
            return false;
        }

        // Work cannot start or finish before the appointment is approved.
        if (!APPOINTMENT_APPROVED.equals(appointmentStatus)
                && (SERVICE_IN_PROGRESS.equals(serviceStatus)
                || SERVICE_COMPLETED.equals(serviceStatus))) {
            return false;
        }

        appointmentModel.setValueAt(serviceStatus, row, 7);
        return true;
    }

    /**
     * Cancels the service while keeping both records for history.
     */
    public boolean cancelService(int appointmentID) {
        int row = getAppointmentRow(appointmentID);

        if (row == -1) {
            return false;
        }

        String serviceStatus = String.valueOf(
                appointmentModel.getValueAt(row, 7)
        );

        if (SERVICE_COMPLETED.equals(serviceStatus)
                || SERVICE_CANCELLED.equals(serviceStatus)) {
            return false;
        }

        appointmentModel.setValueAt(SERVICE_CANCELLED, row, 7);
        appointmentModel.setValueAt(APPOINTMENT_CANCELLED, row, 6);
        updateLinkedBillApproval(appointmentID, APPOINTMENT_CANCELLED);
        return true;
    }

    public DefaultTableModel getAppointmentModel() {
        return appointmentModel;
    }

    public Vector<Vector> getAppointmentRecords() {
        return appointmentModel.getDataVector();
    }

    public Vector<Vector> getAppointmentRecordsForClient(String clientUsername) {
        Vector<Vector> records = new Vector<>();
        String username = normalizeUsername(clientUsername);

        for (int row = 0; row < appointmentModel.getRowCount(); row++) {
            if (username.equalsIgnoreCase(
                    String.valueOf(appointmentModel.getValueAt(row, 9))
            )) {
                records.add((Vector) appointmentModel.getDataVector().get(row));
            }
        }

        return records;
    }

    private boolean isValidAppointmentStatus(String status) {
        return APPOINTMENT_PENDING.equals(status)
                || APPOINTMENT_APPROVED.equals(status)
                || APPOINTMENT_CANCELLED.equals(status);
    }

    private boolean isValidServiceStatus(String status) {
        return SERVICE_NOT_STARTED.equals(status)
                || SERVICE_IN_PROGRESS.equals(status)
                || SERVICE_COMPLETED.equals(status);
    }

    // =========================================================
    // C. PRODUCT MANAGEMENT
    // =========================================================

    private void createProductColumns() {
        productModel.addColumn("ID");
        productModel.addColumn("Product");
        productModel.addColumn("Category");
        productModel.addColumn("Price");
        productModel.addColumn("Stock");
    }

    private void seedDefaultProducts() {
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

        productCount = findMaximumID(productModel, 0);
    }

    /**
     * Adds one product and returns its new ID, or -1 on invalid/duplicate data.
     */
    public int addProduct(String name, String category, double price, int stock) {
        String productName = name == null ? "" : name.trim();
        String productCategory = category == null ? "" : category.trim();

        if (productName.isEmpty()
                || productCategory.isEmpty()
                || price < 0
                || stock < 0
                || hasProductNamed(productName)) {
            return -1;
        }

        int productID = nextProductID();
        productModel.addRow(new Object[]{
                productID,
                productName,
                productCategory,
                formatPlainAmount(price),
                stock
        });

        return productID;
    }

    public boolean updateProduct(int productID,
                                 String name,
                                 String category,
                                 double price,
                                 int stock) {
        int row = getProductRow(productID);
        String productName = name == null ? "" : name.trim();
        String productCategory = category == null ? "" : category.trim();

        if (row == -1
                || productName.isEmpty()
                || productCategory.isEmpty()
                || price < 0
                || stock < 0
                || hasProductNamedExcept(productName, productID)) {
            return false;
        }

        productModel.setValueAt(productName, row, 1);
        productModel.setValueAt(productCategory, row, 2);
        productModel.setValueAt(formatPlainAmount(price), row, 3);
        productModel.setValueAt(stock, row, 4);
        return true;
    }

    public boolean deleteProduct(int productID) {
        int row = getProductRow(productID);

        if (row == -1) {
            return false;
        }

        productModel.removeRow(row);

        // Remove the deleted product from every client's cart so no cart keeps
        // a reference to a product that no longer exists.
        for (DefaultTableModel cartModel : clientCartModels.values()) {
            for (int cartRow = cartModel.getRowCount() - 1; cartRow >= 0; cartRow--) {
                if (String.valueOf(productID).equals(
                        String.valueOf(cartModel.getValueAt(cartRow, 0))
                )) {
                    cartModel.removeRow(cartRow);
                }
            }
        }

        return true;
    }

    public boolean hasProductNamed(String name) {
        String productName = name == null ? "" : name.trim();

        for (int row = 0; row < productModel.getRowCount(); row++) {
            if (productName.equalsIgnoreCase(
                    String.valueOf(productModel.getValueAt(row, 1))
            )) {
                return true;
            }
        }

        return false;
    }

    public boolean hasProductNamedExcept(String name, int excludedProductID) {
        String productName = name == null ? "" : name.trim();

        for (int row = 0; row < productModel.getRowCount(); row++) {
            int currentID = Integer.parseInt(
                    String.valueOf(productModel.getValueAt(row, 0))
            );

            if (currentID != excludedProductID
                    && productName.equalsIgnoreCase(
                    String.valueOf(productModel.getValueAt(row, 1))
            )) {
                return true;
            }
        }

        return false;
    }

    public int getProductRow(int productID) {
        return findRowByID(productModel, productID, 0);
    }

    public int getProductStock(int productID) {
        int row = getProductRow(productID);
        return row == -1
                ? -1
                : Integer.parseInt(String.valueOf(productModel.getValueAt(row, 4)));
    }

    public boolean updateProductStock(int productID, int newStock) {
        int row = getProductRow(productID);

        if (row == -1 || newStock < 0) {
            return false;
        }

        productModel.setValueAt(newStock, row, 4);
        return true;
    }

    public DefaultTableModel getProductModel() {
        return productModel;
    }

    public Vector<Vector> getProductRecords() {
        return productModel.getDataVector();
    }

    // =========================================================
    // D. SHOPPING CART MANAGEMENT
    // =========================================================

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

    /**
     * Adds a product to one client's cart. Adding the same product again merges
     * the quantities instead of creating a duplicate cart row.
     *
     * @return null on success, or a user-facing error message.
     */
    public synchronized String addProductToCart(String clientUsername,
                                                int productID,
                                                int quantity) {
        if (quantity <= 0) {
            return "Please enter a valid quantity.";
        }

        int productRow = getProductRow(productID);

        if (productRow == -1) {
            return "The selected product could not be found.";
        }

        int stock = Integer.parseInt(
                String.valueOf(productModel.getValueAt(productRow, 4))
        );

        if (quantity > stock) {
            return "Not enough stock available.";
        }

        DefaultTableModel cartModel = getCartModel(clientUsername);
        int existingCartRow = findCartRow(cartModel, productID);

        String productName = String.valueOf(
                productModel.getValueAt(productRow, 1)
        );
        double price = parseAmount(
                productModel.getValueAt(productRow, 3)
        );

        if (existingCartRow == -1) {
            cartModel.addRow(new Object[]{
                    productID,
                    productName,
                    quantity,
                    formatPlainAmount(price * quantity)
            });
        } else {
            int existingQuantity = Integer.parseInt(
                    String.valueOf(cartModel.getValueAt(existingCartRow, 2))
            );
            int newQuantity = existingQuantity + quantity;

            cartModel.setValueAt(newQuantity, existingCartRow, 2);
            cartModel.setValueAt(
                    formatPlainAmount(price * newQuantity),
                    existingCartRow,
                    3
            );
        }

        productModel.setValueAt(stock - quantity, productRow, 4);
        return null;
    }

    public synchronized boolean removeProductFromCart(String clientUsername,
                                                      int cartRow) {
        DefaultTableModel cartModel = getCartModel(clientUsername);

        if (cartRow < 0 || cartRow >= cartModel.getRowCount()) {
            return false;
        }

        int productID = Integer.parseInt(
                String.valueOf(cartModel.getValueAt(cartRow, 0))
        );
        int quantity = Integer.parseInt(
                String.valueOf(cartModel.getValueAt(cartRow, 2))
        );

        int productRow = getProductRow(productID);

        if (productRow != -1) {
            int stock = Integer.parseInt(
                    String.valueOf(productModel.getValueAt(productRow, 4))
            );
            productModel.setValueAt(stock + quantity, productRow, 4);
        }

        cartModel.removeRow(cartRow);
        return true;
    }

    public synchronized void clearCart(String clientUsername) {
        getCartModel(clientUsername).setRowCount(0);
    }

    public synchronized double getCartTotal(String clientUsername) {
        DefaultTableModel cartModel = getCartModel(clientUsername);
        double total = 0;

        for (int row = 0; row < cartModel.getRowCount(); row++) {
            total += parseAmount(cartModel.getValueAt(row, 3));
        }

        return total;
    }

    public Vector<Vector> getCartRecords(String clientUsername) {
        return getCartModel(clientUsername).getDataVector();
    }

    public Vector<Vector> getCartRecords() {
        return getCartModel().getDataVector();
    }

    private int findCartRow(DefaultTableModel cartModel, int productID) {
        for (int row = 0; row < cartModel.getRowCount(); row++) {
            if (String.valueOf(productID).equals(
                    String.valueOf(cartModel.getValueAt(row, 0))
            )) {
                return row;
            }
        }

        return -1;
    }

    // =========================================================
    // E. BILLING MANAGEMENT
    // =========================================================

    private void createBillColumns() {
        billModel.addColumn("Bill #");
        billModel.addColumn("Client");
        billModel.addColumn("Type");
        billModel.addColumn("Description");
        billModel.addColumn("Amount");
        billModel.addColumn("Approval Status");
        billModel.addColumn("Payment Status");
        billModel.addColumn("Date");
        billModel.addColumn("Client Username");
    }

    /**
     * Creates the service bill that belongs to a newly created appointment.
     */
    private int createServiceBill(String clientUsername,
                                  String clientName,
                                  String serviceName,
                                  String details,
                                  String vehicle,
                                  double estimate) {
        int billID = nextBillID();
        String description = serviceName + " - " + details + " (" + vehicle + ")";

        billModel.addRow(new Object[]{
                billID,
                clientName,
                BILL_TYPE_SERVICE,
                description,
                formatPeso(estimate),
                BILL_APPROVAL_PENDING,
                BILL_UNPAID,
                getCurrentTimestamp(),
                clientUsername
        });

        return billID;
    }

    /**
     * Creates a product bill. Product bills never require appointment approval.
     */
    public int createProductBill(String clientUsername,
                                 String description,
                                 double amount) {
        String username = clientUsername == null ? "" : clientUsername.trim();
        String billDescription = description == null ? "" : description.trim();

        if (username.isEmpty() || billDescription.isEmpty() || amount < 0) {
            return -1;
        }

        String clientName = getClientName(username);

        if (clientName == null) {
            clientName = username;
        }

        int billID = nextBillID();
        billModel.addRow(new Object[]{
                billID,
                clientName,
                BILL_TYPE_PRODUCT,
                billDescription,
                formatPeso(amount),
                BILL_APPROVAL_NOT_REQUIRED,
                BILL_UNPAID,
                getCurrentTimestamp(),
                username
        });

        return billID;
    }

    public int getBillRow(int billID) {
        return findRowByID(billModel, billID, 0);
    }

    public String getBillType(int billID) {
        int row = getBillRow(billID);
        return row == -1
                ? null
                : String.valueOf(billModel.getValueAt(row, 2));
    }

    public String getBillApprovalStatus(int billID) {
        int row = getBillRow(billID);
        return row == -1
                ? null
                : String.valueOf(billModel.getValueAt(row, 5));
    }

    public String getBillPaymentStatus(int billID) {
        int row = getBillRow(billID);
        return row == -1
                ? null
                : String.valueOf(billModel.getValueAt(row, 6));
    }

    public double getBillAmount(int billID) {
        int row = getBillRow(billID);
        return row == -1 ? 0 : parseAmount(billModel.getValueAt(row, 4));
    }

    public boolean isBillPaid(int billID) {
        return BILL_PAID.equals(getBillPaymentStatus(billID));
    }

    public boolean isBillOwnedByClient(int billID, String clientUsername) {
        int row = getBillRow(billID);

        if (row == -1) {
            return false;
        }

        return normalizeUsername(clientUsername).equalsIgnoreCase(
                String.valueOf(billModel.getValueAt(row, 8))
        );
    }

    public boolean isServiceBillPayable(int billID) {
        return getPaymentBlockReason(billID, null) == null;
    }

    /**
     * Returns the reason a service bill cannot be paid, or null when it can.
     */
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

        if (!APPOINTMENT_APPROVED.equals(appointmentStatus)) {
            return "Your service appointment must be approved by the admin first."
                    + "\nCurrent Appointment Status: " + appointmentStatus;
        }

        if (!SERVICE_COMPLETED.equals(serviceStatus)) {
            return "The service work must be completed before this bill can be paid."
                    + "\nCurrent Service Status: " + serviceStatus;
        }

        return null;
    }

    /**
     * Full payment validation for any bill type, including ownership and the
     * service workflow rules.
     */
    public String getPaymentBlockReason(int billID, String clientUsername) {
        int row = getBillRow(billID);

        if (row == -1) {
            return "The selected bill could not be found.";
        }

        if (clientUsername != null
                && !isBillOwnedByClient(billID, clientUsername)) {
            return "You can only pay your own bills.";
        }

        if (BILL_PAID.equals(String.valueOf(billModel.getValueAt(row, 6)))) {
            return "This bill has already been paid.";
        }

        String billType = String.valueOf(billModel.getValueAt(row, 2));

        if (BILL_TYPE_PRODUCT.equals(billType)) {
            return null;
        }

        if (!BILL_TYPE_SERVICE.equals(billType)) {
            return "This bill type cannot be paid.";
        }

        String serviceReason = getPaymentBlockReason(billID);

        if (serviceReason != null) {
            return serviceReason;
        }

        Integer appointmentID = serviceBillAppointmentIDs.get(billID);

        if (appointmentID == null) {
            return "The linked appointment record could not be found.";
        }

        int appointmentRow = getAppointmentRow(appointmentID);

        if (appointmentRow == -1) {
            return "The linked appointment record could not be found.";
        }

        if (clientUsername != null
                && !normalizeUsername(clientUsername).equalsIgnoreCase(
                String.valueOf(appointmentModel.getValueAt(appointmentRow, 9))
        )) {
            return "The linked appointment does not belong to this client.";
        }

        return null;
    }

    /**
     * Records a successful payment and creates its transaction/receipt row.
     * The validation runs here as well, so payment rules cannot be bypassed by
     * calling this method directly.
     *
     * @return the new receipt ID, or -1 when payment is not allowed.
     */
    public synchronized int payBill(int billID,
                                    String clientUsername,
                                    String paymentMethod) {
        lastPaymentError = null;
        lastReceiptID = -1;

        if (clientUsername == null || clientUsername.trim().isEmpty()) {
            lastPaymentError = "A client account is required to pay a bill.";
            return -1;
        }

        String blockReason = getPaymentBlockReason(billID, clientUsername);

        if (blockReason != null) {
            lastPaymentError = blockReason;
            return -1;
        }

        String method = paymentMethod == null ? "" : paymentMethod.trim();

        if (method.isEmpty()) {
            lastPaymentError = "Please select a payment method.";
            return -1;
        }

        int row = getBillRow(billID);

        if (row == -1) {
            lastPaymentError = "The selected bill could not be found.";
            return -1;
        }

        String clientName = String.valueOf(billModel.getValueAt(row, 1));
        String amount = String.valueOf(billModel.getValueAt(row, 4));

        billModel.setValueAt(BILL_PAID, row, 6);

        int receiptID = nextReceiptID();
        transactionModel.addRow(new Object[]{
                receiptID,
                billID,
                clientName,
                amount,
                method,
                getCurrentTimestamp(),
                normalizeUsername(clientUsername)
        });

        lastReceiptID = receiptID;
        return receiptID;
    }

    public String getLastPaymentError() {
        return lastPaymentError;
    }

    public int getLastReceiptID() {
        return lastReceiptID;
    }

    /**
     * Total unpaid balance for one client only.
     */
    public double getOutstandingBalance(String clientUsername) {
        String username = normalizeUsername(clientUsername);
        double balance = 0;

        for (int row = 0; row < billModel.getRowCount(); row++) {
            if (!username.equalsIgnoreCase(
                    String.valueOf(billModel.getValueAt(row, 8))
            )) {
                continue;
            }

            if (!BILL_UNPAID.equals(
                    String.valueOf(billModel.getValueAt(row, 6))
            )) {
                continue;
            }

            // Cancelled service bills stay in history but are not collectible.
            if (BILL_TYPE_SERVICE.equals(
                    String.valueOf(billModel.getValueAt(row, 2))
            )) {
                Integer appointmentID = serviceBillAppointmentIDs.get(
                        Integer.parseInt(String.valueOf(billModel.getValueAt(row, 0)))
                );

                if (appointmentID != null
                        && APPOINTMENT_CANCELLED.equals(
                        getAppointmentStatus(appointmentID)
                )) {
                    continue;
                }
            }

            balance += parseAmount(billModel.getValueAt(row, 4));
        }

        return balance;
    }

    public DefaultTableModel getBillModel() {
        return billModel;
    }

    public Vector<Vector> getBillRecords() {
        return billModel.getDataVector();
    }

    public Vector<Vector> getBillRecordsForClient(String clientUsername) {
        Vector<Vector> records = new Vector<>();
        String username = normalizeUsername(clientUsername);

        for (int row = 0; row < billModel.getRowCount(); row++) {
            if (username.equalsIgnoreCase(
                    String.valueOf(billModel.getValueAt(row, 8))
            )) {
                records.add((Vector) billModel.getDataVector().get(row));
            }
        }

        return records;
    }

    // =========================================================
    // F. TRANSACTION MANAGEMENT
    // =========================================================

    private void createTransactionColumns() {
        transactionModel.addColumn("Receipt #");
        transactionModel.addColumn("Bill #");
        transactionModel.addColumn("Client");
        transactionModel.addColumn("Amount");
        transactionModel.addColumn("Method");
        transactionModel.addColumn("Date");
        transactionModel.addColumn("Client Username");
    }

    public DefaultTableModel getTransactionModel() {
        return transactionModel;
    }

    public Vector<Vector> getTransactionRecords() {
        return transactionModel.getDataVector();
    }

    public Vector<Vector> getTransactionRecordsForClient(String clientUsername) {
        Vector<Vector> records = new Vector<>();
        String username = normalizeUsername(clientUsername);

        for (int row = 0; row < transactionModel.getRowCount(); row++) {
            if (username.equalsIgnoreCase(
                    String.valueOf(transactionModel.getValueAt(row, 6))
            )) {
                records.add((Vector) transactionModel.getDataVector().get(row));
            }
        }

        return records;
    }

    public int getTransactionRow(int receiptID) {
        return findRowByID(transactionModel, receiptID, 0);
    }

    // =========================================================
    // G. ID MANAGEMENT
    // =========================================================

    public int nextAppointmentID() {
        appointmentCount = Math.max(
                appointmentCount,
                findMaximumID(appointmentModel, 0)
        );
        return ++appointmentCount;
    }

    public int nextBillID() {
        billCount = Math.max(
                billCount,
                findMaximumID(billModel, 0)
        );
        return ++billCount;
    }

    public int nextReceiptID() {
        receiptCount = Math.max(
                receiptCount,
                findMaximumID(transactionModel, 0)
        );
        return ++receiptCount;
    }

    public int nextProductID() {
        productCount = Math.max(
                productCount,
                findMaximumID(productModel, 0)
        );
        return ++productCount;
    }

    private int nextCustomerID() {
        customerCount = Math.max(
                customerCount,
                findMaximumCustomerID()
        );

        do {
            customerCount++;
        } while (findClientRowByCustomerID(customerCount) != -1);

        return customerCount;
    }

    public void resetProductCount(int count) {
        productCount = Math.max(count, findMaximumID(productModel, 0));
    }

    // =========================================================
    // RECORD RELATIONSHIPS
    // =========================================================

    public void linkServiceBill(int billID, int appointmentID) {
        serviceBillAppointmentIDs.put(billID, appointmentID);
    }

    public Integer getAppointmentIDForBill(int billID) {
        return serviceBillAppointmentIDs.get(billID);
    }

    private void updateLinkedBillApproval(int appointmentID, String status) {
        for (int row = 0; row < billModel.getRowCount(); row++) {
            if (!BILL_TYPE_SERVICE.equals(
                    String.valueOf(billModel.getValueAt(row, 2))
            )) {
                continue;
            }

            int billID = Integer.parseInt(
                    String.valueOf(billModel.getValueAt(row, 0))
            );
            Integer linkedAppointmentID = serviceBillAppointmentIDs.get(billID);

            if (linkedAppointmentID != null
                    && linkedAppointmentID == appointmentID) {
                billModel.setValueAt(status, row, 5);
            }
        }
    }

    // =========================================================
    // INTERNAL HELPERS
    // =========================================================

    private int findRowByID(DefaultTableModel model, int id, int idColumn) {
        for (int row = 0; row < model.getRowCount(); row++) {
            Object value = model.getValueAt(row, idColumn);

            if (value == null) {
                continue;
            }

            try {
                if (Integer.parseInt(value.toString().trim()) == id) {
                    return row;
                }
            } catch (NumberFormatException ignored) {
                // Skip rows that do not contain a numeric ID.
            }
        }

        return -1;
    }

    private int findMaximumID(DefaultTableModel model, int idColumn) {
        int maximum = 0;

        for (int row = 0; row < model.getRowCount(); row++) {
            Object value = model.getValueAt(row, idColumn);

            if (value == null) {
                continue;
            }

            try {
                int currentID = Integer.parseInt(value.toString().trim());
                maximum = Math.max(maximum, currentID);
            } catch (NumberFormatException ignored) {
                // Skip rows that do not contain a numeric ID.
            }
        }

        return maximum;
    }

    private int findMaximumCustomerID() {
        int maximum = 1000;

        for (int row = 0; row < clientAccountModel.getRowCount(); row++) {
            try {
                int currentID = Integer.parseInt(
                        String.valueOf(clientAccountModel.getValueAt(row, 3)).trim()
                );
                maximum = Math.max(maximum, currentID);
            } catch (NumberFormatException ignored) {
                // Skip malformed customer IDs.
            }
        }

        return maximum;
    }

    private int findClientRowByCustomerID(int customerID) {
        for (int row = 0; row < clientAccountModel.getRowCount(); row++) {
            try {
                int currentID = Integer.parseInt(
                        String.valueOf(clientAccountModel.getValueAt(row, 3)).trim()
                );

                if (currentID == customerID) {
                    return row;
                }
            } catch (NumberFormatException ignored) {
                // Skip malformed customer IDs.
            }
        }

        return -1;
    }

    private String normalizeUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "guest";
        }

        return username.trim().toLowerCase();
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat(DATE_FORMAT).format(new Date());
    }

    private String formatPeso(double amount) {
        return String.format("₱%.2f", amount);
    }

    private String formatPlainAmount(double amount) {
        return String.format("%.2f", amount);
    }

    private double parseAmount(Object value) {
        if (value == null) {
            return 0;
        }

        String text = value.toString()
                .replace("₱", "")
                .replace(",", "")
                .trim();

        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
