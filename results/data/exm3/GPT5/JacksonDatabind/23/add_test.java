// com/fasterxml/jackson/databind/ser/TestJsonSerialize2.java::testEmptyInclusionScalars

        class DW { public double d; public DW(double v){ d = v; } }
        DW dz = new DW(0.0);
        assertEquals("{}", inclMapper.writeValueAsString(dz));
        assertEquals("{\"d\":1.0}", inclMapper.writeValueAsString(new DW(1.0)));
