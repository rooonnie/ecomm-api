package com.rooonnie.ecomm.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.rooonnie.ecomm.support.TestAuth;

@SpringBootTest
@AutoConfigureMockMvc
class CheckoutApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void checkoutReservesStockThenPayCapturesIt() throws Exception {
        long categoryId = id(getJson("/api/categories"));
        long manufacturerId = id(getJson("/api/manufacturers"));
        long cutTapeId = idByCode(getJson("/api/packaging-types"), "CUT_TAPE");

        String admin = TestAuth.adminToken(mockMvc);

        long productId = id(mockMvc.perform(post("/api/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mpn": "RC0603FR-07100KL",
                                  "manufacturerId": %d,
                                  "categoryId": %d,
                                  "name": "100 kOhm 0603 chip resistor",
                                  "packageCase": "0603",
                                  "lifecycle": "ACTIVE"
                                }
                                """.formatted(manufacturerId, categoryId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        long skuId = id(mockMvc.perform(post("/api/skus")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "packagingTypeId": %d,
                                  "skuCode": "RC0603-100K-CT",
                                  "qtyPerPack": 1,
                                  "moq": 10,
                                  "status": "ACTIVE"
                                }
                                """.formatted(productId, cutTapeId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(put("/api/skus/" + skuId + "/inventory")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qtyOnHand\": 250}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/skus/" + skuId + "/price-breaks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "breaks": [
                                    {"minQty": 1, "unitPrice": 2.50},
                                    {"minQty": 100, "unitPrice": 1.10}
                                  ]
                                }
                                """))
                .andExpect(status().isOk());

        String registered = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"buyer@example.com\",\"name\":\"Test Buyer\",\"password\":\"password1\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long userId = id(registered);
        String token = bearerToken(registered);

        long addressId = id(mockMvc.perform(post("/api/users/" + userId + "/addresses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "line1": "123 Test St",
                                  "city": "Manila",
                                  "country": "PH",
                                  "postal": "1000"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(post("/api/users/" + userId + "/cart/items")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skuId\": %d, \"qty\": 150}".formatted(skuId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].unitPrice").value(1.1));

        long orderId = id(mockMvc.perform(post("/api/users/" + userId + "/checkout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressId\": %d}".formatted(addressId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.payment.status").value("PENDING"))
                .andExpect(jsonPath("$.items[0].packagingCode").value("CUT_TAPE"))
                .andExpect(jsonPath("$.subtotal").value(165.0))
                .andExpect(jsonPath("$.shippingFee").value(15.0))
                .andExpect(jsonPath("$.total").value(180.0))
                .andExpect(jsonPath("$.payment.amount").value(180.0))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(get("/api/skus/" + skuId + "/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtyOnHand").value(250))
                .andExpect(jsonPath("$.qtyReserved").value(150))
                .andExpect(jsonPath("$.qtyAvailable").value(100));

        mockMvc.perform(post("/api/orders/" + orderId + "/pay")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.payment.status").value("PAID"));

        mockMvc.perform(get("/api/skus/" + skuId + "/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtyOnHand").value(100))
                .andExpect(jsonPath("$.qtyReserved").value(0))
                .andExpect(jsonPath("$.qtyAvailable").value(100));

        mockMvc.perform(post("/api/orders/" + orderId + "/cancel")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void cancelPendingOrderReleasesReservedStock() throws Exception {
        long categoryId = id(getJson("/api/categories"));
        long manufacturerId = id(getJson("/api/manufacturers"));
        long cutTapeId = idByCode(getJson("/api/packaging-types"), "CUT_TAPE");

        String admin = TestAuth.adminToken(mockMvc);

        long productId = id(mockMvc.perform(post("/api/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mpn": "RC0603FR-07220KL",
                                  "manufacturerId": %d,
                                  "categoryId": %d,
                                  "name": "220 kOhm 0603 chip resistor",
                                  "packageCase": "0603",
                                  "lifecycle": "ACTIVE"
                                }
                                """.formatted(manufacturerId, categoryId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        long skuId = id(mockMvc.perform(post("/api/skus")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "packagingTypeId": %d,
                                  "skuCode": "RC0603-220K-CT",
                                  "qtyPerPack": 1,
                                  "moq": 10,
                                  "status": "ACTIVE"
                                }
                                """.formatted(productId, cutTapeId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(put("/api/skus/" + skuId + "/inventory")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qtyOnHand\": 250}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/skus/" + skuId + "/price-breaks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "breaks": [
                                    {"minQty": 1, "unitPrice": 2.50},
                                    {"minQty": 100, "unitPrice": 1.10}
                                  ]
                                }
                                """))
                .andExpect(status().isOk());

        String registered = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cancel-buyer@example.com\",\"name\":\"Cancel Buyer\",\"password\":\"password1\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long userId = id(registered);
        String token = bearerToken(registered);

        long addressId = id(mockMvc.perform(post("/api/users/" + userId + "/addresses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "line1": "123 Test St",
                                  "city": "Manila",
                                  "country": "PH",
                                  "postal": "1000"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(post("/api/users/" + userId + "/cart/items")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skuId\": %d, \"qty\": 150}".formatted(skuId)))
                .andExpect(status().isOk());

        long orderId = id(mockMvc.perform(post("/api/users/" + userId + "/checkout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressId\": %d}".formatted(addressId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(post("/api/orders/" + orderId + "/cancel")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.payment.status").value("CANCELLED"));

        mockMvc.perform(get("/api/skus/" + skuId + "/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtyOnHand").value(250))
                .andExpect(jsonPath("$.qtyReserved").value(0))
                .andExpect(jsonPath("$.qtyAvailable").value(250));

        mockMvc.perform(post("/api/orders/" + orderId + "/cancel")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/orders/" + orderId + "/pay")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void checkoutUsesInternationalFeeOutsidePh() throws Exception {
        long categoryId = id(getJson("/api/categories"));
        long manufacturerId = id(getJson("/api/manufacturers"));
        long cutTapeId = idByCode(getJson("/api/packaging-types"), "CUT_TAPE");

        String admin = TestAuth.adminToken(mockMvc);

        long productId = id(mockMvc.perform(post("/api/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mpn": "RC0603FR-07470KL",
                                  "manufacturerId": %d,
                                  "categoryId": %d,
                                  "name": "470 kOhm 0603 chip resistor",
                                  "packageCase": "0603",
                                  "lifecycle": "ACTIVE"
                                }
                                """.formatted(manufacturerId, categoryId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        long skuId = id(mockMvc.perform(post("/api/skus")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "packagingTypeId": %d,
                                  "skuCode": "RC0603-470K-CT",
                                  "qtyPerPack": 1,
                                  "moq": 10,
                                  "status": "ACTIVE"
                                }
                                """.formatted(productId, cutTapeId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(put("/api/skus/" + skuId + "/inventory")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qtyOnHand\": 250}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/skus/" + skuId + "/price-breaks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "breaks": [
                                    {"minQty": 1, "unitPrice": 2.50},
                                    {"minQty": 100, "unitPrice": 1.10}
                                  ]
                                }
                                """))
                .andExpect(status().isOk());

        String registered = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ship-intl@example.com\",\"name\":\"Intl Buyer\",\"password\":\"password1\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long userId = id(registered);
        String token = bearerToken(registered);

        long addressId = id(mockMvc.perform(post("/api/users/" + userId + "/addresses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "line1": "1 Market St",
                                  "city": "San Francisco",
                                  "country": "US",
                                  "postal": "94105"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(post("/api/users/" + userId + "/cart/items")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skuId\": %d, \"qty\": 10}".formatted(skuId)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/users/" + userId + "/checkout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressId\": %d}".formatted(addressId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subtotal").value(25.0))
                .andExpect(jsonPath("$.shippingFee").value(45.0))
                .andExpect(jsonPath("$.total").value(70.0))
                .andExpect(jsonPath("$.payment.amount").value(70.0));
    }

    private String getJson(String path) throws Exception {
        return mockMvc.perform(get(path)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    private static String bearerToken(String json) {
        int start = json.indexOf("\"token\":\"") + 9;
        return json.substring(start, json.indexOf('"', start));
    }

    private static long id(String json) {
        int start = json.indexOf("\"id\":") + 5;
        int end = json.indexOf(",", start);
        if (end < 0) {
            end = json.indexOf("}", start);
        }
        return Long.parseLong(json.substring(start, end).trim());
    }

    private static long idByCode(String json, String code) {
        int codeIndex = json.indexOf("\"code\":\"" + code + "\"");
        int idIndex = json.lastIndexOf("\"id\":", codeIndex) + 5;
        return Long.parseLong(json.substring(idIndex, json.indexOf(",", idIndex)).trim());
    }
}
