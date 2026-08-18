// org/apache/commons/lang3/math/NumberUtilsTest.java::TestLang747
assertEquals(Long.valueOf(-0x8000000000000000L), NumberUtils.createNumber("-0x0008000000000000"));
assertEquals(new BigInteger("8000000000000000", 16), NumberUtils.createNumber("#8000000000000000"));