    public final Object computeValue(EvalContext context) {
        return compute(args[0].computeValue(context), args[1].computeValue(context)) 
                ? Boolean.TRUE : Boolean.FALSE;
    }

// trigger testcase
public void testEmptyNodeSetOperations() {
        assertXPathValue(context, "/idonotexist = 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "/idonotexist != 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "/idonotexist < 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "/idonotexist > 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "/idonotexist >= 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "/idonotexist <= 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$array[position() < 1] = 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$array[position() < 1] != 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$array[position() < 1] < 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$array[position() < 1] > 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$array[position() < 1] >= 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$array[position() < 1] <= 0", Boolean.FALSE, Boolean.class);
    }
