package com.example.url_shortner.repository;

import com.example.url_shortner.dto.OriginalUrlDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UrlRepository {

    private final JdbcTemplate jdbcTemplate;

    public UrlRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }
    public String createUrlShortCode(String url,String short_code){

        String sql = "INSERT INTO url_shortener (ORIGINAL_URL,SHORT_CODE) VALUES (?,?) ";

        jdbcTemplate.update(sql,url,short_code);
        return short_code;
    }

    public OriginalUrlDto findByCode(String code) {
        System.out.println (" here in repository");
        String sql = "SELECT original_url FROM url_shortener WHERE short_code = ?";
        try {
            return jdbcTemplate.queryForObject(sql,
                    (rs, rowNum) -> new OriginalUrlDto(
                            rs.getString("original_url")
                    ),
                    code
            );
        } catch (Exception e) {
            System.out.println("here " + e.getMessage());
            return null;
        }
    }
}
