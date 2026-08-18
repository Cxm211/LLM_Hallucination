// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java
public void testFoldArithmeticModSpecial() {
    // MOD with negative divisor, half-integer quotient (buggy version gives wrong sign)
    fold("x = 5 % -2", "x = 1");
    // MOD with negative dividend, half-integer quotient
    fold("x = -3 % 2", "x = -1");
    // MOD with non-integer operands, half-integer quotient
    fold("x = 5.5 % -2", "x = 1.5");
  }
