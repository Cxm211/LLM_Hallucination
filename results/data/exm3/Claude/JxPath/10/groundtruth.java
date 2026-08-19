// ===== FIXED org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression :: computeValue(EvalContext) [lines 41-44] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-10-fixed/src/java/org/apache/commons/jxpath/ri/compiler/CoreOperationRelationalExpression.java =====
    public final Object computeValue(EvalContext context) {
        return compute(args[0].compute(context), args[1].compute(context))
                ? Boolean.TRUE : Boolean.FALSE;
    }
