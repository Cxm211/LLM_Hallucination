// com/google/javascript/jscomp/CodePrinterTest.java
public void testFunctionWithBlockUnderLabel() {
    // Label with block containing single function.
    assertPrint("if(x)A:{function f(){}}",
                "if(x){A:function f(){}}");
  }
