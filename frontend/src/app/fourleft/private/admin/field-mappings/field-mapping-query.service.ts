import {Injectable} from '@angular/core';
import {BehaviorSubject, filter, map, mergeAll, Observable} from 'rxjs';
import {FieldMappingsService} from '../../../shared/field-mappings.service';
import {FieldMappingTo} from '@server-models';

@Injectable()
export class FieldMappingQueryService {

  private _fieldMappings: BehaviorSubject<FieldMappingTo[]> = new BehaviorSubject([] as FieldMappingTo[]);
  public readonly fieldMappings: Observable<FieldMappingTo[]> = this._fieldMappings.asObservable();


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
