// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java::testSimpleForIn
noInline("var a,b,x = a in b; delete b[a]; x");