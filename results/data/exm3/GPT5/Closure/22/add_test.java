// com/google/javascript/jscomp/CheckSideEffectsTest.java::testUselessCode
test("for(x == 0; ; ) {}", "for(JSCOMPILER_PRESERVE(x == 0); ; ) {}", e);