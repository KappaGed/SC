package src.objects;

public class SecureUser {

    private String username;
    private String publicKey;

    public SecureUser(String username, String publicKey) {
        this.username = username;
        this.publicKey = publicKey;
    }

    public String getUsername() {
        return username;
    }

    public String getPublicKey() {
        return publicKey;
    }
    @Override
    public String toString() {
        return username + ":" + publicKey;
    }
}
