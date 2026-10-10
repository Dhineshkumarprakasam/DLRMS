package com.example.demo;

import com.example.demo.model.AdminAccount;
import com.example.demo.model.PersonnelCredential;
import com.example.demo.model.User;
import com.example.demo.repository.AdminAccountRepository;
import com.example.demo.repository.PersonnelCredentialRepository;
import com.example.demo.repository.PersonnelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:personnel;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class PersonnelServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PersonnelRepository personnelRepository;

	@Autowired
	private PersonnelCredentialRepository personnelCredentialRepository;

	@Autowired
	private AdminAccountRepository adminAccountRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void setUp() {
		personnelCredentialRepository.deleteAll();
		adminAccountRepository.deleteAll();
		personnelRepository.deleteAll();

		User personnel = new User();
		personnel.setServiceNumber("USER-100");
		personnel.setName("Test User");
		personnel = personnelRepository.save(personnel);

		PersonnelCredential credential = new PersonnelCredential();
		credential.setPersonnel(personnel);
		credential.setPasswordHash(passwordEncoder.encode("user-password-100"));
		personnelCredentialRepository.save(credential);

		AdminAccount admin = new AdminAccount();
		admin.setForceNumber("ADMIN-100");
		admin.setPasswordHash(passwordEncoder.encode("admin-password-100"));
		adminAccountRepository.save(admin);
	}

	@Test
	void authenticatesAdminWithoutReturningSensitiveFields() throws Exception {
		mockMvc.perform(post("/api/authenticate")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"forceNumber\":\"ADMIN-100\",\"password\":\"admin-password-100\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("ADMIN"))
				.andExpect(jsonPath("$.forceNumber").value("ADMIN-100"))
				.andExpect(jsonPath("$.passwordHash").doesNotExist());
	}

	@Test
	void authenticatesRegularPersonnelAsUser() throws Exception {
		mockMvc.perform(post("/api/authenticate")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"forceNumber\":\"USER-100\",\"password\":\"user-password-100\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("USER"));
	}

	@Test
	void rejectsWrongAndMissingCredentials() throws Exception {
		mockMvc.perform(post("/api/authenticate")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"forceNumber\":\"USER-100\",\"password\":\"wrong-password\"}"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/authenticate")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void protectsPersonnelApiForAdminOnly() throws Exception {
		mockMvc.perform(get("/api/personnel"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/api/personnel").with(user("USER-100").roles("USER")))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/personnel").with(user("ADMIN-100").roles("ADMIN")))
				.andExpect(status().isOk());
	}

	    @Test
	    void adminCanStillCreatePersonnelRecords() throws Exception {
		mockMvc.perform(post("/api/personnel/save")
			    .with(user("ADMIN-100").roles("ADMIN"))
			    .contentType(MediaType.APPLICATION_JSON)
			    .content("{\"serviceNumber\":\"USER-200\",\"name\":\"New User\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.serviceNumber").value("USER-200"));
	    }

	@Test
	void onlyAdminCanProvisionPersonnelCredentials() throws Exception {
		mockMvc.perform(post("/api/personnel/USER-100/credentials")
					.with(user("USER-100").roles("USER"))
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"password\":\"replacement-password-100\"}"))
				.andExpect(status().isForbidden());

		mockMvc.perform(post("/api/personnel/USER-100/credentials")
					.with(user("ADMIN-100").roles("ADMIN"))
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"password\":\"replacement-password-100\"}"))
				.andExpect(status().isNoContent());
	}

}
