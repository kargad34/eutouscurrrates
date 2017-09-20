package org.gokhanka.euuscurrrates;

import org.gokhanka.euuscurrrates.api.ApiResult;
import org.gokhanka.euuscurrrates.db.DbActions;
import org.gokhanka.euuscurrrates.utility.TpsViaDelayedQueue;
import org.gokhanka.euuscurrrates.utility.Utility;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.apache.derby.tools.sysinfo;

/**
 * Unit test for simple EuroToUsdMicroApp.
 */
public class EuroToUsdMicroAppTest {

    static String            urlNameL    ;
    static String            urlNameB   ;
    static EuroToUsdMicroApp server      = null;
    static int               countOfRows = 0;
    static long              starTime    = 0L;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public EuroToUsdMicroAppTest() {
        super();
    }

    /**
     * @return the suite of tests being tested
     */
    /*    public static Test suite() {
        return new TestSuite(EuroToUsdMicroAppTest.class);
    }*/

    /**
     * Before running tests server is started
     * two dummy records are inserted in order to use in tests
     */
    @org.junit.BeforeClass
    public static void setUpClass() {
        server = new EuroToUsdMicroApp();
        boolean res1 = DbActions.insertLates(1107311405, "1.5252");
        boolean res2 = DbActions.insertLates(1107311410, "1.2525");
        server.start();
        if (res1 != true || res2 != true) {
            System.out.println("the test data can be corrupted, data can not be created control it please");
        }
        try {
            Thread.sleep(500);
        } catch (Exception e) {

        }
        urlNameL = "http://localhost:"+Utility.HTTP_PORT+"/eurtousd/latest";
        urlNameB = "http://localhost:" + Utility.HTTP_PORT+"/eurtousd/between";
        starTime = System.currentTimeMillis();
        countOfRows = DbActions.getCountOfRecords();
    }

    /**
     * dummy records are erased from DB and server is stopped
     */
    @org.junit.AfterClass
    public static void tearDownClass() {
        boolean res1 = DbActions.deleteItem(1107311405);
        boolean res2 = DbActions.deleteItem(1107311410);
        if (res1 != true || res2 != true) {
            System.out.println("the test data can be corrupted, data can not be deleted control it please");
        }
        server.stop();
    }

    /**
     * search for the results inserted in @BeforeClass stage
     */
    @org.junit.Test
    public void testApiBetween() {
        System.out.println("testApiBetween:"
                + " search for the results inserted in @BeforeClass stage");
        String param1 = "?startDate=" + "1107311400";
        String param2 = "&endDate=" + "1107311420";
        String urlname = urlNameB + param1 + param2;
        ApiResult result = TestUtility.parseDoc(TestUtility.getConnection(urlname));
        assertTrue(result.getStatus().equalsIgnoreCase(Utility.success)
                && result.getRateList().size() >= 2);
    }

    /**
     *  search for dates that do not exist
     */
    @org.junit.Test
    public void testApiBetweenEmptyResult() {
        System.out.println("testApiBetween:" + " search for dates that  do not exist");
        String param1 = "?startDate=" + "1106301400";
        String param2 = "&endDate=" + "1106301420";
        String urlname = urlNameB + param1 + param2;
        ApiResult result = TestUtility.parseDoc(TestUtility.getConnection(urlname));
        assertTrue(result.getStatus().equalsIgnoreCase(Utility.EMPTY_RESULT));
    }

    /**
     * input validation test: same input in the two parameters
     */
    @org.junit.Test
    public void testApiBetweenSame() {
        System.out.println("testApiBetweenSame:"
                + " input validation test: same input in the two parameters");
        String param1 = "?startDate=" + "1708092300";
        String param2 = "&endDate=" + "1708092300";
        String urlname = urlNameB + param1 + param2;
        ApiResult result = TestUtility.parseDoc(TestUtility.getConnection(urlname));
        assertFalse(result.getStatus().equalsIgnoreCase(Utility.success));
    }

    /**
     * input validation test: end date is before start date
     */
    @org.junit.Test
    public void testApiBetweenWrongOrder() {
        System.out.println("testApiBetweenWrongOrder:"
                + " input validation test: end date is before start date");
        String param1 = "?startDate=" + "1708092359";
        String param2 = "&endDate=" + "1708092300";
        String urlname = urlNameB + param1 + param2;
        ApiResult result = TestUtility.parseDoc(TestUtility.getConnection(urlname));
        assertFalse(result.getStatus().equalsIgnoreCase(Utility.success));
    }

    /**
     * input validation test: the dates are not suitable for parsing
     * expected values are dates in format yyMMddHHmm
     */
    @org.junit.Test
    public void testApiBetweenWrongFormatBoth() {
        System.out.println("testApiBetweenWrongFormatBoth:"
                + " input validation test: the dates are not suitable for parsing. expected values are dates in format yyMMddHHmm");
        String param1 = "?startDate=" + "1700000100";
        String param2 = "&endDate=" + "1700000105";
        String urlname = urlNameB + param1 + param2;
        ApiResult result = TestUtility.parseDoc(TestUtility.getConnection(urlname));
        assertFalse(result.getStatus().equalsIgnoreCase(Utility.success));
    }

    /**
     * input validation test: the dates are not suitable for parsing
     * expected values are dates in format yyMMddHHmm
     */
    @org.junit.Test
    public void testApiBetweenWrongFormatStart() {
        System.out.println("testApiBetweenWrongFormatStart:"
                + " input validation test: the dates are not suitable for parsing. expected values are dates in format yyMMddHHmm");
        String param1 = "?startDate=" + "1700000100";
        String param2 = "&endDate=" + "1708092300";
        String urlname = urlNameB + param1 + param2;
        ApiResult result = TestUtility.parseDoc(TestUtility.getConnection(urlname));
        assertFalse(result.getStatus().equalsIgnoreCase(Utility.success));
    }

    /**
     * input validation test: the dates are not suitable for parsing
     * expected values are dates in format yyMMddHHmm
     */
    @org.junit.Test
    public void testApiBetweenWrongFormatEnd() {
        System.out.println("testApiBetweenWrongFormatEnd:"
                + " input validation test: the dates are not suitable for parsing. expected values are dates in format yyMMddHHmm");
        String param1 = "?startDate=" + "1708092300";
        String param2 = "&endDate=" + "1700000105";
        String urlname = urlNameB + param1 + param2;
        ApiResult result = TestUtility.parseDoc(TestUtility.getConnection(urlname));
        assertFalse(result.getStatus().equalsIgnoreCase(Utility.success));
    }

    /**
     * test for the rest api that is giving the latest recorded rate value
     */
    @org.junit.Test
    public void testApiLatest() {
        System.out.println("testApiLatest:"
                + " test for the rest api that is giving the latest recorded rate value");
        ApiResult result = TestUtility.parseDoc(TestUtility.getConnection(urlNameL));
        assertTrue(result.getStatus().equalsIgnoreCase(Utility.success)
                && result.getRateList().size() == 1);
    }

    /**
     * traffic test. here a traffic value greater than the configured one is generated
     */
    @org.junit.Test
    public void testApiLoadFail() {
        System.out.println("testApiLoadFail:"
                + " traffic test. here a traffic value greater than the configured one is generated");
        String param1 = "?startDate=" + "1107311400";
        String param2 = "&endDate=" + "1107311420";
        String urlname = urlNameB + param1 + param2;
        ApiResult result = new ApiResult();
        result.setStatus(Utility.success);
        TpsViaDelayedQueue requestCountPerSecController = new TpsViaDelayedQueue((int) (Utility.MAX_ALLOWED_REQUEST
                * 1.10), Utility.MAX_ALLOWED_REQUEST_DURATION);
        for (int i = 0; i <= 1000; i++) {
            if (requestCountPerSecController.getTpsWhenAvailable()) {
                result = TestUtility.parseDoc(TestUtility.getConnection(urlname));
                if (!result.getStatus().equalsIgnoreCase(Utility.success)) {
                    break;
                }
            }
        }
        assertFalse(result.getStatus().equalsIgnoreCase(Utility.success));
    }

    /**
     * traffic test. here a traffic value less than the configured one is generated
     */
    @org.junit.Test
    public void testApiLoadSucc() {
        System.out.println("testApiLoadSucc:"
                + " traffic test. here a traffic value less than the configured one is generated");
        String param1 = "?startDate=" + "1107311400";
        String param2 = "&endDate=" + "1107311420";
        String urlname = urlNameB + param1 + param2;
        ApiResult result = new ApiResult();
        result.setStatus(Utility.success);
        TpsViaDelayedQueue requestCountPerSecController = new TpsViaDelayedQueue((int) (Utility.MAX_ALLOWED_REQUEST
                * 0.90), Utility.MAX_ALLOWED_REQUEST_DURATION);
        for (int i = 0; i <= 1000; i++) {
            if (requestCountPerSecController.getTpsWhenAvailable()) {
                result = TestUtility.parseDoc(TestUtility.getConnection(urlname));
                if (!result.getStatus().equalsIgnoreCase(Utility.success)) {
                    break;
                }
            }
        }
        assertTrue(result.getStatus().equalsIgnoreCase(Utility.success));
    }

    /**
     *test to see if scheduled job is doing its job
     */
    @org.junit.Test
    public void testSchedular() {
        System.out.println("testSchedular:"
                + " test to see if scheduled job is doing its job... can take a couple of minutes to complete");
        long now = System.currentTimeMillis();
        int past = (int) (((now - starTime) / 1000) / 60);
        int minWait = 0;
        if (past < Utility.SCHEDULAR_INIT_DELAY) {
            minWait = ((Utility.SCHEDULAR_INIT_DELAY - past));
        } else if (past == Utility.SCHEDULAR_INIT_DELAY) {
            minWait = 1;
        }
        try {
            TimeUnit.MINUTES.sleep(minWait);
        } catch (InterruptedException e) {

        }
        int addedRows = DbActions.getCountOfRecords() - countOfRows;
        assertTrue(addedRows > 0);
    }
}
