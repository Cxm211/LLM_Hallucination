// ===== FIXED org.apache.commons.jxpath.ri.compiler.CoreFunction :: functionCeiling(EvalContext) [lines 662-669] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-14-fixed/src/java/org/apache/commons/jxpath/ri/compiler/CoreFunction.java =====
    protected Object functionCeiling(EvalContext context) {
        assertArgCount(1);
        double v = InfoSetUtil.doubleValue(getArg1().computeValue(context));
        if (Double.isNaN(v) || Double.isInfinite(v)) {
        	return new Double(v);
        }
        return new Double(Math.ceil(v));
    }

// ===== FIXED org.apache.commons.jxpath.ri.compiler.CoreFunction :: functionFloor(EvalContext) [lines 653-660] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-14-fixed/src/java/org/apache/commons/jxpath/ri/compiler/CoreFunction.java =====
    protected Object functionFloor(EvalContext context) {
        assertArgCount(1);
        double v = InfoSetUtil.doubleValue(getArg1().computeValue(context));
        if (Double.isNaN(v) || Double.isInfinite(v)) {
        	return new Double(v);
        }
        return new Double(Math.floor(v));
    }

// ===== FIXED org.apache.commons.jxpath.ri.compiler.CoreFunction :: functionRound(EvalContext) [lines 671-678] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-14-fixed/src/java/org/apache/commons/jxpath/ri/compiler/CoreFunction.java =====
    protected Object functionRound(EvalContext context) {
        assertArgCount(1);
        double v = InfoSetUtil.doubleValue(getArg1().computeValue(context));
        if (Double.isNaN(v) || Double.isInfinite(v)) {
        	return new Double(v);
        }
        return new Double(Math.round(v));
    }
