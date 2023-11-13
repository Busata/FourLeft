import { Component } from '@angular/core';
import {map, switchMap} from "rxjs";
import {ActivatedRoute, Params} from "@angular/router";
import {ProfileTo} from "@server-models";
import {HttpClient} from "@angular/common/http";
import {MatSnackBar} from "@angular/material/snack-bar";

@Component({
  selector: 'app-easports-wrcprofile-container',
  templateUrl: './easports-wrcprofile-container.component.html',
  styleUrls: ['./easports-wrcprofile-container.component.scss']
})
export class EASportsWRCProfileContainerComponent {

  data!: ProfileTo;

  constructor(private httpClient: HttpClient, private activatedRoute: ActivatedRoute, private snackBar: MatSnackBar) {
  }

  ngOnInit(): void {
    this.activatedRoute.params.pipe(map((params: Params) => params['requestId']), switchMap(requestId => {
      return this.httpClient.get<ProfileTo>(`/api_v2/profile/${requestId}`);
    })).subscribe(data => {
      this.data = data;
    })
  }

  saveForm($event: any) {
    this.activatedRoute.params.pipe(map((params: Params) => params['requestId']), switchMap(requestId => {
      return this.httpClient.post<ProfileTo>(`/api_v2/profile/${requestId}`, $event);
    })).subscribe(data => {
      this.data = data;
      this.snackBar.open("Profile updated.", "Close");
    })
  }
}
