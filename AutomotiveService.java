public abstract class AutomotiveService {

    protected String serviceName;

    public AutomotiveService(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public abstract double getServicePrice(String serviceDetail);

    public abstract String[] getServiceDetails();
}