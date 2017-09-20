package org.gokhanka.euuscurrrates.api;
import java.util.List;

import org.gokhanka.euuscurrrates.db.EurToUsd;
/**
 * the results are transformed to json objects by this class
 * @author gokhanka
 *
 */
public class ApiResult {
 private String status = null;
 private List<EurToUsd> rateList = null;

public String getStatus() {
    return status;
}

public void setStatus(String status) {
    this.status = status;
}

public List<EurToUsd> getRateList() {
    return rateList;
}

public void setRateList(List<EurToUsd> rateList) {
    this.rateList = rateList;
}
 
}
