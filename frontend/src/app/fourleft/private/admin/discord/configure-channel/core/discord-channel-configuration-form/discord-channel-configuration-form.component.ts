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

  public activeConfiguration = 'results';
  public resultsType: string = 'singleClub'
  public pointsType: string = 'defaultPoints'

  public get configuringBasic() {
    return this.activeConfiguration === 'basic';
  }

  public get configuringResults() {
    return this.activeConfiguration === 'results';
  }

  public get configuringPoints() {
    return this.activeConfiguration === 'points';
  }

  public get isPreviewOpen() {
    return this.activeConfiguration === 'preview';
  }

  @Input()
  public set existingForm(value: DiscordChannelConfigurationTo) {
    if(!value) { return;}

    this.configuation = value;
    this.resultsType = this.configuation?.clubView?.resultsView?.type || 'singleClub';
    this.pointsType = this.configuation?.clubView?.pointsView?.type || 'defaultPoints';

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

  configureBasic() {
    this.activeConfiguration = 'basic';
  }

  configureResults() {
    this.activeConfiguration = 'results';
  }
  configurePoints() {
    this.activeConfiguration = 'points';
  }
  openPreview() {
    this.activeConfiguration = 'preview';
  }

  saveConfiguration() {
    this.formSave.emit(this.form.value);
  }

  setResultsType(type: string) {
    if(confirm("This will reset the current results data, are you sure?")) {
      this.resultsType = type;
    }
  }

  setPointsType(type: string) {
    if(confirm("This will reset the current points data, are you sure?")) {
      this.pointsType = type;
    }
  }
}
