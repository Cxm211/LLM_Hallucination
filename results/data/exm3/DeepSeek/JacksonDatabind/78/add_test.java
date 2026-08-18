// com/fasterxml/jackson/databind/interop/IllegalTypesCheckTest.java
public void testIssue1599_JdbcRowSetImpl() throws Exception
    {
        final String JSON = aposToQuotes(
 "{'id': 125,\n"
+" 'obj':[ 'com.sun.rowset.JdbcRowSetImpl',\n"
+"  {\n"
+"    'dataSourceName' : 'ldap://localhost:9999/Exploit',\n"
+"    'autoCommit' : true\n"
+"  }\n"
+" ]\n"
+"}"
        );
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        try {
            mapper.readValue(JSON, Bean1599.class);
            fail("Should not pass");
        } catch (JsonMappingException e) {
            verifyException(e, "Illegal type");
            verifyException(e, "to deserialize");
            verifyException(e, "prevented for security reasons");
        }
    }
