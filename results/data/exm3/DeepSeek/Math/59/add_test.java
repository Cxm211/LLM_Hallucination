// org/apache/commons/math/util/FastMathTest.java
@Test
    public void testMaxFloatEdgeCases() {
        float[][] pairs = {
            { 1.0f, -1.0f },
            { -0.0f, +0.0f },
            { +0.0f, -0.0f },
            { Float.MAX_VALUE, -Float.MAX_VALUE },
            { Float.MIN_NORMAL, -Float.MIN_NORMAL },
            { Float.MIN_VALUE, -Float.MIN_VALUE },
            { Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY },
            { Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY }
        };
        for (float[] pair : pairs) {
            Assert.assertEquals("max(" + pair[0] + ", " + pair[1] + ")",
                                Math.max(pair[0], pair[1]),
                                FastMath.max(pair[0], pair[1]),
                                MathUtils.EPSILON);
            Assert.assertEquals("max(" + pair[1] + ", " + pair[0] + ")",
                                Math.max(pair[1], pair[0]),
                                FastMath.max(pair[1], pair[0]),
                                MathUtils.EPSILON);
        }
    }
