import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {FieldMappingsService} from '../field-mappings.service';
import {FieldMappingTo, FieldMappingType} from '@server-models';
import {map, mergeMap, of, take} from 'rxjs';

@Component({
  selector: 'app-field-map',
  templateUrl: './field-map.component.html',
  styleUrls: ['./field-map.component.scss']
})
export class FieldMapComponent implements OnInit{

  @Input()
  value: string = "";

  @Input()
  type!: FieldMappingType;

  public mappings: FieldMappingTo[] = [];
  public mapping: string = '';



  constructor(private fieldMappingService: FieldMappingsService) {
  }

  ngOnInit(): void {
    this.fieldMappingService.getFieldMappings().pipe(take(1)).subscribe(mappings => {
      this.mappings = mappings.filter(mapping => mapping.context === 'FRONTEND');

      this.findMapping().subscribe(mapping => {
        this.mapping = mapping;
      });
    });
  }

  findMapping() {
    let fieldMappingTo = this.mappings.find(mapping => {
      return mapping.fieldMappingType === this.type && mapping.name === this.value;
    });

    return of(fieldMappingTo).pipe(mergeMap(mapping => {
      if(mapping === undefined) {
        return this.fieldMappingService.requestFieldMapping({
          type: this.type,
          context: 'FRONTEND',
          name: this.value
        }).pipe(map(x => '?'))
      } else {
        return of(mapping.value);
      }
    }));

  }

}
