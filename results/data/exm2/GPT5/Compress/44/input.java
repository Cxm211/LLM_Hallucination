    public ChecksumCalculatingInputStream(final Checksum checksum, final InputStream in) {



        this.checksum = checksum;
        this.in = in;
    }

// trigger testcase
public void testClassInstantiationWithParameterBeingNullThrowsNullPointerExceptionOne() {

        ChecksumCalculatingInputStream checksumCalculatingInputStream = new ChecksumCalculatingInputStream(null,null);


    }

public void testClassInstantiationWithParameterBeingNullThrowsNullPointerExceptionThree() {

        ChecksumCalculatingInputStream checksumCalculatingInputStream = new ChecksumCalculatingInputStream(new CRC32(),null);

    }

public void testClassInstantiationWithParameterBeingNullThrowsNullPointerExceptionTwo() {

        ChecksumCalculatingInputStream checksumCalculatingInputStream = new ChecksumCalculatingInputStream(null,new ByteArrayInputStream(new byte[1]));


    }
