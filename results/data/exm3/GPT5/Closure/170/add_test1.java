// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java::testVarAssinInsideHookIssue965
noInline("var i = 0; return (i = 5) + i;");