import { Component, OnInit } from '@angular/core';
import {ClubMergerService} from "../club-merger.service";

@Component({
  selector: 'app-club-merge-results',
  templateUrl: './club-merge-results.component.html',
  styleUrls: ['./club-merge-results.component.scss']
})
export class ClubMergeResultsComponent implements OnInit {

  public firstClubId: string = "361785";
  public secondClubId: string = "361786";

  public firstEvent: any = {};
  public secondEvent: any = {};

  public mergedEntries: any = [];

  public clubChampionshipSummary: any = {};

  constructor(private clubMergerService: ClubMergerService) { }

  ngOnInit(): void {

  }

  loadClubEvents(clubId: string) {
    this.clubMergerService.getClubEvents(clubId).subscribe(results => {
      this.clubChampionshipSummary[clubId] = results;
    })
  }

  mergeResults() {
    this.clubMergerService.mergeEvents({
      firstClub: this.createKey(this.firstEvent),
      secondClub: this.createKey(this.secondEvent)
    }).subscribe(mergedEntries => {
      this.mergedEntries = mergedEntries;
    });
  }

  createKey(event: any) {
    return {
      challengeId: event.challengeId,
      eventId: event.eventId,
      stageId: event.stageId,
    }
  }
}
