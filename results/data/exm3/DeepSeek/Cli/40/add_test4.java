// org/apache/commons/cli/TypeHandlerTest.java
public void testCreateValueURLInvalid() throws Exception {
    TypeHandler.createValue("not-a-url", URL.class);
}
