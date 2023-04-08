import {Component, Input} from '@angular/core';
import {AbstractControl, FormArray, FormControl, FormGroup} from "@angular/forms";
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

  showPlayers() {
    return ["INCLUDE","EXCLUDE"].indexOf(this.playerFilterType) !== -1;
  }


  private get playerFilter() {
    return this.mergeClubForm.get('playerFilter') as FormGroup;
  }

  private get playerFilterType() {
    return this.playerFilter?.get('playerFilterType')?.value;
  }

  public get players() {
    return this.playerFilter.get('racenetNames') as FormArray;
  }

  addPlayer(name: string) {
    this.players.push(new FormControl(name, {}))
  }

  removePlayer(i: number) {
    this.players.removeAt(i);
  }

  updatePlayers() {
    if(this.playerFilterType == 'NONE') {
      this.players.clear();
    }
  }
}
