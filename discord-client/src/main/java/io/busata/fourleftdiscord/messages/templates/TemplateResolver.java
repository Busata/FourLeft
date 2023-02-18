package io.busata.fourleftdiscord.messages.templates;


public interface TemplateResolver<T> {

    String resolve(MessageTemplate template, T value);


}
