// org/apache/commons/lang3/math/NumberUtilsTest.java::testStringCreateNumberEnsureNoPrecisionLossWithDoubleSuffixSmall
@Test
public void testStringCreateNumberEnsureNoPrecisionLossWithDoubleSuffixSmall(){
    String shouldBeDoubleWithSuffix = "1e-50D";
    assertTrue(NumberUtils.createNumber(shouldBeDoubleWithSuffix) instanceof Double);
}