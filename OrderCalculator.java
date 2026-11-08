import javax.swing.table.DefaultTableModel;

public class OrderCalculator {

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