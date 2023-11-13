package io.busata.fourleft.backendeasportswrc.application.discord.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageTemplate {
    final Pattern recurringElementPattern = Pattern.compile("\\|([a-zA-Z]+):(.*)\\|");

    private String template;
    Map<String, String> recurringTemplates = new HashMap<>();


    public MessageTemplate(String template) {
        Matcher matcher = recurringElementPattern.matcher(template);

        StringBuilder result = new StringBuilder();

        while(matcher.find()) {

            matcher.appendReplacement(result, "\\${%s}".formatted(matcher.group(1)));
            recurringTemplates.put(matcher.group(1), matcher.group(2));
        }

        this.template = matcher.appendTail(result).toString();
    }

    public String getNormalizedTemplate() {
        return this.template;
    }

    public String getReccuringTemplate(String key) {
        return this.recurringTemplates.get(key);
    }
}
