package io.busata.fourleftdiscord.mediaarchive;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;

/**
 * Multipart encoder for {@link ImageStoreApi}. Deliberately NOT annotated with
 * {@code @Configuration}: it is referenced via {@code @FeignClient(configuration = ...)} and must
 * stay out of component scan, or this encoder would replace the default one for every Feign client.
 */
@RequiredArgsConstructor
public class ImageStoreFeignConfiguration {
    private final ObjectFactory<HttpMessageConverters> messageConverters;

    @Bean
    public Encoder feignEncoder() {
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }
}
