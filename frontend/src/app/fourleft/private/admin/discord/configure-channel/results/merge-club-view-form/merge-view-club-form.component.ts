import {Component, Input} from '@angular/core';
import {AbstractControl} from "@angular/forms";
import {MergedClubViewForm} from "./merged-club-view.form";
import {SingleClubViewForm} from "../single-club-view-form/single-club-view.form";

@Component({
  selector: 'app-merge-club-view-form',
  templateUrl: './merge-view-club-form.component.html',
  styleUrls: ['./merge-view-club-form.component.scss']
})
export class MergeViewClubFormComponent {

  @Input("formGroup")
  mergeClubForm!: MergedClubViewForm

  cast(form: AbstractControl) {
    return form as SingleClubViewForm;
  }

}
