import { Component, OnInit } from '@angular/core';
import {FieldMappingsService} from "../../../../shared/field-mappings.service";
import {FieldMappingTo} from '@server-models';

@Component({
  selector: 'app-field-mappings-list',
  templateUrl: './field-mappings-list.component.html',
  styleUrls: ['./field-mappings-list.component.scss']
})
export class FieldMappingsListComponent implements OnInit {

  public mappings: FieldMappingTo[] = [];

  constructor(private fieldMappingsService: FieldMappingsService) {
  }

  ngOnInit(): void {
    this.fieldMappingsService.getFieldMappings().subscribe(mappings => {
      this.mappings = mappings;
    })
  }


  getUnmapped() {
    return this.mappings.filter(mapping => !mapping.mappedByUser);
  }

  getMapped() {
    return this.mappings.filter(mapping => mapping.mappedByUser);
  }

  saveMapping(id: string, value: any) {
    this.fieldMappingsService.saveFieldMapping(id, value).subscribe(fieldMapping => {
      this.mappings = [...this.mappings.filter(mapping => mapping.id !== id), fieldMapping];
    });
  }
}
