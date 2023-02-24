import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {ActiveEventInfo} from './domain/active-event-info';
import {Tier} from './domain/tier';
import {Player} from './domain/player';


@Injectable()
export class ClubTiersService {

  constructor(private httpClient: HttpClient) {
  }


  public getCurrentEvent(clubId: number) : Observable<ActiveEventInfo> {
    return this.httpClient.get<ActiveEventInfo>( `/api/clubs/${clubId}/active_event`);
  }

  public createTier(clubId: number, data: any): Observable<Tier> {
    return this.httpClient.post<Tier>(`/api/clubs/${clubId}/tiers`, data);

  }

  public getTiers(clubId: number): Observable<Tier[]> {
    return this.httpClient.get<Tier[]>(`/api/clubs/${clubId}/tiers`);
  }

  public getPlayers(): Observable<Player[]> {
    return this.httpClient.get<Player[]>('/api/players');
  }

  public createPlayer(racenet: string): Observable<Player> {
    return this.httpClient.post<Player>('/api/players', {racenet});
  }

  public getPlayersByTier(id: string): Observable<Player[]> {
    return this.httpClient.get<Player[]>(`/api/players/by_tier/${id}`);
  }

  public assignPlayerToTier(tierId: string, playerId: string) {
    return this.httpClient.post<any>(`/api/tiers/${tierId}/player/${playerId}`, {})
  }

  public clearTier(playerId: string) {
    return this.httpClient.delete<any>(`/api/players/${playerId}/clear_tiers`)
  }

  deletePlayer(id: string) {
    return this.httpClient.delete<any>(`/api/players/${id}`)
  }

  addVehicles(tierId: string, challengeId: string, eventId: string, vehicles: any[]) {
    return this.httpClient.post<any>(
      `/api/tiers/${tierId}/competition/${challengeId}/${eventId}/vehicles`, {
        vehicles
      }
    )
  }

  getVehicles(tierId: string, challengeId: string, eventId: string): any {
    return this.httpClient.get<any>(
      `/api/tiers/${tierId}/competition/${challengeId}/${eventId}/vehicles`)
  }
}
