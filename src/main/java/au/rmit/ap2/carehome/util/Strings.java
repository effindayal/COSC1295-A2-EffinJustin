package au.rmit.ap2.carehome.util;
public final class Strings {
    private Strings() {}
    public static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
