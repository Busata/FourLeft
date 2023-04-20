import {Injectable} from "@angular/core";
import {filter, mergeMap, ReplaySubject} from "rxjs";
import {AuthenticationService} from "./authentication.service";
import {LoginService} from "./login.service";

@Injectable()
export class UserStoreService {
  private _user: ReplaySubject<User> = new ReplaySubject<User>(1);

  constructor(private authenticationService: AuthenticationService, private loginService: LoginService) {
    this.loadInitialData();
  }


  public get user() {
    return this._user.asObservable();
  }

  private loadInitialData() {
    this.loginService.loggedIn.pipe(
      filter(x => x),
      mergeMap(x => this.authenticationService.getAuthenticatedUser())
    ).subscribe(user => {
      this._user.next(user);
    })
  }
}


export interface User {
  name: string;
  roles: string[];
}
