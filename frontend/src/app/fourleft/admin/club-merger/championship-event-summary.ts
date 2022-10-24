export interface ChampionshipEventSummary {
  name: string;
  events: ChampionshipEventEntry[];
}

export interface ChampionshipEventEntry {
  name: string;
  eventName: string;
  stageName: string;
  challengeId: string;
  eventId: string;
  stageId: string;
  vehicleClass: string;
  isFinished: boolean;
  isCurrent: boolean;
}
