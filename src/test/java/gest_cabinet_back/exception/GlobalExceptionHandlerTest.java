package gest_cabinet_back.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ApiTestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void successResponseIsWrappedInCommonFormat() throws Exception {
        mockMvc.perform(get("/api/test/success"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Opération effectuée avec succès"))
                .andExpect(jsonPath("$.data.value").value("ok"));
    }

    @Test
    void createdResponsesKeep201AndNoContentResponsesStayEmpty() throws Exception {
        mockMvc.perform(post("/api/test/created"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.value").value("created"));

        mockMvc.perform(get("/api/test/no-content"))
                .andExpect(status().isNoContent())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(""));
    }

    @Test
    void typedNotFoundExceptionReturnsUniform404() throws Exception {
        mockMvc.perform(get("/api/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void invalidInputReturns422WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\" \"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(422))
                .andExpect(jsonPath("$.errors[0]").value("value : doit être renseigné"));
    }

    @Test
    void unexpectedExceptionDoesNotExposeInternalDetails() throws Exception {
        mockMvc.perform(get("/api/test/internal"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Une erreur interne est survenue."))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("secret"))));
    }

    @Test
    void authenticationAndAuthorizationErrorsHaveCorrectStatuses() throws Exception {
        mockMvc.perform(get("/api/test/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401));

        mockMvc.perform(get("/api/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(403));
    }

    @Test
    void businessConflictReturns409() throws Exception {
        mockMvc.perform(get("/api/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    @Test
    void malformedBodyAndUnsupportedMethodReturn400And405() throws Exception {
        mockMvc.perform(post("/api/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));

        mockMvc.perform(post("/api/test/success"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.statusCode").value(405));
    }

    @RestController
    static class ApiTestController {

        @GetMapping("/api/test/success")
        Object success() {
            return java.util.Map.of("value", "ok");
        }

        @PostMapping("/api/test/created")
        @ResponseStatus(HttpStatus.CREATED)
        Object created() {
            return java.util.Map.of("value", "created");
        }

        @GetMapping("/api/test/no-content")
        ResponseEntity<Void> noContent() {
            return ResponseEntity.noContent().build();
        }

        @GetMapping("/api/test/not-found")
        Object notFound() {
            throw new RessourceNotFoundException("Ressource introuvable.");
        }

        @PostMapping("/api/test/validation")
        Object validation(@Valid @RequestBody TestPayload payload) {
            return payload;
        }

        @GetMapping("/api/test/internal")
        Object internalError() {
            throw new RuntimeException("secret database details");
        }

        @GetMapping("/api/test/unauthorized")
        Object unauthorized() {
            throw new BadCredentialsException("secret credentials");
        }

        @GetMapping("/api/test/forbidden")
        Object forbidden() {
            throw new AccessDeniedException("internal authorization details");
        }

        @GetMapping("/api/test/conflict")
        Object conflict() {
            throw new ConflictException("Cette ressource est déjà présente.");
        }
    }

    record TestPayload(@NotBlank(message = "doit être renseigné") String value) {
    }
}
