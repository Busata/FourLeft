import {Component, OnInit} from '@angular/core';
import {ResultService} from '../result.service';
import {ClubViewTo} from '@server-models';

@Component({
  selector: 'app-results-container',
  templateUrl: './results-container.component.html',
  styleUrls: ['./results-container.component.scss']
})
export class ResultsContainerComponent implements OnInit{
  public views: ClubViewTo[] = [];


  constructor(private resultService: ResultService) {

  }

  ngOnInit(): void {
    this.resultService.getClubViews().subscribe(views => {
      this.views = views;
    })
  }

}
