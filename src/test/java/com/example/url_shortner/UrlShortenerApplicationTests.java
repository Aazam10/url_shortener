package com.example.url_shortner;

import com.example.url_shortner.dto.BulkUrlRequestDto;
import com.example.url_shortner.dto.CreateUrlRequestDto;
import com.example.url_shortner.dto.UrlDetailsResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

	private static final String enterpriseKey = "hvdwtydtwqcdwqdygwqcr";

	private static final String hobbyKey = "hfvweytcdqwin";
	@Test
	void contextLoads() {
	}

	@Test
	public void shortenAndRedirectTest() throws Exception {
		String originalUrl = "https://example.com";

        CreateUrlRequestDto urlRequestDto = new CreateUrlRequestDto();
        urlRequestDto.setUrl(originalUrl);

		String shortCode = createShortUrl(urlRequestDto, enterpriseKey);

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
		CreateUrlRequestDto urlRequestDto = new CreateUrlRequestDto();
		urlRequestDto.setUrl(originalUrl);

		String shortCode = createShortUrl(urlRequestDto, enterpriseKey);

		// Perform delete request
		mockMvc.perform(delete("/url/" + shortCode).header("x-api-key", enterpriseKey))
				.andExpect(status().isNoContent());


		// Attempt to redirect with deleted short code
		mockMvc.perform(get("/redirect")
						.param("code", shortCode))
				.andExpect(status().isNotFound());
	}


	@Test
	public void deleteShortCodeOfOtherUser() throws Exception{
		String originalUrl = "https://delete-test.com";
		CreateUrlRequestDto urlRequestDto = new CreateUrlRequestDto();

		urlRequestDto.setUrl(originalUrl);

		String shortCode = createShortUrl(urlRequestDto, enterpriseKey);

		System.out.println(" short code " + shortCode);

		mockMvc.perform(delete("/url/" + shortCode).header("x-api-key",hobbyKey))
				.andExpect(status().isUnauthorized());

	}

	@Test
	public void blankOriginalUrlTest() throws Exception{
		String originalUrl = "";

		CreateUrlRequestDto urlRequestDto = new CreateUrlRequestDto();
		urlRequestDto.setUrl(originalUrl);

		assertEquals(HttpStatus.BAD_REQUEST.value(),makePostMvcRequest(urlRequestDto, enterpriseKey).getResponse().getStatus());
	}

	@Test
	public void expiredUrlTest() throws Exception{
		String originalUrl = "https://leetcode.com";
		CreateUrlRequestDto urlRequestDto = new CreateUrlRequestDto();
		urlRequestDto.setUrl(originalUrl);
		urlRequestDto.setExpiryDate(LocalDateTime.now());
		String shortCode = createShortUrl(urlRequestDto, enterpriseKey);

		mockMvc.perform(get("/redirect")
				.header("x-api-key", enterpriseKey)
				.param("code",shortCode)
				).andExpect(status().isNotFound());
	}

	@Test
	public void useExistingCustomShortCode() throws Exception{
		String originalUrl = "https://leetcode.com";
		CreateUrlRequestDto urlRequestDto = new CreateUrlRequestDto();
		urlRequestDto.setUrl(originalUrl);
		urlRequestDto.setExpiryDate(LocalDateTime.now());
		String shortCode = createShortUrl(urlRequestDto, enterpriseKey);

		urlRequestDto.setShortCode(shortCode);

		assertEquals(HttpStatus.BAD_REQUEST.value(),
				makePostMvcRequest(urlRequestDto, enterpriseKey).getResponse().getStatus());

	}

	@Test
	public void createBulkUrlsTest() throws  Exception{
        String originalUrl1 = "https://youtube.com";
		String originalUrl2 = "https://google.com";
		CreateUrlRequestDto createUrlRequestDto1 = new CreateUrlRequestDto();
		createUrlRequestDto1.setUrl(originalUrl1);
		CreateUrlRequestDto createUrlRequestDto2 = new CreateUrlRequestDto();
		createUrlRequestDto2.setUrl(originalUrl2);
        List<CreateUrlRequestDto> bulkUrls = new ArrayList<>(List.of(createUrlRequestDto1, createUrlRequestDto2));
		BulkUrlRequestDto bulkUrlRequestDto = new BulkUrlRequestDto();
		bulkUrlRequestDto.setUrls(bulkUrls);
		mockMvc.perform(post("/shorten/bulk")
				             .header("x-api-key", enterpriseKey)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(bulkUrlRequestDto)))
				.andExpect(status().isCreated());

	}

	@Test
	public void createBulkUrlsUnauthorizedTest() throws  Exception{
		String originalUrl1 = "https://youtube.com";
		String originalUrl2 = "https://google.com";
		CreateUrlRequestDto createUrlRequestDto1 = new CreateUrlRequestDto();
		createUrlRequestDto1.setUrl(originalUrl1);
		CreateUrlRequestDto createUrlRequestDto2 = new CreateUrlRequestDto();
		createUrlRequestDto2.setUrl(originalUrl2);
		List<CreateUrlRequestDto> bulkUrls = new ArrayList<>(List.of(createUrlRequestDto1, createUrlRequestDto2));
		BulkUrlRequestDto bulkUrlRequestDto = new BulkUrlRequestDto();
		bulkUrlRequestDto.setUrls(bulkUrls);
		mockMvc.perform(post("/shorten/bulk")
						.header("x-api-key", hobbyKey)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(bulkUrlRequestDto)))
				.andExpect(status().isForbidden());

	}

	@Test
	public void createUrlWithCustomShortCode() throws Exception{
		String originalUrl = "https://github.com";
		String shortCode = LocalDateTime.now().toString();
		CreateUrlRequestDto urlRequestDto = new CreateUrlRequestDto();
		urlRequestDto.setUrl(originalUrl);
		urlRequestDto.setShortCode(shortCode);
		String shortCodeReceived = createShortUrl(urlRequestDto, enterpriseKey);

		assertEquals(shortCode,shortCodeReceived);

	}

	@Test
	public void duplicateUrlTest() throws Exception {
        String originalUrl = "https://example.com";

        CreateUrlRequestDto urlRequestDto = new CreateUrlRequestDto();
		urlRequestDto.setUrl(originalUrl);
		String shortCode1 = createShortUrl(urlRequestDto, enterpriseKey);
		String shortCode2 = createShortUrl(urlRequestDto, enterpriseKey);

		assertNotEquals(shortCode1, shortCode2, "Short codes for duplicate URLs should match");
	}

	@Test
	public void findOriginalUrlOfNonExistentCodeTest() throws Exception {
		mockMvc.perform(get("/redirect")
						.param("code", "non-existent-code"))
				.andExpect(status().isNotFound());
	}

	private MvcResult makePostMvcRequest(CreateUrlRequestDto urlRequestDto,String apiKey) throws Exception {
		return mockMvc.perform(post("/shorten")
						.contentType(MediaType.APPLICATION_JSON)
                        .header("x-api-key",apiKey)
						.content(objectMapper.writeValueAsString(urlRequestDto)))
				.andReturn();
	}

	private String extractShortCode(MvcResult result) throws Exception {
		String responseJson = result.getResponse().getContentAsString();
		UrlDetailsResponseDto response = objectMapper.readValue(responseJson, UrlDetailsResponseDto.class);
		return response.getShortCode();
	}

	private String createShortUrl(CreateUrlRequestDto urlRequestDto,String apiKey) throws Exception {

		MvcResult result = makePostMvcRequest(urlRequestDto,apiKey);
		return extractShortCode(result);
	}


}