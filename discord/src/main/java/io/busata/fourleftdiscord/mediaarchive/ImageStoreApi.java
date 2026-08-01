package io.busata.fourleftdiscord.mediaarchive;

import feign.form.FormData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;

import java.util.UUID;

@FeignClient(name = "imagestore", url = "${imagestore.url}", configuration = ImageStoreFeignConfiguration.class)
public interface ImageStoreApi {

    @PostMapping(value = "/store", consumes = "multipart/form-data")
    UUID storeImage(@RequestPart("file") FormData file);
}
