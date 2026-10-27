package model;

// One row of the score sheet CSV, e.g.
// #01400001,#ping.lee.2023,_,PING LEE,ping.lee.2023@computing.smu.edu.sg
// Values are stored without the "#" in front.
public class Student {
    private String orgDefinedId;
    private String username;
    private String lastName;
    private String firstName;
    private String email;

    public Student(String orgDefinedId, String username, String lastName,
            String firstName, String email) {
        this.orgDefinedId = orgDefinedId;
        this.username = username;
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
    }

    public String getOrgDefinedId() {
        return orgDefinedId;
    }

    public String getUsername() {
        return username;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "Student [orgDefinedId=" + orgDefinedId
                + ", username=" + username + ", lastName=" + lastName
                + ", firstName=" + firstName + ", email=" + email + "]";
    }
}
