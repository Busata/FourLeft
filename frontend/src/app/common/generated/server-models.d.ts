/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 2.35.1025 on 2025-02-06 16:45:55.

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

export interface AliasRequestResultTo {
    aliasExists: boolean;
    requestId: string;
}

export interface AliasUpdateDataTo {
    id: string;
    displayName: string;
    controller: ControllerType;
    platform: Platform;
    racenet: string;
    trackCommunity: boolean;
    aliases: string[];
}

export interface AliasUpdateRequestTo {
    racenet: string;
    discordId: string;
}

export interface ChampionshipEventEntryTo {
    countryId: string;
    eventName: string;
    stageName: string;
    challengeId: string;
    eventId: string;
    stageId: number;
    stageCondition: string;
    vehicleClass: string;
    isCurrent: boolean;
    isFinished: boolean;
}

export interface ChampionshipEventSummaryTo {
    name: string;
    isHardcoreDamage: boolean;
    useAssists: boolean;
    unexpectedMoments: boolean;
    forceCockpit: boolean;
    events: ChampionshipEventEntryTo[];
}

export interface ChampionshipStageEntryTo {
    country: string;
    vehicleClass: string;
    stageNames: string[];
}

export interface ChampionshipStageSummaryTo {
    name: string;
    isHardcoreDamage: boolean;
    useAssists: boolean;
    unexpectedMoments: boolean;
    forceCockpit: boolean;
    stageSummary: ChampionshipStageEntryTo;
}

export interface ChampionshipStandingEntryTo {
    rank: number;
    nationality: string;
    displayName: string;
    points: number;
}

export interface ChannelConfigurationTo {
    description: string;
    channelId: number;
    clubId: number;
    postClubResults: boolean;
    postCommunityResults: boolean;
    useBadges: boolean;
    hasPowerStage: boolean;
    championshipPointsType: ChampionshipPointsType;
    customChampionshipCycle: number;
}

export interface ClubMemberTo {
    displayName: string;
    membershipType: string;
    championshipGolds: number;
    championshipSilvers: number;
    championshipBronzes: number;
    championshipParticipation: number;
}

export interface ClubResultTo {
    eventId: string;
    eventChallengeId: string;
    eventName: string;
    stageName: string;
    stageNames: string[];
    vehicleClass: string;
    country: string;
    lastUpdate: Date;
    endTime: Date;
    entries: ResultEntryTo[];
}

export interface CommunityChallengeBoardEntryTo {
    rank: number;
    name: string;
    nationality: string;
    vehicleName: string;
    stageTime: string;
    stageDiff: string;
    totalRank: number;
    percentageRank: number;
    isDnf: boolean;
}

export interface CommunityChallengeSummaryTo {
    type: DR2CommunityEventType;
    vehicleClass: string;
    eventLocations: string[];
    firstStageName: string;
    topOnePercentEntry: CommunityChallengeBoardEntryTo;
    firstEntry: CommunityChallengeBoardEntryTo;
    entries: CommunityChallengeBoardEntryTo[];
}

export interface CommunityLeaderboardTrackingTo {
    id: string;
    racenet: string;
    alias: string;
    trackRallyCross: boolean;
    trackDaily: boolean;
    trackMonthly: boolean;
    trackWeekly: boolean;
}

export interface CustomChampionshipStandingEntryTo {
    rank: number;
    nationality: string;
    displayName: string;
    points: number;
    powerStagePoints: number;
}

export interface DriverEntryTo {
    result: DriverResultTo;
    relative: DriverRelativeResultTo;
    dnf: boolean;
}

export interface DriverRelativeResultTo {
    activityRank: number;
    activityPercentageRank: number;
    activityTotalDiff: string;
    powerStageRank: number;
    powerStageTotalDiff: string;
}

export interface DriverResultTo {
    racenet: string;
    nationality: string;
    platform: PlatformTo;
    activityTotalTime: string;
    powerStageTotalTime: string;
    isDnf: boolean;
    vehicles: VehicleEntryTo[];
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

export interface PlatformTo {
    platform: Platform;
    controller: ControllerType;
}

export interface PlayerCreateTo {
    racenet: string;
}

export interface QueryNameResultsTo {
    found: boolean;
}

export interface QueryTrackResultsTo {
    countryId: string;
    countryName: string;
    longStage: StageOptionTo;
    firstShort: StageOptionTo;
    secondShort: StageOptionTo;
    reverseLongStage: StageOptionTo;
    reverseFirstShort: StageOptionTo;
    reverseSecondShort: StageOptionTo;
}

export interface ResultEntryTo {
    rank: number;
    name: string;
    nationality: string;
    vehicle: string;
    totalTime: string;
    totalDiff: string;
    stageTime: string;
    stageDiff: string;
    stageRank: number;
    isDnf: boolean;
    platform: Platform;
    controllerType: ControllerType;
}

export interface StageOptionTo {
    displayName: string;
    length: number;
    isQueried: boolean;
}

export interface StageResultTo {
    vehicle: string;
    stageTime: string;
    stageDiff: string;
    stageRank: number;
    isDnf: boolean;
}

export interface StandingPointPairTo {
    rank: number;
    point: number;
}

export interface TrackUserRequestTo {
    racenet: string;
}

export interface VehicleEntryTo {
    vehicleName: string;
    vehicleAllowed: boolean;
}

export interface ClubViewTo {
    id: string;
    description: string;
    badgeType: BadgeType;
    resultsView: ResultsViewToUnion;
    pointsView: PointsCalculatorToUnion;
}

export interface DefaultPointsCalculatorTo extends PointsCalculatorTo {
    type: "defaultPoints";
}

export interface FixedPointsCalculatorTo extends PointsCalculatorTo {
    type: "fixedPoints";
    joinChampionshipsCount: number;
    offsetChampionship: string;
    pointSystem: PointSystemTo;
}

export interface PlayerRestrictionsTo {
}

export interface PointSystemTo {
    id: string;
    description: string;
    defaultStandingPoint: number;
    defaultPowerstagePoint: number;
    defaultDNFPoint: number;
    standingPoints: StandingPointPairTo[];
    powerStagePoints: StandingPointPairTo[];
}

export interface PointsCalculatorTo {
    type: "defaultPoints" | "fixedPoints";
}

export interface DiscordChannelConfigurationTo {
    id: string;
    enableAutoposts: boolean;
    channelId: number;
    clubView: ClubViewTo;
}

export interface CommunityChallengeViewTo extends ResultsViewTo {
    type: "communityChallengeView";
    postDailies: boolean;
    postWeeklies: boolean;
    postMonthlies: boolean;
    badgeType: BadgeType;
}

export interface ConcatenationViewTo extends ResultsViewTo {
    type: "concatenationClub";
    name: string;
    resultViews: SingleClubViewTo[];
}

export interface MergedViewTo extends ResultsViewTo {
    type: "mergeClub";
    mergeMode: MergeMode;
    name: string;
    racenetFilter: RacenetFilterTo;
    resultViews: SingleClubViewTo[];
}

export interface PartitionViewTo extends ResultsViewTo {
    type: "partitionClub";
    resultsView: ResultsViewToUnion;
    partitionElements: RacenetFilterTo[];
}

export interface RacenetFilterTo {
    id: string;
    name: string;
    filterMode: RacenetFilterMode;
    racenetNames: string[];
    enabled: boolean;
}

export interface ResultsViewTo {
    type: "communityChallengeView" | "concatenationClub" | "mergeClub" | "partitionClub" | "singleClub";
    id: string;
    associatedClubs: number[];
}

export interface SingleClubViewTo extends ResultsViewTo {
    type: "singleClub";
    clubId: number;
    name: string;
    usePowerstage: boolean;
    powerStageIndex: number;
    racenetFilter: RacenetFilterTo;
}

export interface DiscordAuthenticationStatusTo {
    authenticated: boolean;
}

export interface DiscordChannelSummaryTo {
    id: string;
    name: string;
    hasConfiguration: boolean;
}

export interface DiscordChannelTo {
    id: string;
    type: number;
    name: string;
}

export interface DiscordGuildMemberEventTo {
    discordId: string;
    username: string;
    memberEvent: MemberEvent;
}

export interface DiscordGuildMemberTo {
    id: string;
    discordId: string;
    username: string;
}

export interface DiscordGuildPermissionTo {
    canManage: boolean;
}

export interface DiscordGuildSummaryTo {
    id: string;
    name: string;
    icon: string;
    botJoined: boolean;
}

export interface DiscordGuildTo {
    id: string;
    name: string;
    icon: string;
    owner: boolean;
    permissions: number;
}

export interface DiscordMemberTo {
    user: DiscordUserTo;
    nick: string;
}

export interface DiscordTokenTo {
    access_token: string;
    token_type: string;
    expires_in: number;
    refresh_token: string;
    scope: string;
}

export interface DiscordUserTo {
    id: string;
    username: string;
    bot: boolean;
    email: string;
}

export interface GuildMemberTo {
    nick: string;
}

export interface GalleryPhotoTo {
    id: string;
    title: string;
    published: boolean;
    preview: boolean;
    tags: string[];
}

export interface GalleryPhotoUpdateTo {
    title: string;
    published: boolean;
    preview: boolean;
    tags: string[];
}

export interface GalleryPublicPhotoTo {
    id: string;
    title: string;
    preview: boolean;
    tags: string[];
}

export interface GalleryTagNodeTo {
    id: string;
    label: string;
    showOn: string;
    nodes: GalleryTagOptionTo[];
}

export interface GalleryTagOptionTo {
    id: string;
    label: string;
}

export interface MessageLogTo {
    messageType: MessageType;
    viewType: ViewType;
    messageId: number;
    channelId: number;
}

export interface ClubResultSummaryTo {
    clubName: string;
    eventCountry: string;
    eventStage: string;
    endTime: Date;
    nationality: string;
    vehicle: string;
    rank: number;
    totalEntries: number;
    percentageRank: number;
    isDnf: boolean;
    totalTime: string;
    totalDiff: string;
}

export interface CommunityResultSummaryTo {
    countryName: string;
    vehicleClass: string;
    endTime: Date;
    nationality: string;
    vehicle: string;
    rank: number;
    totalEntries: number;
    percentageRank: number;
    isDnf: boolean;
    totalTime: string;
    totalDiff: string;
}

export interface UserResultSummaryTo {
    communityActiveEventSummaries: CommunityResultSummaryTo[];
    communityPreviousEventSummaries: CommunityResultSummaryTo[];
    clubActiveEventSummaries: ClubResultSummaryTo[];
    clubPreviousEventSummaries: ClubResultSummaryTo[];
}

export interface UserTo {
    name: string;
    roles: string[];
}

export interface ActivityInfoTo {
    id: string;
    eventId: string;
    eventChallengeId: string;
    eventName: string;
    stageNames: string[];
    vehicleClass: string;
    country: string;
    lastUpdate: Date;
    endTime: Date;
    restrictions: ResultRestrictionsTo;
}

export interface PointPairTo {
    name: string;
    nationality: string;
    points: number;
}

export interface ResultListTo {
    name: string;
    activityInfoTo: ActivityInfoTo[];
    totalUniqueEntries: number;
    results: DriverEntryTo[];
}

export interface ResultRestrictionsTo {
    id: string;
    vehicleClass: string;
    resultId: string;
    challengeId: string;
    eventId: string;
    restrictedVehicles: VehicleTo[];
}

export interface SinglePointListTo {
    name: string;
    points: PointPairTo[];
}

export interface VehicleTo {
    id: string;
    displayName: string;
}

export interface ViewEventEntryTo {
    countryId: string;
    eventName: string;
    startTime: Date;
    endTime: Date;
    stageNames: string[];
    stageCondition: string;
    vehicleClass: string;
    isCurrent: boolean;
    isFinished: boolean;
}

export interface ViewEventSummaryTo {
    header: string;
    events: ViewEventEntryTo[];
}

export interface ViewPointsTo {
    points: SinglePointListTo[];
}

export interface ViewPropertiesTo {
    powerStage: boolean;
    badgeType: BadgeType;
}

export interface ViewResultTo {
    viewEventKey: string;
    description: string;
    viewPropertiesTo: ViewPropertiesTo;
    multiListResults: ResultListTo[];
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

export type ChampionshipPointsType = "DEFAULT" | "JRC";

export type DR2CommunityEventType = "Daily" | "Weekly" | "Monthly";

export type FieldMappingType = "HUMAN_READABLE" | "FLAG" | "EMOTE" | "IMAGE" | "COLOUR";

export type FieldMappingContext = "BACKEND" | "FRONTEND";

export type BadgeType = "NONE" | "PERCENTAGE" | "RANKED";

export type MergeMode = "ADD_TIMES" | "TAKE_BEST";

export type RacenetFilterMode = "NONE" | "INCLUDE" | "FILTER" | "TRACK_COMMUNITY";

export type MemberEvent = "JOINED" | "LEFT";

export type MessageType = "AUTO_POST" | "RESULTS_POST" | "JOIN_POST" | "LEAVE_POST" | "CURRENT_RESULTS_POST" | "PREVIOUS_RESULTS_POST" | "COMMUNITY_EVENT" | "AUTOMATED_CLUB_POST";

export type ViewType = "STANDARD" | "EXTRA";

export type PointsCalculatorToUnion = DefaultPointsCalculatorTo | FixedPointsCalculatorTo;

export type ResultsViewToUnion = SingleClubViewTo | MergedViewTo | PartitionViewTo | ConcatenationViewTo | CommunityChallengeViewTo;
