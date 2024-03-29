import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {VehicleTo} from '@server-models';

@Injectable({
  providedIn: 'root'
})
export class QueryService {

    constructor(private http: HttpClient) { }



  public getVehicles(vehicleClass: string) :Observable<VehicleTo[]> {
      return this.http.get<VehicleTo[]>(`/api/internal/query/vehicle_class/${vehicleClass}`)
  }

  public getPlayers(playerName: string) :Observable<string[]> {
    return this.http.get<string[]>(`/api/external/query/name?query=${playerName}`)
  }
}
