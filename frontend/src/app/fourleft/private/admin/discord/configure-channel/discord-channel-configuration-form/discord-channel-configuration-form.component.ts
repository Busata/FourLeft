import {Component, EventEmitter, Input, Output} from '@angular/core';
import {FormControl, FormGroup} from "@angular/forms";
import {ClubViewsFormHelper} from "../services/club-views-form-helper.service";
import {CreateDiscordChannelConfigurationTo} from "../../../../../../common/generated/server-models";

@Component({
  selector: 'app-discord-channel-configuration-form',
  templateUrl: './discord-channel-configuration-form.component.html',
  styleUrls: ['./discord-channel-configuration-form.component.scss']
})
export class DiscordChannelConfigurationFormComponent {

  @Input()
  public set existingForm(value: CreateDiscordChannelConfigurationTo) {
    if(!value) { return;}

    console.log(value);
    this.clubViewsFormHelper.setResultsView(this.clubViewForm, value.clubView.resultsView.type);
    this.clubViewsFormHelper.setPointsView(this.clubViewForm, value.clubView.pointsView.type);

    this.form.patchValue(value);
  }

  @Output()
  formSave = new EventEmitter<any>();


  constructor(private clubViewsFormHelper: ClubViewsFormHelper) {
  }

  form = new FormGroup({
    enableAutoposts: new FormControl(true, {}),
    clubView: new FormGroup({
      badgeType: new FormControl("NONE", {
        nonNullable: true
      }),
    })
  })
  public get clubViewForm() {
    return this.form.get('clubView') as FormGroup;
  };

  saveConfiguration() {
    this.formSave.emit(this.form.value);
  }
}
