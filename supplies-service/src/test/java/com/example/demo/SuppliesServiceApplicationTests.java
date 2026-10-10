package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
	void suppliesApiRequiresAdmin() throws Exception {
		mockMvc.perform(get("/api/supplies"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/api/supplies").with(user("USER-100").roles("USER")))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/supplies").with(user("ADMIN-100").roles("ADMIN")))
				.andExpect(status().isOk());
	}

	    @Test
	    void adminCanStillCreateSupplyItems() throws Exception {
		mockMvc.perform(post("/api/supplies/save")
			    .with(user("ADMIN-100").roles("ADMIN"))
			    .contentType(MediaType.APPLICATION_JSON)
			    .content("{\"itemCode\":\"MED-100\",\"name\":\"First aid kit\","
				    + "\"category\":\"MEDICAL\",\"unit\":\"each\","
				    + "\"quantityOnHand\":5,\"reorderLevel\":2}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.itemCode").value("MED-100"));
	    }

}
