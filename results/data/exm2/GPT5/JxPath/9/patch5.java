public void testNan() {
        assertXPathValue(context, "$nan > $nan", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan < $nan", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan >= $nan", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan <= $nan", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan >= $nan and $nan <= $nan", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan = $nan", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan != $nan", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan > 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan < 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan >= 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan <= 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan >= 0 and $nan <= 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan = 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan != 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan > 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan < 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan >= 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan <= 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan >= 1 and $nan <= 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan = 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan != 1", Boolean.FALSE, Boolean.class);
    }