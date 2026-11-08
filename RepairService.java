/**
 * Repair service type.
 * The switch below maps each repair problem to its standard price.
 */
public class RepairService extends AutomotiveService {

    public RepairService() {
        super("Repair Services");
    }

    /** Returns the price for the selected repair problem. */
    @Override
    public double getServicePrice(String serviceDetail) {

        switch (serviceDetail) {
            case "Engine Overheating":
            case "Radiator Problems":
                return 2150;

            case "Engine Failure":
                return 8750;

            case "Brake Problems":
            case "Starting Problems":
                return 1500;

            case "Transmission Problems":
                return 6000;

            case "Clutch Problems":
            case "Fuel System Problems":
                return 2400;

            case "Flat Tire":
                return 300;

            case "Battery Failure":
                return 500;

            case "Electrical Problems":
            case "Exhaust System Problems":
                return 1750;

            case "Air Conditioning Problems":
            case "Steering Problems":
                return 3000;

            case "Suspension Problems":
                return 2900;

            default:
                return 0;
        }
    }

    /** Returns the repair problems shown in the appointment dropdown. */
    @Override
    public String[] getServiceDetails() {

        return new String[]{
                "Engine Overheating",
                "Engine Failure",
                "Brake Problems",
                "Transmission Problems",
                "Clutch Problems",
                "Flat Tire",
                "Battery Failure",
                "Electrical Problems",
                "Air Conditioning Problems",
                "Radiator Problems",
                "Suspension Problems",
                "Steering Problems",
                "Fuel System Problems",
                "Starting Problems",
                "Exhaust System Problems"
        };
    }
}