// com/google/javascript/rhino/jstype/JSTypeTest.java
public void testRestrictedTypeGivenToBooleanUnionThree() {
    // Union of number, string, and null.
    UnionType union = (UnionType) createUnionType(NUMBER_TYPE, STRING_TYPE, NULL_TYPE);
    // Number and string can be both truthy and falsy, null is falsy.
    // For true outcome, only number and string are possible.
    UnionType expectedTrue = (UnionType) createUnionType(NUMBER_TYPE, STRING_TYPE);
    assertTypeEquals(expectedTrue, union.getRestrictedTypeGivenToBooleanOutcome(true));
    // For false outcome, all three are possible (number can be 0, string can be "", null).
    assertTypeEquals(union, union.getRestrictedTypeGivenToBooleanOutcome(false));
  }
