beans = {
    // Character Encoding Filter Bean
    characterEncodingFilter(org.springframework.web.filter.CharacterEncodingFilter) {
        encoding = "utf-8"
    }

    // Conversion Service Bean
    conversionService(org.springframework.context.support.ConversionServiceFactoryBean)
}