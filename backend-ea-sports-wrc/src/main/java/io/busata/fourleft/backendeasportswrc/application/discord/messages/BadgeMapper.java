package io.busata.fourleft.backendeasportswrc.application.discord.messages;

public class BadgeMapper {

    public static String createBadge(long rank, int totalEntries) {

        if(totalEntries > 1000) {
            var percentageRank = ((float) rank / (float) totalEntries) * 100f;
            return createPercentageBasedIcon(percentageRank, false);
        } else {
            return createRankBasedIcon(rank, false);
        }

    }

    public static String createPercentageBasedIcon(float percentageRank, boolean isDnf) {
        if (isDnf) {
            return "<:f_respects:894913859532496916>";
        } else if (percentageRank <= 1) {
            return "<:Rank_S:971454722030600214>";
        } else if (percentageRank <= 10) {
            return "<:Rank_A:971454722458411048>";
        } else if (percentageRank <= 20) {
            return "<:Rank_B:971454722429046824>";
        } else if (percentageRank <= 35) {
            return "<:Rank_C:971454722043150387>";
        } else if (percentageRank <= 50) {
            return "<:Rank_D:971454722244497410>";
        } else {
            return "<:blank:894976571406966814>";
        }
    }
    public static String createRankBasedIcon(long rank, boolean isDnf) {
        if (isDnf) {
            return "<:f_respects:894913859532496916>";
        } else if (rank <= 10) {
            return "<:Rank_S:971454722030600214>";
        } else if (rank <= 25) {
            return "<:Rank_A:971454722458411048>";
        } else if (rank <= 50) {
            return "<:Rank_B:971454722429046824>";
        } else if (rank <= 75) {
            return "<:Rank_C:971454722043150387>";
        } else if (rank <= 100) {
            return "<:Rank_D:971454722244497410>";
        } else {
            return "<:blank:894976571406966814>";
        }
    }

}
