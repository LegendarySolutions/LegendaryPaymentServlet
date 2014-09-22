package legacycode;

public class CustomerData {

    private String email;
    private String fullName;

    public CustomerData(String email, String fullName) {
        this.email = email;
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

}
