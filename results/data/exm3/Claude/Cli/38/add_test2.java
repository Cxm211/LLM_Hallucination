// org/apache/commons/cli/bug/BugCLI265Test.java
@Test
public void shouldRejectInvalidConcatenatedOptions() throws Exception {
    String[] invalidConcatenated = new String[] { "-ax" };

    try {
        parser.parse(options, invalidConcatenated);
        fail("Expected ParseException for invalid concatenated option");
    } catch (ParseException e) {
        // Expected exception
    }
}