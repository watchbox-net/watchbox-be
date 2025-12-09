package net.watchbox.global.validation;

import java.util.regex.Pattern;

public class ValidationPatterns {
    public static final Pattern NICKNAME =
            Pattern.compile("^[가-힣a-zA-Z0-9]{2,20}$");

    private ValidationPatterns() {}
}
