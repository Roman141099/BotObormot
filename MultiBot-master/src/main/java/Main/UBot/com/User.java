package Main.UBot.com;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("User firstname")
    private String firstName;
    @SerializedName("User lastname")
    private String lastName;
    @SerializedName("User ID")
    private final Long userChatId;
    @SerializedName("Session settings")
    private Session currentSession;

    public User() {
        userChatId = 0L;
        currentSession = null;
        firstName = "DefaultFirstName";
        lastName = "DefaultLastName";
    }

    public User(Long userChatId) {
        this.userChatId = userChatId;
    }

    public User(Long userChatId, Session currentSession, String firstName, String lastName) {
        this.userChatId = userChatId;
        this.currentSession = currentSession;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Long getUserChatId() {
        return userChatId;
    }



    public Session getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(Session currentSession) {
        this.currentSession = currentSession;
    }

    @Override
    public String toString() {
        return "User{id=" + userChatId + ", current session=" + currentSession + '}';
    }

    public String getLastName() {
        return lastName;
    }
}
