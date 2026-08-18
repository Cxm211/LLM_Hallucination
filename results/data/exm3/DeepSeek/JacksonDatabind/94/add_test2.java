// com/fasterxml/jackson/databind/interop/IllegalTypesCheckTest.java
public void testC3P0AfterCloseLoggingComboPooledDataSource() throws Exception
{
    _testIllegalType(com.mchange.v2.c3p0.debug.AfterCloseLoggingComboPooledDataSource.class); // [databind#1931]
}
