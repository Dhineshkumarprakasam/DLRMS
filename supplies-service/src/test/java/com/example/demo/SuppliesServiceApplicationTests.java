package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class SuppliesServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void suppliesApiWorksWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/supplies"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/supplies/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemCode\":\"MED-100\",\"name\":\"First aid kit\","
                                + "\"category\":\"MEDICAL\",\"unit\":\"each\","
                                + "\"quantityOnHand\":5,\"reorderLevel\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCode").value("MED-100"));
    }
}
