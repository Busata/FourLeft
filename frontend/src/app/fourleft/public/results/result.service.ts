import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ClubViewTo} from '@server-models';

@Injectable({
  providedIn: 'root'
})
export class ResultService {

  constructor(private http: HttpClient)  {
  }


  public getClubViews() {
    return this.http.get<ClubViewTo[]>('/api/external/views')
  }
}
