import {Component, Input, OnInit} from '@angular/core';
import {Tier} from '../domain/tier';
import {moveItemInArray, transferArrayItem} from '@angular/cdk/drag-drop';
import {Player} from '../domain/player';
import {ClubTiersService} from '../club-tiers.service';
import {take} from 'rxjs';

@Component({
  selector: 'app-tier-configuration',
  templateUrl: './tier-configuration.component.html',
  styleUrls: ['./tier-configuration.component.scss']
})
export class TierConfigurationComponent implements OnInit {

  @Input()
  public tier!: Tier;

  public players: Player[] = [];
  constructor(public clubTiersService: ClubTiersService) { }

  ngOnInit(): void {
    this.clubTiersService.getPlayersByTier(this.tier.id).subscribe(players => {
      this.players = players;
    })
  }

  doSomething(tierId:any , event: any) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      this.clubTiersService.assignPlayerToTier(tierId, event.previousContainer.data[event.previousIndex].id).pipe(take(1)).subscribe();

      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex,
      );
  }}
}
