// buggy function
    public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
        double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(args[0].computeValue(context));
        double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(args[1].computeValue(context));
        return l > r ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
        double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(args[0].computeValue(context));
        double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(args[1].computeValue(context));
        return l >= r ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
        double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(args[0].computeValue(context));
        double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(args[1].computeValue(context));
        return l < r ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
        double l = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(args[0].computeValue(context));
        double r = org.apache.commons.jxpath.ri.InfoSetUtil.doubleValue(args[1].computeValue(context));
        return l <= r ? Boolean.TRUE : Boolean.FALSE;
    }

    protected CoreOperationRelationalExpression(Expression[] args) {
        super(args);
    }

    protected final boolean isSymmetric() {
        return false;
    }

// trigger testcase
// org/apache/commons/jxpath/ri/compiler/CoreOperationTest.java::testNodeSetOperations
public void testNodeSetOperations() {
        assertXPathValue(context, "$array > 0", Boolean.TRUE, Boolean.class);
        assertXPathValue(context, "$array >= 0", Boolean.TRUE, Boolean.class);
        assertXPathValue(context, "$array = 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$array = 0.25", Boolean.TRUE, Boolean.class);
        assertXPathValue(context, "$array = 0.5", Boolean.TRUE, Boolean.class);
        assertXPathValue(context, "$array = 0.50000", Boolean.TRUE, Boolean.class);
        assertXPathValue(context, "$array = 0.75", Boolean.TRUE, Boolean.class);
        assertXPathValue(context, "$array < 1", Boolean.TRUE, Boolean.class);
        assertXPathValue(context, "$array <= 1", Boolean.TRUE, Boolean.class);
        assertXPathValue(context, "$array = 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$array > 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$array < 0", Boolean.FALSE, Boolean.class);
    }
