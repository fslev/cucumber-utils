package ro.cucumber.core.engineering.compare.comparators;

import ro.cucumber.core.engineering.placeholders.PlaceholdersGenerator;
import ro.skyah.comparator.JsonComparator;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class CustomJsonComparator implements JsonComparator {

    private Map<String, String> assignSymbols = new HashMap<>();

    public boolean compareValues(Object expected, Object actual) {
        String expectedString = expected.toString();
        String actualString = actual.toString();
        PlaceholdersGenerator parser = new PlaceholdersGenerator(expectedString, actualString);

        boolean hasAssignSymbols = !parser.getPlaceholdersMap().isEmpty();
        if (hasAssignSymbols) {
            expectedString = parser.parse();
        }
        try {
            Pattern pattern = Pattern.compile(expectedString);
            if (pattern.matcher(actualString).matches()) {
                if (hasAssignSymbols) {
                    this.assignSymbols.putAll(parser.getPlaceholdersMap());
                }
                return true;
            } else {
                return false;
            }
        } catch (PatternSyntaxException e) {
            if (expectedString.equals(actual.toString())) {
                if (hasAssignSymbols) {
                    this.assignSymbols.putAll(parser.getPlaceholdersMap());
                }
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean compareFields(String expected, String actual) {
        try {
            Pattern pattern = Pattern.compile(expected);
            return pattern.matcher(actual).matches();
        } catch (PatternSyntaxException e) {
            return expected.equals(actual);
        }
    }

    public Map<String, String> getAssignSymbols() {
        return assignSymbols;
    }
}
