import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {User} from "./user";
import {Injectable} from "@angular/core";

@Injectable()
export class UsersService {

  constructor(private httpClient: HttpClient) {
  }


  public getUsers() : Observable<User[]> {
    return this.httpClient.get<User[]>("/api/internal/community/users");
  }

  public createUser(nickName: string, alias: string): Observable<User> {
    return this.httpClient.post<User>("/api/internal/community/track_user", {
      nickName,
      alias
    });
  }

  public deleteUser(user: User) {
    return this.httpClient.delete(`/api/internal/community/users/${user.id}`)
  }


}
