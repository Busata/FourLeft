import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {FieldMapping} from "./field-mapping";

@Injectable()
export class FieldMappingsService {

  constructor(private httpClient: HttpClient) {
  }

  public getFieldMappings() : Observable<FieldMapping[]> {
    return this.httpClient.get<FieldMapping[]>("/api/discord/field_mappings");
  }

  saveFieldMapping(id: string, value: any): Observable<FieldMapping> {
    return this.httpClient.put<FieldMapping>(`/api/discord/field_mappings/${id}`, value);
  }
}
