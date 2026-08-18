// com/fasterxml/jackson/core/TestJsonPointer.java::testIZeroIndex
ptr = JsonPointer.compile("/01");
assertEquals(-1, ptr.getMatchingIndex());