import {Injectable} from '@angular/core';
import {BehaviorSubject, filter, map, mergeAll, Observable} from 'rxjs';
import {FieldMapping} from './field-mapping';
import {FieldMappingsService} from './field-mappings.service';

@Injectable()
export class FieldMappingQueryService {

  private _fieldMappings: BehaviorSubject<FieldMapping[]> = new BehaviorSubject([] as FieldMapping[]);
  public readonly fieldMappings: Observable<FieldMapping[]> = this._fieldMappings.asObservable();


  constructor(private fieldMappingService: FieldMappingsService) {
    this.loadInitialData();
  }


  public loadInitialData() {
    this.fieldMappingService.getFieldMappings().subscribe((fieldMappings) => {
      this._fieldMappings.next(fieldMappings);
    })
  }

  public getReadableName(field: string) {
    return this.fieldMappings.pipe(
      mergeAll(),
      filter(fieldMapping => {
        return fieldMapping.name === field && fieldMapping.fieldMappingType === "HUMAN_READABLE";
      }),
      map(fieldMapping => {
        return fieldMapping.value
      }),
    )
  }
}
