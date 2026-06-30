/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 2.35.1025 on 2026-06-30 15:28:45.

export interface RoutesTo {
}

export interface ChampionshipSettingsTo {
    name: string;
    format: number;
    bonusPointsMode: number;
    scoringSystem: number;
    isTuningAllowed: boolean;
    isHardcoreDamageEnabled: boolean;
    trackDegradation: number;
    isAssistsAllowed: boolean;
}

export interface ClubChampionshipResultTo {
    id: string;
    string: string;
    absoluteOpenDate: Date;
    absoluteCloseDate: Date;
    championshipSettings: ChampionshipSettingsTo;
    events: ClubEventResultTo[];
}

export interface ClubEventResultTo {
    eventSettings: EventSettingsTo;
    stages: StageSettingsTo[];
    id: string;
    status: string;
    absoluteOpenDate: Date;
    absoluteCloseDate: Date;
    leaderboardEntries: ClubResultEntryTo[];
}

export interface ClubOverviewTo {
    id: string;
    clubName: string;
    clubDescription: string;
    clubCreatedAt: Date;
    activeMemberCount: number;
    lastLeaderboardUpdate: Date;
    lastDetailsUpdate: Date;
    championships: ClubChampionshipResultTo[];
}

export interface ClubResultEntryTo {
    wrcPlayerId: string;
    playerName: string;
    nationalityID: number;
    platform: number;
    rank: number;
    vehicle: string;
    time: Duration;
    timeAccumulated: Duration;
    timePenalty: Duration;
    differenceToFirst: Duration;
    differenceAccumulated: Duration;
}

export interface DiscordClubConfigurationTo {
    channelId: number;
    clubId: string;
}

export interface DiscordClubCreateConfigurationTo {
    guildId: number;
    channelId: number;
    clubId: string;
    autoPosting: boolean;
}

export interface DiscordClubRemoveConfigurationTo {
    guildId: number;
    channelId: number;
    clubId: string;
}

export interface EventSettingsTo {
    location: string;
    weatherSeason: string;
    vehicleClass: string;
    duration: string;
}

export interface ProfileTo {
    id: string;
    displayName: string;
    controller: ControllerType;
    platform: Platform;
    peripheral: PeripheralType;
    racenet: string;
    trackDiscord: boolean;
}

export interface ProfileUpdateRequestResultTo {
    requestId: string;
    foundProfile: boolean;
}

export interface ProfileUpdateRequestTo {
    racenet: string;
    discordId: string;
    userName: string;
}

export interface SetupChannelResultTo {
    id: number;
    name: string;
}

export interface StageSettingsTo {
    route: string;
    timeOfDay: string;
    serviceArea: string;
    weatherAndSurface: string;
}

export interface FIATickerUpdateTo {
    title: string;
    tickerEventKey: string;
    dateTime: number;
    text: string;
    imageUrl: string;
}

export interface FieldMappingRequestTo {
    name: string;
    type: FieldMappingType;
    context: FieldMappingContext;
}

export interface FieldMappingTo {
    id: string;
    name: string;
    value: string;
    fieldMappingType: FieldMappingType;
    context: FieldMappingContext;
    mappedByUser: boolean;
}

export interface FieldMappingUpdateTo {
    name: string;
    value: string;
    fieldMappingType: FieldMappingType;
}

export interface MessageLogTo {
    messageType: MessageType;
    viewType: ViewType;
    messageId: number;
    channelId: number;
}

export interface UserTo {
    name: string;
    roles: string[];
}

export interface Duration extends TemporalAmount, Comparable<Duration>, Serializable {
}

export interface TemporalAmount {
    units: TemporalUnit[];
}

export interface Serializable {
}

export interface TemporalUnit {
    durationEstimated: boolean;
    duration: Duration;
    timeBased: boolean;
    dateBased: boolean;
}

export interface Comparable<T> {
}

export type ControllerType = "WHEEL" | "CONTROLLER" | "KEYBOARD" | "OTHER" | "UNKNOWN";

export type Platform = "PC" | "PLAYSTATION" | "XBOX" | "EPIC" | "EA_APP" | "STEAM" | "STEAM_DECK" | "OTHER" | "UNKNOWN";

export type PeripheralType = "MONITOR" | "TRIPLES" | "WIDESCREEN" | "VR" | "OTHER" | "UNKNOWN";

export type FieldMappingType = "HUMAN_READABLE" | "FLAG" | "EMOTE" | "IMAGE" | "COLOUR";

export type FieldMappingContext = "BACKEND" | "FRONTEND";

export type MessageType = "AUTO_POST" | "RESULTS_POST" | "JOIN_POST" | "LEAVE_POST" | "CURRENT_RESULTS_POST" | "PREVIOUS_RESULTS_POST" | "COMMUNITY_EVENT" | "AUTOMATED_CLUB_POST";

export type ViewType = "STANDARD" | "EXTRA";
