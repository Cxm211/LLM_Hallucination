// buggy function
    public ChecksumCalculatingInputStream(final Checksum checksum, final InputStream in) {



        this.checksum = checksum;
        this.in = in;
    }

// trigger testcase
// org/apache/commons/compress/utils/ChecksumCalculatingInputStreamTest.java::testClassInstantiationWithParameterBeingNullThrowsNullPointerExceptionOne
public void testClassInstantiationWithParameterBeingNullThrowsNullPointerExceptionOne() {

        ChecksumCalculatingInputStream checksumCalculatingInputStream = new ChecksumCalculatingInputStream(null,null);


    }

// org/apache/commons/compress/utils/ChecksumCalculatingInputStreamTest.java::testClassInstantiationWithParameterBeingNullThrowsNullPointerExceptionThree
public void testClassInstantiationWithParameterBeingNullThrowsNullPointerExceptionThree() {

        ChecksumCalculatingInputStream checksumCalculatingInputStream = new ChecksumCalculatingInputStream(new CRC32(),null);

    }

// org/apache/commons/compress/utils/ChecksumCalculatingInputStreamTest.java::testClassInstantiationWithParameterBeingNullThrowsNullPointerExceptionTwo
public void testClassInstantiationWithParameterBeingNullThrowsNullPointerExceptionTwo() {

        ChecksumCalculatingInputStream checksumCalculatingInputStream = new ChecksumCalculatingInputStream(null,new ByteArrayInputStream(new byte[1]));


    }
