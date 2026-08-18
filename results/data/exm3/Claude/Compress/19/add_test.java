// org/apache/commons/compress/archivers/zip/ZipFileTest.java
public void testInsufficientDataInZip64ExtraField() throws Exception {
        // This test verifies that when rawCentralDirectoryData has insufficient length,
        // the method correctly throws a ZipException
        byte[] insufficientData = new byte[7]; // Less than DWORD (8 bytes)
        
        // Create a mock object that simulates the scenario
        // Assuming we have access to a test utility that can set rawCentralDirectoryData
        // and call reparseCentralDirectoryData directly
        
        try {
            // This would require direct instantiation and field access
            // Using reflection or a test-specific constructor
            Class<?> clazz = Class.forName("org.apache.commons.compress.archivers.zip.Zip64ExtendedInformationExtraField");
            Object instance = clazz.getDeclaredConstructor().newInstance();
            
            java.lang.reflect.Field field = clazz.getDeclaredField("rawCentralDirectoryData");
            field.setAccessible(true);
            field.set(instance, insufficientData);
            
            java.lang.reflect.Method method = clazz.getDeclaredMethod("reparseCentralDirectoryData",
                boolean.class, boolean.class, boolean.class, boolean.class);
            method.setAccessible(true);
            
            method.invoke(instance, true, false, false, false);
            fail("Expected ZipException for insufficient data");
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            assertTrue("Expected ZipException", cause instanceof org.apache.commons.compress.archivers.zip.ZipException);
            assertTrue("Exception message should mention length mismatch",
                cause.getMessage().contains("doesn't match"));
        }
    }