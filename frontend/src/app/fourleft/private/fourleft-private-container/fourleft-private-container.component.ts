import {Component, OnInit} from '@angular/core';
import {LoginService} from "../services/security/login.service";
import {UserStoreService} from "../services/security/user-store.service";

@Component({
  selector: 'app-app-container',
  templateUrl: './fourleft-private-container.component.html',
  styleUrls: ['./fourleft-private-container.component.scss']
})
export class FourleftPrivateContainerComponent implements OnInit{

  constructor(public authenticationService: LoginService, private userStoreStore: UserStoreService) { }

  ngOnInit(): void {
    this.authenticationService.configure();
  }

}
