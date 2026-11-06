public class InstallationService extends AutomotiveService {

    public InstallationService() {
        super("Installation Services");
    }

    @Override
    public double getServicePrice(String serviceDetail) {

        switch (serviceDetail) {
            case "Muffler Installation":
            case "Motorcycle Alarm Installation":
            case "Shock Absorber Installation":
                return 550;

            case "Tire Installation":
            case "Headlight Installation":
                return 325;

            case "Battery Installation":
            case "Side Mirror Installation":
                return 200;

            case "LED Light Installation":
                return 500;

            case "Tail Light Installation":
                return 275;

            case "Horn Installation":
                return 250;

            case "Car Alarm Installation":
            case "Reverse Camera Installation":
                return 1000;

            case "Speaker Installation":
            case "Dashcam Installation":
                return 650;

            case "Brake Pad Installation":
                return 400;

            default:
                return 0;
        }
    }

    @Override
    public String[] getServiceDetails() {

        return new String[]{
                "Muffler Installation",
                "Tire Installation",
                "Battery Installation",
                "LED Light Installation",
                "Headlight Installation",
                "Tail Light Installation",
                "Horn Installation",
                "Side Mirror Installation",
                "Motorcycle Alarm Installation",
                "Car Alarm Installation",
                "Speaker Installation",
                "Dashcam Installation",
                "Reverse Camera Installation",
                "Shock Absorber Installation",
                "Brake Pad Installation"
        };
    }
}