import {Component, OnInit} from '@angular/core';
import {MatSlideToggleChange} from "@angular/material/slide-toggle";
import {MatSnackBar} from "@angular/material/snack-bar";

@Component({
  selector: 'app-user-community-progress',
  templateUrl: './user-community-progress.component.html',
  styleUrls: ['./user-community-progress.component.scss']
})
export class UserCommunityProgressComponent implements OnInit {
  public racenet: string = "Catty7073";
  public includeNickname: boolean = false;

  public filterByBefore: boolean = false;
  public filterByAfter: boolean = false;

  public generatedUrl = "";
  beforeDate: any;
  afterDate: any;

  constructor(private _snackBar: MatSnackBar) {
  }

  ngOnInit(): void {
        this.generateUrl();
    }


  public generateUrl() {
    let encoded = encodeURIComponent(this.racenet);
    this.generatedUrl = `https://fourleft.busata.io/api/public/users/community/progression?query=${encoded}`;
    if (this.includeNickname) {
      this.generatedUrl += "&includeName=true";
    }

    if (this.filterByBefore && !!this.beforeDate) {
      this.generatedUrl += `&before=${this.beforeDate}`;
    }

    if (this.filterByAfter && !!this.afterDate) {
      this.generatedUrl += `&after=${this.afterDate}`;
    }
  }

  updateSetNickname($event: MatSlideToggleChange) {
    this.includeNickname = $event.checked;

  }

  updateBefore($event: MatSlideToggleChange) {
    this.filterByBefore = $event.checked;
  }

  updateAfter($event: MatSlideToggleChange) {
    this.filterByAfter = $event.checked;

  }

  copyToClipboard() {

    this._snackBar.open("Copied to clipboard", "Close");
  }
}
