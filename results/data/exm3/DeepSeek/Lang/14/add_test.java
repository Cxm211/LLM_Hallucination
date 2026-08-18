// org/apache/commons/lang3/StringUtilsEqualsIndexOfTest.java
public void testEqualsAdditional() {
    // Test cases that fail in buggy version due to reliance on .equals()
    CharSequence string = "hello";
    CharSequence stringBuffer = new StringBuffer("hello");
    assertTrue("String vs StringBuffer should be equal", StringUtils.equals(string, stringBuffer));
    assertTrue("StringBuffer vs String should be equal", StringUtils.equals(stringBuffer, string));
    
    // Test with empty sequences
    CharSequence emptyString = "";
    CharSequence emptyStringBuilder = new StringBuilder("");
    assertTrue("Empty String vs StringBuilder should be equal", StringUtils.equals(emptyString, emptyStringBuilder));
    
    // Test with unicode characters
    CharSequence uniString = "café";
    CharSequence uniStringBuilder = new StringBuilder("café");
    assertTrue("Unicode strings should be equal", StringUtils.equals(uniString, uniStringBuilder));
}
