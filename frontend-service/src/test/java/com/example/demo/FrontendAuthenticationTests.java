package com.example.demo;

import com.example.demo.controller.LoginPageController.AuthenticationResult;
import com.example.demo.controller.LoginPageController.LoginRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@AutoConfigureMockMvc
@SpringBootTest
class FrontendAuthenticationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestTemplate restTemplate;

        @Test
        void sendsUnauthenticatedDirectPageRequestsToLogin() throws Exception {
                mockMvc.perform(get("/"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/login"));
        }

    @Test
    void redirectsAdminToExistingDashboardAndKeepsSessionOnRefresh() throws Exception {
        when(restTemplate.postForObject(eq("http://localhost:8081/api/authenticate"),
                any(LoginRequest.class), eq(AuthenticationResult.class)))
                .thenReturn(new AuthenticationResult("ADMIN-100", "ADMIN"));

        MvcResult login = mockMvc.perform(post("/login").with(csrf())
                        .param("forceNumber", "ADMIN-100")
                        .param("password", "admin-password-100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        HttpSession session = login.getRequest().getSession(false);
        mockMvc.perform(get("/").session((MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void redirectsUserOnlyToWelcomeAndLogoutInvalidatesSession() throws Exception {
        when(restTemplate.postForObject(eq("http://localhost:8081/api/authenticate"),
                any(LoginRequest.class), eq(AuthenticationResult.class)))
                .thenReturn(new AuthenticationResult("USER-100", "USER"));

        MvcResult login = mockMvc.perform(post("/login").with(csrf())
                        .param("forceNumber", "USER-100")
                        .param("password", "user-password-100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        mockMvc.perform(get("/welcome").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"));
        mockMvc.perform(get("/").session(session))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
        assertTrue(session.isInvalid());
        mockMvc.perform(get("/welcome"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void rejectsInvalidAndMissingCredentialsWithoutCallingClientSideRoles() throws Exception {
        when(restTemplate.postForObject(eq("http://localhost:8081/api/authenticate"),
                any(LoginRequest.class), eq(AuthenticationResult.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        mockMvc.perform(post("/login").with(csrf())
                        .param("forceNumber", "UNKNOWN")
                        .param("password", "wrong-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));

        mockMvc.perform(post("/login").with(csrf()).param("forceNumber", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }
}