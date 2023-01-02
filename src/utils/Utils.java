package utils;

public class Utils {
    public static final int SERVER_PORT = 5260;
    public static final String LOCALHOST = "127.0.0.1";
    public static final int MAX_COLS = 30;

    public static void handleException(Exception e){
        System.out.println("Encountered an exception");
        e.printStackTrace();
    }
}
