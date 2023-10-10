package io.busata.fourleft.api.models.gallery;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record GalleryTagNodeTo(String id, String label, String showOn, List<GalleryTagOptionTo> nodes) {


    public Optional<String> getLabel(UUID selectedId) {
        return this.nodes.stream().filter(node -> node.id() == selectedId.toString()).findFirst().map(GalleryTagOptionTo::label);
    }
}
