package com.rooonnie.ecomm.catalog;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void productCanHaveCutTapeAndReelSkus() throws Exception {
        mockMvc.perform(get("/api/packaging-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].code", hasItem("CUT_TAPE")))
                .andExpect(jsonPath("$[*].code", hasItem("TAPE_AND_REEL")));

        MvcResult categories = mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andReturn();
        MvcResult manufacturers = mockMvc.perform(get("/api/manufacturers"))
                .andExpect(status().isOk())
                .andReturn();

        long categoryId = extractFirstId(categories.getResponse().getContentAsString());
        long manufacturerId = extractFirstId(manufacturers.getResponse().getContentAsString());
        long cutTapeId = extractIdByCode(
                mockMvc.perform(get("/api/packaging-types")).andReturn().getResponse().getContentAsString(),
                "CUT_TAPE"
        );
        long reelId = extractIdByCode(
                mockMvc.perform(get("/api/packaging-types")).andReturn().getResponse().getContentAsString(),
                "TAPE_AND_REEL"
        );

        MvcResult productResult = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mpn": "RC0603FR-0710KL",
                                  "manufacturerId": %d,
                                  "categoryId": %d,
                                  "name": "10 kOhm 1%% 0603 chip resistor",
                                  "packageCase": "0603",
                                  "lifecycle": "ACTIVE"
                                }
                                """.formatted(manufacturerId, categoryId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mpn").value("RC0603FR-0710KL"))
                .andReturn();

        long productId = extractId(productResult.getResponse().getContentAsString());

        mockMvc.perform(post("/api/skus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "packagingTypeId": %d,
                                  "skuCode": "RC0603-CT",
                                  "qtyPerPack": 1,
                                  "moq": 10,
                                  "status": "ACTIVE"
                                }
                                """.formatted(productId, cutTapeId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.packagingCode").value("CUT_TAPE"));

        mockMvc.perform(post("/api/skus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "packagingTypeId": %d,
                                  "skuCode": "RC0603-TR",
                                  "qtyPerPack": 5000,
                                  "reelSize": "7in",
                                  "moq": 1,
                                  "status": "ACTIVE"
                                }
                                """.formatted(productId, reelId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.packagingCode").value("TAPE_AND_REEL"));

        mockMvc.perform(get("/api/products/" + productId + "/skus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    private static long extractFirstId(String json) {
        int start = json.indexOf("\"id\":") + 5;
        int end = json.indexOf(",", start);
        return Long.parseLong(json.substring(start, end).trim());
    }

    private static long extractId(String json) {
        return extractFirstId(json);
    }

    private static long extractIdByCode(String json, String code) {
        int codeIndex = json.indexOf("\"code\":\"" + code + "\"");
        int idIndex = json.lastIndexOf("\"id\":", codeIndex) + 5;
        int end = json.indexOf(",", idIndex);
        return Long.parseLong(json.substring(idIndex, end).trim());
    }
}
