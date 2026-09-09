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
class CartApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postingSameSkuReplacesQtyAndPutSetsOrClearsLine() throws Exception {
        long categoryId = id(getJson("/api/categories"));
        long manufacturerId = id(getJson("/api/manufacturers"));
        long cutTapeId = idByCode(getJson("/api/packaging-types"), "CUT_TAPE");

        String admin = TestAuth.adminToken(mockMvc);

        long productId = id(mockMvc.perform(post("/api/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mpn": "RC0603FR-07330RL",
                                  "manufacturerId": %d,
                                  "categoryId": %d,
                                  "name": "330 Ohm 0603 chip resistor",
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
                                  "skuCode": "RC0603-330R-CT",
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
                        .content("{\"email\":\"cart-qty@example.com\",\"name\":\"Cart Qty\",\"password\":\"password1\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long userId = id(registered);
        String token = bearerToken(registered);

        String cartJson = mockMvc.perform(post("/api/users/" + userId + "/cart/items")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skuId\": %d, \"qty\": 10}".formatted(skuId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].qty").value(10))
                .andExpect(jsonPath("$.items[0].unitPrice").value(2.5))
                .andReturn().getResponse().getContentAsString();
        long itemId = itemId(cartJson);

        mockMvc.perform(post("/api/users/" + userId + "/cart/items")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skuId\": %d, \"qty\": 10}".formatted(skuId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].qty").value(10));

        mockMvc.perform(put("/api/users/" + userId + "/cart/items/" + itemId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qty\": 5}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/users/" + userId + "/cart/items/" + itemId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qty\": 150}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].qty").value(150))
                .andExpect(jsonPath("$.items[0].unitPrice").value(1.1));

        mockMvc.perform(put("/api/users/" + userId + "/cart/items/" + itemId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qty\": 0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
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

    private static long itemId(String json) {
        int start = json.indexOf("\"items\":[{\"id\":") + 15;
        return Long.parseLong(json.substring(start, json.indexOf(",", start)).trim());
    }

    private static long idByCode(String json, String code) {
        int codeIndex = json.indexOf("\"code\":\"" + code + "\"");
        int idIndex = json.lastIndexOf("\"id\":", codeIndex) + 5;
        return Long.parseLong(json.substring(idIndex, json.indexOf(",", idIndex)).trim());
    }
}
