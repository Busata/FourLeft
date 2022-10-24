package io.busata.fourleftdiscord.messages.templates;

import org.apache.commons.lang3.SerializationUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MessageTemplate {

    private final String baseTemplate;
    private final Map<String, String> recurringTemplates = new HashMap<>();

    public MessageTemplate(String baseTemplate) {
        this.baseTemplate = baseTemplate;
    }

    public static MessageTemplate messageTemplate(String baseTemplate) {
        return new MessageTemplate(baseTemplate);
    }


    public MessageTemplate withRecurringTemplate(String variable, String template) {
        this.recurringTemplates.put(variable, template);
        return this;
    }

    public String getRecurringTemplate(String key) {
        return this.recurringTemplates.getOrDefault(key, "");
    }

    public String getTemplate() {
        return this.baseTemplate;
    }


    public MessageTemplate copy() {
        MessageTemplate messageTemplate = new MessageTemplate(this.baseTemplate);
        messageTemplate.recurringTemplates.putAll(this.recurringTemplates);
        return messageTemplate;
    }


}
