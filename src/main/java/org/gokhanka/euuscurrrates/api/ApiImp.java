package org.gokhanka.euuscurrrates.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gokhanka.euuscurrrates.db.DbActions;
import org.gokhanka.euuscurrrates.db.EurToUsd;
import org.gokhanka.euuscurrrates.utility.Utility;
/**
 * the REST API interfaces are mainly implemented here
 * @author gokhanka
 *
 */
public class ApiImp {

    private static ApiImp instance = null;
    private static Object mutex    = new Object();
    private static final Logger    logger                       = LogManager.getLogger();
    private ApiImp() {
        super();
    }

    public static ApiImp getInstance() {
        if (instance == null) {
            synchronized (mutex) {
                if (instance == null) {
                    instance = new ApiImp();
                }
            }
        }
        return instance;
    }
    /**
     * the latest value of the rate is returned here
     * @return
     */
    public ApiResult getLatestRate() {
        ApiResult result = null;
        EurToUsd tmp = DbActions.getLatestRate();
        if (tmp != null) {
            ArrayList<EurToUsd> data = new ArrayList<EurToUsd>();
            data.add(tmp);
            result = constructSuccResp(data);
        } else {
            result = getExcResult();
        }
        return result;
    }
    /**
     * to retrieve the historic results for the given dates
     * @param start
     * @param end
     * @return
     */
    public ApiResult getHistoricalRates(int start, int end) {
        ApiResult result = null;
        List<EurToUsd> tmp = DbActions.getHistoricRates(start, end);
        if (tmp != null && !tmp.isEmpty()) {
            result = constructSuccResp(tmp);
        } else {
            result = constructErrorResp(Utility.EMPTY_RESULT);
        }
        return result;
    }

    public ApiResult getExcResult() {
        logger.error("an internal exception has occured");
        return constructErrorResp(Utility.INTERNAL_ERROR);
    }
    
    public ApiResult getSuccWithEmptyResult() {
        List<EurToUsd> tmp = new ArrayList<EurToUsd>();
        return constructSuccResp(tmp);
    }

    public ApiResult getTimeOutResult() {
        logger.error("request can ot be processed within time limits");
        return constructErrorResp(Utility.TIMEOUT_ERR);
    }

    public ApiResult getTpsResult() {
        logger.error("tps exceeded");
        return constructErrorResp(Utility.TPS_EXCEEDED);
    }

    public ApiResult getWrongInput() {
        logger.error("Wrong input received");
        return constructErrorResp(Utility.WRONG_INPUT);
    }

    private ApiResult constructErrorResp(String errMsg) {
        ApiResult result = null;
        result = new ApiResult();
        result.setStatus(errMsg);
        result.setRateList(null);
        return result;
    }
    private ApiResult constructSuccResp(List<EurToUsd> resultSet) {
        ApiResult result = null;
        result = new ApiResult();
        result.setStatus(Utility.success);
        result.setRateList(resultSet);
        return result;
    }

}
