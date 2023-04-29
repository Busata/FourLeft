package io.busata.fourleft.application.dirtrally2;

import io.busata.fourleft.domain.dirtrally2.clubs.repository.CommunityChallengeSummaryProjection;
import io.busata.fourleft.domain.dirtrally2.clubs.repository.LeaderboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProgressImageService {

    private final LeaderboardRepository leaderboardRepository;
    public BufferedImage createImage(String query, boolean includeName, Optional<LocalDate> before, Optional<LocalDate> after) {
        List<CommunityChallengeSummaryProjection> communityChallengeSummary = leaderboardRepository.findCommunityChallengeSummary(query)
                .stream().filter(summary -> {
                    LocalDate challengeDate = summary.getChallengeDate();
                    boolean isBefore = before.map(challengeDate::isBefore).orElse(true);
                    boolean isAfter = after.map(challengeDate::isAfter).orElse(true);
                    return isBefore && isAfter;
                }).toList();

        int totalSize = communityChallengeSummary.size();

        int imageHeight = 50;
        BufferedImage imageOut = new BufferedImage(totalSize*2, imageHeight, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < communityChallengeSummary.size()*2; x++) {
            CommunityChallengeSummaryProjection summaryEntry = communityChallengeSummary.get(x/2);

            final var percentageRank = ((float) summaryEntry.getRank() / (float) summaryEntry.getTotal()) * 100f;
            final var color = getPixelColour(percentageRank, summaryEntry.getIsDnf());

            for(int y = 0; y < imageHeight; y++) {
                imageOut.setRGB(x, y, color);
            }
        }

        if(includeName) {
            final var graphics = imageOut.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.setFont(new Font("Arial", Font.BOLD, 10));
            int i = graphics.getFontMetrics().stringWidth(query) + 5;
            graphics.drawString(query, totalSize * 2 - i, 47);
            graphics.dispose();
        }

        return imageOut;
    }

    private static int getPixelColour(float percentageRank, boolean isDnf) {
        if (isDnf) {
            return Color.decode("#212529").getRGB();
        } else if (percentageRank <= 1) {
            return Color.decode("#ffc107").getRGB();
        } else if (percentageRank <= 10) {
            return Color.decode("#dc3545").getRGB();
        } else if (percentageRank <= 35) {
            return Color.decode("#198754").getRGB();
        } else if (percentageRank <= 75) {
            return Color.decode("#0d6efd").getRGB();
        } else {
            return Color.decode("#6c757d").getRGB();
        }
    }

}
