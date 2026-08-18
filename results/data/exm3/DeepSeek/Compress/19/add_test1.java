// org/apache/commons/compress/archivers/zip/ZipFileTest.java
public void testReparseCentralDirectoryDataExcessWithSizes() throws Exception {
        org.apache.commons.compress.archivers.zip.Zip64ExtendedInformationExtraField field = new org.apache.commons.compress.archivers.zip.Zip64ExtendedInformationExtraField();
        field.rawCentralDirectoryData = new byte[17];
        field.reparseCentralDirectoryData(true, true, false, false);
    }
