import javax.swing.table.DefaultTableModel;

public class OrderCalculator {
    private double totalAmount;

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

    public boolean validatePayment(String method, String cardName, String cardNumber) {
        if (method.equals("Debit") || method.equals("Credit")) {
            if (cardName.trim().isEmpty() || cardNumber.trim().isEmpty()) {
                return false;
            }

            if (!cardNumber.trim().matches("\\d{12,19}")) {
                return false;
            }
        }
        return true;
    }
}