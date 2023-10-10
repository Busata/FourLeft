package io.busata.fourleft.endpoints.gallery;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.busata.fourleft.api.models.gallery.GalleryTagNodeTo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GalleryTagFactory {

    private final ObjectMapper objectMapper;


    @SneakyThrows
    public List<GalleryTagNodeTo> create() {
        return objectMapper.readValue(ResourceUtils.getURL("classpath:tags/tag_graph.json").openStream(), new TypeReference<>() {});
    }
}
