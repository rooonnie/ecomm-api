package com.rooonnie.ecomm.rereel;

import static org.hamcrest.Matchers.nullValue;
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
class RereelApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void completeMovesCutTapeStockOntoMiniReel() throws Exception {
        long categoryId = id(getJson("/api/categories"));
        long manufacturerId = id(getJson("/api/manufacturers"));
        long cutTapeId = idByCode(getJson("/api/packaging-types"), "CUT_TAPE");
        long miniReelId = idByCode(getJson("/api/packaging-types"), "MINI_REEL");

        String admin = TestAuth.adminToken(mockMvc);

        long productId = id(mockMvc.perform(post("/api/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mpn": "RC0603FR-07511L",
                                  "manufacturerId": %d,
                                  "categoryId": %d,
                                  "name": "5.11 kOhm 0603 chip resistor",
                                  "packageCase": "0603",
                                  "lifecycle": "ACTIVE"
                                }
                                """.formatted(manufacturerId, categoryId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        long sourceSkuId = id(mockMvc.perform(post("/api/skus")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "packagingTypeId": %d,
                                  "skuCode": "RC0603-5K11-CT",
                                  "qtyPerPack": 1,
                                  "moq": 10,
                                  "status": "ACTIVE"
                                }
                                """.formatted(productId, cutTapeId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(put("/api/skus/" + sourceSkuId + "/inventory")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qtyOnHand\": 250}"))
                .andExpect(status().isOk());

        String token = bearerToken(register("rereel-complete@example.com", "Rereel User"));

        long jobId = id(mockMvc.perform(post("/api/rereel-jobs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceSkuId": %d,
                                  "targetPackagingTypeId": %d,
                                  "qty": 100,
                                  "reelSize": "7IN"
                                }
                                """.formatted(sourceSkuId, miniReelId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.sourcePackagingCode").value("CUT_TAPE"))
                .andExpect(jsonPath("$.targetPackagingCode").value("MINI_REEL"))
                .andExpect(jsonPath("$.targetSkuId").value(nullValue()))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(get("/api/skus/" + sourceSkuId + "/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtyOnHand").value(250))
                .andExpect(jsonPath("$.qtyReserved").value(100))
                .andExpect(jsonPath("$.qtyAvailable").value(150));

        String completed = mockMvc.perform(post("/api/rereel-jobs/" + jobId + "/complete")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.targetSkuCode").value("RC0603-5K11-CT-MR"))
                .andReturn().getResponse().getContentAsString();
        long targetSkuId = nestedId(completed, "targetSkuId");

        mockMvc.perform(get("/api/skus/" + sourceSkuId + "/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtyOnHand").value(150))
                .andExpect(jsonPath("$.qtyReserved").value(0))
                .andExpect(jsonPath("$.qtyAvailable").value(150));

        mockMvc.perform(get("/api/skus/" + targetSkuId + "/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtyOnHand").value(100))
                .andExpect(jsonPath("$.qtyReserved").value(0))
                .andExpect(jsonPath("$.qtyAvailable").value(100));

        mockMvc.perform(post("/api/rereel-jobs/" + jobId + "/complete")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void cancelReleasesReservedCutTape() throws Exception {
        long categoryId = id(getJson("/api/categories"));
        long manufacturerId = id(getJson("/api/manufacturers"));
        long cutTapeId = idByCode(getJson("/api/packaging-types"), "CUT_TAPE");
        long rereelId = idByCode(getJson("/api/packaging-types"), "REREEL");

        String admin = TestAuth.adminToken(mockMvc);

        long productId = id(mockMvc.perform(post("/api/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mpn": "RC0603FR-07680KL",
                                  "manufacturerId": %d,
                                  "categoryId": %d,
                                  "name": "680 kOhm 0603 chip resistor",
                                  "packageCase": "0603",
                                  "lifecycle": "ACTIVE"
                                }
                                """.formatted(manufacturerId, categoryId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        long sourceSkuId = id(mockMvc.perform(post("/api/skus")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "packagingTypeId": %d,
                                  "skuCode": "RC0603-680K-CT",
                                  "qtyPerPack": 1,
                                  "moq": 10,
                                  "status": "ACTIVE"
                                }
                                """.formatted(productId, cutTapeId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(put("/api/skus/" + sourceSkuId + "/inventory")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"qtyOnHand\": 250}"))
                .andExpect(status().isOk());

        String token = bearerToken(register("rereel-cancel@example.com", "Rereel Cancel"));

        long jobId = id(mockMvc.perform(post("/api/rereel-jobs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceSkuId": %d,
                                  "targetPackagingTypeId": %d,
                                  "qty": 80
                                }
                                """.formatted(sourceSkuId, rereelId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(post("/api/rereel-jobs/" + jobId + "/cancel")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get("/api/skus/" + sourceSkuId + "/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtyOnHand").value(250))
                .andExpect(jsonPath("$.qtyReserved").value(0))
                .andExpect(jsonPath("$.qtyAvailable").value(250));

        mockMvc.perform(post("/api/rereel-jobs/" + jobId + "/cancel")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isConflict());
    }

    private String register(String email, String name) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"name\":\"%s\",\"password\":\"password1\"}".formatted(email, name)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private static String bearerToken(String json) {
        int start = json.indexOf("\"token\":\"") + 9;
        return json.substring(start, json.indexOf('"', start));
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

    private static long nestedId(String json, String field) {
        int start = json.indexOf("\"" + field + "\":") + field.length() + 3;
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
