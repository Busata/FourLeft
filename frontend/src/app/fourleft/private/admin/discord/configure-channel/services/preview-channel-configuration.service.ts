import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {ViewResultTo} from "../../../../../../common/generated/server-models";

@Injectable({
  providedIn: 'root'
})
export class PreviewChannelConfigurationService {

  constructor(private httpClient: HttpClient) {

  }

  public getResults(viewId: string): Observable<ViewResultTo> {
    return this.httpClient.get<ViewResultTo>(`/api/views/${viewId}/results/current`);
  }
}
