import {Component, EventEmitter,  OnInit, Output} from '@angular/core';
import {QueryService} from '../../services/query-service';
import {BehaviorSubject, debounceTime, Subject} from 'rxjs';

@Component({
  selector: 'app-player-search',
  templateUrl: './player-search.component.html',
  styleUrls: ['./player-search.component.scss']
})
export class PlayerSearchComponent implements OnInit {

  @Output()
  public added = new EventEmitter<string>();


  public searchSubject = new BehaviorSubject<string>('');
  public suggestedPlayers: string[] = [];



  constructor(private queryService: QueryService) { }
  ngOnInit(): void {
    this.searchSubject.pipe(debounceTime(50)).subscribe(value => {
      this.queryService.getPlayers(value).subscribe(players => {
        this.suggestedPlayers = players;
      });
    })
  }

  public addPlayer(player: string) {
    this.added.emit(player);
    this.suggestedPlayers = [];
    this.searchSubject.next('');
  }


  queryPlayers(value: string) {
    this.searchSubject.next(value);
    if(!value) {
      this.suggestedPlayers = [];
      return;
    }
  }
}
