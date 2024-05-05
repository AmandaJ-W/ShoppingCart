package com.techelevator.services;
import com.techelevator.exception.DaoException;
import com.techelevator.model.Tax;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
public class TaxService {
    private String taxApiUrl = "https://teapi.netlify.app/api/statetax?state=";
    private RestTemplate restTemplate = new RestTemplate();

    public BigDecimal getTaxRateByState(String stateCode) {
        try {
            String apiUrl = taxApiUrl + stateCode;
            Tax taxRate = restTemplate.getForObject(apiUrl, Tax.class);
            if (taxRate != null) {
                return taxRate.getSalesTax();
            }
        } catch (DaoException e) {
            e.getMessage();
        }
        return BigDecimal.ZERO;
    }
}


