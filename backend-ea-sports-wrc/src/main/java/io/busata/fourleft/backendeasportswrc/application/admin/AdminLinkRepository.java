package io.busata.fourleft.backendeasportswrc.application.admin;

import io.busata.fourleft.backendeasportswrc.domain.models.admin.AdminLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface AdminLinkRepository extends JpaRepository<AdminLink, UUID> {
}
