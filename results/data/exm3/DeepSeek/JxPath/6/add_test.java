// org/apache/commons/jxpath/ri/compiler/VariableTest.java
package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathTestCase;
import java.util.*;

public class VariableTest extends JXPathTestCase {
    // Original test
    public void testIterateVariable() throws Exception {
        assertXPathValueIterator(context, "$d", list("a", "b"));
        assertXPathValue(context, "$d = 'a'", Boolean.TRUE);
        assertXPathValue(context, "$d = 'b'", Boolean.TRUE);
    }

    // New test for array comparison
    public void testEqualArrayToString() throws Exception {
        context.setVariable("arr", new String[]{"x", "y"});
        assertXPathValue(context, "$arr = 'x'", Boolean.TRUE);
        assertXPathValue(context, "$arr = 'y'", Boolean.TRUE);
        assertXPathValue(context, "$arr = 'z'", Boolean.FALSE);
    }

    // New test for null comparison
    public void testEqualNull() throws Exception {
        context.setVariable("n", null);
        assertXPathValue(context, "$n = 'a'", Boolean.FALSE);
    }
}
