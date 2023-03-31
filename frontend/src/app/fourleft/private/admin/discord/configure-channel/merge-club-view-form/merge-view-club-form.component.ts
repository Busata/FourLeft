import {Component, Input} from '@angular/core';
import {AbstractControl, FormArray, FormControl, FormGroup} from "@angular/forms";
import {ClubViewsFormHelper} from "../services/club-views-form-helper.service";

@Component({
  selector: 'app-merge-club-view-form',
  templateUrl: './merge-view-club-form.component.html',
  styleUrls: ['./merge-view-club-form.component.scss']
})
export class MergeViewClubFormComponent {

  @Input("formGroup")
  mergeClubForm!: FormGroup

  constructor(private clubViewsFormHelperService: ClubViewsFormHelper) {
  }

  addSingleClub() {
    this.clubViews.push(this.clubViewsFormHelperService.createSingleClubViewForm());
  }

  cast(form: AbstractControl) {
    return form as FormGroup;
  }


  public get clubViews() {
    return this.mergeClubForm.get('clubViews') as FormArray;
  }
}
