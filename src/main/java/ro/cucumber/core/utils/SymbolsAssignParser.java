package ro.cucumber.core.utils;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SymbolsAssignParser {

    private static final String SYMBOL_ASSIGN_START = "~\\[";
    private static final String SYMBOL_ASSIGN_END = "\\]";
    private static final String SYMBOL_ASSIGN_REGEX =
            SYMBOL_ASSIGN_START + "(.*?)" + SYMBOL_ASSIGN_END;

    private static final Pattern SYMBOL_ASSIGN_PATTERN = Pattern.compile(SYMBOL_ASSIGN_REGEX,
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

    private String stringWithSymbols;
    private String stringWithValues;
    private Map<String, String> assignSymbols = new LinkedHashMap<>();

    public SymbolsAssignParser(String stringWithSymbols, String stringWithValues) {
        this.stringWithSymbols = stringWithSymbols;
        this.stringWithValues = stringWithValues;
        setAssignSymbols();
    }

    public String getStringWithAssignValues() {
        String str = stringWithSymbols;
        for (Map.Entry<String, String> e : assignSymbols.entrySet()) {
            str = str.replaceAll(SYMBOL_ASSIGN_START + e.getKey() + SYMBOL_ASSIGN_END,
                    e.getValue());
        }
        return str;
    }

    public Map<String, String> getAssignSymbols() {
        return this.assignSymbols;
    }

    public void setAssignSymbols() {
        List<String> symbolNames = getAssignSymbolNames();
        if (symbolNames.isEmpty()) {
            return;
        }
        String quotedStringWithSymbols = "\\Q" + stringWithSymbols + "\\E";
        for (String name : symbolNames) {
            quotedStringWithSymbols = quotedStringWithSymbols
                    .replaceAll(SYMBOL_ASSIGN_START + name + SYMBOL_ASSIGN_END, "\\\\E(.*)\\\\Q");
        }
        Pattern pattern = Pattern.compile(quotedStringWithSymbols,
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(stringWithValues);
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                assignSymbols.put(symbolNames.get(i - 1), matcher.group(i));
            }
        }
    }

    private List<String> getAssignSymbolNames() {
        List<String> names = new ArrayList<>();
        Matcher matcher = SYMBOL_ASSIGN_PATTERN.matcher(stringWithSymbols);
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                names.add(matcher.group(i));
            }
        }
        return names;
    }
}
