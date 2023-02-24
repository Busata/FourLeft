export interface ActiveEventInfo {
    country: string;
    stageNames: string[];
    vehicleClass: string;
    eventId: string;
    challengeId: string;
    vehicles: {id: string; displayName: string;}[]
}
