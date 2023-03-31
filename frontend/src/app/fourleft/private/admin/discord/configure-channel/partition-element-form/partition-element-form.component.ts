import {Component, Input} from '@angular/core';
import {FormArray, FormControl, FormGroup} from "@angular/forms";

@Component({
  selector: 'app-partition-element-form',
  templateUrl: './partition-element-form.component.html',
  styleUrls: ['./partition-element-form.component.scss']
})
export class PartitionElementFormComponent {

  @Input("formGroup")
  partitionElementFormGroup!: FormGroup;



  public get players() {
    return this.partitionElementFormGroup.get('players') as FormArray;
  }

  addPlayer(name: string) {
    this.players.push(new FormControl(name, {}))
  }

  removePlayer(i: number) {
    this.players.removeAt(i);
  }
}
