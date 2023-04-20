import {Component, Input} from '@angular/core';
import {FormArray, FormControl, FormGroup} from "@angular/forms";
import {PartitionElementForm} from "./partition-element.form";

@Component({
  selector: 'app-partition-element-form',
  templateUrl: './partition-element-form.component.html',
  styleUrls: ['./partition-element-form.component.scss']
})
export class PartitionElementFormComponent {

  @Input("formGroup")
  partitionElementFormGroup!: PartitionElementForm;
}
