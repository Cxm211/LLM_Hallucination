// org/apache/commons/cli/TypeHandlerTest.java
public void testCreateValueIntegerValid() throws Exception {
    Integer result = TypeHandler.createValue("123", Integer.class);
    assertEquals(Integer.valueOf(123), result);
}
