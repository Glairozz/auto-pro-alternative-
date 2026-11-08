import javax.swing.table.DefaultTableModel;

/**
 * Calculates money values for the product cart.
 */
public class OrderCalculator {

    /**
     * Adds every subtotal in the cart table.
     * Column 3 of each row stores the subtotal for that product.
     */
    public static double calculateCartTotal(DefaultTableModel cartModel) {
        double total = 0;

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            double subtotal = Double.parseDouble(
                    cartModel.getValueAt(i, 3).toString()
            );
            total += subtotal;
        }
        return total;
    }
}