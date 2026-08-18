// com/google/javascript/jscomp/PeepholeSubstituteAlternateSyntaxTest.java
public void testAssocitivity() {
    test("var a,b,c; a || (b || c); a * (b * c); a | (b | c)",
        "var a,b,c; (a || b) || c; (a * b) * c; (a | b) | c");
    testSame("var a,b,c; a % (b % c); a / (b / c); a - (b - c);");
    // Additional test cases for edge cases
    testSame("a * (b % c * d)"); // mixing % and * within parentheses
    testSame("a % (b * c % d)"); // mixing * and % within parentheses
    testSame("(a % b) * (c % d)"); // % on both sides of *
    testSame("a / (b / c / d)"); // division is left-associative but not associative
    testSame("a - (b - c - d)"); // subtraction is left-associative but not associative
    // Test that associative transformations are applied only when safe
    test("a + (b + c)", "(a + b) + c");
    test("a * (b * c)", "(a * b) * c");
    test("a | (b | c)", "(a | b) | c");
    // Ensure no transformation for non-associative operators
    testSame("a % (b % c)");
    testSame("a / (b / c)");
    testSame("a - (b - c)");
  }
