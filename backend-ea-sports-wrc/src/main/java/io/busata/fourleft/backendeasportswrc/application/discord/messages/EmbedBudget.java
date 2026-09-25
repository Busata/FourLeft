package io.busata.fourleft.backendeasportswrc.application.discord.messages;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;

/**
 * Lays rendered entry lines out as embed fields without breaking Discord's limits: a field value holds at
 * most {@value MessageEmbed#VALUE_MAX_LENGTH} characters, an embed at most
 * {@value MessageEmbed#EMBED_MAX_LENGTH_BOT} characters and {@value MessageEmbed#MAX_FIELD_AMOUNT} fields.
 * Lines that no longer fit are dropped and summarised as "…and N more". The caller reserves room for
 * whatever it still adds afterwards (footer fields).
 */
public final class EmbedBudget {

    private EmbedBudget() {
    }

    public static final String MORE_TEMPLATE = "*…and %d more*";

    // "…and 999 more" plus the joining newline, so the notice always fits where it is appended.
    private static final int MORE_NOTICE_LENGTH = 24;

    /**
     * Adds the lines as fields of at most {@code groupSize} lines. {@code firstFieldName} titles the first
     * field (a section header); the rest are untitled. Returns how many lines were laid out.
     */
    public static int addEntryFields(EmbedBuilder embed, List<String> lines, int groupSize, String firstFieldName,
                                     int reservedLength, int reservedFields) {
        int maxLength = MessageEmbed.EMBED_MAX_LENGTH_BOT - reservedLength;
        int maxFields = MessageEmbed.MAX_FIELD_AMOUNT - reservedFields;
        int maxValueLength = MessageEmbed.VALUE_MAX_LENGTH - MORE_NOTICE_LENGTH;

        // Invariant: the open field (name + value, plus a "…and N more" notice) always fits the embed.
        StringBuilder field = new StringBuilder();
        int fieldLines = 0;
        int laidOut = 0;
        String fieldName = firstFieldName == null ? EmbedBuilder.ZERO_WIDTH_SPACE : firstFieldName;

        for (String rawLine : lines) {
            String line = truncate(rawLine, maxValueLength);

            if (fieldLines > 0 && (fieldLines >= groupSize || field.length() + 1 + line.length() > maxValueLength)) {
                embed.addField(fieldName, field.toString(), false);
                fieldName = EmbedBuilder.ZERO_WIDTH_SPACE;
                field.setLength(0);
                fieldLines = 0;
            }

            int valueLength = field.isEmpty() ? line.length() : field.length() + 1 + line.length();
            if (embed.getFields().size() + 1 > maxFields
                    || embed.length() + fieldName.length() + valueLength + MORE_NOTICE_LENGTH > maxLength) {
                break;
            }

            if (!field.isEmpty()) {
                field.append('\n');
            }
            field.append(line);
            fieldLines++;
            laidOut++;
        }

        int dropped = lines.size() - laidOut;
        if (dropped > 0 && fieldLines > 0) {
            field.append('\n').append(MORE_TEMPLATE.formatted(dropped));
        }
        if (fieldLines > 0) {
            embed.addField(fieldName, field.toString(), false);
        }
        return laidOut;
    }

    public static String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength - 1) + "…";
    }
}
