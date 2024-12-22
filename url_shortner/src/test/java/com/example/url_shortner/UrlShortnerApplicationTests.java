package com.example.url_shortner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest()
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UrlShortnerApplicationTests {


	@Autowired
	private MockMvc mockMvc;

	@Autowired

	private ObjectMapper objectMapper;


	@Autowired
	private JdbcTemplate jdbcTemplate;



	@Test
	void contextLoads() {
	}

	record ShortenRequest(String url){};
	record ShortenResponse(String short_code){};


	@Test
	public void shortenAndRedirectTest() throws Exception {
		String originalUrl = "https://leetcode.com";

		ShortenRequest shortenRequest = new ShortenRequest(originalUrl);

		MvcResult shortenResult = mockMvc.perform(post("/shorten")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(shortenRequest)))
				.andExpect(status().isCreated())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn();

		String responseJson = shortenResult.getResponse().getContentAsString();
		ShortenResponse shortenResponse = objectMapper.readValue(responseJson,ShortenResponse.class);

		String shortCode = shortenResponse.short_code();

		assertNotNull(shortCode, "Short code should not be null");
		assertFalse(shortCode.isEmpty(), "Short code should not be empty");

		mockMvc.perform(get("/redirect")
						.param("code", shortCode))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", originalUrl));

	}
}
