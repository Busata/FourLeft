package io.busata.fourleftdiscord.autoposting.club_results;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageEditSpec;
import io.busata.fourleft.common.MessageType;
import io.busata.fourleft.api.models.DriverEntryTo;
import io.busata.fourleft.api.models.views.ViewResultTo;
import io.busata.fourleftdiscord.autoposting.club_results.domain.AutoPostEntry;
import io.busata.fourleftdiscord.autoposting.club_results.domain.AutoPostEntryRepository;
import io.busata.fourleftdiscord.autoposting.club_results.model.AutoPostableView;
import io.busata.fourleftdiscord.autoposting.club_results.model.AutoPostableFactory;
import io.busata.fourleftdiscord.channel_configuration.DiscordChannelConfigurationService;
import io.busata.fourleftdiscord.client.FourLeftClient;
import io.busata.fourleftdiscord.messages.DiscordMessageGateway;
import io.busata.fourleftdiscord.messages.MessageTemplateFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Slf4j
@Component
public class AutopostClubResultsMessageService {
    protected final FourLeftClient api;
    protected final DiscordMessageGateway discordMessageGateway;
    protected final AutoPostEntryRepository autoPostEntryRepository;

    private final AutoPostableFactory autoPostableFactory;
    protected final MessageTemplateFactory messageTemplateFactory;
    protected final DiscordChannelConfigurationService discordChannelConfigurationService;


    @Transactional
    public void postNewEntries(Snowflake channelId, ViewResultTo clubResult, List<String> unpostedEntries) {
        tryReusingLastMessage(channelId, clubResult).ifPresentOrElse(message -> {
            try {
                updateLastMessage(clubResult, unpostedEntries, message);
            } catch (MessagePostingException ex) {
                log.info("Creating new message instead", ex);
                createNewMessage(channelId, clubResult, unpostedEntries);
            }
        }, () -> {
            createNewMessage(channelId, clubResult, unpostedEntries);
        });
    }
    void createNewMessage(Snowflake channelId, ViewResultTo clubResult, List<String> newEntries) {
        final var autoPost = autoPostableFactory.create(clubResult, newEntries);
        final var autopostMessage = createAutopostMessage(autoPost);
        var messageId = -1L;

        try {
            messageId = discordMessageGateway.postMessage(
                    channelId,
                    autopostMessage,
                    MessageType.AUTO_POST
            ).asLong();
        } catch (Exception ex) {
            log.error("Something went wrong saving the message to server", ex);
        } finally {
            saveNewEntries(channelId, messageId, autoPost);
        }
    }
    void updateLastMessage(ViewResultTo clubResult, List<String> newEntries, Message message) {
        final List<String> entriesToPost = mergePostedWithUnposted(clubResult, newEntries, message);
        final var autoPost = autoPostableFactory.create(clubResult, entriesToPost);

        final var autopostMessage = createAutopostMessage(autoPost);

        try {
            message.edit(MessageEditSpec.builder().contentOrNull(autopostMessage).build()).block();
        } catch (Exception ex) {
            throw new MessagePostingException("Failed to edit", ex);
        }
        saveNewEntries(message.getChannelId(), message.getId().asLong(), autoPost);
    }

    private List<String> mergePostedWithUnposted(ViewResultTo clubResult, List<String> newEntries, Message message) {
        final var postedEntries = autoPostEntryRepository.findByMessageId(message.getId().asLong());

        final var updatedEntries = postedEntries.stream()
                .flatMap(autoPostEntry ->
                        clubResult.getResultEntries().stream()
                                .filter(clubEntry -> clubEntry.racenet().equals(autoPostEntry.getName()))
                                .findFirst()
                                .stream()
                ).map(DriverEntryTo::racenet).toList();

        return Stream.concat(updatedEntries.stream(), newEntries.stream()).collect(Collectors.toList());
    }

    protected String createAutopostMessage(AutoPostableView autoPostableView) {
        return messageTemplateFactory.createAutopostMessage(autoPostableView);
    }

    public void saveNewEntries(Snowflake channelId, Long messageId, AutoPostableView multiView) {
        multiView.getMultiListResults().stream().flatMap(multiList -> {
            return multiList.results().stream().map(entry -> {
                    final var autoPostEntry = new AutoPostEntry();
                createAutopostEntry(autoPostEntry, entry, messageId, channelId.asString() + "#" + multiView.getEventKey());

                return autoPostEntry;
            });
        }).forEach(autoPostEntryRepository::save);
    }


    public static void createAutopostEntry(AutoPostEntry autoPostEntry, DriverEntryTo entry, Long messageId, String eventKey) {
        autoPostEntry.setName(entry.racenet());
        autoPostEntry.setTotalTime(entry.activityTotalTime());
        autoPostEntry.setNationality(entry.nationality());
        autoPostEntry.setVehicle(entry.vehicles().get(0).vehicleName());
        autoPostEntry.setMessageId(messageId);

        autoPostEntry.setEventKey(eventKey);
    }
    Optional<Message> tryReusingLastMessage(Snowflake channelId, ViewResultTo viewResultTo) {
        return discordMessageGateway.getLastMessage(channelId).filter(lastMessage -> canBeReused(lastMessage, viewResultTo.getViewEventKey()));
    }

    boolean canBeReused(Message lastMessage, String viewEventKey) {
        boolean isAutopost = api.hasMessage(lastMessage.getId().asLong(), MessageType.AUTO_POST);
        if(!isAutopost) {
            return false;
        }

        return autoPostEntryRepository.findByEventKey(lastMessage.getChannelId().asString() + "#" + viewEventKey).stream().anyMatch(entry -> entry.getMessageId().equals(lastMessage.getId().asLong()));

    }
}
