package com.mik.mvc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToDateConverter());
    }

}


class StringToDateConverter implements Converter<String, Date> {

    @Override

    public Date convert(String source) {
        DateTimeFormatter formatter;
        if(source.length() == 10) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        }else {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        }

        LocalDateTime date = LocalDateTime.parse(source, formatter);
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = date.atZone(zoneId);

        return Date.from(zonedDateTime.toInstant());
    }

}
