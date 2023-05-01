package io.busata.fourleft.endpoints.dirtrally2;


import io.busata.fourleft.api.RoutesTo;
import io.busata.fourleft.api.models.overview.UserResultSummaryTo;
import io.busata.fourleft.application.dirtrally2.UserOverviewService;
import io.busata.fourleft.application.dirtrally2.UserProgressImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserProgressEndpoint {

    private final UserProgressImageService userProgressImageService;
    private final UserOverviewService userOverviewService;


    @GetMapping(value = RoutesTo.USER_COMMUNITY_PROGRESSION, produces = "image/png")
    public BufferedImage calculateUser(@RequestParam String query,
                                       @RequestParam(required = false, defaultValue = "false") boolean includeName,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> before,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  Optional<LocalDate> after) {
        return userProgressImageService.createImage(query, includeName, before, after);
    }

    @GetMapping(RoutesTo.USER_OVERVIEW)
    public UserResultSummaryTo getUserOverview(@RequestParam String query) {
        return  userOverviewService.getUserOverview(query);
    }


}
