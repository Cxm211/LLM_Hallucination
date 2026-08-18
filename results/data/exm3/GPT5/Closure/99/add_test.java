// com/google/javascript/jscomp/CheckGlobalThisTest.java::testComputedPrototype1
public void testComputedPrototype1() {\n  testSame("a['prototype'].x = function() { this.foo = 3; };");\n}