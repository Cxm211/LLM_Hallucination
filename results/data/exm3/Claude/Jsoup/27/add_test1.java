// org/jsoup/helper/DataUtilTest.java
@Test
public void testCharsetCaseSensitivity() {
    assertEquals("utf-8", DataUtil.getCharsetFromContentType("text/html; charset=Utf-8"));
    assertEquals("iso-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=\"iso-8859-1\""));
    assertEquals("windows-1252", DataUtil.getCharsetFromContentType("text/html; charset=WiNdOwS-1252"));
}