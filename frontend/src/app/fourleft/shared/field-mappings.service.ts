import {HttpClient} from "@angular/common/http";
import {BehaviorSubject, Observable, shareReplay} from "rxjs";
import {Injectable} from "@angular/core";
import {FieldMappingRequestTo, FieldMappingTo} from '@server-models';

@Injectable({providedIn: 'root'})
export class FieldMappingsService {

  private _fieldMappings= new BehaviorSubject<FieldMappingTo[]>([]);


  constructor(private httpClient: HttpClient) {
  }

  public getFieldMappings() {
    return this._fieldMappings.asObservable();
  }

  public loadFieldMappings() {
    this.httpClient.get<FieldMappingTo[]>("/api/external/discord/field_mappings").subscribe(fieldMappings => {
      this._fieldMappings.next(fieldMappings);
    })
  }

  public requestFieldMapping(request: FieldMappingRequestTo) {
    return this.httpClient.post("/api/external/discord/field_mappings", request);
  }

  saveFieldMapping(id: string, value: any): Observable<FieldMappingTo> {
    return this.httpClient.put<FieldMappingTo>(`/api/internal/discord/field_mappings/${id}`, value);
  }
}
