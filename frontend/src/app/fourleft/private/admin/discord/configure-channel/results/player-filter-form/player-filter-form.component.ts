import {Component, Input, OnInit} from '@angular/core';
import {PlayerFilterForm} from './player-filter.form';
import {QueryService} from '../../services/query-service';
import {debounceTime, Subject} from 'rxjs';

@Component({
  selector: 'app-player-filter-form',
  templateUrl: './player-filter-form.component.html',
  styleUrls: ['./player-filter-form.component.scss']
})
export class PlayerFilterFormComponent implements OnInit{

  @Input("formGroup")
  playerFilterFormGroup!: PlayerFilterForm;

  public suggestedPlayers: string[] = [];
  player: any = '';

  private searchSubject = new Subject<string>();

  constructor(private queryService: QueryService) {
  }

  ngOnInit(): void {
        this.searchSubject.pipe(debounceTime(50)).subscribe(value => {
          this.queryService.getPlayers(value).subscribe(players => {
            this.suggestedPlayers = players;
          });
        })
    }

  showPlayers() {
    return ["INCLUDE","EXCLUDE"].indexOf(this.playerFilterFormGroup.playerFilterType.value) !== -1;
  }

  updatePlayers() {
    if(this.playerFilterFormGroup.playerFilterType.value == 'NONE') {
      this.playerFilterFormGroup.racenetNames.clear();
    }
  }

  queryPlayers(value: string) {
    this.searchSubject.next(value);
    if(!value) {
      this.suggestedPlayers = [];
      return;
    }
  }

  addPlayer(player: string) {
    this.playerFilterFormGroup.addPlayer(player);
    this.suggestedPlayers=[]
    this.player='';
  }
}
