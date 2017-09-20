package org.gokhanka.euuscurrrates.rates;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gokhanka.euuscurrrates.db.DbActions;
import org.gokhanka.euuscurrrates.utility.DaemonThreadFactory;
import org.gokhanka.euuscurrrates.utility.Utility;

public class Schedular {

    private static final Logger             logger        = LogManager.getLogger();
    private static ScheduledExecutorService scheduledPool = null;

    public static void startTimer() {
        Runnable task = new Runnable() {

            @Override
            public void run() {
                long start = System.currentTimeMillis();
                String rate = UsdRateGetter.getInstance().getRate();
                int time = Integer.parseInt(Utility.df.format(new Date()));
                if (!DbActions.insertLates(time, rate)) {
                    logger.error("Rate can not be inserted with {} and {} , elapsed time {} ",
                                 rate,
                                 time,
                                 (System.currentTimeMillis() - start));
                }
            }
        };
        DaemonThreadFactory dtf = new DaemonThreadFactory();
        scheduledPool = Executors.newScheduledThreadPool(1, dtf);
        scheduledPool.scheduleWithFixedDelay(task,
                                             Utility.SCHEDULAR_INIT_DELAY,
                                             Utility.SCHEDULAR_REG_DELAY,
                                             TimeUnit.MINUTES);
        logger.info("Schedular started for {} mins for init delay and {} minsfor regular delay",
                    Utility.SCHEDULAR_INIT_DELAY,
                    Utility.SCHEDULAR_REG_DELAY);
    }

    public static void stopTimer() {
        scheduledPool.shutdownNow();
    }
}