import javax.swing.UIManager;
import java.awt.Color;

public class Theme {

    /** Applies FlatLaf and the shared colors, rounded corners, and table styling. */
    public static void init() {
        try {
            // FlatLaf = a free library that makes Swing look modern (flat, clean, rounded)
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        } catch (Exception e) {
            System.out.println("FlatLaf library not found - using the normal look. See README.txt, step 1.");
            return;
        }

        Color blue = new Color(0x1456A0);
        Color text = new Color(0x172033);
        Color page = new Color(0xF3F6FB);

        // rounded corners
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);              // combo boxes, spinners
        UIManager.put("TextComponent.arc", 12);          // text fields

        // blue focus outline and tabs
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.focusColor", blue);
        UIManager.put("TabbedPane.underlineColor", blue);
        UIManager.put("TabbedPane.selectedForeground", blue);
        UIManager.put("TabbedPane.tabHeight", 40);

        // tables: taller rows, light header, soft blue selection
        UIManager.put("Table.rowHeight", 30);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.selectionBackground", new Color(0xDCE8F8));
        UIManager.put("Table.selectionForeground", text);
        UIManager.put("TableHeader.background", page);
        UIManager.put("TableHeader.foreground", text);
        UIManager.put("TableHeader.bottomSeparatorColor", new Color(0xD1D5DB));

        // slim, rounded scroll bars
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 12);
    }
}
