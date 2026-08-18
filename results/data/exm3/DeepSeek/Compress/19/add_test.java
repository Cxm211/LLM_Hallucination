// org/apache/commons/compress/archivers/zip/ZipFileTest.java
public void testReparseCentralDirectoryDataExcessWithDiskStart() throws Exception {
        org.apache.commons.compress.archivers.zip.Zip64ExtendedInformationExtraField field = new org.apache.commons.compress.archivers.zip.Zip64ExtendedInformationExtraField();
        field.rawCentralDirectoryData = new byte[5];
        field.reparseCentralDirectoryData(false, false, false, true);
    }
