import java.util.Arrays;

public class parenErrorCheck {
    String stringForCheck;
    String[] openParens = {"(", "[", "{"};
    String[] closeParens = {")", "]", "}"};

    public parenErrorCheck(String stringForCheck) {
        this.stringForCheck = stringForCheck;
    }

    private boolean inOpenParens(char current) {
        return Arrays.stream(openParens).toList().contains(current);
    }

    private boolean inCloseParens(char current) {
        return Arrays.stream(closeParens).toList().contains(current);
    }

    public boolean check() {
        int parensNumber = 0;

        for (int pos = 0; pos < stringForCheck.length(); pos++) {
            char current = stringForCheck.charAt(pos);

            if (inOpenParens(current)) {
                parensNumber++;
            } else if (inCloseParens(current)) {
                parensNumber = parensNumber - 1;
                if (parensNumber < 0) {
                    return false;
                }
            }
        }

        return parensNumber == 0;
    }
}
