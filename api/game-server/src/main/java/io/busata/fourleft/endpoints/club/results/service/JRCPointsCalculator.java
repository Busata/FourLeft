package io.busata.fourleft.endpoints.club.results.service;

import org.springframework.stereotype.Component;

@Component
public class    JRCPointsCalculator {
    public int calculateEventPoints(long rank) {
        return switch((int) rank) {
            case 1 -> 50;
            case 2 -> 44;
            case 3 -> 41;
            case 4 -> 38;
            case 5 -> 35;
            case 6 -> 32;
            case 7 -> 30;
            case 8 -> 28;
            case 9 -> 26;
            case 10 -> 24;
            case 11 -> 22;
            case 12 -> 20;
            case 13 -> 18;
            case 14 -> 17;
            case 15 -> 16;
            case 16 -> 15;
            case 17 -> 14;
            case 18 -> 13;
            case 19 -> 12;
            case 20 -> 11;
            case 21 -> 10;
            case 22 -> 9;
            case 23 -> 8;
            case 24 -> 7;
            case 25 -> 6;
            case 26 -> 5;
            case 27 -> 4;
            case 28 -> 3;
            case 29 -> 2;
            default -> 1;
        };
    }
    public int calculatePowerStagePoints(long rank) {
        return switch ((int) rank) {
            case 1 -> 5;
            case 2 -> 4;
            case 3 -> 3;
            case 4 -> 2;
            case 5 -> 1;
            default -> 0;
        };
    }

}
