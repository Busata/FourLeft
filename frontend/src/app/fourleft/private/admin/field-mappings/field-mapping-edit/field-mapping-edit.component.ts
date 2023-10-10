import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {FieldMappingTo} from '@server-models';

@Component({
  selector: 'app-field-mapping-edit',
  templateUrl: './field-mapping-edit.component.html',
  styleUrls: ['./field-mapping-edit.component.scss']
})
export class FieldMappingEditComponent implements OnInit {
  public form: UntypedFormGroup;

  @Input()
  public set fieldMapping(value: FieldMappingTo) {
    this.form.patchValue({
      name: value.name,
      value: value.value,
      fieldMappingType: value.fieldMappingType,
      fieldMappingContext: value.context
    })
  }

  @Output()
  public save = new EventEmitter();

  constructor(private fb: UntypedFormBuilder) {
    this.form = this.fb.group({
      name: [{value: "", disabled: true}],
      value: [{value:"", disabled: false}],
      fieldMappingType: [{value:"", disabled: true}],
      fieldMappingContext: [{value: "", disabled: true}]
    })
  }

  ngOnInit(): void {

  }

  saveMapping() {
    this.save.emit(this.form.getRawValue());
  }
}
