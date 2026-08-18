// org/apache/commons/jxpath/ri/compiler/CoreFunctionTest.java
public void testCoreFunctionsRoundSpecial() {
        assertXPathValue(context, "round(-0.5)", new Double(-0.0));
        assertXPathValue(context, "round(-0.0)", new Double(-0.0));
        assertXPathValue(context, "round(1e19)", new Double(1e19));
        assertXPathValue(context, "round(-1e19)", new Double(-1e19));
    }
