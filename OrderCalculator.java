import javax.swing.table.DefaultTableModel;

public class OrderCalculator {
    private double totalAmount;
    private boolean paymentSuccessful;
    private String selectedMethod;

    public void computeOrder(double price, int quantity) {
        totalAmount = price * quantity;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double calculateCartTotal(DefaultTableModel cartModel) {
        double total = 0;

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            double subtotal = Double.parseDouble(
                    cartModel.getValueAt(i, 3).toString()
            );
            total += subtotal;
        }

        return total;
    }

    public String formatAmount(double amount) {
        return String.format("₱%.2f", amount);
    }

    public boolean validatePayment(String method, String cardName, String cardNumber) {
        selectedMethod = method;

        if (method.equals("Debit") || method.equals("Credit")) {
            if (cardName.trim().isEmpty() || cardNumber.trim().isEmpty()) {
                return false;
            }

            if (!cardNumber.trim().matches("\\d{12,19}")) {
                return false;
            }
        }

        paymentSuccessful = true;
        return true;
    }

    public boolean isPaymentSuccessful() {
        return paymentSuccessful;
    }

    public String getSelectedMethod() {
        return selectedMethod;
    }
}
