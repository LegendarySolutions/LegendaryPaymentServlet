package pl.warsjawa.legacycode;

public class Order {

    private String id;
    private int totalPrice;
    private String status = "NEW";
    private CustomerData customerData;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getStatus() {
        return status;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }

    public CustomerData getCustomerData() {
        return customerData;
    }

    public void setCustomerData(CustomerData data) {
        this.customerData = data;
    }
    
}
