/**
 * Shared base class for repair and installation services.
 * Each subclass supplies its own service names, prices, and selectable details.
 */
public abstract class AutomotiveService {

    protected String serviceName;

    public AutomotiveService(String serviceName) {
        this.serviceName = serviceName;
    }

    /** Returns the display name, such as "Repair Services". */
    public String getServiceName() {
        return serviceName;
    }

    /** Returns the price for one selected service detail. */
    public abstract double getServicePrice(String serviceDetail);

    /** Returns the service details shown in the appointment dropdown. */
    public abstract String[] getServiceDetails();
}