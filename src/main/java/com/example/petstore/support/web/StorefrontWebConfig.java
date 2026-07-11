package com.example.petstore.support.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * Storefront-wide i18n wiring. The legacy Pet Store shipped catalog data in three locales
 * ({@code en_US}, {@code ja_JP}, {@code zh_CN}); we surface that with a session-scoped locale,
 * default {@code en_US}, switchable via a {@code ?lang=} query parameter (e.g. {@code ?lang=ja_JP}).
 *
 * <p>The default is fixed (not Accept-Language negotiated) so browsing is deterministic and
 * always lands on seeded data; users opt into other locales explicitly.
 */
@Configuration
public class StorefrontWebConfig implements WebMvcConfigurer {

    /** Query parameter that switches locale, matching the seeded {@code lang_COUNTRY} form. */
    static final String LOCALE_PARAM = "lang";

    @Bean
    LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.of("en", "US"));
        return resolver;
    }

    @Bean
    LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName(LOCALE_PARAM);
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
