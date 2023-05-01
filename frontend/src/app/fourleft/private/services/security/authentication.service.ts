import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {User} from "./user-store.service";
import {RoutesTo} from "@server-models";


@Injectable()
export class AuthenticationService {
  constructor(private httpClient: HttpClient) {
  }

  public getAuthenticatedUser() : Observable<User> {
    return this.httpClient.get<User>("/api/internal/security/user");
  }
}
