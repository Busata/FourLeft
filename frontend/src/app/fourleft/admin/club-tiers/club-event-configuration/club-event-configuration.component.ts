import {Component, Input, OnInit} from '@angular/core';
import {Tier} from '../domain/tier';
import {ActiveEventInfo} from '../domain/active-event-info';

@Component({
  selector: 'app-club-event-configuration',
  templateUrl: './club-event-configuration.component.html',
  styleUrls: ['./club-event-configuration.component.scss']
})
export class ClubEventConfigurationComponent implements OnInit {

  @Input()
  public tiers: Tier[] = []

  @Input()
  public eventInfo!: ActiveEventInfo

  constructor() { }

  ngOnInit(): void {
  }

}
