import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {ChampionshipEventSummary} from "./championship-event-summary";


@Injectable()
export class ClubMergerService {

  constructor(private httpClient: HttpClient) {
  }

  public getClubEvents(clubId: string) : Observable<ChampionshipEventSummary> {
    return this.httpClient.get<ChampionshipEventSummary>( `/api/clubs/${clubId}/event_summary`);
  }

  public mergeEvents(request: any) : Observable<any> {
    return this.httpClient.post<any>( `/api/merge`, request);
  }
}
