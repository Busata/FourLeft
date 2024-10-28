package io.busata.fourleft.backendeasportswrc.application.discord.results;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CustomPoints {
   // Define a static map to hold the mappings
    private static final Map<Integer, Integer> valueMap;

    // Initialize the map in a static block
    static {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 250);
        map.put(2, 240);
        map.put(3, 230);
        map.put(4, 220);
        map.put(5, 210);
        map.put(6, 200);
        map.put(7, 195);
        map.put(8, 190);
        map.put(9, 185);
        map.put(10, 180);
        map.put(11, 175);
        map.put(12, 170);
        map.put(13, 165);
        map.put(14, 160);
        map.put(15, 155);
        map.put(16, 150);
        map.put(17, 145);
        map.put(18, 140);
        map.put(19, 135);
        map.put(20, 130);
        map.put(21, 125);
        map.put(22, 120);
        map.put(23, 115);
        map.put(24, 110);
        map.put(25, 105);
        map.put(26, 100);
        map.put(27, 98);
        map.put(28, 96);
        map.put(29, 94);
        map.put(30, 92);
        map.put(31, 90);
        map.put(32, 88);
        map.put(33, 86);
        map.put(34, 84);
        map.put(35, 82);
        map.put(36, 80);
        map.put(37, 78);
        map.put(38, 76);
        map.put(39, 74);
        map.put(40, 72);
        map.put(41, 70);
        map.put(42, 68);
        map.put(43, 66);
        map.put(44, 64);
        map.put(45, 62);
        map.put(46, 60);
        map.put(47, 58);
        map.put(48, 56);
        map.put(49, 54);
        map.put(50, 52);
        map.put(51, 50);
        map.put(52, 49);
        map.put(53, 48);
        map.put(54, 47);
        map.put(55, 46);
        map.put(56, 45);
        map.put(57, 44);
        map.put(58, 43);
        map.put(59, 42);
        map.put(60, 41);
        map.put(61, 40);
        map.put(62, 39);
        map.put(63, 38);
        map.put(64, 37);
        map.put(65, 36);
        map.put(66, 35);
        map.put(67, 34);
        map.put(68, 33);
        map.put(69, 32);
        map.put(70, 31);
        map.put(71, 30);
        map.put(72, 29);
        map.put(73, 28);
        map.put(74, 27);
        map.put(75, 26);
        map.put(76, 25);
        map.put(77, 24);
        map.put(78, 23);
        map.put(79, 22);
        map.put(80, 21);
        map.put(81, 20);
        map.put(82, 19);
        map.put(83, 18);
        map.put(84, 17);
        map.put(85, 16);
        map.put(86, 15);
        map.put(87, 14);
        map.put(88, 13);
        map.put(89, 12);
        map.put(90, 11);
        map.put(91, 10);
        map.put(92, 9);
        map.put(93, 8);
        map.put(94, 7);
        map.put(95, 6);
        map.put(96, 5);
        map.put(97, 4);
        map.put(98, 3);
        map.put(99, 2);
        map.put(100, 1);

        // Make the map unmodifiable
        valueMap = Collections.unmodifiableMap(map);
    }

    // Public method to get the value based on index
    public static Integer getValue(int index) {
        return valueMap.getOrDefault(index, 1);
    }
}
