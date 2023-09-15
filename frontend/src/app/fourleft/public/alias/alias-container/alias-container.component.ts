import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Params} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {map, mergeMap, switchMap} from "rxjs";
import {AliasUpdateDataTo} from "@server-models";
import {MatSnackBar} from "@angular/material/snack-bar";

@Component({
  selector: 'app-alias-container',
  templateUrl: './alias-container.component.html',
  styleUrls: ['./alias-container.component.scss']
})
export class AliasContainerComponent implements OnInit {

  data?: AliasUpdateDataTo;

  constructor(private httpClient: HttpClient, private activatedRoute: ActivatedRoute, private snackBar: MatSnackBar) {
  }

  ngOnInit(): void {
    this.activatedRoute.params.pipe(map((params: Params) => params['requestId']), switchMap(requestId => {
     return this.httpClient.get<AliasUpdateDataTo>(`/api/external/aliases/${requestId}`);
    })).subscribe(data => {
      this.data = data;
    })
  }

  saveForm($event: any) {
    this.activatedRoute.params.pipe(map((params: Params) => params['requestId']), switchMap(requestId => {
      return this.httpClient.post<AliasUpdateDataTo>(`/api/external/aliases/${requestId}`, $event);
    })).subscribe(data => {
      this.data = data;

      this.snackBar.open("Alias updated.", "Close");
    })
  }
}
