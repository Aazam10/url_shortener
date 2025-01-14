package com.example.url_shortner.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Converter
public class TextBasedDateTimeConverter implements AttributeConverter<LocalDateTime,String> {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Override
    public String convertToDatabaseColumn(LocalDateTime localDateTime) {
            return localDateTime != null  ? localDateTime.format(dateFormatter) : null;
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String text) {
        return text != null ? LocalDateTime.parse(text,dateFormatter) : null;
    }
}
