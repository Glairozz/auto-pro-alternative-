public final class SessionUser {
    private static String username;
    private static String displayName;

    public static void loginClient(String user, String name) {
        username = user.trim();
        displayName = (name == null || name.isBlank()) ? username : name;
    }
    public static void logout() { username = null; displayName = null; }

    public static String getUsername()    { return username == null ? "guest" : username; }
    public static String getDisplayName() { return displayName == null ? getUsername() : displayName; }
}