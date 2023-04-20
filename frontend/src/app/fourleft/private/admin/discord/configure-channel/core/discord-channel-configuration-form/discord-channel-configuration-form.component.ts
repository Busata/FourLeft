import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {DiscordChannelConfigurationTo} from "@server-models";
import {DiscordChannelConfigurationForm} from "./discord-channel-configuration.form";

@Component({
  selector: 'app-discord-channel-configuration-form',
  templateUrl: './discord-channel-configuration-form.component.html',
  styleUrls: ['./discord-channel-configuration-form.component.scss']
})
export class DiscordChannelConfigurationFormComponent implements OnInit {
  public form!: DiscordChannelConfigurationForm;
  public configuation!: DiscordChannelConfigurationTo;

  @Input()
  public set existingForm(value: DiscordChannelConfigurationTo) {
    if(!value) { return;}

    this.configuation = value;

    this.form = new DiscordChannelConfigurationForm(value);
  }

  @Output()
  formSave = new EventEmitter<any>();

  constructor() {
  }

  ngOnInit(): void {
    this.form = new DiscordChannelConfigurationForm();

    this.form.valueChanges.subscribe((value) => {
      console.log(value);
    })
  }


  saveConfiguration() {
    this.formSave.emit(this.form.value);
  }
}
