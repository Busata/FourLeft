import {Component, EventEmitter, Input, Output} from '@angular/core';
import {PartitionElementForm} from "./partition-element.form";

@Component({
  selector: 'app-partition-element-form',
  templateUrl: './partition-element-form.component.html',
  styleUrls: ['./partition-element-form.component.scss']
})
export class PartitionElementFormComponent {

  @Input("formGroup")
  partitionElementFormGroup!: PartitionElementForm;

  @Input()
  public canPromote = false;

  @Input()
  public canDemote = false;

  @Output()
  public promote = new EventEmitter();

  @Output()
  public demote = new EventEmitter();

}
