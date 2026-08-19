// ===== FIXED org.apache.commons.jxpath.ri.compiler.CoreOperationCompare :: CoreOperationCompare [lines 39-41] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-9-fixed/src/java/org/apache/commons/jxpath/ri/compiler/CoreOperationCompare.java =====
    public CoreOperationCompare(Expression arg1, Expression arg2) {
        this(arg1, arg2, false);
    }

// ===== FIXED org.apache.commons.jxpath.ri.compiler.CoreOperationCompare :: equal(Object, Object) [lines 130-161] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-9-fixed/src/java/org/apache/commons/jxpath/ri/compiler/CoreOperationCompare.java =====
    protected boolean equal(Object l, Object r) {
        if (l instanceof Pointer) {
            l = ((Pointer) l).getValue();
        }

        if (r instanceof Pointer) {
            r = ((Pointer) r).getValue();
        }

        boolean result;
        if (l instanceof Boolean || r instanceof Boolean) {
            result = l == r || InfoSetUtil.booleanValue(l) == InfoSetUtil.booleanValue(r);
        } else if (l instanceof Number || r instanceof Number) {
            //if either side is NaN, no comparison returns true:
            double ld = InfoSetUtil.doubleValue(l);
            if (Double.isNaN(ld)) {
                return false;
            }
            double rd = InfoSetUtil.doubleValue(r);
            if (Double.isNaN(rd)) {
                return false;
            }
            result = ld == rd;
        } else {
            if (l instanceof String || r instanceof String) {
                l = InfoSetUtil.stringValue(l);
                r = InfoSetUtil.stringValue(r);
            }
            result = l == r || l != null && l.equals(r);
        }
        return result ^ invert;
    }

// ===== FIXED org.apache.commons.jxpath.ri.compiler.CoreOperationNotEqual :: CoreOperationNotEqual [lines 27-29] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-9-fixed/src/java/org/apache/commons/jxpath/ri/compiler/CoreOperationNotEqual.java =====
    public CoreOperationNotEqual(Expression arg1, Expression arg2) {
        super(arg1, arg2, true);
    }
