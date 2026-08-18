// org/apache/commons/compress/archivers/zip/ZipFileTest.java
public void testExactLengthZip64ExtraField() throws Exception {
        // This test verifies that when rawCentralDirectoryData has exact expected length,
        // the method works correctly (both buggy and fixed versions should handle this)
        byte[] exactData = new byte[8]; // Exactly DWORD (8 bytes) for uncompressed size
        // Fill with some valid data representing a size
        exactData[0] = 0x10; exactData[1] = 0x00; exactData[2] = 0x00; exactData[3] = 0x00;
        exactData[4] = 0x00; exactData[5] = 0x00; exactData[6] = 0x00; exactData[7] = 0x00;
        
        try {
            Class<?> clazz = Class.forName("org.apache.commons.compress.archivers.zip.Zip64ExtendedInformationExtraField");
            Object instance = clazz.getDeclaredConstructor().newInstance();
            
            java.lang.reflect.Field field = clazz.getDeclaredField("rawCentralDirectoryData");
            field.setAccessible(true);
            field.set(instance, exactData);
            
            java.lang.reflect.Method method = clazz.getDeclaredMethod("reparseCentralDirectoryData",
                boolean.class, boolean.class, boolean.class, boolean.class);
            method.setAccessible(true);
            
            // Should not throw exception
            method.invoke(instance, true, false, false, false);
            
            // Verify the size was parsed
            java.lang.reflect.Method getSize = clazz.getDeclaredMethod("getSize");
            getSize.setAccessible(true);
            Object size = getSize.invoke(instance);
            assertNotNull("Size should be set", size);
        } catch (Exception e) {
            fail("Should not throw exception for exact length data: " + e.getMessage());
        }
    }