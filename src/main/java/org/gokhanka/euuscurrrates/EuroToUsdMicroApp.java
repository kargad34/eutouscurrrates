package org.gokhanka.euuscurrrates;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.init;
import static spark.Spark.port;
import static spark.Spark.threadPool;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.gokhanka.euuscurrrates.api.ApiImp;
import org.gokhanka.euuscurrrates.api.ApiResult;
import org.gokhanka.euuscurrrates.db.DbConnectionManager;
import org.gokhanka.euuscurrrates.rates.Schedular;
import org.gokhanka.euuscurrrates.utility.DaemonThreadFactory;
import org.gokhanka.euuscurrrates.utility.TpsViaDelayedQueue;
import org.gokhanka.euuscurrrates.utility.Utility;

import spark.Spark;
/**
 * Main class of the project. Life cycle of the app is maintained here,
 * Additionally the sparkjava micro http server is also maintained here
 * @author gokhanka
 *
 */
public class EuroToUsdMicroApp implements Runnable {

    static final Logger   logger                       = LogManager.getLogger();
    TpsViaDelayedQueue    requestCountPerSecController = new TpsViaDelayedQueue(Utility.MAX_ALLOWED_REQUEST,
                                                                                Utility.MAX_ALLOWED_REQUEST_DURATION);
    Thread                threadNew                    = null;
    private AtomicBoolean halted                       = new AtomicBoolean(Utility.TRUE);
    private AtomicBoolean alive                        = new AtomicBoolean(Utility.FALSE);
    private ExecutorService pool = null;
    /**
     * Constructor For the main class
     * EuroToUsdMicroApp class is used to host the features provided by application
     * It starts and stops HTTP server for REST API http://localhost:8088/eurtousd/between?startDate={}&endDate={}
     * and http://localhost:8088/eurtousd/latest
     * additionally to stop service there is one more interface http://localhost:8088/eurtousd/stopService
     */ 
    public EuroToUsdMicroApp() {
        LoggerContext context = (LoggerContext) LogManager.getContext(Utility.FALSE);
        File file = new File("src/main/resources/log4j2.xml");
        context.setConfigLocation(file.toURI());
        org.apache.log4j.BasicConfigurator.configure();
        Utility.loadProperties();
        DaemonThreadFactory dtf = new DaemonThreadFactory();
        pool = Executors.newFixedThreadPool(Utility.HTTP_THEAD_MAX,dtf);
        DbConnectionManager.getInstance();
        Schedular.startTimer();
        threadPool(Utility.HTTP_THEAD_MAX, Utility.HTTP_THEAD_MIN, Utility.HTTP_IDLE_TIME);
        port(Utility.HTTP_PORT);
    }

    /**
     * Used to start the HTTP Service
     */
    public void start() {
        if (this.threadNew == null) {
            threadNew = new Thread(this);
            threadNew.setDaemon(Utility.TRUE);
            threadNew.start();
        } else {
            if (halted.compareAndSet(Utility.TRUE, Utility.FALSE)) {
                initHttp();
                logger.info(" Service Started");
            } else {
                logger.warn("HTTP Already Started");
            }
        }
    }

    /**
     *used to exit from the application
     */
    public void stop() {
        DbConnectionManager.getInstance().closeDB();
        Utility.quit();
        alive.set(Utility.FALSE);
    }

    /**
     * Used to halt the HTTP Service, but it can be started again with start()
     * can not be used by external apps for now
     */
    public void block() {
        if (halted.compareAndSet(Utility.FALSE, Utility.TRUE)) {
            Spark.stop();
            logger.warn("HTPP Stopped");
        } else {
            logger.warn("HTTP Already Stopped");
        }
    }

    public AtomicBoolean getAlive() {
        return alive;
    }

    public void setAlive() {
        this.alive.set(Utility.TRUE);
    }

    public static void main(String[] args) {

        EuroToUsdMicroApp app = new EuroToUsdMicroApp();
        boolean started = app.runCommand(args[0].trim());
        if (started) {
            app.setAlive();
            while (app.getAlive().get()) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                }
            }
            Spark.stop();
            System.out.println("Goodbye ...");
        } else {
            System.out.println("can not be started");
        }
    }

    /**
     * 
     * @param command : command to be executed: start or block
     * in order to start the HTTP REST Service or stop it
     * @return if the command is block or start true, otherwise false
     */
    public boolean runCommand(String command) {
        boolean result = Utility.TRUE;
        if (command.equalsIgnoreCase(Utility.BLOCK)) {
            block();
            System.out.println("HTTP blocked");
        } else if (command.equalsIgnoreCase(Utility.START)) {
            start();
            System.out.println(" Service started");
        } else {
            logger.warn("Unknown or stop command: {}", command);
            result = Utility.FALSE;
        }
        return result;
    }

    @Override
    public void run() {
        if (logger.isDebugEnabled())
            logger.debug("Hello World! I am on port {}", Utility.HTTP_PORT);
        if (halted.compareAndSet(Utility.TRUE, Utility.FALSE)) {
            initHttp();
            logger.info("HTTP Service Started");
        } else {
            logger.warn("exiting");
            Utility.quit();
        }
    }

    /**
     * To start the REST API and the related HTTP Service this method is used
     * Sparkjava is used to build the REST Service
     */
    private void initHttp() {
        init();
        before((req, res) -> {
            res.type("application/json");
        });
        get("/eurtousd/between", (req, res) -> {
            logger.debug("Request to get historic rates is received");
            String start = req.queryParams("startDate").trim();
            String end = req.queryParams("endDate").trim();
            int[] input = Utility.validateInput(start, end);
            if (input != null) {
                return executeHistoryRequest(input[0], input[1]);
            } else {
                return ApiImp.getInstance().getWrongInput();
            }
        }, Utility.json());

        get("/eurtousd/latest", (req, res) -> {
            logger.debug("Request to get latest rate is received");
            return executeLatestRequest();
        }, Utility.json());

        get("/eurtousd/stopService", (req, res) -> {
            logger.debug("Request to stop service is received");
            stop();
            return ApiImp.getInstance().getSuccWithEmptyResult();
        }, Utility.json());

    }
    /**
     * in order not the bind the requestor to the execution of the request asnych execution
     * is used here. For the latest rate request the requestor wait no more than 1 seconds here
     * if the request handling
     *  takes more than 3 secs the requestor is informed about that situation with a proper 
     *  status message
     * @return
     */
    private ApiResult executeLatestRequest() {
        ApiResult result = null;
        if (amIAllowed()) {
            final CompletableFuture<ApiResult> responseFuture = CompletableFuture.supplyAsync(() -> ApiImp.getInstance().getLatestRate(),pool);
            try {
                result = responseFuture.get(Utility.ONE, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error("Error in executeLatestRequest ", e);
                result = ApiImp.getInstance().getExcResult();
            } catch (ExecutionException e) {
                logger.error("Error in executeLatestRequest ", e);
                result = ApiImp.getInstance().getExcResult();
            } catch (TimeoutException e) {
                logger.error("Error in executeLatestRequest ", e);
                result = ApiImp.getInstance().getTimeOutResult();
            }
        } else {
            result = ApiImp.getInstance().getTpsResult();
        }
        return result;
    }
    /**
     * n order not the bind the requestor to the execution of the request asnych execution
     * is used here. For the latest rate request the requestor wait no more than 3 seconds here.
     *  the rate values between the dates start and end is returned and if the request handling
     *  takes more than 3 secs the requestor is informed about that situation with a proper 
     *  status message
     * @param start
     * @param end
     * @return
     */
    private ApiResult executeHistoryRequest(int start, int end) {
        ApiResult result = null;
        if (amIAllowed()) {
            final CompletableFuture<ApiResult> responseFuture = CompletableFuture.supplyAsync(() -> ApiImp.getInstance().getHistoricalRates(start,
                                                                                                                                            end),pool);
            try {
                result = responseFuture.get(Utility.THREE, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error("Error in executeHistoryRequest ", e);
                result = ApiImp.getInstance().getExcResult();
            } catch (ExecutionException e) {
                logger.error("Error in executeHistoryRequest ", e);
                result = ApiImp.getInstance().getExcResult();
            } catch (TimeoutException e) {
                logger.error("Error in executeHistoryRequest ", e);
                result = ApiImp.getInstance().getTimeOutResult();
            }
        } else {
            result = ApiImp.getInstance().getTpsResult();
        }
        return result;
    }

    private boolean amIAllowed() {
        return requestCountPerSecController.isTpsAvailable();
    }
}
