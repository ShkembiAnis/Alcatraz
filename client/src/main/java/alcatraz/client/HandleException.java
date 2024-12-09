package alcatraz.client;

import alcatraz.shared.exceptions.PlayerNotRegisteredException;

public class HandleException {

    public static String handleCauseException(Throwable thrownException, Class<?>... exceptions) {
        for (Class<?> exceptionClass : exceptions) {
            if (exceptionClass.isInstance(thrownException)) {
                return thrownException.getMessage();
            }
        }
        thrownException.printStackTrace();
        return "Unexpected Error!";
    }
}
