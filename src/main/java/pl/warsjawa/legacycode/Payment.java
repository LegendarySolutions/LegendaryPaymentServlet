package pl.warsjawa.legacycode;

public class Payment {

    private String id;
    private String state;
    private Integer amount;

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String newState) {
        this.state = newState;
    }
    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
