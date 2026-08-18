// com/google/javascript/jscomp/NormalizeTest.java::testMakeLocalNamesUnique
test("/** @suppress {duplicate} */ var a; /** @suppress {duplicate} */ var a;", "var a;");