// com/google/javascript/jscomp/NormalizeTest.java
public void testCatchBlockVariableConflictWithMakeLocalNamesUniqueFalse() {
    compiler.getOptions().setMakeLocalNamesUnique(false);
    test(
        "function f() { try {throw 0;} catch(e) {e;} var e = 1; }",
        "function f() { try {throw 0;} catch(e) {e;} var e = 1; }",
        "function f() { try {throw 0;} catch(e) {e;} var e = 1; }",
        null,
        null);
  }
