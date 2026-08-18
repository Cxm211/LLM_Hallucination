// org/apache/commons/jxpath/ri/compiler/CoreOperationTest.java
public void testEmptyNodeSetOperationsAdditional() {
        assertXPathValue(context, "/idonotexist = 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "/idonotexist != 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "/idonotexist < 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "/idonotexist > 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "/idonotexist >= 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "/idonotexist <= 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "/idonotexist = -1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "/idonotexist = /ialsonotexist", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "/idonotexist = 'test'", Boolean.FALSE, Boolean.class);
    }
