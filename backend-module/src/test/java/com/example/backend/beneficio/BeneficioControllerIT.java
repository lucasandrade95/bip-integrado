package com.example.backend.beneficio;

import com.example.backend.beneficio.dto.TransferRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BeneficioControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void listsSeededBeneficios() throws Exception {
        mockMvc.perform(get("/api/v1/beneficios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    void transferUpdatesBalances() throws Exception {
        TransferRequest req = new TransferRequest();
        req.setFromId(1L);
        req.setToId(2L);
        req.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/v1/beneficios/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/beneficios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(900.00));
        mockMvc.perform(get("/api/v1/beneficios/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(600.00));
    }

    @Test
    void transferRejectsInsufficientBalance() throws Exception {
        TransferRequest req = new TransferRequest();
        req.setFromId(1L);
        req.setToId(2L);
        req.setAmount(new BigDecimal("9999999.00"));

        mockMvc.perform(post("/api/v1/beneficios/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void transferByNameUpdatesBalances() throws Exception {
        TransferRequest req = new TransferRequest();
        req.setFromName("Beneficio A");
        req.setToName("Beneficio B");
        req.setAmount(new BigDecimal("50.00"));

        mockMvc.perform(post("/api/v1/beneficios/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/beneficios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(950.00));
        mockMvc.perform(get("/api/v1/beneficios/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(550.00));
    }

    @Test
    void transferByUnknownNameFails() throws Exception {
        TransferRequest req = new TransferRequest();
        req.setFromName("Nao Existe");
        req.setToId(2L);
        req.setAmount(new BigDecimal("10.00"));

        mockMvc.perform(post("/api/v1/beneficios/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void transferRejectsSameAccount() throws Exception {
        TransferRequest req = new TransferRequest();
        req.setFromId(1L);
        req.setToId(1L);
        req.setAmount(new BigDecimal("10.00"));

        mockMvc.perform(post("/api/v1/beneficios/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void transferRejectsNonPositiveAmount() throws Exception {
        TransferRequest req = new TransferRequest();
        req.setFromId(1L);
        req.setToId(2L);
        req.setAmount(new BigDecimal("0.00"));

        mockMvc.perform(post("/api/v1/beneficios/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
