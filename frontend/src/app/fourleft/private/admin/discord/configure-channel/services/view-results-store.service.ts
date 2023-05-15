import {Injectable} from '@angular/core';
import {Observable, shareReplay} from 'rxjs';
import {ResultRestrictionsTo, ViewPointsTo, ViewResultTo} from 'src/app/common/generated/server-models';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ViewResultsStoreService {

  private _viewResults = new Map<string, Observable<ViewResultTo>>();
  private _previousResults = new Map<string, Observable<ViewResultTo>>();
  private _viewStandings = new Map<string, Observable<ViewPointsTo>>();

  private _resultRestrictions = new Map<string, Observable<ResultRestrictionsTo[]>>();

  constructor(private http: HttpClient) {
  }

  getResults(viewId: string): Observable<ViewResultTo> {
    if(!this._viewResults.has(viewId)) {
      this._viewResults.set(viewId, this.http.get<ViewResultTo>(`/api/external/views/${viewId}/results/current`)
        .pipe(shareReplay()));
    }

    return this._viewResults.get(viewId) as Observable<ViewResultTo>;
  }
  getPreviousResults(viewId: string): Observable<ViewResultTo> {
    if(!this._previousResults.has(viewId)) {
      this._previousResults.set(viewId, this.http.get<ViewResultTo>(`/api/external/views/${viewId}/results/previous`)
        .pipe(shareReplay()));
    }

    return this._previousResults.get(viewId) as Observable<ViewResultTo>;
  }


  getPoints(viewId: string): Observable<ViewPointsTo> {
    if(!this._viewStandings.has(viewId)) {
      this._viewStandings.set(viewId, this.http.get<ViewPointsTo>(`/api/external/views/${viewId}/standings/current`)
        .pipe(shareReplay()));
    }

    return this._viewStandings.get(viewId) as Observable<ViewPointsTo>;
  }


  getResultRestrictions(resultViewId: string): Observable<ResultRestrictionsTo[]> {
    if(!this._resultRestrictions.has(resultViewId)) {
      this._resultRestrictions.set(resultViewId, this.http.get<ResultRestrictionsTo[]>(`/api/internal/views/${resultViewId}/restrictions`)
        .pipe(shareReplay()));
    }

    return this._resultRestrictions.get(resultViewId) as Observable<ResultRestrictionsTo[]>;
  }


  createResultRestrictions(resultViewId: string, $event: ResultRestrictionsTo) {
    this.http.post(`/api/internal/views/${resultViewId}/restrictions`, $event).subscribe();
  }
}
