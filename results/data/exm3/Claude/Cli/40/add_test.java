// org/apache/commons/cli/TypeHandlerTest.java
@Test(expected = ParseException.class)
public void testCreateValueBoolean_failure() throws Exception
{
    TypeHandler.createValue("test-string", Boolean.class);
}