package ro.cucumber.core.matchers;

import ro.cucumber.core.matchers.comparators.CustomXmlComparator;
import ro.cucumber.core.matchers.exceptions.MatcherException;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;
import static ro.cucumber.core.utils.XmlUtils.isValid;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.DifferenceEvaluators;
import org.xmlunit.diff.ElementSelectors;

public class XmlMatcher implements SymbolsAssignMatchable {

    private String expected;
    private String actual;
    private CustomXmlComparator comparator = new CustomXmlComparator();

    public XmlMatcher(Object expected, Object actual) throws MatcherException {
        this.expected = expected.toString();
        this.actual = actual.toString();
        if (!isValid(this.expected) || !isValid(this.actual)) {
            throw new MatcherException("Malformed XML");
        }
    }

    @Override
    public Map<String, String> match() {
        assertThat(actual, isSimilarTo(expected).ignoreWhitespace()
                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName))
                .withDifferenceEvaluator(
                        DifferenceEvaluators.chain(DifferenceEvaluators.Default, comparator)));
        return comparator.getAssignSymbols();
    }
}