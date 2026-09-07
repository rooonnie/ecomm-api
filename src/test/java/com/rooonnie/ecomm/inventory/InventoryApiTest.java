package com.rooonnie.ecomm.inventory;

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
class InventoryApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void stockAndVolumePriceArePerSku() throws Exception {
        long categoryId = extractFirstId(mockMvc.perform(get("/api/categories")).andReturn().getResponse().getContentAsString());
        long manufacturerId = extractFirstId(mockMvc.perform(get("/api/manufacturers")).andReturn().getResponse().getContentAsString());
        long cutTapeId = extractIdByCode(
                mockMvc.perform(get("/api/packaging-types")).andReturn().getResponse().getContentAsString(),
                "CUT_TAPE"
        );

        long productId = extractFirstId(mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mpn": "RC0603FR-074K7L",
                                  "manufacturerId": %d,
                                  "categoryId": %d,
                                  "name": "4.7 kOhm 0603 chip resistor",
                                  "packageCase": "0603",
                                  "lifecycle": "ACTIVE"
                                }
                                """.formatted(manufacturerId, categoryId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());

        long skuId = extractFirstId(mockMvc.perform(post("/api/skus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "packagingTypeId": %d,
                                  "skuCode": "RC0603-4K7-CT",
                                  "qtyPerPack": 1,
                                  "moq": 10,
                                  "status": "ACTIVE"
                                }
                                """.formatted(productId, cutTapeId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());

        mockMvc.perform(get("/api/skus/" + skuId + "/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtyOnHand").value(0))
                .andExpect(jsonPath("$.qtyAvailable").value(0));

        mockMvc.perform(put("/api/skus/" + skuId + "/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qtyOnHand\": 250}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtyOnHand").value(250))
                .andExpect(jsonPath("$.qtyAvailable").value(250));

        mockMvc.perform(put("/api/skus/" + skuId + "/price-breaks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "breaks": [
                                    {"minQty": 1, "unitPrice": 2.50},
                                    {"minQty": 100, "unitPrice": 1.10},
                                    {"minQty": 1000, "unitPrice": 0.45}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        mockMvc.perform(get("/api/skus/" + skuId + "/price-breaks/quote").param("qty", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appliedMinQty").value(1))
                .andExpect(jsonPath("$.unitPrice").value(2.5));

        mockMvc.perform(get("/api/skus/" + skuId + "/price-breaks/quote").param("qty", "150"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appliedMinQty").value(100))
                .andExpect(jsonPath("$.unitPrice").value(1.1))
                .andExpect(jsonPath("$.lineTotal").value(165.0));
    }

    private static long extractFirstId(String json) {
        int start = json.indexOf("\"id\":") + 5;
        int end = json.indexOf(",", start);
        if (end < 0) {
            end = json.indexOf("}", start);
        }
        return Long.parseLong(json.substring(start, end).trim());
    }

    private static long extractIdByCode(String json, String code) {
        int codeIndex = json.indexOf("\"code\":\"" + code + "\"");
        int idIndex = json.lastIndexOf("\"id\":", codeIndex) + 5;
        int end = json.indexOf(",", idIndex);
        return Long.parseLong(json.substring(idIndex, end).trim());
    }
}
