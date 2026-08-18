// com/google/javascript/jscomp/CodePrinterTest.java
public void testDoLoopWithBlockUnderLabel() {
    // Label with block containing single do loop.
    assertPrint("if(x)A:{do{foo()}while(y)}",
                "if(x){A:do foo();while(y)}");
  }
