import {Injectable} from "@angular/core";
import {BehaviorSubject} from "rxjs";


@Injectable()
export class DiscordIntegrationService {

  private _authenticatedUser = new BehaviorSubject<boolean>(false);

}
