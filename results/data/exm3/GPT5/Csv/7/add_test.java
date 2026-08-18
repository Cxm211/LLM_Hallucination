// org/apache/commons/csv/CSVParserTest.java
import static org.junit.Assert.assertEquals;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.Test;

public class CSVParserTest {
    @Test
    public void testDuplicateHeaderEntriesRemapped() throws Exception {
        CSVParser parser = CSVParser.parse("a,b,a\n1,2,3\n", CSVFormat.DEFAULT.withHeader(new String[]{}));
        Map<String, Integer> map = parser.getHeaderMap();
        assertEquals(Integer.valueOf(0), map.get("a"));
        assertEquals(Integer.valueOf(1), map.get("b"));
        assertEquals(Integer.valueOf(2), map.get("a.1"));
    }
}
