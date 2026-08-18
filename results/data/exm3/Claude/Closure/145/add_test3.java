// com/google/javascript/jscomp/CodePrinterTest.java
public void testLabeledBlockWithDoLoop() {
    // Test labeled block containing DO loop
    assertPrint("if(x)A:{do{foo()}while(y)}",
        "if(x){A:do foo();while(y)}");
}