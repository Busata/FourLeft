package io.busata.fourleftdiscord.messages;

public class BadgeMapper {

    public static String createPercentageBasedIcon(float percentageRank, boolean isDnf) {
        if (isDnf) {
            return "<:f_respects:894913859532496916>";
        } else if (percentageRank <= 1) {
            return "<:Rank_S:971454722030600214>";
        } else if (percentageRank <= 10) {
            return "<:Rank_A:971454722458411048>";
        } else if (percentageRank <= 35) {
            return "<:Rank_B:971454722429046824>";
        } else if (percentageRank <= 75) {
            return "<:Rank_C:971454722043150387>";
        } else {
            return "<:Rank_D:971454722244497410>";
        }
    }
    public static String createRankBasedIcon(long rank, boolean isDnf) {
        if (isDnf) {
            return "<:f_respects:894913859532496916>";
        } else if (rank <= 5) {
            return "<:Rank_S:971454722030600214>";
        } else if (rank <= 12) {
            return "<:Rank_A:971454722458411048>";
        } else if (rank <= 25) {
            return "<:Rank_B:971454722429046824>";
        } else if (rank <= 40) {
            return "<:Rank_C:971454722043150387>";
        } else {
            return "<:Rank_D:971454722244497410>";
        }
    }
}
