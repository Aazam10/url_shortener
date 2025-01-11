package com.example.url_shortner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UrlShortenerApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	record ShortenRequest(String url) {}
	record ShortenResponse(String short_code) {}

	@Test
	void contextLoads() {
	}

	@Test
	public void shortenAndRedirectTest() throws Exception {
		String originalUrl = "https://example.com";
		String shortCode = createShortUrl(originalUrl);

		assertNotNull(shortCode, "Short code should not be null");
		assertFalse(shortCode.isEmpty(), "Short code should not be empty");

		mockMvc.perform(get("/redirect")
						.param("code", shortCode))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", originalUrl));
	}

	@Test
	public void deleteShortenedUrlTest() throws Exception {
		String originalUrl = "https://delete-test.com";
		String shortCode = createShortUrl(originalUrl);

		// Perform delete request
		mockMvc.perform(delete("/url/" + shortCode))
				.andExpect(status().isNoContent());


		// Attempt to redirect with deleted short code
		mockMvc.perform(get("/redirect")
						.param("code", shortCode))
				.andExpect(status().isNotFound());
	}

	@Test
	public void duplicateUrlTest() throws Exception {
		String originalUrl = "https://github.com";
		ShortenRequest request = new ShortenRequest(originalUrl);

		String shortCode1 = extractShortCode(makePostMvcRequest(request));
		String shortCode2 = extractShortCode(makePostMvcRequest(request));

		assertNotEquals(shortCode1, shortCode2, "Short codes for duplicate URLs should match");
	}

	@Test
	public void findOriginalUrlOfNonExistentCodeTest() throws Exception {
		mockMvc.perform(get("/redirect")
						.param("code", "non-existent-code"))
				.andExpect(status().isNotFound());
	}

	private MvcResult makePostMvcRequest(ShortenRequest request) throws Exception {
		return mockMvc.perform(post("/shorten")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andReturn();
	}

	private String extractShortCode(MvcResult result) throws Exception {
		String responseJson = result.getResponse().getContentAsString();
		ShortenResponse response = objectMapper.readValue(responseJson, ShortenResponse.class);
		return response.short_code();
	}

	private String createShortUrl(String originalUrl) throws Exception {
		ShortenRequest request = new ShortenRequest(originalUrl);
		MvcResult result = makePostMvcRequest(request);
		return extractShortCode(result);
	}


}