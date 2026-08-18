    private boolean compute(Object left, Object right) {
        left = reduce(left);
        right = reduce(right);

        if (left instanceof InitialContext) {
            ((InitialContext) left).reset();
        }
        if (right instanceof InitialContext) {
            ((InitialContext) right).reset();
        }
        if (left instanceof Iterator && right instanceof Iterator) {
            return findMatch((Iterator) left, (Iterator) right);
        }
        if (left instanceof Iterator) {
            return containsMatch((Iterator) left, right);
        }
        if (right instanceof Iterator) {
            return containsMatch((Iterator) right, left);
        }
        double ld = InfoSetUtil.doubleValue(left);
        if (Double.isNaN(ld)) {
            return false;
        }
        double rd = InfoSetUtil.doubleValue(right);
        if (Double.isNaN(rd)) {
            return false;
        }
        return evaluateCompare(ld == rd ? 0 : ld < rd ? -1 : 1);
    }

    private boolean containsMatch(Iterator it, Object value) {
        while (it.hasNext()) {
            Object element = it.next();
            if (compute(element, value)) {
                return true;
            }
        }
        return false;
    }

// trigger testcase
public void testComplexOperationWithVariables() {
        JXPathContext context = JXPathContext.newContext(null);
        context.getVariables().declareVariable("a", Integer.valueOf(0));
        context.getVariables().declareVariable("b", Integer.valueOf(0));
        context.getVariables().declareVariable("c", Integer.valueOf(1));
        assertXPathValue(context, "$a + $b <= $c", Boolean.TRUE);
    }
