import {Component, Input} from '@angular/core';
import {AbstractControl} from "@angular/forms";
import {PartitionClubViewForm} from "./partition-club-view.form";
import {RacenetFilterForm} from "../racenet-filter-form/racenet-filter.form";

@Component({
  selector: 'app-partition-club-view-form',
  templateUrl: './partition-club-view-form.component.html',
  styleUrls: ['./partition-club-view-form.component.scss']
})
export class PartitionClubViewFormComponent {

  @Input("formGroup")
  partitionFormGroup!: PartitionClubViewForm;

  cast(partitionElementForm: AbstractControl) {
    return partitionElementForm as RacenetFilterForm;
  }

  promotePlayer(idx: number, playerIdx: number) {
    const sourcePartitionElement = this.partitionFormGroup.partitionElements.controls[idx] as RacenetFilterForm;
    const targetPartitionElement = this.partitionFormGroup.partitionElements.controls[idx - 1] as RacenetFilterForm;

    targetPartitionElement.racenetNames.push(sourcePartitionElement.racenetNames.controls[playerIdx]);
    sourcePartitionElement.racenetNames.removeAt(playerIdx);
  }

  demotePlayer(idx: number, playerIdx: number) {
    const sourcePartitionElement = this.partitionFormGroup.partitionElements.controls[idx] as RacenetFilterForm;
    const targetPartitionElement = this.partitionFormGroup.partitionElements.controls[idx + 1] as RacenetFilterForm;
    targetPartitionElement.racenetNames.push(sourcePartitionElement.racenetNames.controls[playerIdx]);
    sourcePartitionElement.racenetNames.removeAt(playerIdx);
  }
}
