package io.busata.fourleft.infrastructure.clients.rendercache;

import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@FeignClient(name = "rendercache", url="https://rendercache.busata.io", configuration = RendercacheFeignConfiguration.class)
public interface RendercacheApi {


    @PostMapping(value="/store", consumes="multipart/form-data")
    @Headers("Content-Type: multipart/form-data")
    UUID storeImage(@RequestPart("file") MultipartFile file);

    @DeleteMapping(value="/{id}")
    void delete(@PathVariable UUID id, @RequestHeader(value = "X-APPLICATION-KEY") String applicationKey);
}
