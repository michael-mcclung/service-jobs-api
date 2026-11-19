package com.macmcclung.jobs.job;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class JobApiTest {
    @Autowired WebApplicationContext context;
    @Autowired JobRepository repo;
    MockMvc mvc;
    final String tomorrow = LocalDateTime.now().plusDays(1).withNano(0).toString();

    @BeforeEach
    void setUp() {
        repo.deleteAll();
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private String create(String customer, String tech) throws Exception {
        String body = """
            {"customerName":"%s","description":"AC tune-up","technician":"%s",
             "scheduledFor":"%s","quotedPrice":129.00}""".formatted(customer, tech, tomorrow);
        MvcResult r = mvc.perform(post("/api/jobs").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/jobs/")))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andReturn();
        return r.getResponse().getHeader("Location");
    }

    @Test
    void createReadAndList() throws Exception {
        String loc = create("Acme HVAC", "Sam");
        create("Bluebonnet Dental", "Lee");
        mvc.perform(get(loc)).andExpect(status().isOk()).andExpect(jsonPath("$.customerName").value("Acme HVAC"));
        mvc.perform(get("/api/jobs")).andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/api/jobs").param("technician", "sam")).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void statusLifecycleAndConflict() throws Exception {
        String loc = create("Acme HVAC", "Sam");
        mvc.perform(patch(loc + "/status").contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Not allowed in the job's current status"));
        mvc.perform(patch(loc + "/status").contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        mvc.perform(get("/api/jobs").param("status", "IN_PROGRESS")).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(delete(loc)).andExpect(status().isConflict());
    }

    @Test
    void validationErrorsListTheFields() throws Exception {
        mvc.perform(post("/api/jobs").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"\",\"description\":\"x\",\"scheduledFor\":\"2020-01-01T09:00:00\",\"quotedPrice\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.customerName").exists())
                .andExpect(jsonPath("$.errors.scheduledFor").exists())
                .andExpect(jsonPath("$.errors.quotedPrice").exists());
    }

    @Test
    void malformedAndMissing() throws Exception {
        mvc.perform(post("/api/jobs").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/jobs/9999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No job with id 9999"));
        mvc.perform(get("/api/jobs").param("status", "BOGUS")).andExpect(status().isBadRequest());
    }

    @Test
    void rescheduleAndDelete() throws Exception {
        String loc = create("Trinity Auto", "Sam");
        String later = LocalDateTime.now().plusDays(3).withNano(0).toString();
        mvc.perform(patch(loc + "/schedule").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scheduledFor\":\"%s\",\"technician\":\"Lee\"}".formatted(later)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.technician").value("Lee"));
        mvc.perform(delete(loc)).andExpect(status().isNoContent());
        mvc.perform(get(loc)).andExpect(status().isNotFound());
    }
}
