package com.visiansystems.psdcmd;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple PsdCmd.
 */
public class PsdCmdTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public PsdCmdTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( PsdCmdTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testPsdCmd()
    {
        assertTrue( true );
    }
}

