package com.cardra.server.api

import com.cardra.server.exception.GlobalExceptionHandler
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class UiControllerTest {
    private val mvc: MockMvc =
        MockMvcBuilders.standaloneSetup(UiController())
            .setControllerAdvice(GlobalExceptionHandler())
            .build()

    @Test
    fun `theme api returns brand colors`() {
        mvc.perform(get("/api/v1/ui/theme"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.mainColor").value("#00A676"))
            .andExpect(jsonPath("$.subColor").value("#E0D0C1"))
    }

    @Test
    fun `contracts api returns route list with required size`() {
        mvc.perform(get("/api/v1/ui/contracts"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.routes[0].method").exists())
            .andExpect(jsonPath("$.routes.length()").value(9))
            .andExpect(jsonPath("$.routes[0].path").value("/api/v1/cards/generate"))
            .andExpect(jsonPath("$.routes[1].path").value("/api/v1/cards/{id}"))
            .andExpect(jsonPath("$.routes[2].path").value("/api/v1/research/run"))
    }
}
