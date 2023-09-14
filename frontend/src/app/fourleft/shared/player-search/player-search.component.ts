  import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {QueryService} from '../../private/admin/discord/configure-channel/services/query-service';
import {BehaviorSubject, debounceTime} from 'rxjs';

@Component({
  selector: 'app-player-search',
  templateUrl: './player-search.component.html',
  styleUrls: ['./player-search.component.scss']
})
export class PlayerSearchComponent implements OnInit {

  @Input()
  public showLabel: boolean = true;

  @Input()
  public placeholder: string = "Search racenet";

  @Input()
  public limit = 10;

  @Output()
  public added = new EventEmitter<string>();


  public searchSubject = new BehaviorSubject<string>('');
  public suggestedPlayers: string[] = [];



  constructor(private queryService: QueryService) { }
  ngOnInit(): void {
    this.searchSubject.pipe(debounceTime(50)).subscribe(value => {
      this.queryService.getPlayers(value).subscribe(players => {
        this.suggestedPlayers = players.slice(0, this.limit);
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
