// org/apache/commons/cli/TypeHandlerTest.java
public void testCreateValueFileValid() throws Exception {
    File result = TypeHandler.createValue("test.txt", File.class);
    assertNotNull(result);
    assertEquals("test.txt", result.getPath());
}
