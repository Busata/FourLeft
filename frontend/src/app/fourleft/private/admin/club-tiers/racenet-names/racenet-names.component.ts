import { Component, OnInit } from '@angular/core';
import {ClubTiersService} from '../club-tiers.service';
import {Player} from '../domain/player';
import {moveItemInArray, transferArrayItem} from '@angular/cdk/drag-drop';
import {take} from 'rxjs';

@Component({
  selector: 'app-racenet-names',
  templateUrl: './racenet-names.component.html',
  styleUrls: ['./racenet-names.component.scss']
})
export class RacenetNamesComponent implements OnInit {

  players: Player[] = [];
  racenet: string= "";

  constructor(public clubTiersService: ClubTiersService) { }

  ngOnInit(): void {
    this.clubTiersService.getPlayers().subscribe(players => {
      this.players = players;
    })
  }

  doSomething(event: any) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      this.clubTiersService.clearTier(event.previousContainer.data[event.previousIndex].id).pipe(take(1)).subscribe()

      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex,
      );
    }
  }

  createPlayer() {
    this.clubTiersService.createPlayer(this.racenet).subscribe(player => {
      this.racenet = "";
      this.players.push(player);
    })
  }

  removePlayer(item: Player) {
    this.clubTiersService.deletePlayer(item.id).subscribe(() => {
     this.players = this.players.filter(p => p.id !== item.id);
    })
  }
}
