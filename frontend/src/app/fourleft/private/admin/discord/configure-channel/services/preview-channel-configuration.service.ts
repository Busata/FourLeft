import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {ViewPointsTo, ViewResultTo} from "../../../../../../common/generated/server-models";

@Injectable({
  providedIn: 'root'
})
export class PreviewChannelConfigurationService {

  constructor(private httpClient: HttpClient) {

  }

  public getResults(viewId: string): Observable<ViewResultTo> {
    return this.httpClient.get<ViewResultTo>(`/api/views/${viewId}/results/current`);
  }
  public getPoints(viewId: string): Observable<ViewPointsTo> {
    return this.httpClient.get<ViewPointsTo>(`/api/views/${viewId}/standings/current`);
  }
}
