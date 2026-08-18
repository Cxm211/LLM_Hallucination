// buggy function
    public Iterator iterate(EvalContext context) {
        Object result = compute(context);
        if (result instanceof EvalContext) {
            return new ValueIterator((EvalContext) result);
        }
        return ValueUtils.iterate(result);
    }

    public Iterator iteratePointers(EvalContext context) {
        Object result = compute(context);
        if (result == null) {
            return Collections.EMPTY_LIST.iterator();
        }
        if (result instanceof EvalContext) {
            return (EvalContext) result;
        }
        return new PointerIterator(ValueUtils.iterate(result),
                new QName(null, "value"),
                context.getRootContext().getCurrentNodePointer().getLocale());
    }

// trigger testcase
// org/apache/commons/jxpath/ri/compiler/ExtensionFunctionTest.java::testNodeSetReturn
public void testNodeSetReturn() {
        assertXPathValueIterator(
            context,
            "test:nodeSet()/name",
            list("Name 1", "Name 2"));

        assertXPathValueIterator(
            context,
            "test:nodeSet()",
            list(testBean.getBeans()[0], testBean.getBeans()[1]));

        assertXPathPointerIterator(
            context,
            "test:nodeSet()/name",
            list("/beans[1]/name", "/beans[2]/name"));
            
        assertXPathValueAndPointer(
            context,
            "test:nodeSet()/name",
            "Name 1",
            "/beans[1]/name");        

        assertXPathValueAndPointer(
            context,
            "test:nodeSet()/@name",
            "Name 1",
            "/beans[1]/@name");
    }
