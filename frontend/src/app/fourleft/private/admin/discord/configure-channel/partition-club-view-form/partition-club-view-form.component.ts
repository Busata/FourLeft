import {Component, Input} from '@angular/core';
import {AbstractControl, FormArray, FormControl, FormGroup} from "@angular/forms";
import {ClubViewsFormHelper} from "../services/club-views-form-helper.service";

@Component({
  selector: 'app-partition-club-view-form',
  templateUrl: './partition-club-view-form.component.html',
  styleUrls: ['./partition-club-view-form.component.scss']
})
export class PartitionClubViewFormComponent {

  @Input("formGroup")
  partitionFormGroup!: FormGroup;

  constructor(private clubViewsFormHelperService: ClubViewsFormHelper) {
  }
  setResultsView(evt: any) {
    const value = evt.target.value;

    if(value == 'singleClub') {
      this.setSingleClubView();
    } else if (value=='mergeClub') {
      this.setMergeClubView();
    } else {
      this.partitionFormGroup.setControl('resultsView', undefined);
    }
  }

  get resultsViewType() {
    let type = this.resultViewForm.get('type') as FormControl;
    return type.value;
  }
  get resultViewForm() {
    return this.partitionFormGroup.get('resultsView') as FormGroup;
  }

  get partitionElements() {
    return this.partitionFormGroup.get('partitionElements') as FormArray;
  }

  hasResultsView() {
    return !!this.partitionFormGroup.get('resultsView');
  }

  setSingleClubView() {
    this.partitionFormGroup.setControl('resultsView', this.clubViewsFormHelperService.createSingleClubViewForm());
  }

  setMergeClubView() {
    this.partitionFormGroup.setControl('resultsView', this.clubViewsFormHelperService.createMergeClubView());
  }


  addPartitionElement() {
    const formGroup = this.clubViewsFormHelperService.createPartitionElement();

    this.partitionElements.push(formGroup);
  }

  cast(partitionElementForm: AbstractControl) {
    return partitionElementForm as FormGroup;
  }

  removePartitionElement(i: number) {
    this.partitionElements.removeAt(i);
  }
}
