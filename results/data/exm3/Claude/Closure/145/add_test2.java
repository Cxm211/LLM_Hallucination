// com/google/javascript/jscomp/CodePrinterTest.java
public void testBlockWithMultipleStatementsAndDo() {
    // Block with multiple statements should keep braces
    assertPrint("if(x){var a=1;do{foo()}while(y)}",
        "if(x){var a=1;do foo();while(y)}");
}