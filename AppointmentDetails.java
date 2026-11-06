public class AppointmentDetails {

    private AutomotiveService service;
    private String vehicleInfo;
    private String serviceDetail;

    public AppointmentDetails(AutomotiveService service, String vehicleInfo, String serviceDetail) {
        this.service = service;
        this.vehicleInfo = vehicleInfo;
        this.serviceDetail = serviceDetail;
    }

    public String validateServiceRequest() {

        if (service == null) {
            return "Invalid Service";
        }

        if (vehicleInfo == null || vehicleInfo.trim().isEmpty()) {
            return "Invalid Vehicle Information";
        }

        if (serviceDetail == null || serviceDetail.trim().isEmpty()) {
            return "Invalid Service Details";
        }

        if (getEstimatedPrice() <= 0) {
            return "Invalid Service Price";
        }

        return "Valid Appointment";
    }

    public double getEstimatedPrice() {
        return service.getServicePrice(serviceDetail);
    }

    public String getVehicleInfo() {
        return vehicleInfo;
    }

    public String getServiceDetail() {
        return serviceDetail;
    }

    public String getServiceName() {
        return service.getServiceName();
    }
}

