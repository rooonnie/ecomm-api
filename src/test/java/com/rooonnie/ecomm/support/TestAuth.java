package com.rooonnie.ecomm.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

public final class TestAuth {

    private TestAuth() {
    }

    public static String adminToken(MockMvc mockMvc) throws Exception {
        return login(mockMvc, "admin@ecomm.local", "adminpass1");
    }

    public static String login(MockMvc mockMvc, String email, String password) throws Exception {
        String json = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        int start = json.indexOf("\"token\":\"") + 9;
        return json.substring(start, json.indexOf('"', start));
    }
}
