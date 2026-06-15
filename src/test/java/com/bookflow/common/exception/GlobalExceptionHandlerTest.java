package com.bookflow.common.exception;

import com.bookflow.auth.service.AuthService;
import com.bookflow.auth.service.CustomUserDetailsService;
import com.bookflow.security.BearerAuthenticationEntryPoint;
import com.bookflow.security.BookFlowAccessDeniedHandler;
import com.bookflow.security.CurrentUserService;
import com.bookflow.security.JwtAuthenticationFilter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestController.class})
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private CurrentUserService currentUserService;

	@MockitoBean
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@MockitoBean
	private CustomUserDetailsService customUserDetailsService;

	@MockitoBean
	private BearerAuthenticationEntryPoint bearerAuthenticationEntryPoint;

	@MockitoBean
	private BookFlowAccessDeniedHandler bookFlowAccessDeniedHandler;

	@Test
	void shouldReturnStandardValidationErrorResponse() throws Exception {
		mockMvc.perform(post("/test/errors/validation")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "",
								  "email": "not-an-email"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Validation failed"))
				.andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.details[0].field").exists())
				.andExpect(jsonPath("$.details[0].message").exists())
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void shouldReturnStandardNotFoundErrorResponse() throws Exception {
		mockMvc.perform(get("/test/errors/not-found"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Business not found."))
				.andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"))
				.andExpect(jsonPath("$.details").isArray())
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void shouldReturnStandardConflictErrorResponse() throws Exception {
		mockMvc.perform(get("/test/errors/conflict"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Email already exists."))
				.andExpect(jsonPath("$.errorCode").value("CONFLICT"))
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void shouldReturnStandardNotFoundResponseForMissingRoutes() throws Exception {
		mockMvc.perform(get("/test/errors/missing-route"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Resource not found."))
				.andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
	}

	@Test
	void shouldHideStackTraceForUnexpectedErrors() throws Exception {
		mockMvc.perform(get("/test/errors/unexpected"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("An unexpected error occurred."))
				.andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
				.andExpect(content().string(not(containsString("IllegalStateException"))))
				.andExpect(content().string(not(containsString("internal stack trace detail"))));
	}

	@RestController
	@RequestMapping("/test/errors")
	public static class TestController {

		@PostMapping("/validation")
		void validateRequest(@Valid @RequestBody TestRequest request) {
		}

		@GetMapping("/not-found")
		void throwNotFound() {
			throw new ResourceNotFoundException("Business not found.");
		}

		@GetMapping("/conflict")
		void throwConflict() {
			throw new ConflictException("Email already exists.");
		}

		@GetMapping("/unexpected")
		void throwUnexpected() {
			throw new IllegalStateException("internal stack trace detail");
		}

	}

	record TestRequest(
			@NotBlank String name,
			@Email String email
	) {
	}

}
