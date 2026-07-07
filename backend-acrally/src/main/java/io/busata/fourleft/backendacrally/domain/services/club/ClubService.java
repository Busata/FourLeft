package io.busata.fourleft.backendacrally.domain.services.club;

import io.busata.fourleft.backendacrally.domain.models.club.Club;
import io.busata.fourleft.backendacrally.domain.models.club.ClubMembership;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClubService {

    private static final int MAX_NAME_LENGTH = 120;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;
    private static final int MAX_SOCIAL_LINK_LENGTH = 300;

    private final ClubRepository repository;
    private final ClubMembershipRepository membershipRepository;

    @Transactional
    public Club create(UUID createdBy, String rawName, String rawDescription, String rawSocialLink) {
        final String name = rawName == null ? "" : rawName.trim();
        final String description = rawDescription == null ? "" : rawDescription.trim();
        final String socialLink = rawSocialLink == null ? "" : rawSocialLink.trim();

        if (name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A club name is required.");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Club name must be at most %d characters.".formatted(MAX_NAME_LENGTH));
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Description must be at most %d characters.".formatted(MAX_DESCRIPTION_LENGTH));
        }
        if (!socialLink.isBlank()) {
            if (socialLink.length() > MAX_SOCIAL_LINK_LENGTH) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Social link must be at most %d characters.".formatted(MAX_SOCIAL_LINK_LENGTH));
            }
            if (!socialLink.startsWith("http://") && !socialLink.startsWith("https://")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Social link must be a http(s) URL.");
            }
        }

        if (repository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A club with this name already exists.");
        }

        Club club = repository.save(new Club(
                name,
                description.isBlank() ? null : description,
                socialLink.isBlank() ? null : socialLink,
                createdBy));
        // The creator is a member of their own club from the start.
        membershipRepository.save(new ClubMembership(club.getId(), createdBy));
        return club;
    }
}
