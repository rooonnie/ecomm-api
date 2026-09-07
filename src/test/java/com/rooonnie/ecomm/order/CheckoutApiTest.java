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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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

        long productId = id(mockMvc.perform(post("/api/products")
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qtyOnHand\": 250}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/skus/" + skuId + "/price-breaks")
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

        long userId = id(mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"buyer@example.com\",\"name\":\"Test Buyer\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        long addressId = id(mockMvc.perform(post("/api/users/" + userId + "/addresses")
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skuId\": %d, \"qty\": 150}".formatted(skuId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].unitPrice").value(1.1));

        long orderId = id(mockMvc.perform(post("/api/users/" + userId + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressId\": %d}".formatted(addressId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.payment.status").value("PENDING"))
                .andExpect(jsonPath("$.items[0].packagingCode").value("CUT_TAPE"))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(get("/api/skus/" + skuId + "/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtyOnHand").value(250))
                .andExpect(jsonPath("$.qtyReserved").value(150))
                .andExpect(jsonPath("$.qtyAvailable").value(100));

        mockMvc.perform(post("/api/orders/" + orderId + "/pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.payment.status").value("PAID"));

        mockMvc.perform(get("/api/skus/" + skuId + "/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtyOnHand").value(100))
                .andExpect(jsonPath("$.qtyReserved").value(0))
                .andExpect(jsonPath("$.qtyAvailable").value(100));
    }

    private String getJson(String path) throws Exception {
        return mockMvc.perform(get(path)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
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
