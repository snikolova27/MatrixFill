package utils;

public class Utils {
    public static final int SERVER_PORT = 5260;
    public static final String LOCALHOST = "127.0.0.1";
    public static final int MAX_COLS = 10;

    public static void handleException(Exception e){
        System.out.println("Encountered an exception");
        e.printStackTrace();
    }
    public static String welcome() {
        return """
                Welcome to the Concurrent Matrix Fill Client-Server application. Its purpose is to fill a 2D matrix
                using multiple threads where each thread fills the matrix with a different number that is randomly generated.
                """;
    }

    public static String helpHandler() {
        return """
                Available commands:
                 threads - after pressing enter on this command you must enter the number of threads to work with.
                 After you enter the number, press enter one more time to see the result.
                 help - get a list of the available commands
                 over - disconnect from the server""";
    }
}
