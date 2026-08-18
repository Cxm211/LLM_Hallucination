// org/jsoup/nodes/DocumentTest.java
@Test
public void testEscapeWhitespace() throws Exception {
    // Use reflection to call the package-private escape method
    Class<?> entitiesClass = Class.forName("org.jsoup.nodes.Entities");
    java.lang.reflect.Method escapeMethod = entitiesClass.getDeclaredMethod("escape", StringBuilder.class, String.class,
            org.jsoup.nodes.Document.OutputSettings.class, boolean.class, boolean.class, boolean.class);
    escapeMethod.setAccessible(true);
    
    // Helper to call escape
    java.util.function.Function<Object[], String> callEscape = params -> {
        StringBuilder accum = new StringBuilder();
        try {
            escapeMethod.invoke(null, accum, (String)params[0], (org.jsoup.nodes.Document.OutputSettings)params[1],
                    (boolean)params[2], (boolean)params[3], (boolean)params[4]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return accum.toString();
    };
    
    // Base settings with UTF-8
    org.jsoup.nodes.Document.OutputSettings out = new org.jsoup.nodes.Document.OutputSettings();
    out.escapeMode(org.jsoup.nodes.Entities.EscapeMode.base);
    out.charset(java.nio.charset.StandardCharsets.UTF_8);
    
    // normaliseWhite = true, stripLeadingWhite = false
    String input1 = "  a  b  c  ";
    String result1 = callEscape.apply(new Object[]{input1, out, false, true, false});
    org.junit.Assert.assertEquals(" a b c ", result1);
    
    // normaliseWhite = true, stripLeadingWhite = true
    String input2 = "  a  b  c  ";
    String result2 = callEscape.apply(new Object[]{input2, out, false, true, true});
    org.junit.Assert.assertEquals("a b c ", result2);
    
    // normaliseWhite = false
    String input3 = "  a  b  c  ";
    String result3 = callEscape.apply(new Object[]{input3, out, false, false, false});
    org.junit.Assert.assertEquals(input3, result3);
    
    // Bug check: 0xA0 in xhtml with Shift_JIS
    org.jsoup.nodes.Document.OutputSettings out2 = new org.jsoup.nodes.Document.OutputSettings();
    out2.escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml);
    out2.charset(java.nio.charset.Charset.forName("Shift_JIS"));
    String input4 = "\u00A0";
    String result4 = callEscape.apply(new Object[]{input4, out2, false, false, false});
    org.junit.Assert.assertFalse(result4.contains("?"));
    org.junit.Assert.assertTrue(result4.contains("&#xa0;") || result4.contains("&nbsp;"));
}
