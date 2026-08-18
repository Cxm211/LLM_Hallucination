// com/fasterxml/jackson/core/read/NonStandardUnquotedNamesTest.java
public void testUnquotedIssue510BelowMaxCode() throws Exception
{
    char[] fullChars = new char[4001];
    for (int i = 0; i < 3998; i++) {
         fullChars[i] = ' ';
    }
    fullChars[3998] = '{';
    fullChars[3999] = 'a';
    fullChars[4000] = (char) (codes.length - 1); // just below maxCode

    JsonParser p = UNQUOTED_FIELDS_F.createParser(new java.io.StringReader(new String(fullChars)));
    assertToken(JsonToken.START_OBJECT, p.nextToken());
    try {
        p.nextToken();
        fail("Should not pass");
    } catch (JsonParseException e) {
        ; // should fail here
    }
    p.close();
}