package io.busata.fourleft.backendeasportswrc.application.discord.messages;

import java.util.regex.Pattern;

public interface TemplateResolver<T, K> {

    K render(String template, T value);
}
