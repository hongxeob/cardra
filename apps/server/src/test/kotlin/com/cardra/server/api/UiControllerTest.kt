package com.cardra.server.api

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class UiControllerTest {
    private val mvc: MockMvc = MockMvcBuilders.standaloneSetup(UiController()).build()

    @Test
    fun `ui theme returns cardra colors`() {
        mvc.perform(get("/api/v1/ui/theme"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.mainColor").value("#00A676"))
            .andExpect(jsonPath("$.subColor").value("#E0D0C1"))
    }

    @Test
    fun `ui contracts returns route list`() {
        mvc.perform(get("/api/v1/ui/contracts"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.theme.mainColor").value("#00A676"))
            .andExpect(jsonPath("$.routes").isArray)
    }
}
