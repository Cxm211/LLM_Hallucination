// org/apache/commons/cli/TypeHandlerTest.java
@Test(expected = ParseException.class)
public void testCreateValueCustomClass_failure() throws Exception
{
    TypeHandler.createValue("test-string", java.util.ArrayList.class);
}