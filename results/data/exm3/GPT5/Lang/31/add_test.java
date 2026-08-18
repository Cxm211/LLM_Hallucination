// org/apache/commons/lang3/StringUtilsEqualsIndexOfTest.java::testContainsAnyCharArrayWithSupplementaryChars
final String highOnly = CharU20000.substring(0, 1) + "A"; // high surrogate followed by non-matching char
assertEquals(false, StringUtils.containsAny(highOnly, CharU20000.toCharArray()));