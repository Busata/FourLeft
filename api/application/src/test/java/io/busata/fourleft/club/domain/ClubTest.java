package io.busata.fourleft.club.domain;

import io.busata.fourleft.domain.clubs.models.Championship;
import io.busata.fourleft.domain.clubs.models.Club;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.clubs.models.Stage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClubTest {


    @Test
    public void testPreviousEventWithSingleFinishedChampionships() {
        Club club = new Club();
        Championship c1 = new Championship();
        c1.setActive(false);

        c1.setEvents(List.of(createSingleStageEvent("1", "Finished")));

        club.updateChampionships(List.of(c1));

        assertThat(club.getPreviousEvent().orElseThrow().getName()).isEqualTo("1");
    }

    @Test
    public void testPreviousEventWithTwoChampionships() {
        Club club = new Club();
        Championship c1 = new Championship();
                c1.setReferenceId("1");
        c1.setActive(false);

        c1.setEvents(List.of(createSingleStageEvent("1", "Finished")));

        Championship c2 = new Championship();
        c2.setReferenceId("2");
        c2.setActive(true);
        c2.setEvents(List.of(createSingleStageEvent("2", "Active")));

        club.updateChampionships(List.of(c1, c2));

        assertThat(club.getPreviousEvent().orElseThrow().getName()).isEqualTo("1");
    }

    @Test
    public void testCurrentEventWithSingleFinishedChampionships() {
        Club club = new Club();
        Championship c1 = new Championship();
        c1.setActive(false);

        c1.setEvents(List.of(createSingleStageEvent("1", "Finished")));

        club.updateChampionships(List.of(c1));

        assertThat(club.getCurrentEvent()).isNotPresent();
    }

    @Test
    public void testPreviousEventWithTwoChampionshipsMultipleEvents() {
        Club club = new Club();
        Championship c1 = new Championship();
        c1.setActive(false);

        c1.setEvents(List.of(createSingleStageEvent("1", "Finished")));

        Championship c2 = new Championship();
        c2.setActive(true);
        c2.setEvents(List.of(
                createSingleStageEvent("2", "Finished"),
                createSingleStageEvent("3", "Active")
        ));

        club.updateChampionships(List.of(c1, c2));

        assertThat(club.getPreviousEvent().orElseThrow().getName()).isEqualTo("2");
    }

    @Test
    public void testPreviousEventWithSingleChampionship() {
        Club club = new Club();
        Championship c1 = new Championship();
        c1.setActive(true);
        c1.setEvents(List.of(
                createSingleStageEvent("1", "Finished"),
                createSingleStageEvent("2", "Active")
        ));

        club.updateChampionships(List.of(c1));

        assertThat(club.getPreviousEvent().orElseThrow().getName()).isEqualTo("1");
    }
    @Test
    public void testPreviousEventWithFutureChampionship() {
        Club club = new Club();
        Championship c1 = new Championship();
        c1.setActive(false);
        c1.setEvents(List.of(
                createSingleStageEvent("2", "Finished"),
                createSingleStageEvent("3", "Active")
        ));

        Championship c2 = new Championship();
        c2.setActive(true);

        c2.setEvents(List.of(createSingleStageEvent("1", "Finished")));

        Championship c3 = new Championship();
        c3.setActive(false);
        c3.setEvents(List.of(
                createSingleStageEvent("2", "Finished"),
                createSingleStageEvent("3", "Active")
        ));

        club.updateChampionships(List.of(c1, c2, c3));

        assertThat(club.getPreviousEvent().orElseThrow().getName()).isEqualTo("1");
    }

    @Test
    public void testCurrentEventWithTwoChampionships() {
        Club club = new Club();
        Championship c1 = new Championship();
        c1.setActive(false);

        c1.setEvents(List.of(createSingleStageEvent("1", "Finished")));

        Championship c2 = new Championship();
        c2.setActive(true);
        c2.setEvents(List.of(createSingleStageEvent("2", "Active")));

        club.updateChampionships(List.of(c1, c2));

        assertThat(club.getCurrentEvent().get().getName()).isEqualTo("2");
    }

    @Test
    public void testCurrentEventWithTwoChampionshipsMultipleEvents() {
        Club club = new Club();
        Championship c1 = new Championship();
        c1.setActive(false);

        c1.setEvents(List.of(createSingleStageEvent("1", "Finished")));

        Championship c2 = new Championship();
        c2.setActive(true);
        c2.setEvents(List.of(
                createSingleStageEvent("2", "Finished"),
                createSingleStageEvent("3", "Active")
        ));

        club.updateChampionships(List.of(c1, c2));

        assertThat(club.getCurrentEvent().get().getName()).isEqualTo("3");
    }

    @Test
    public void testCurrentEventWithSingleChampionship() {
        Club club = new Club();
        Championship c1 = new Championship();
        c1.setActive(true);
        c1.setEvents(List.of(
                createSingleStageEvent("1", "Finished"),
                createSingleStageEvent("2", "Active")
        ));

        club.updateChampionships(List.of(c1));

        assertThat(club.getCurrentEvent().get().getName()).isEqualTo("2");
    }
    @Test
    public void testCurrentEventWithFutureChampionship() {
        Club club = new Club();
        Championship c1 = new Championship();
        c1.setActive(false);
        c1.setEvents(List.of(
                createSingleStageEvent("2", "Finished"),
                createSingleStageEvent("3", "Active")
        ));

        Championship c2 = new Championship();
        c2.setActive(true);

        c2.setEvents(List.of(createSingleStageEvent("1", "Finished")));

        Championship c3 = new Championship();
        c3.setActive(false);
        c3.setEvents(List.of(
                createSingleStageEvent("5", "Finished"),
                createSingleStageEvent("4", "Active")
        ));

        club.updateChampionships(List.of(c1, c2, c3));

        assertThat(club.getCurrentEvent()).isNotPresent();
    }

    private Event createSingleStageEvent(String name, String status) {
        Event event = new Event();
        event.setReferenceId("123");
        event.setName(name);
        event.setEventStatus(status);

        Stage stage1 = new Stage();
        stage1.setReferenceId(0L);

        event.updateStages(List.of(stage1));

        return event;
    }


}