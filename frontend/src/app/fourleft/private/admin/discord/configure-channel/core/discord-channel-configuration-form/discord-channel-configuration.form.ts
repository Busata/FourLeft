import {FormControl, FormGroup} from "@angular/forms";
import {ClubViewForm} from "../club-view-form/club-view.form";
import {DiscordChannelConfigurationTo} from "@server-models";

export class DiscordChannelConfigurationForm extends FormGroup {
  public readonly enableAutoposts = this.get('enableAutoposts') as FormControl;
  public readonly clubView = this.get('clubView') as ClubViewForm;

  constructor(value?: DiscordChannelConfigurationTo) {
    super({
      enableAutoposts: new FormControl(value?.enableAutoposts, {}),
      clubView: new ClubViewForm(value?.clubView)
    })
  }
}
