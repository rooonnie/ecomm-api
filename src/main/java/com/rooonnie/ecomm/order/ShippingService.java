package com.rooonnie.ecomm.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ShippingService {

    private final String domesticCountry;
    private final BigDecimal domesticFee;
    private final BigDecimal internationalFee;

    public ShippingService(
            @Value("${ecomm.shipping.domestic-country}") String domesticCountry,
            @Value("${ecomm.shipping.domestic-fee}") BigDecimal domesticFee,
            @Value("${ecomm.shipping.international-fee}") BigDecimal internationalFee
    ) {
        this.domesticCountry = domesticCountry.trim();
        this.domesticFee = domesticFee.setScale(4, RoundingMode.HALF_UP);
        this.internationalFee = internationalFee.setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal quote(String country) {
        if (country != null && country.trim().equalsIgnoreCase(domesticCountry)) {
            return domesticFee;
        }
        return internationalFee;
    }
}
