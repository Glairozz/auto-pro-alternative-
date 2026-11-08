/**
 * Keeps the currently logged-in client in memory.
 * Every screen reads the same username and display name from this class.
 */
public final class SessionUser {
    private static String username;
    private static String displayName;

    /** Saves the active client after a successful login. */
    public static void loginClient(String user, String name) {
        username = user.trim();
        displayName = (name == null || name.isBlank()) ? username : name;
    }

    /** Clears the active client when the user logs out. */
    public static void logout() { username = null; displayName = null; }

    /** Returns the logged-in username, or "guest" when nobody is logged in. */
    public static String getUsername()    { return username == null ? "guest" : username; }

    /** Returns the full name shown in the UI, falling back to the username. */
    public static String getDisplayName() { return displayName == null ? getUsername() : displayName; }
}