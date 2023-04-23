import {Component, Input} from '@angular/core';
import {AbstractControl} from "@angular/forms";
import {PartitionClubViewForm} from "./partition-club-view.form";
import {PartitionElementForm} from "../partition-element-form/partition-element.form";

@Component({
  selector: 'app-partition-club-view-form',
  templateUrl: './partition-club-view-form.component.html',
  styleUrls: ['./partition-club-view-form.component.scss']
})
export class PartitionClubViewFormComponent {

  @Input("formGroup")
  partitionFormGroup!: PartitionClubViewForm;

  cast(partitionElementForm: AbstractControl) {
    return partitionElementForm as PartitionElementForm;
  }

  promotePlayer(idx: number, playerIdx: number) {
    const sourcePartitionElement = this.partitionFormGroup.partitionElements.controls[idx] as PartitionElementForm;
    const targetPartitionElement = this.partitionFormGroup.partitionElements.controls[idx - 1] as PartitionElementForm;

    targetPartitionElement.racenetNames.push(sourcePartitionElement.racenetNames.controls[playerIdx]);
    sourcePartitionElement.racenetNames.removeAt(playerIdx);
  }

  demotePlayer(idx: number, playerIdx: number) {
    const sourcePartitionElement = this.partitionFormGroup.partitionElements.controls[idx] as PartitionElementForm;
    const targetPartitionElement = this.partitionFormGroup.partitionElements.controls[idx + 1] as PartitionElementForm;
    targetPartitionElement.racenetNames.push(sourcePartitionElement.racenetNames.controls[playerIdx]);
    sourcePartitionElement.racenetNames.removeAt(playerIdx);
  }
}
