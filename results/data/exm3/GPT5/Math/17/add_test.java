// org/apache/commons/math3/dfp/DfpTest.java::testMultiply
        test(ninf.multiply(0),
             nan,
             DfpField.FLAG_INVALID, "Multiply #35.1");